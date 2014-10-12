package info.androidhive.slidingmenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ServiceClass extends Service{

	public static final long NOTIFY_INTERVAL = 10*1000;
	private Handler mHandler = new Handler();
	private Timer mTimer = null;
	private String URL_HIT = "http://54.215.205.214/getbdaykid";
	JSONArray array = null;
	private String response = "";
	
	int mStartMode;       // indicates how to behave if the service is killed
	IBinder mBinder;      // interface for clients that bind
	boolean mAllowRebind; // indicates whether onRebind should be used
	private NotificationManager mNotificationManager;
	private int counter = 0, incrementBy = 1;
	private static boolean isRunning = false;

	private List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps
	// track of
	// all
	// current
	// registered
	// clients.
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_INT_VALUE = 3;
	public static final int MSG_SET_STRING_VALUE = 4;

	String TAG = "log";

	private final Messenger mMessenger = new Messenger(
			new IncomingMessageHandler());

	@Override
	public void onCreate() {
		// The service is being created
		//showNotification();
		//mTimer.scheduleAtFixedRate(new MyTask(), 0, 5000L);
		isRunning = true;
		if(mTimer!=null)mTimer.cancel();
		else mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimeDisplayClassTask(), 0, NOTIFY_INTERVAL);
	}
	
	class TimeDisplayClassTask extends TimerTask{

		@Override
		public void run() {
			new GetData().execute(URL_HIT);
			
		}
		
	}
	
	private class GetData extends AsyncTask<String, Void, String>{

		private String result;

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			DefaultHttpClient httpClient = new DefaultHttpClient();
           
            
            HttpGet httpGet = new HttpGet(params[0]);
            try {
            	
            	httpGet.setHeader("Accept", "application/json");
            	httpGet.setHeader("Content-type", "application/json");
				 HttpResponse httpResponse = httpClient.execute(httpGet);
				 
				 InputStream ip = httpResponse.getEntity().getContent();
				 
				 if(ip != null)
		                result = convertInputStreamToString(ip);
		            else
		                result = "Did not work!";
		            Log.i("result here ", result);
				 
				 
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
           
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
try {
				
				JSONObject resultObj = new JSONObject(result);
				Boolean status = resultObj.getBoolean("status");
				if (status)
				{
					showNotification(result);
					// populate the arrays
					
				}else {
					//Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
				}
				//array = new JSONArray(result);
				//showNotification(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
       
        inputStream.close();
        return result;
       
    }

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// The service is starting, due to a call to startService()
		Log.d(TAG, "S:onStartCommand(): Received start id " + startId + ": "
				+ intent);
		return START_STICKY; 

	}

	private void showNotification(String result) {
		
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("flag12", "1");
		i.putExtra("studData", result);
		
		//i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		i.addFlags(
			    Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i
				, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification.Builder(this)
		.setContentTitle("Two Cents of Hope")
		.setContentText("Donate to a child on his/her special day!!")
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentIntent(contentIntent).build();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		int k = -1;
		k++;
		if (k==10)
			k = 0;
		mNotificationManager.notify(k, notification);

	}

	@Override
	public IBinder onBind(Intent intent) {
		// A client is binding to the service with bindService()
		Log.d(TAG, "S:onBind() - return mMessenger.getBinder()");

		// getBinder()
		// Return the IBinder that this Messenger is using to communicate with
		// its associated Handler; that is, IncomingMessageHandler().

		return mMessenger.getBinder();

	}

	private void sendMessageToUI(int intvaluetosend) {
		Log.d(TAG, "S:sendMessageToUI");
		Iterator<Messenger> messengerIterator = mClients.iterator();

		if (messengerIterator.hasNext()) {


			while (messengerIterator.hasNext()) {
				Messenger messenger = messengerIterator.next();
				try {
					// Send data as an Integer
					Log.d(TAG, "S:TX MSG_SET_INT_VALUE");
					messenger.send(Message.obtain(null, MSG_SET_INT_VALUE,
							intvaluetosend, 0));

					// Send data as a String
					Bundle bundle = new Bundle();
					bundle.putString("str1", "ab" + intvaluetosend + "cd");
					Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
					msg.setData(bundle);
					Log.d(TAG, "S:TX MSG_SET_STRING_VALUE");
					messenger.send(msg);

				} catch (RemoteException e) {
					// The client is dead. Remove it from the list.
					mClients.remove(messenger);
				}
			}
		}else {
			// start the activity here

			Intent i = new Intent();
			i.setClass(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// All clients have unbound with unbindService()
		return mAllowRebind;
	}
	@Override
	public void onRebind(Intent intent) {
		// A client is binding to the service with bindService(),
		// after onUnbind() has already been called
	}
	@Override
	public void onDestroy() {
		// The service is no longer used and is being destroyed
		Log.d(TAG, "S:onDestroy():Service Stopped");
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
		}
		counter = 0;
		mNotificationManager.cancelAll(); // Cancel the persistent notification.
		isRunning = false;
	}

/*	private class MyTask extends TimerTask {
		@Override
		public void run() {
			Log.d(TAG, "T:MyTask():Timer doing work." + counter);
			try {
				counter += incrementBy;
				//sendMessageToUI(counter);

			} catch (Throwable t) { // you should always ultimately catch all
				// exceptions in timer tasks.
				Log.e("TimerTick", "Timer Tick Failed.", t);
			}
		}
	}*/

	private class IncomingMessageHandler extends Handler { // Handler of
		// incoming messages
		// from clients.
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "S:handleMessage: " + msg.what);
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				/*Log.d(TAG, "S: RX MSG_REGISTER_CLIENT:mClients.add(msg.replyTo) ");
				mClients.add(msg.replyTo);*/
				break;
			case MSG_UNREGISTER_CLIENT:
				/*Log.d(TAG, "S: RX MSG_REGISTER_CLIENT:mClients.remove(msg.replyTo) ");
				mClients.remove(msg.replyTo);*/
				break;
			case MSG_SET_INT_VALUE:
				/*incrementBy = msg.arg1;*/
				break;
			default:
				/*super.handleMessage(msg);*/
			}
		}
	}

}
