package com.example.fast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Our extension of the SQLite Helper.  Based off of ToDo List lab and the Google Developer website.
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper{
		 
	    private static String DB_NAME = "faster2.db";
	    private static final String DB_CREATE = "CREATE TABLE fast_table(_id INTEGER PRIMARY KEY autoincrement, username TEXT not null,"
	    		+ " average_speed NUMERIC not null, max_speed NUMERIC not null, distance NUMERIC not null, " 
	    		+ " average_heartrate NUMERIC not null);";
	    private SQLiteDatabase myDatabase; 
	    private final Context myContext;
        private static final int DATABASE_VERSION = 14;

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            this.myContext = context;
        }

	 
        /**
	     * Creates an empty database on the system and rewrites it with your own database.
	     * */
	    public void createDatabase() throws IOException{
	 
	    	boolean dbExist = checkDatabase();
	 
	    	if(dbExist){
	    	}else{
	        	this.getReadableDatabase();
	 
	        	try {
	    			copyDatabase();
	    		} catch (IOException e) {
	        		throw new Error("Error copying database");
	        	}
	    	}
	 
	    }
	 
	    /**
	     * Check if the database already exist to avoid re-copying the file each time you open the application.
	     * @return true if it exists, false if it doesn't
	     */
	    private boolean checkDatabase(){
	 
	    	SQLiteDatabase checkDB = null;
	 
	    	try{
	    		String myPath = DB_NAME;
	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
	 
	    	}catch(SQLiteException e){
	    	}
	 
	    	if(checkDB != null){
	    		checkDB.close();
	    	}
	 
	    	return checkDB != null ? true : false;
	    }
	 
	    /**
	     * Copies your database from your local assets-folder to the just created empty database in the
	     * system folder, from where it can be accessed and handled.
	     * This is done by transfering bytestream.
	     * */
	    private void copyDatabase() throws IOException{
	 
	    	//Open your local db as the input stream
	    	InputStream myInput = myContext.getAssets().open(DB_NAME);
	 
	    	// Path to the just created empty db
	    	String outFileName = DB_NAME;
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream(outFileName);
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
	 
	    public void openDatabase() throws SQLException{
	 
	    	//Open the database
	        String myPath = DB_NAME;
	    	myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
	 
	    }
	 
	    @Override
		public synchronized void close() {
	 
	    	    if(myDatabase != null)
	    		    myDatabase.close();
	 
	    	    super.close();
	 
		}
	 
		@Override
		public void onCreate(SQLiteDatabase db) {
			//db.execSQL("DROP TABLE IF EXISTS fast_table");
			db.execSQL(DB_CREATE);
		}
	 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	 
			db.execSQL("DROP TABLE IF EXISTS fast_table");
            onCreate(db);
		}
}
