package com.example.fast;

import android.location.Location;

/**
 * Object that holds data about a users current run.  This data should be saved to the database when completed.
 * @author michael
 *
 */
public class Statistics {

	private double averageSpeed = 0;
	private double maxSpeed = 0;
	private double distanceTravelled = 0;
	private double averageHeartrate;
	private Location previousLocation;
	
	/**
	 * Updates all stored data based on a newLocation found.  Takes in the new location and heartrate and calculates
	 * moving averages and updates the data.
	 * @param newLocation
	 * @param heartrate
	 */
	public void update(Location newLocation, double heartrate){
	
		if(previousLocation != null){
			updateSpeed(previousLocation, newLocation);
			updateHeartrate(heartrate);	
			/* Update Distance */
			updateDistance(previousLocation, newLocation);
		}
		previousLocation = newLocation;
	}
	
	private void updateHeartrate(double heartrate) {
		
		if(averageHeartrate != 0.0){
			averageHeartrate = (averageHeartrate + heartrate)/2;
		}
		else{
			averageHeartrate = heartrate;
		}
			
	}


	private void updateSpeed(Location previousLocation2, Location newLocation) {
		
		float distance = previousLocation2.distanceTo(newLocation);
		if(distance == Float.NaN){
			distance = 0;
		}
		
		double instantSpeed = distance/((newLocation.getElapsedRealtimeNanos() - previousLocation2.getElapsedRealtimeNanos())/1e9);
		if(instantSpeed == Double.NaN){
			instantSpeed = 0;
		}
		
		/* Max speed check */
		if(instantSpeed > maxSpeed)
			maxSpeed = instantSpeed;

		averageSpeed = (averageSpeed + instantSpeed)/2;
		
	}

	private void updateDistance(Location previousLocation2, Location newLocation) {
		
		float distance = previousLocation2.distanceTo(newLocation);
		/* Update total */
		distanceTravelled = distanceTravelled + distance;
	}

	/**
	 * Converts Meters Per Seconds to Miles Per Hour
	 * @param speed
	 * @return
	 */
	public static double milesPerHour(double speed){
		return speed * 2.23694;
	}
	
	/**
	 * Converts Meters to Miles
	 * @param speed
	 * @return
	 */
	public static double miles(double distance){
		return distance * 0.000621371;
	}
	
	/**
	 * Converts Meters Per Seconds to Kilometers Per Hour
	 * @param speed
	 * @return
	 */
	public static double kilometersPerHour(double speed){
		return speed * 3.6;
	}
	
	/**
	 * Converts Meters to Kilometers
	 * @param speed
	 * @return
	 */
	public static double kilometers(double distance){
		return distance * 0.001;
	}
	

	public double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}
	
	public Location getPreviousLocation() {
		return previousLocation;
	}

	public double getAverageHeartrate() {
		return averageHeartrate;
	}
	public void setAverageHeartrate(double averageHeartrate) {
		this.averageHeartrate = averageHeartrate;
	}
	public void setPreviousLocation(Location previousLocation) {
		this.previousLocation = previousLocation;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getDistanceTravelled() {
		return distanceTravelled;
	}

	public void setDistanceTravelled(double distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}
}


