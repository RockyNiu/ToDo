package com.rockyniu.todolist.location;

import com.rockyniu.todolist.util.LogHelper;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {
	final String _logTag = "Monitor Location";

	@Override
	public void onLocationChanged(Location location) {
		String provider = location.getProvider();
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		float accuracy = location.getAccuracy();
		long time = location.getTime();

		String logMessage = LogHelper.FormatLocationInfo(provider, latitude,
				longitude, accuracy, time);
		
		Log.d(_logTag, "Monitor Location: " + logMessage);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(_logTag, "Monitor Location - Provider Enabled: " + provider);

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(_logTag, "Monitor Location - Provider Disabled: " + provider);
	}

}
