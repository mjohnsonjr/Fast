package com.example.fast.heart;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.fast.R;

/**
 * 
 * Fragment that displays all heartrate information to the user, when shown.
 *
 */
public class HeartFragment extends Fragment{
	
private static final int HR_CHECK_INTERVAL =  5000; //  5 seconds (default)
	
	private int heartRate = 0;
	private String heartRateString;
	
	private HxmBluetoothHelper hxmHelper;
	private Handler alertHandler;

	// Text Views
	private TextView connectionStatusText;
	private TextView heartRateText;
	private TextView instantSpeedText;
	private TextView batteryText;
	// Buttons
	private Button connectButton;
	private Button disconnectButton;
	
	//Timer
	private Handler mHandler = new Handler();
	private long startTime = 0, currentTime = 0;
	/**
	 * REFRESH_RATE defines how often we should update the timer to show how much time has elapsed.
	 * refresh every 100 milliseconds
	 */
	private final int REFRESH_RATE = 500;
	MediaPlayer mPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  
        View rootView = inflater.inflate(R.layout.fragment_heart, container, false);
          
        return rootView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
    	
		connectionStatusText = (TextView) this.getActivity().findViewById(R.id.connection_status);
		connectionStatusText.setText(R.string.disconnect);
		connectButton = (Button) this.getActivity().findViewById(R.id.connect);
		connectButton.setOnClickListener(onConnectListener);
		disconnectButton = (Button) this.getActivity().findViewById(R.id.disconnect);
		disconnectButton.setOnClickListener(onDisconnectListener);
		findViews();
		
		hxmHelper = new HxmBluetoothHelper(this.getActivity(), dataHandler);

		alertHandler = new Handler();
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	private void findViews()
	{
		connectionStatusText = (TextView) this.getActivity().findViewById(R.id.connection_status);
		heartRateText        = (TextView) this.getActivity().findViewById(R.id.heart_rate);
		instantSpeedText     = (TextView) this.getActivity().findViewById(R.id.instant_speed);
		batteryText = (TextView) this.getActivity().findViewById(R.id.battery);

		connectButton    = (Button) this.getActivity().findViewById(R.id.connect);
		disconnectButton = (Button) this.getActivity().findViewById(R.id.disconnect);
	}

	OnClickListener onConnectListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			hxmHelper.connect();
			
			heartRateText.setText(R.string.heart_rate_zero);
			instantSpeedText.setText(R.string.instant_speed_zero);

			if (hxmHelper.isConnected())
			{
				String statusConnected = HeartFragment.this.getResources().getString(R.string.connect);
				connectionStatusText.setText(statusConnected + ' ' +  hxmHelper.getDevice().getName());

				startTime = System.currentTimeMillis();
				mHandler.removeCallbacks(startTimer);
				mHandler.postDelayed(startTimer, REFRESH_RATE);
			}
			else
			{
				connectionStatusText.setText(R.string.status_unable_to_connect);
			}
		}
	};

	private OnClickListener onDisconnectListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			// Disconnect from the HxM Device
			if(hxmHelper.isConnected()){
				
				hxmHelper.disconnect();
				connectionStatusText.setText(R.string.status_disconnected);
			}
		}
	};

	private final Handler dataHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{

			case HxmBluetoothHelper.MESSAGE_INSTANT_SPEED:
				String instantSpeed = msg.getData().getString(HxmBluetoothHelper.DATA_INSTANT_SPEED);
				instantSpeedText.setText(instantSpeed);
				Log.d("HeartMonitor", "Instaneous speed is " + instantSpeed);

				break;
				
			case HxmBluetoothHelper.MESSAGE_HEART_RATE:
				String heartRate2 = msg.getData().getString(HxmBluetoothHelper.DATA_HEART_RATE);
				heartRate = Integer.parseInt(heartRate2);
				heartRateString = heartRate2;
				heartRateText.setText(heartRate2);
				Log.d("HeartMonitor", "Heart rate is " + heartRate2);
				
				break;
				
			case HxmBluetoothHelper.MESSAGE_BATTERY:
				int battery = msg.getData().getInt(HxmBluetoothHelper.DATA_BATTERY);
				batteryText.setText(Integer.toString(battery));
				Log.d("HeartMonitor", "Battery is " + battery);
				
				break;
			}
		}
	};
	
	public String getHeart(){
		if(heartRateString != null){
			return heartRateString;
		}
		else return "0.0";
	}
	public TextView getConnection(){
		return connectionStatusText;
	}

    /**
     * Converts the elapsed given time and updates the display
     * 
     * @param time the time to update the current display to
     */
    private void updateTimer (long time){


		//Convert the milliseconds,seconds,minutes,hours to String and format to ensure it has a leading zero when required
    	Integer seconds = (int) ((time/1000));
		Integer minutes = seconds / 60;
		Integer hours = minutes / 60;
		
		seconds = seconds % 60;
		minutes = minutes % 60;
		String hoursString = leadingZero(hours);
		String minutesString = leadingZero(minutes);
		String secondsString = leadingZero(seconds);
		
		String concat = hoursString + ":" + minutesString + ":" + secondsString;

		String msString = String.valueOf(time % 360000);
		Character msChar = msString.charAt(msString.length() -3);
	}
    
    private String leadingZero(Integer n) {
		
    	return n > 9 ? n.toString() : "0" + n.toString(); 
	}

	/**
     * Create a Runnable startTimer that makes timer runnable.
     */
    private Runnable startTimer = new Runnable() {
    	   public void run() {
    		   currentTime = System.currentTimeMillis() - startTime;
    		   updateTimer(currentTime);
    		   mHandler.postDelayed(this, REFRESH_RATE);
    		   if(hxmHelper.isConnected() && (heartRate < 50 || heartRate > 80)){
    			   //mPlayer.start();
    			   Log.d("HeartRateTry", "Measured" + heartRate);
    		   }
    		}
    	};

}
