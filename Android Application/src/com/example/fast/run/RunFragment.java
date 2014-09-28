package com.example.fast.run;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fast.Database;
import com.example.fast.MainActivity;
import com.example.fast.R;
import com.example.fast.Statistics;


/**
 * 
 * Screen seen when the user selects the Run Now option.  Displays all statistics of the current run.
 *
 */
public class RunFragment extends Fragment {
	
	private TextView gpsText;
	private TextView distance;
	private TextView speed;
	private TextView heartRate;
	private TextView connected;
	private boolean clicked = false;
	private Database database;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_run, container, false);
		
		return view;
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		
		final Button startButton = (Button) getActivity().findViewById(R.id.startButton);
		final Button stopButton = (Button) getActivity().findViewById(R.id.endButton);
		
		gpsText = (TextView) getActivity().findViewById(R.id.runTime);
		distance = (TextView) getActivity().findViewById(R.id.distance);
		speed = (TextView) getActivity().findViewById(R.id.speed);
		database = ((MainActivity)getActivity()).getDatabase();
		heartRate = (TextView) getActivity().findViewById(R.id.heartRateConnected);
		connected = (TextView) getActivity().findViewById(R.id.heartRateCurrent);
				
		

		
		/* Start button */
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(clicked == false){
					clicked = true;
					((MainActivity) getActivity()).startStatisticsUpdater();
					startButton.setText("Pause");
					stopButton.setVisibility(View.VISIBLE); //show the stop button
				}
				else{
					clicked = false;
					((MainActivity) getActivity()).stopStatisticsUpdater();
					startButton.setText("Resume");
				}
			}
		});
		
		/* Stop (End) button */
		stopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clicked = false;
				/* Store stats, ask to share? */
				((MainActivity) getActivity()).stopStatisticsUpdater();
				startButton.setText("Start");
				stopButton.setVisibility(View.GONE); //after clicked, make invisible
				
				/* Post the updated values to the DATABASE */
				/* Schema (_id): AVERAGE_SPEED, MAX_SPEED, DISTANCE, USERNAME */
		        updateDatabaseRecord("TEST");
		        
		        /* Set the home frag text, update from DB */
		        ((MainActivity) getActivity()).updateEverything();
		        
			}
		});
		

	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setRetainInstance(true);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	/*
	 * Updates the database with the new data once the run has been ended.
	 */
	private void updateDatabaseRecord(String name){
		double avgSpeed = ((MainActivity)getActivity()).getStatistics().getAverageSpeed();
        double maxSpeed = ((MainActivity)getActivity()).getStatistics().getMaxSpeed();
       	double distance = ((MainActivity)getActivity()).getStatistics().getDistanceTravelled();
       	double heartrate = ((MainActivity)getActivity()).getStatistics().getAverageHeartrate();
        database.updateRecords(name, avgSpeed, maxSpeed, distance, heartrate);
	}
	
	/**
	 *  Updates all textviews with new data in the runFragment
	 *  */
	public void updateRunTextViews() {
		
		Statistics statistics = ((MainActivity) getActivity()).getStatistics();
		
		/* Update text in Run Fragment, if possible */
			if(((MainActivity)getActivity()).getUsingMetric()){
		   		distance.setText(MainActivity.format(Statistics.kilometers(statistics.getDistanceTravelled())) + " kilometers");
		   		speed.setText(MainActivity.format(Statistics.kilometersPerHour(statistics.getAverageSpeed())) + " km/h");
			}
			
			else{
		   		distance.setText(MainActivity.format(Statistics.miles(statistics.getDistanceTravelled())) + " miles");
		   		speed.setText(MainActivity.format(Statistics.milesPerHour(statistics.getAverageSpeed())) + " MPH");
			}
			
			heartRate.setText(MainActivity.format(((MainActivity) getActivity()).getHeartRate()) + " bpm");
			connected.setText(((MainActivity) getActivity()).getConnection());
	}
	
	public TextView getGpsText(){
		return gpsText;
	}
	
	public TextView getDistance(){
		return distance;
	}
	
	public TextView getSpeed(){
		return speed;
	}
}
