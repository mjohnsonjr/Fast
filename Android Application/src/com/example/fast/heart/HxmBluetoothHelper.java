package com.example.fast.heart;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import zephyr.android.HxMBT.BTClient;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


/**
 * Used for the heartrate monitors.  Borrowed heavily from the heartrate lab.
 *
 */
public class HxmBluetoothHelper
{
	public static final int MESSAGE_HEART_RATE    = 0x0100;
	public static final int MESSAGE_INSTANT_SPEED = 0x0101;
	
	public static final int MESSAGE_BATTERY = 0x0110;
	public static final String DATA_BATTERY = "com.example.btheartrate.data.battery";
	
	public static final String DATA_HEART_RATE    = "com.example.btheartrate.data.heart_rate";
	public static final String DATA_INSTANT_SPEED = "com.example.btheartrate.data.instant_speed";
	
	private static final String BIO_HARNESS_MAC = "00:07:80:9D:8A:E8";
	private static final String BIO_HARNESS_PIN = "1234";
	
	private static final String HXM_PREFIX = "HXM";

	private Context context;
	private Handler handler;

	private BluetoothAdapter adapter;

	private BTClient btClient;
	private ConnectedListener connectedListener;

	public HxmBluetoothHelper(Context context, Handler handler)
	{
		adapter = BluetoothAdapter.getDefaultAdapter();

		this.context = context;
		this.handler = handler;
		
		registerReceivers();
	}

	private void registerReceivers()
	{
		// Sending a message to android that we are going to initiate a pairing request
		IntentFilter pairingRequestFilter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
		// Registering a new BTBroadcast receiver from the Main Activity context with pairing request event
		context.getApplicationContext().registerReceiver(new PairingRequestReceiver(), pairingRequestFilter);

		// Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
		IntentFilter bondStateChangedReceiver = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.getApplicationContext().registerReceiver(new BondStateReceiver(), bondStateChangedReceiver);
	}

	public boolean connect()
	{	
		if(adapter == null) return false;
		
		BluetoothDevice hxmDevice = findHxmDevice(adapter);

		btClient = new BTClient(adapter, hxmDevice.getAddress());
		connectedListener = new ConnectedListener(handler);
		btClient.addConnectedEventListener(connectedListener);
		
		if(btClient.IsConnected())
		{
			btClient.start();
			return true;
		}

		return false;
	}

	private BluetoothDevice findHxmDevice(BluetoothAdapter adapter)
	{
		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

		for (BluetoothDevice device : pairedDevices)
		{
			if (device.getName().startsWith(HXM_PREFIX))
			{
				return device;
			}
		}

		return adapter.getRemoteDevice(BIO_HARNESS_MAC);
	}
	
	public BluetoothDevice getDevice()
	{
		if(btClient != null)
		{
			return btClient.getDevice();
		}
		
		return null;
	}
	
	public boolean isConnected()
	{
		if(btClient != null)
		{
			return btClient.IsConnected();
		}
		
		return false;
	}
	
	public void disconnect()
	{
		// This disconnects listener from acting on received messages
		btClient.removeConnectedEventListener(connectedListener);
		// Close the communication with the device and throw an exception if failure
		btClient.Close();
	}

	private class BondStateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle extras = intent.getExtras();
			Log.d("HeartMonitor", "Bond State: " + extras.getInt(BluetoothDevice.EXTRA_BOND_STATE));
		}
	}

	private class PairingRequestReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle extras = intent.getExtras();
			
			Log.d("PairingRequest", intent.getAction());
			Log.d("PairingRequest", extras.get(BluetoothDevice.EXTRA_DEVICE).toString());
			Log.d("PairingRequest", extras.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
			
			try
			{
				BluetoothDevice device = adapter.getRemoteDevice(extras.get(BluetoothDevice.EXTRA_DEVICE).toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
				byte[] pin = (byte[]) m.invoke(device, BIO_HARNESS_PIN);
				m = device.getClass().getMethod("setPin", new Class[] { pin.getClass() });
				Object result = m.invoke(device, pin);
				Log.d("PairingRequest", result.toString());
			}
			catch (SecurityException         e) { e.printStackTrace(); }
			catch (NoSuchMethodException     e) { e.printStackTrace(); }
			catch (IllegalArgumentException  e) { e.printStackTrace(); }
			catch (IllegalAccessException    e) { e.printStackTrace(); }
			catch (InvocationTargetException e) { e.printStackTrace(); }
		}
	}
}
