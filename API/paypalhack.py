from flask import Flask, request, jsonify, Response
import MySQLdb
import datetime
import simplejson as json

paypalhack = Flask(__name__)

@paypalhack.route('/')

def index():
    return jsonify(status=False)

@paypalhack.route('/fetchkids', methods=['POST'])

def fetchKids():
    db = MySQLdb.connect("localhost","user","password","database")
    cursor = db.cursor(MySQLdb.cursors.DictCursor)
    # Get the parsed contents of the form data
    jsont = request.json
    interests = []
    interests = jsont["interests"]
    sqlfinalset = False

     
    offset = 0
    if len(interests) > 0:
      str_interests = '|'.join(interests)

      sqlf = "select id, count(id) as cnt from interests where interest regexp '%s' \
             group by id order by cnt desc limit 10" % (str(str_interests))

      try:
        cursor.execute(sqlf)
        rows = cursor.fetchall()

        id_list = []

        for row in rows:
          id_list.append(str(row["id"]))

        matchFlag = False
        if len(id_list) != 0:
            matchFlag = True

        

        id2_list = []

        sqlp = "select id from student where city regexp '%s' and id in ('%s') \
                order by donations limit 3" % (str(jsont["city"]), ("','".join(str(w) for w in id_list)))
        try:
          cursor.execute(sqlp)
          rows = cursor.fetchall()

          for row in rows:
            id2_list.append(str(row["id"]))
          
          if len(id2_list) > 0 and matchFlag:
            offset= len(id2_list) 
             
          if len(id2_list) == 0 and len(id_list) > 2:
            id2_list.append(str(id_list[0]))
            id2_list.append(str(id_list[1]))
            offset=2

          if len(id2_list) < 3:
            id3_limit = 3 - len(id2_list)
            sqlp2 = "select id from student where city regexp '%s' and id not in ('%s') order by donations ASC \
                    limit %d" % (str(jsont["city"]), ("','".join(str(x) for x in id2_list)), id3_limit)

            try:
              cursor.execute(sqlp2)
              rows = cursor.fetchall()

              for row in rows:
                id2_list.append(str(row["id"]))

            except Exception, err:
              # Rollback in case there is any error
              print(cursor._last_executed)
              print (Exception, err)
              return jsonify(status=False)

              
          if len(id2_list) < 5:
             id4_limit = 5 - len(id2_list)
             sqlp3 = "select id from student where id not in ('%s') order by donations ASC, merit DESC \
                     limit %d" % (("','".join(str(y) for y in id2_list)), id4_limit)

             try:
                cursor.execute(sqlp3)

                rows = cursor.fetchall()

                for row in rows:
                  id2_list.append(str(row["id"]))
             except Exception, err:
                  # Rollback in case there is any error
                print(cursor._last_executed)
                print (Exception, err)
                return jsonify(status=False)

             sqlfinal = "select * from student where id in ('%s')" % (("','".join(str(z) for z in id2_list)))
             sqlfinalset = True

          if not sqlfinalset:
            sqlfinal = "select * from student where id in ('%s')" % (("','".join(str(z) for z in id_list)))
          try:
            cursor.execute(sqlfinal)
            rows = cursor.fetchall()

            details = []
            
            inc=0     
            for row in rows:
              
              inner_json = {}
              inner_json["id"]                    = row["id"]
              inner_json["applicant"]             = row["applicant"]
              inner_json["degree"]                = row["degree"]
              inner_json["amountsought"]          = row["amountsought"]
              inner_json["donations"]             = row["donations"]
              inner_json["annualhouseholdincome"] = row["annualhouseholdincome"]
              inner_json["awards"]                = row["awards"]
              inner_json["extracurricular"]       = row["extracurricular"]
              inner_json["occupation"]            = row["occupation"]
              inner_json["photoURL"]              = row["photoURL"]
              inner_json["description"]           = row["description"]
              inner_json["urgency"]               = row["urgency"]
              inner_json["merit"]                 = row["merit"]
              
	      if inc <= offset:
                inc = inc+1   
                sqlp4 = "select offset from interests where interest regexp '%s' and id = '%s' \
                        group by id limit 1" % ((str(str_interests)),row["id"])

                try:
                  cursor.execute(sqlp4)

                  allrows = cursor.fetchall()
                  if len(allrows) < 1:
                     inner_json["matching"]                 = "n"
                     inner_json["offset"]                 = "null"

                  for single in allrows:
                    inner_json["matching"]                 = "y"
                    inner_json["offset"]                 = str(single["offset"])
                    
                except Exception, err:
                  # Rollback in case there is any error
                  print(cursor._last_executed)
                  print (Exception, err)
                  return jsonify(status=False)


              else:
                  inner_json["matching"]                 = "n"
                  inner_json["offset"]                 = "null"

              details.append(inner_json)

            return_dic ={"status":True,"kidlist":details}

            return Response(json.dumps(return_dic), mimetype='application/json')
          except Exception, err:
            # Rollback in case there is any error
            print(cursor._last_executed)
            print (Exception, err)
            return jsonify(status=False)

        except Exception, err:
          # Rollback in case there is any error
          print(cursor._last_executed)
          print (Exception, err)
          return jsonify(status=False)
      except Exception, err:
        # Rollback in case there is any error
        print(cursor._last_executed)
        print (Exception, err)
        return jsonify(status=False)




@paypalhack.route('/getbdaykid')

def getbdayKid():
   db = MySQLdb.connect("localhost","user","password","database")
   cursor = db.cursor(MySQLdb.cursors.DictCursor)
   # Get the parsed contents of the form data

   sqldob = "select * from student where MONTH(dob) = MONTH(NOW()) AND DAY(dob) = DAY(NOW())"
   try:
     cursor.execute(sqldob)
     row = cursor.fetchone()

     if not row:
       return jsonify(status=False)
     else:
       inner_json = {}
       inner_json["id"]                    = row["id"]
       inner_json["applicant"]             = row["applicant"]
       inner_json["degree"]                = row["degree"]
       inner_json["amountsought"]          = row["amountsought"]
       inner_json["donations"]             = row["donations"]
       inner_json["annualhouseholdincome"] = row["annualhouseholdincome"]
       inner_json["awards"]                = row["awards"]
       inner_json["extracurricular"]       = row["extracurricular"]
       inner_json["occupation"]            = row["occupation"]
       inner_json["photoURL"]              = row["photoURL"]
       inner_json["description"]           = row["description"]
       inner_json["urgency"]               = row["urgency"]
       inner_json["merit"]                 = row["merit"]
       inner_json["matching"]              = "n"
       inner_json["offset"]                = "null"

       return_dic ={"status":True,"bdaykid":inner_json}
       return Response(json.dumps(return_dic), mimetype='application/json')

   except Exception, err:
      # Rollback in case there is any error
      print(cursor._last_executed)
      print (Exception, err)
      return jsonify(status=False)


@paypalhack.route('/updateamounts',  methods=['POST'])

def updateAmounts():
    db = MySQLdb.connect("localhost","user","password","database")
    cursor = db.cursor()
    jsont = request.json

    id = str(jsont["id"])
    donation = int(jsont["donation"])
    
    sqlu = "update student set donations = donations + %d, amountsought = amountsought - %d where id = '%s'" % (donation, donation, id)

    try:
       cursor.execute(sqlu)
       db.commit()
       return jsonify(status=True)

    except Exception, err:
       # Rollback in case there is any error
       print(cursor._last_executed)
       print (Exception, err)
       return jsonify(status=False)
    
if __name__ == '__main__':

    paypalhack.run(port=80, debug=True)
