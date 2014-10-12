package info.androidhive.slidingmenu;

import info.androidhive.slidingmenu.adapter.NavDrawerListAdapter;
import info.androidhive.slidingmenu.model.NavDrawerItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalService;

public class MainActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	
	protected static ImageLoader imageLoader = ImageLoader.getInstance();

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	private JSONArray kidsArray;
	
	private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "credential from developer.paypal.com";
	
    SharedPreferences.Editor editor = null;
	SharedPreferences prefs = null;
	DetailsBean[] details;
	DetailsBean[] detailsNotif;
	String json = null;
	 String  flag12 = "0";
	String data;
	
	private static PayPalConfiguration config = new PayPalConfiguration()
    .environment(CONFIG_ENVIRONMENT)
    .clientId(CONFIG_CLIENT_ID)
    // The following are only used in PayPalFuturePaymentActivity.
    .merchantName("Hipster Store")
    .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
    .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

	
	NavDrawerItem nvNotif;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		imageLoader.init(ImageLoaderConfiguration.createDefault(this));
		
		ActionBar ab = getActionBar();
		ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2e2e2e")));


		int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android"); if (actionBarTitleId > 0) { TextView title = (TextView) findViewById(actionBarTitleId); if (title != null) { title.setTextColor(Color.WHITE); } }
		

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		// Find People
		nvNotif = new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1));
		nvNotif.setCounterVisibility(true);
		nvNotif.setCount("0");
		
		navDrawerItems.add(nvNotif);
		// Photos
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		// Communities, Will add a counter here
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		// Pages
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
		// What's hot, We  will add a counter here
//		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));
		

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		
		
		
		Intent i = getIntent();
		Bundle b = i.getExtras();
		
		 data = b.getString("kids");
		//if (json == null) {
		 json = b.getString("interests");
		 
		//}
		
		
		JSONObject resultObj;
		try {
			
			if (data != null) {
				prefs = getPreferences(MODE_PRIVATE);
				 editor = prefs.edit();
					editor.putString("jsonhere",
							json);
					
					editor.commit();
					
					
				resultObj = new JSONObject(data);
				kidsArray = resultObj.getJSONArray("kidlist");
				
				details = new DetailsBean[kidsArray.length()];
				
				for (int l = 0; l<kidsArray.length();l++ ) 
				{
					JSONObject jsonObject = kidsArray.getJSONObject(l);
		            String URL = jsonObject.getString("photoURL");
		            CharSequence target="\\/";
					CharSequence replace="/";
					String fixedUrl=URL.replace(target,replace);
					details[l] = new DetailsBean();
		            details[l].setPhotoURL(fixedUrl);
		            
		            String id = jsonObject.getString("id"); ;                   
		            String applicant = jsonObject.getString("applicant");;
		            String degree = jsonObject.getString("degree");;
		            String amountsought= jsonObject.getString("amountsought");;
		            String donations= jsonObject.getString("donations");;
		            String annualhouseholdincome = jsonObject.getString("annualhouseholdincome");;
		            String awards = jsonObject.getString("awards");;
		            String extracurricular = jsonObject.getString("extracurricular");;
		            String occupation = jsonObject.getString("occupation");;
		            String offset = jsonObject.getString("offset");;
		            String matching = jsonObject.getString("matching");;
		          
		            String description = jsonObject.getString("description");;
		            String urgency = jsonObject.getString("urgency");;
		            String merit = jsonObject.getString("merit");;
					//URL = URL.substring(2, URL.length()-2);
		            details[l].setAmountsought(amountsought);
		            details[l].setAnnualhouseholdincome(annualhouseholdincome);
		            details[l].setApplicant(applicant);
		            details[l].setAwards(awards);
		            details[l].setDegree(degree);
		            details[l].setDescription(description);
		            details[l].setDonations(donations);
		            details[l].setExtracurricular(extracurricular);
		            details[l].setId(id);
		            details[l].setMerit(merit);
		            details[l].setOccupation(occupation);
		            details[l].setUrgency(urgency);
		            details[l].setMatching(matching);
		            details[l].setOffset(offset);
					
				}
				
				if (savedInstanceState == null) {
					// on first time display view for first nav item
					displayView(0);
				}
			}else {
				 data = b.getString("studData");
				 flag12 = b.getString("flag12");
				 resultObj = new JSONObject(data);
				 JSONObject res = resultObj.getJSONObject("bdaykid");
				 detailsNotif = new DetailsBean[1];
				 String URL = res.getString("photoURL");
		            CharSequence target="\\/";
					CharSequence replace="/";
					String fixedUrl=URL.replace(target,replace);
					detailsNotif[0] = new DetailsBean();
					detailsNotif[0].setPhotoURL(fixedUrl);
		            
		            String id = res.getString("id"); ;                   
		            String applicant = res.getString("applicant");;
		            String degree = res.getString("degree");;
		            String amountsought= res.getString("amountsought");;
		            String donations= res.getString("donations");;
		            String annualhouseholdincome = res.getString("annualhouseholdincome");;
		            String awards = res.getString("awards");;
		            String extracurricular = res.getString("extracurricular");;
		            String occupation = res.getString("occupation");;
		            String offset = res.getString("offset");;
		            String matching = res.getString("matching");;
		          
		            String description = res.getString("description");;
		            String urgency = res.getString("urgency");;
		            String merit = res.getString("merit");;
					//URL = URL.substring(2, URL.length()-2);
		            detailsNotif[0].setAmountsought(amountsought);
		            detailsNotif[0].setAnnualhouseholdincome(annualhouseholdincome);
		            detailsNotif[0].setApplicant(applicant);
		            detailsNotif[0].setAwards(awards);
		            detailsNotif[0].setDegree(degree);
		            detailsNotif[0].setDescription(description);
		            detailsNotif[0].setDonations(donations);
		            detailsNotif[0].setExtracurricular(extracurricular);
		            detailsNotif[0].setId(id);
		            detailsNotif[0].setMerit(merit);
		            detailsNotif[0].setOccupation(occupation);
		            detailsNotif[0].setUrgency(urgency);
		            detailsNotif[0].setMatching(matching);
		            detailsNotif[0].setOffset(offset);
		            
		            if (savedInstanceState == null) {
		    			// on first time display view for first nav item
		    			displayView(1);
		    		}
				
			}
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);
		
		
	    
	}
	
	

	/**
	 * Slide menu item click listener
	 * */
	
	int pos = 0;
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			//displayView(position);
			//if (flag12.equals("1")){
				pos = position;
				if (position == 0)
					new GetData().execute("http://54.215.205.214/fetchkids");
				else displayView(position);
					
				
			//}else displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	String result;
	private class GetData extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			DefaultHttpClient httpClient = new DefaultHttpClient();
           // HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
            HttpPost httpGet = new HttpPost(params[0]);
            try {
				
				 
		         
		         //String json = manJson.toString();
            	prefs = getPreferences(MODE_PRIVATE);
				json = prefs.getString("jsonhere", null);
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
			//showProgress(false);
			try {
				
				JSONObject resultObj = new JSONObject(result);
				Boolean status = resultObj.getBoolean("status");
				if (status)
				{
					//resultObj = new JSONObject(result);
					kidsArray = resultObj.getJSONArray("kidlist");
					
					details = new DetailsBean[kidsArray.length()];
					
					for (int l = 0; l<kidsArray.length();l++ ) 
					{
						JSONObject jsonObject = kidsArray.getJSONObject(l);
			            String URL = jsonObject.getString("photoURL");
			            CharSequence target="\\/";
						CharSequence replace="/";
						String fixedUrl=URL.replace(target,replace);
						details[l] = new DetailsBean();
			            details[l].setPhotoURL(fixedUrl);
			            
			            String id = jsonObject.getString("id"); ;                   
			            String applicant = jsonObject.getString("applicant");;
			            String degree = jsonObject.getString("degree");;
			            String amountsought= jsonObject.getString("amountsought");;
			            String donations= jsonObject.getString("donations");;
			            String annualhouseholdincome = jsonObject.getString("annualhouseholdincome");;
			            String awards = jsonObject.getString("awards");;
			            String extracurricular = jsonObject.getString("extracurricular");;
			            String occupation = jsonObject.getString("occupation");;
			            String offset = jsonObject.getString("offset");;
			            String matching = jsonObject.getString("matching");;
			          
			            String description = jsonObject.getString("description");;
			            String urgency = jsonObject.getString("urgency");;
			            String merit = jsonObject.getString("merit");;
						//URL = URL.substring(2, URL.length()-2);
			            details[l].setAmountsought(amountsought);
			            details[l].setAnnualhouseholdincome(annualhouseholdincome);
			            details[l].setApplicant(applicant);
			            details[l].setAwards(awards);
			            details[l].setDegree(degree);
			            details[l].setDescription(description);
			            details[l].setDonations(donations);
			            details[l].setExtracurricular(extracurricular);
			            details[l].setId(id);
			            details[l].setMerit(merit);
			            details[l].setOccupation(occupation);
			            details[l].setUrgency(urgency);
			            details[l].setMatching(matching);
			            details[l].setOffset(offset);
						
					}
					
					displayView(pos);
					
					
					// populate the arrays
					
				}else {
					Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	public static PhotosFragment photoFragment;
	private void displayView(int position) {
		// update the main content by replacing fragments
		 Fragment fragment = null;
		switch (position) {
		case 0:
			Log.d("daa here ", data+"");
			
			photoFragment = new PhotosFragment(this,details);
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, photoFragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			//}
			break;
		case 1:
			photoFragment = new PhotosFragment(this,detailsNotif);
			FragmentManager fragmentManager1 = getFragmentManager();
			fragmentManager1.beginTransaction()
					.replace(R.id.frame_container, photoFragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
			nvNotif.setCount("1");
			
			break;
		case 2:
			fragment = new PagesFragment();
			break;
		case 3:
			fragment = new CommunityFragment();
			break;
		case 4:
			fragment = new FindPeopleFragment();
			break;
		case 5:
			fragment = new PagesFragment();
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

}
