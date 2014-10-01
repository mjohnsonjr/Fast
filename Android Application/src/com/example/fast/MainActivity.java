package com.example.fast;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.fast.adapter.NavDrawerListAdapter;
import com.example.fast.heart.HeartFragment;
import com.example.fast.home.HomeFragment;
import com.example.fast.model.NavDrawerItem;
import com.example.fast.preferences.FastPreferences;
import com.example.fast.run.RunFragment;
import com.example.fast.share.ShareFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.plus.PlusClient;


/***
 *  Main activity.  Implements ConnectionCallbacks and OnConnectionFailedListener to 
 *  allow the use of Google APIs for Google+ posting.
 * @author michael
 *
 */
public class MainActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener {
	
	/* DrawerLayout (Android) */
	private DrawerLayout mDrawerLayout; // Layout that holds the data for the Drawer object
	private ListView mDrawerList; // List for Drawer to show
	private ActionBarDrawerToggle mDrawerToggle; // handles drawer actions
	private CharSequence mDrawerTitle; // nav drawer title
	private CharSequence mTitle; // used to store app title
	private Integer backStackCount = 0;
	private Long milliseconds = 0L;

	/* Array Holding Drawer Strings, Icons, and Objects */
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	
	/* Keeps track of current screen */
	private Fragment currentFragment;
	private HomeFragment homeFragment;
	private RunFragment runFragment;
	private HeartFragment heartFragment;
	private ShareFragment shareFragment;
	private MapFragment mapFragment;
	
	/* GPS Items */
	private GPSTracker gpsTracker;
	private Handler mHandler;
	private Handler timeHandler;
	private final int REFRESH_RATE = 1000 * 3; /* 3 second per update */
	private final int TIMER = 100; /* Handler for timer callback */
	private Statistics statistics;
	private Location previousLocation;
	
	/* SQL Lite Database, schema */
	private Database database;
	public static final String TABLE_NAME = "android_metadata";
	
	/* Google+ */
	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	 
	/* Google Maps! */
	private GoogleMap map;
	private PolylineOptions path;
	
	/* Shared Preferences */
	private SharedPreferences prefs;
	private OnSharedPreferenceChangeListener listener;
	private boolean usingMetric;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* Google Plus */
		mPlusClient = new PlusClient.Builder(this, this, this)
				.setActions("http://schemas.google.com/AddActivity",
						"http://schemas.google.com/BuyActivity")
				.build();

		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
		
		
		/* SQL Lite */
		database = new Database( getApplicationContext() );
	
		
		/* Setup GPS Tracking, timers */
		gpsTracker = new GPSTracker( getApplicationContext() );
		mHandler =  new Handler();
		timeHandler = new Handler();
		gpsTracker.startUsingGPS();
		
		/* Statistics Handler */
		statistics = new Statistics( getApplicationContext() );
		
		/* Preferences Handling */
		prefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
		usingMetric = prefs.getBoolean("metric", false);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			  public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
					usingMetric = prefs.getBoolean("metric", false);		
		  }
		};
		prefs.registerOnSharedPreferenceChangeListener(listener);
		
		
		/* Drawer Layout (Android) */
		mTitle = mDrawerTitle = getTitle();
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items); 
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		setDrawerItems();
		setDrawerListeners();

		if (savedInstanceState == null) {
			displayView(0);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateEverything();
	}
	
	
	@Override
	protected void onDestroy() {
		gpsTracker.stopUsingGPS();
		super.onDestroy();
	}

	
	/** Inflates our menu (preferences only currently) */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fast_preferences_menu, menu);
		return true;
	}
	
	/** Handle preferences selected. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/* toggle nav drawer on selecting action bar app icon/title*/
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			// Display Settings page
			Intent intent = new Intent(this, FastPreferences.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.menu_preferences).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();
	}

	/**
	 * Displaying fragment view for selected navigation drawer list item
	 */
	private void displayView(int position) {

		/* update the main content by replacing fragments */
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		
		Fragment previous = currentFragment;
		switch (position) {
		case 0:
			if(homeFragment == null){
				homeFragment = new HomeFragment();
				transaction.add(R.id.frame_container, homeFragment);
			}
			currentFragment = homeFragment;
			break;
		case 1:
			if(runFragment == null){
				runFragment = new RunFragment();
				transaction.add(R.id.frame_container, runFragment);
			}
			currentFragment = runFragment;
			break;
		case 2:
			if(heartFragment == null){
				heartFragment = new HeartFragment();
				transaction.add(R.id.frame_container, heartFragment);
			}
			currentFragment = heartFragment;
			break;
		case 3:
			if(shareFragment == null){
				shareFragment = new ShareFragment();
				transaction.add(R.id.frame_container, shareFragment);
			}
			currentFragment = shareFragment;
			break;
		case 4:
			if(mapFragment == null){
				mapFragment = createMapFragment();	
			}
			currentFragment = mapFragment;
			break;

		default:
			break;
		}

		if (currentFragment != null) {
			if(previous != null){
				transaction.addToBackStack((backStackCount++).toString());
				/* Hide last fragment */
				transaction.hide(previous);
			}

			/* Add new Fragment */
			transaction.show(currentFragment).commit();
				
			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}

	/**
	 * Sets title of app based on davigation drawer item selected
	 * (Sets it to the name of the option)
	 */
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
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
		// Pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}



	/*
	 * Slide menu item click listener
	 */
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	
	/*
	 * adds need items to Drawer object 
	 * 1 for no errors else 0
	 */
	private int setDrawerItems() {
		navDrawerItems = new ArrayList<NavDrawerItem>();
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		// Run Now
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
		// Heart Rate
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		// Share
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		// Map
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

		// Recycle the typed array
		navMenuIcons.recycle();
		return 1;
	}

	/*
	 * creates and sets the Drawer Listeners 
	 * return 1 for no errors
	 */
	private int setDrawerListeners() {

		/* Creates default slide listener */
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		/* setting the navigation drawer list adapter */
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);

		/* enabling action bar app icon and behaving it as toggle button */
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open
				R.string.app_name // nav drawer close
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				/* calling onPrepareOptionsMenu() to show action bar icons */
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				/* calling onPrepareOptionsMenu() to hide action bar icons */
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		return 1;
	}
	
	
	/* Time Updater for run screen.  Every 100 milliseconds callback.
	 * Check if location is null, that way we don't start loggin statistics until we
	 * have a successful GPS lock. */
	private Runnable timeUpdater = new Runnable() {
 	   public void run() {

 		   mHandler.postDelayed(this, TIMER);
 		   
 		   if(runFragment != null && gpsTracker.getLocation() != null)
 			   runFragment.getGpsText().setText(formatTime(milliseconds+=TIMER));
 		   
 		}
 	};
	
	/* Data (GPS) Updater for run screen.  Every 3 seconds callback. */
	private Runnable statisticsUpdater = new Runnable() {
 	   public void run() {
 		   
 		   double heartRate = getHeartRate();			   
 		   /* Statistics update handles all logic for stats (check new location against old) */
 		   if(gpsTracker.getLocation() != null && previousLocation != null && !gpsTracker.getLocation().equals(previousLocation)){
 			  statistics.update(gpsTracker.getLocation(), heartRate);
 		   
 			  /* Update Google Map */  
 			  redrawGoogleMap(gpsTracker.getLatitude(), gpsTracker.getLongitude());
 		   }   
 		   previousLocation = gpsTracker.getLocation();
 		   
 		   mHandler.postDelayed(this, REFRESH_RATE);
 		   
 		   if(runFragment != null){
 				runFragment.updateRunTextViews();
 			}
 		   	
 		   
 		}
 	};
 	
 	/*
 	 * Redraws and refocuses the google map, including drawing the line between here and the 
 	 * previous location, as well as moving the camera. (If this is called before the map is made,
 	 * then the map making methods are called in order to make the map.
 	 */
 	private void redrawGoogleMap(double latitude, double longitude){
 		if(map != null){
 			if(path == null){
 				path = new PolylineOptions().color(Color.RED).add(new LatLng(latitude, longitude));
 			}
 			else{
 				path.add(new LatLng(latitude, longitude));
 			}
 			Polyline polyline = map.addPolyline(path);
 			polyline.setVisible(true);
 			
 			/* Camera follows the user */
 			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 20.0f));
 		}
 		
 		else if(mapFragment != null){
 			map = createMap(latitude, longitude);
 		}
 		
 		/* Create the fragment to start the logging */
 		else{
 			mapFragment = createMapFragment();
 		}
 	}
 	
 	/* Creates and stores a google map object */
 	private GoogleMap createMap(double lat, double lon) {
 		GoogleMap map = mapFragment.getMap();
		map.setMyLocationEnabled(true);
		/* Set initial maps position */
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 20.0f));
		return map;
	}

 	/* Creates the map fragment that holds a google map */
	private MapFragment createMapFragment() {
		
 		MapFragment mapFragment = MapFragment.newInstance(new GoogleMapOptions().zoomControlsEnabled(false));
 		getFragmentManager().beginTransaction().add(R.id.frame_container, mapFragment).hide(mapFragment).commit();
		getFragmentManager().executePendingTransactions();
		
		return mapFragment;
	}

	/**
	 *  Starts the thread that runs the Statistics updater (Speed, Distance, etc.).
	 */
	public void startStatisticsUpdater(){
		
		updateEverything();
 		gpsTracker.startUsingGPS();
 		
 		/* Start handlers */ 		
        timeHandler.postDelayed(timeUpdater, TIMER);
 		mHandler.postDelayed(statisticsUpdater, REFRESH_RATE);
 	}
	
	
 	
	/**
	 *  Stops collecting and calculating data, and removes all messages from the handler.
	 */
 	public void stopStatisticsUpdater(){
 		mHandler.removeCallbacks(statisticsUpdater);
 		mHandler.removeMessages(0);
 		timeHandler.removeCallbacks(timeUpdater);
 		timeHandler.removeMessages(0);
 		gpsTracker.stopUsingGPS();
 	}
 	
 	public Database getDatabase(){
 		return database;
 	}
 	

	/* Google plus activity */
 	@Override
 	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
 	    if (requestCode == 9000 && responseCode == RESULT_OK) {
 	        mConnectionResult = null;
 	        mPlusClient.connect();
 	    }
 	}
 	
 	/* Used by google APIs for Google + */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		 if (mConnectionProgressDialog.isShowing()) {

             if (result.hasResolution()) {
                     try {
                             result.startResolutionForResult(this, 9000);
                     } catch (SendIntentException e) {
                             mPlusClient.connect();
                     }
             }
     }

     mConnectionResult = result;
		
	}

	/* Google Plus */
	@Override
	public void onConnected(Bundle connectionHint) {
		  mConnectionProgressDialog.dismiss();
		  Toast.makeText(this, mPlusClient.getAccountName() + " is connected!", Toast.LENGTH_LONG).show();
		  updateEverything();
	}

	/* Google Plus */
	@Override
	public void onDisconnected() {
		 Log.d("MainActivity", "disconnected");
	}
	
	
	/* Getters for fragments, statics */
	public GPSTracker getGpsTracker(){
		return gpsTracker;
	}
	
	public Handler getHandler(){
		return mHandler;
	}
	
	public Runnable getStatisticsUpdater(){
		return statisticsUpdater;
	}
	
	public Statistics getStatistics(){
		return statistics;
	}
	
	public boolean getUsingMetric(){
		return usingMetric;
	}
	
	public Statistics getAllTimeStatistics(){	
		
		return database.selectStatistics("TEST");
	}
	
	/**
	 *  Updates as many components as possible, including all TextViews and Buttons on 
	 *  all fragments attached to this activity.
	 */
	public void updateEverything(){
		if(runFragment != null){
			runFragment.updateRunTextViews();
		}
		if(homeFragment != null){
			homeFragment.updateHomeTextViews();
		}
	}
	
	/** 
	 * Signs into a Google account attached to this phone 
	 **/
	public void signInToGoogleAccount() {	
		/* Need to authenticate a google account */
		if(!mPlusClient.isConnected()){
			mConnectionProgressDialog.setMessage("Signing in...");
			mConnectionProgressDialog.show();
			mPlusClient.connect();
		}		
	}
	
	
	/**
	 * Signs out of the Google Account that is currently signed in.
	 */
	public void signOutOfGoogleAccount() {
		/* Need to authenticate a google account */
		if(mPlusClient.isConnected()){
			mConnectionProgressDialog.setMessage("Signing out...");
			mConnectionProgressDialog.show();
			mPlusClient.connect();
		}		
	}
	
	public PlusClient getPlusClient(){
		return mPlusClient;
	}
	
	public void setConnectionResult(ConnectionResult conn){
		this.mConnectionResult = conn;
	}
	
	public ConnectionResult getConnectionResult(){
		return mConnectionResult;
	}
	
	public ProgressDialog getProgressDialog(){
		return mConnectionProgressDialog;
	}
	
	/**
	 * Formats an extremely long floating point value to 2 decimal places.
	 * @param value
	 * @return
	 */
	public static String format(double value){
		DecimalFormat df = new DecimalFormat("#0.00");
		return df.format(value);
	}
	
	/**
	 * Gets the current heartrate.
	 * @return Current heartrate.
	 */
	public double getHeartRate(){
		if(heartFragment != null){
			return Double.parseDouble(heartFragment.getHeart());
		}
		else return 0;
	}
	
	/**
	 * Returns the current Status of the heartrate monitor connection.
	 * @return Heartrate monitor connection status.
	 */
	public String getConnection(){
		if(heartFragment != null){
			return heartFragment.getConnection().getText().toString();
		}
		else{
			return getString(R.string.heartRateCurrent);
		}
	}
	
	/* Formats the timer into human readable string format */
	private CharSequence formatTime(long time) {
		
    	Integer seconds = (int) ((time/1000));
		Integer minutes = seconds / 60;
		Integer hours = minutes / 60;
		
		seconds = seconds % 60;
		minutes = minutes % 60;
		String hoursString = hours > 9 ? hours.toString() : "0" + hours.toString();
		String minutesString = minutes > 9 ? minutes.toString() : "0" + minutes.toString();
		String secondsString = seconds > 9 ? seconds.toString() : "0" + seconds.toString();

		String msString = String.valueOf(time % 360000);
		Character msChar = msString.charAt(msString.length() -3);
		
		String concat = hoursString + ":" + minutesString + ":" + secondsString + "."+ msChar;
		
		return concat;
	}
}
