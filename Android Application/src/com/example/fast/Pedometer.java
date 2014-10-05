package com.example.fast;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class Pedometer implements SensorEventListener {

	private SensorManager sensorManager;
	private boolean valid;
	private float steps;
	
	
	public Pedometer( Context context ){
		this.sensorManager = (SensorManager) context.getSystemService( Context.SENSOR_SERVICE );
		Sensor stepSensor = this.sensorManager.getDefaultSensor( Sensor.TYPE_STEP_COUNTER );
		
		if( stepSensor != null ){
			sensorManager.registerListener( this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL );
			this.valid = true;
		}
		else{
			Toast.makeText( context, "Unable to grab the step sensor!", Toast.LENGTH_SHORT ).show();
			Log.e("FAST", "Couldn't grab the step sensor!" );
			this.valid = false;
		}
		
		this.steps = 500.0f;
	}
	
	public int getLastKnownSteps(){
		return (int)steps;
	}
	
	public boolean getValid() {
		return this.valid;
	}
	
	public void unregisterListeners(){
		sensorManager.unregisterListener( this );
	}
	
	public void registerListeners(){
		Sensor stepSensor = this.sensorManager.getDefaultSensor( Sensor.TYPE_STEP_COUNTER );
		
		if( stepSensor != null ){
			sensorManager.registerListener( this, stepSensor, SensorManager.SENSOR_DELAY_UI );
			this.valid = true;
		}
		else{
			Log.e("FAST", "Couldn't grab the step sensor!" );
			this.valid = false;
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		switch( event.sensor.getType() ){
			case Sensor.TYPE_STEP_COUNTER:
				steps = event.values[0];
				break;
			case Sensor.TYPE_STEP_DETECTOR:
				break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

}
