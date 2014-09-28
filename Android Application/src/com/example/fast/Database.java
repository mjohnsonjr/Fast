package com.example.fast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Object that represents the database.  Abstracts away cursor and contentvalues with simple methods to create and update entries into the database.
 *
 */
public class Database { 

	private DatabaseHelper dbHelper;  
		private SQLiteDatabase database;  
		public final static String TABLE= "fast_table"; 
		public final static String ID = "_id"; 
		public final static String AVERAGE_SPEED = "average_speed", MAX_SPEED = "max_speed", DISTANCE = "distance", USERNAME = "username", AVERAGE_HEARTRATE = "average_heartrate"; 
	  
	public Database(Context context){  
	    dbHelper = new DatabaseHelper(context);  
	    database = dbHelper.getWritableDatabase();  
	}
	
	/**
	 *  If user not seen before, create them with all data initialized to 0. */
	public long createRecord(String userName){
		
		/* Create row */
		if(userName != null){
			return createRecords(userName, 0, 0, 0, 0);
		}
		else return -1L;
	}

	/**
	 * 	Create a bew record with the following initial data.
	 */
	private long createRecords(String name, double averageSpeed, double maxSpeed, double distance, double heartrate){  
	   
		/* TODO: Query and reinsert */
		
		ContentValues values = new ContentValues();    
	   values.put(USERNAME, name);  
	   values.put(AVERAGE_SPEED, averageSpeed);  
	   values.put(MAX_SPEED, maxSpeed);  
	   values.put(DISTANCE, distance);  
	   values.put(AVERAGE_HEARTRATE, heartrate);
	   
	   return database.insert(TABLE, null, values);  
	}
	
	/* Updates current values with new ones */
	public long updateRecords(String userName, double averageSpeed, double maxSpeed, double distance, double heartrate){  
		   
		/* Query and reinsert */
		Cursor cursor = selectRecords(userName);
		cursor.moveToFirst();
		if(!cursor.isAfterLast()){		
			/* Annoying NAN check */
			double averageSpeedTmp;
			if(averageSpeed + cursor.getDouble(cursor.getColumnIndex(AVERAGE_SPEED)) == Double.NaN){
				averageSpeedTmp = 0;
			}
			else{
				averageSpeedTmp = (averageSpeed + cursor.getDouble(cursor.getColumnIndex(AVERAGE_SPEED)))/2;
			}
			
			ContentValues values = new ContentValues();    
		   values.put(USERNAME, userName);  
		   values.put(AVERAGE_SPEED, averageSpeedTmp);  
		   values.put(MAX_SPEED, maxSpeed > cursor.getDouble(cursor.getColumnIndex(MAX_SPEED)) ? maxSpeed : cursor.getDouble(cursor.getColumnIndex(MAX_SPEED)));  
		   values.put(DISTANCE, distance + cursor.getDouble(cursor.getColumnIndex(DISTANCE)));  
		   values.put(AVERAGE_HEARTRATE, (heartrate + cursor.getDouble(cursor.getColumnIndex(AVERAGE_HEARTRATE)))/2);
		   
		   return database.update(TABLE, values, null, null);  
		}

		return createRecords(userName, averageSpeed, maxSpeed, distance, heartrate);

	}
	
	/**
	 * 
	 * @param userName Record to select (This user)
	 * @return Cursor of all rows found (should only be 1).
	 */
	public Cursor selectRecords(String userName) {
		
	       String[] cols = new String[] {USERNAME, AVERAGE_SPEED, MAX_SPEED, DISTANCE, AVERAGE_HEARTRATE};  
	       Cursor mCursor = database.query(true, TABLE, cols, USERNAME + " = \"" + userName + "\"", null, null, null, null, null);  
	       
	     return mCursor; // iterate to get each value.
	}
	
	/**
	 * 
	 * @param name Name of user to grab stats of.
	 * @return A statistics object filled with this user's data.
	 */
	public Statistics selectStatistics(String name){
		Cursor cursor = selectRecords(name);
		Statistics stat = new Statistics();
		stat.setAverageHeartrate(0.0);
		cursor.moveToFirst();
		
		if(!cursor.isAfterLast()){
			
			stat.setAverageSpeed(cursor.getDouble(cursor.getColumnIndex(AVERAGE_SPEED)));
			stat.setMaxSpeed(cursor.getDouble(cursor.getColumnIndex(MAX_SPEED)));
			stat.setDistanceTravelled(cursor.getDouble(cursor.getColumnIndex(DISTANCE)));
			stat.setAverageHeartrate(cursor.getDouble(cursor.getColumnIndex(AVERAGE_HEARTRATE)));
		}
		return stat;
	}
}
