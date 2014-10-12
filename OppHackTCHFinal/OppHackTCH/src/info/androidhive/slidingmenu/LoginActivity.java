package info.androidhive.slidingmenu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;


public class LoginActivity extends Activity implements ServiceConnection{
	
	// Your Facebook APP ID
		private static String APP_ID = "344520989059602"; // Replace with your App ID
		
		// Instance of Facebook Class
		public  static Facebook facebook = new Facebook(APP_ID);
		private AsyncFacebookRunner mAsyncRunner;
		String FILENAME = "AndroidSSO_data";
	
	SharedPreferences.Editor editor = null;
	SharedPreferences prefs = null;
	private float donation_amount = 0;
	private ServiceConnection mConnection = (ServiceConnection)this;
	
	private String result;
	
	String homeCity;
	String gender;
	ArrayList<String> interests = new ArrayList<String>();
	ArrayList<String> categories = new ArrayList<String>();
	ArrayList<String> likes = new ArrayList<String>();
	
	JSONArray array = null;
	private String response = "";

	private View mProgressView;

	private View mLoginFormView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        
        mLoginFormView = findViewById(R.id.login_form);
		
        mProgressView = findViewById(R.id.login_progress);
        TextView tx = (TextView)findViewById(R.id.textView1);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),
        "fonts/dancingscript.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(),
                "fonts/caviardreamsbold.ttf");
                
        mAsyncRunner = new AsyncFacebookRunner(facebook);
        tx.setTypeface(custom_font,Typeface.ITALIC);
        
        TextView tx2 = (TextView)findViewById(R.id.textView2);
        
        tx2.setTypeface(custom_font1);
        
        startService(new Intent(LoginActivity.this, ServiceClass.class));
        /*bindService(new Intent(this, ServiceClass.class), mConnection,
				Context.BIND_AUTO_CREATE);*/
        
        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null)
        result = b.getString("studData");
        
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        prefs = getPreferences(MODE_PRIVATE);
        editor = prefs.edit();
        editor.putFloat("donation_amount", prefs.getFloat("donation_amount", 0)+donation_amount);
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				//
				
				
				loginToFacebook();
				
			}
		});
    }
    
    @SuppressWarnings("deprecation")
	public void loginToFacebook() {
		
		

		prefs = getPreferences(MODE_PRIVATE);
		String access_token = prefs.getString("access_token", null);
		long expires = prefs.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);



			Log.d("FB Sessionsqweqwe", "" + facebook.isSessionValid());
			
			
		}

		//Toast.makeText(getApplicationContext(), "Name: " +  access_token +"\nEmail: " +facebook.isSessionValid(), Toast.LENGTH_LONG).show();

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		//Log.d("FB Sessionsdddddd", "" + facebook.isSessionValid()+ "  fff "+ Session.openActiveSessionFromCache(AndroidFacebookConnectActivity.this)+ "fffyyy"+facebook.getSession() );
		
		if (!facebook.isSessionValid()) {
			facebook.authorize(this,
					new String[] { "email", "publish_stream", "user_likes", "user_status", "user_interests", "user_hometown", "user_location" },
					new DialogListener() {

				@Override
				public void onCancel() {
					// Function to handle cancel event
				}

				@Override
				public void onComplete(Bundle values) {
					// Function to handle complete event
					// Edit Preferences and update facebook acess_token
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("access_token",
							facebook.getAccessToken());
					editor.putLong("access_expires",
							facebook.getAccessExpires());
					editor.commit();
					
					showProgress(true);
					
					getProfileInformation();
					
					
					
					//Log.d("FB Sessions", "" + facebook.isSessionValid());
				}

				@Override
				public void onError(DialogError error) {
					// Function to handle error
					Toast.makeText(getApplicationContext(), "Name: " +  "\nEmail: " +facebook.isSessionValid(), Toast.LENGTH_LONG).show();
				}

				@Override
				public void onFacebookError(FacebookError fberror) {
					// Function to handle Facebook errors
					Toast.makeText(getApplicationContext(), "Name: " +fberror.getMessage()+  "\nEllllmail: " +facebook.isSessionValid(), Toast.LENGTH_LONG).show();
				}

			});
			
			Log.d("FB Sessions", "" + facebook.isSessionValid());
		}else{
			showProgress(true);
			
			getProfileInformation();
		}
	}
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
    
    @SuppressWarnings("deprecation")
	public void getInterestsInformation() {
		mAsyncRunner.request("me/interests", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Profile", response);
				String json = response;
				try {
					// Facebook Profile JSON data
					final JSONObject profile = new JSONObject(json);

					Log.i("json string", profile.toString());
					
					JSONArray interests = profile.getJSONArray("data");
					
					int len = interests.length();
					for(int i = 0; i < len; ++i) {
					    JSONObject obj = interests.getJSONObject(i);
					    if (checkPresence(obj.getString("name"))) {
					    if (!LoginActivity.this.interests.contains(obj.getString("name").toLowerCase()))
					    	LoginActivity.this.interests.add(obj.getString("name").toLowerCase());
					    }
					}
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							constructJSON();
							
						}
					});
					
					

					// getting name of the user
					//final String name = profile.getString("name");

					// getting email of the user
					//final String email = profile.getString("email");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							//Toast.makeText(getApplicationContext(), profile.toString(), Toast.LENGTH_LONG).show();
						}

					});


				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}
    
    private JSONObject manJson;

	protected void constructJSON() {
		
		 manJson = new JSONObject();
		try {
			manJson.put("city", homeCity.replaceAll("\\/", "/"));
			JSONArray arr = new JSONArray();
			for (String str : interests)
			{
				arr.put(str.replaceAll("\\/", "/"));
				Log.i("json string", str.replaceAll("\\/", "/"));
			}
			for (String str : likes)
			{
				arr.put(str.replaceAll("\\/", "/"));
				Log.i("json string", str.replaceAll("\\/", "/"));
			}
			
			manJson.put("interests", arr);
			//manJson.put("gender", gender);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//String jsonResponse = "{\"city\":"+homeCity + ", \"interests\":"+interests.toString().substring(0, interests.toString().length()-1)+","+likes.toString().substring(1, interests.toString().length())+", \"gender\":"+gender+"}";
		String str = manJson.toString();
		Log.i("json string", str);
		Log.i("json string", str.replaceAll("\\/", "/"));
		
		
		//showProgress(false);
		
		
	    new GetData().execute("http://54.215.205.214/fetchkids");
	    
		//54.215.205.214/fetchkids
	    //getbdaykid
		
	}
	
	
	private class GetData extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			DefaultHttpClient httpClient = new DefaultHttpClient();
           // HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
            HttpPost httpGet = new HttpPost(params[0]);
            try {
				
				 
		         
		         String json = manJson.toString();
		         
		         StringEntity se = new StringEntity(json);
		         httpGet.setEntity(se);
		         httpGet.setHeader("Accept", "application/json");
		         httpGet.setHeader("Content-type","application/json");
		         httpResponse = httpClient.execute(httpGet);
		         
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
            
            Log.d("response", result);
           
			return result;
		}

		

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			showProgress(false);
			try {
				
				JSONObject resultObj = new JSONObject(result);
				Boolean status = resultObj.getBoolean("status");
				if (status)
				{
					JSONArray kidsArray = resultObj.getJSONArray("kidlist");
					
					Intent i = new Intent(LoginActivity.this, MainActivity.class);
					i.putExtra("kids", result);
					i.putExtra("interests", manJson.toString());
					startActivity(i);
					
					finish();
					
					
					// populate the arrays
					
				}else {
					Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

	/**
	 * Get Profile information by making request to Facebook Graph API
	 * */
	@SuppressWarnings("deprecation")
	public void getLikesInformation() {
		mAsyncRunner.request("me/likes", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Profile", response);
				String json = response;
				try {
					// Facebook Profile JSON data
					final JSONObject profile = new JSONObject(json);

					
					Log.i("json string", profile.toString());
					JSONArray interests = profile.getJSONArray("data");
					
					int len = interests.length();
					for(int i = 0; i < len; ++i) {
					    JSONObject obj = interests.getJSONObject(i);
					    if (checkPresence(obj.getString("category"))) {
					    	if (!LoginActivity.this.likes.contains(obj.getString("category").toLowerCase()))
						    	LoginActivity.this.likes.add(obj.getString("category").toLowerCase());
					    }
					    
					}
					
					getInterestsInformation();
					
					

					// getting name of the user
					//final String name = profile.getString("name");

					// getting email of the user
					//final String email = profile.getString("email");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							//.makeText(getApplicationContext(), profile.toString(), Toast.LENGTH_LONG).show();
						}

					});


				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}
	
	String[] strArray = {"book","novel","music","dance","chess", "cricket", "football", "khokho", "hockey", "badminton", "volleyball", "skits", "kabadi", "artist", "athlete", "engineer", "doctor", "politics", "acting", "swim"};
	
	List<String> strings = Arrays.asList(strArray);
	
	protected boolean checkPresence(String string) {
		
		for (int r = 0 ; r< strings.size(); r++) 
		{
			if (string.toLowerCase().contains(strings.get(r)))
			{
				return true;
			}
		}
		return false;
		
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			Log.d("null here ", mLoginFormView+"");
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}


	/**
	 * Get Profile information by making request to Facebook Graph API
	 * */
	@SuppressWarnings("deprecation")
	public void getProfileInformation() {
		mAsyncRunner.request("me", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Profile", response);
				String json = response;
				try {
					// Facebook Profile JSON data
					final JSONObject profile = new JSONObject(json);

					//
					
					Log.i("json string", profile.toString());
					JSONObject home_city = profile.getJSONObject("hometown");
					homeCity = home_city.getString("name").split(",")[0];
					gender = profile.getString("gender");
					
					getLikesInformation();
					
					//Toast.makeText(getApplicationContext(), profile.toString(), .LENGTH_LONG).show();

					// getting name of the user
					//final String name = profile.getString("name");

					// getting email of the user
					//final String email = profile.getString("email");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							//Toast.makeText(getApplicationContext(), profile.toString(), Toast.LENGTH_LONG).show();
						}

					});


				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first

	    Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null)
        result = b.getString("studData");
        
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	   
	}
}
