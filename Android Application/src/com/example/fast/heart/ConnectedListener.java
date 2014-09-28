package com.example.fast.heart;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ConnectListenerImpl;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrPacketEvent;
import zephyr.android.HxMBT.ZephyrPacketListener;
import zephyr.android.HxMBT.ZephyrProtocol;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 
 * Connects and listens for packets from the heartrate monitors.
 */
public class ConnectedListener extends ConnectListenerImpl
{	
	private Handler handler;
	
	public ConnectedListener(Handler handler)
	{
		super(handler, null);
		
		this.handler = handler;
	}
	
	public void Connected(ConnectedEvent<BTClient> event)
	{
		BTClient btClient = event.getSource();
		
		Log.d("ConnectedListener", "Connected to BioHarness " + btClient.getDevice().getName());

		// Creates a new ZephyrProtocol object and adds a new PacketReceivedListener
		ZephyrProtocol zephyrProtocol = new ZephyrProtocol(btClient.getComms());
		zephyrProtocol.addZephyrPacketEventListener(new PacketReceivedListener());
	}
	

	private class PacketReceivedListener implements ZephyrPacketListener
	{
		private static final int HR_SPEED_DIST_PACKET_ID = 0x26;
		
		private HRSpeedDistPacketInfo packetInfo = new HRSpeedDistPacketInfo();
		
		public void ReceivedPacket(ZephyrPacketEvent event)
		{
			ZephyrPacketArgs packet = event.getPacket();
			
			byte crcStatus = packet.getCRCStatus();
			
			if (packet.getMsgID() == HR_SPEED_DIST_PACKET_ID)
			{
				byte[] data = packet.getBytes();
				
				// TODO Create and send a Heart Rate Message (reference the next section)
				byte heartRate =  packetInfo.GetHeartRate(data);
				
				Message heartRateMessage = handler.obtainMessage(HxmBluetoothHelper.MESSAGE_HEART_RATE);
				Bundle heartRateData = new Bundle();
				heartRateData.putByte(HxmBluetoothHelper.DATA_HEART_RATE, heartRate);
				heartRateData.putString(HxmBluetoothHelper.DATA_HEART_RATE, String.valueOf(heartRate));
				heartRateMessage.setData(heartRateData);

				handler.sendMessage(heartRateMessage);
				Log.d("PacketReceivedListner", "Instant Speed is " + heartRate);
				
				// Create and send an Instant Speed Message
				double instantSpeed = packetInfo.GetInstantSpeed(data);
				
				Message instantSpeedMessage = handler.obtainMessage(HxmBluetoothHelper.MESSAGE_INSTANT_SPEED);
				Bundle instantSpeedData = new Bundle();
				instantSpeedData.putDouble(HxmBluetoothHelper.DATA_INSTANT_SPEED, instantSpeed);
				instantSpeedData.putString(HxmBluetoothHelper.DATA_INSTANT_SPEED, String.valueOf(instantSpeed));
				instantSpeedMessage.setData(instantSpeedData);
				
				handler.sendMessage(instantSpeedMessage);
				Log.d("PacketReceivedListener", "Instant Speed is " + instantSpeed);
				
				
				//Create and send a batter rate message
				int battery = packetInfo.GetBatteryChargeInd(data);
				
				Message batteryMessage = handler.obtainMessage(HxmBluetoothHelper.MESSAGE_BATTERY);
				Bundle batteryData = new Bundle();
				batteryData.putInt(HxmBluetoothHelper.DATA_BATTERY, battery);
				batteryMessage.setData(batteryData);
				
				handler.sendMessage(batteryMessage);
				Log.d("PacketRecievedListener", "Batter is " + battery);
				
			}
		}
	}
}
