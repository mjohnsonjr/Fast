package com.example.fast;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

/**
 * This tracker uses ONLY GPS.  Network based location is disabled because we need
 * accuracy for the running application.  Use it outdoors.  Code heavily borrowed from
 * the Google developer website's implementation.
 *
 */

public class GPSTracker implements LocationListener {

	private final Context mContext;
	protected LocationManager locationManager;
	

	private boolean isGPSEnabled = false;
	private boolean canGetLocation = false;

	private Location location;
	private double latitude; 
	private double longitude;
	
	private double lastLockedLatitude;
	private double lastLockedLongitude;

	/* Distance to update in METERS */
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; 
	
	/* Time to update in MS */
	private static final long MIN_TIME_BW_UPDATES = 1000 * 3; 
	
	
	public GPSTracker(Context context) {
		this.mContext = context;
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) mContext.getSystemService(Service.LOCATION_SERVICE);

			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			if (!isGPSEnabled) {
				/* GPS is not enabled */
			} else {
				this.canGetLocation = true;
				
				if (isGPSEnabled) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						if (locationManager != null) {	
								lastLockedLatitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
								lastLockedLongitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
						}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}
	
	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your app
	 * */
	public void startUsingGPS() {
		if (locationManager == null) {
			locationManager = (LocationManager) mContext.getSystemService(Service.LOCATION_SERVICE);
		}
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
			locationManager = null;
			location = null;
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		// return longitude
		return longitude;
	}
	
	public double getLastLockedLatitude(){
		return lastLockedLatitude;
	}
	
	public double getLastLockedLongitude(){
		return lastLockedLongitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
		//Toast.makeText(mContext, location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}