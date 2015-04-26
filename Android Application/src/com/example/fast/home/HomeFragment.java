package com.example.fast.home;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fast.MainActivity;
import com.example.fast.R;
import com.example.fast.Statistics;
import com.google.android.gms.plus.Plus;

/**
 * 
 * Fragment that displays the user's all time stats and is the initial view seen when starting the application.
 *
 */
public class HomeFragment extends Fragment {

	//private SignInButton signInButton;
	private TextView totalDistance;
	private TextView userName;
	private TextView avgHeart;
	private TextView stepCount;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		/* Reference all updatable textviews */
		userName = (TextView) getActivity().findViewById( R.id.userName );
		//signInButton = (SignInButton) getActivity().findViewById(R.id.sign_in_button);
		totalDistance = (TextView) getActivity().findViewById( R.id.totalDistance );
		avgHeart = (TextView) getActivity().findViewById( R.id.averageHeartrate );
		stepCount = (TextView) getActivity().findViewById( R.id.pedometerCount );
		
		/* Sign into Google! */
		((MainActivity)getActivity()).signInToGoogleAccount();
		
		
		/* Query DB for stats TODO: Set homescreen stats. */
		/* Schema (_id): AVERAGE_SPEED, MAX_SPEED, DISTANCE, USERNAME */
		updateHomeTextViews();
	}
	
	/* Updates all textviews with new data in the homeFragment*/
	public void updateHomeTextViews(){
		
			if(((MainActivity) getActivity()).getUsingMetric()){
		   		totalDistance.setText(MainActivity.format(Statistics.kilometers(((MainActivity) getActivity()).getAllTimeStatistics().getDistanceTravelled())) + " kilometers");
			}
			
			else{
		   		totalDistance.setText(MainActivity.format(Statistics.miles(((MainActivity) getActivity()).getAllTimeStatistics().getDistanceTravelled())) + " miles");
			}
			
			if(((MainActivity) getActivity()).getPlusClient().isConnected()){
				userName.setText( Plus.AccountApi.getAccountName(((MainActivity) getActivity()).getPlusClient()));
			}
			
			
			avgHeart.setText(MainActivity.format(((MainActivity) getActivity()).getAllTimeStatistics().getAverageHeartrate()) + " bpm");
			
			stepCount.setText( ( (MainActivity) getActivity()).getSteps()  + " steps");
	}
}
