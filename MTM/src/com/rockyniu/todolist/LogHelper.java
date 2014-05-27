package com.rockyniu.todolist;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.location.Location;

public class LogHelper {
	static final String _timeStampFormat = "yyyy-MM-dd'T'HH:mm:ss";
	static final String _timeStampTimeZoneID = "UTC";

	public static String FormatLocationInfo(String provider, double latitude,
			double longitude, float accuracy, long time) {
		SimpleDateFormat timeStampFormatter = new SimpleDateFormat(
				_timeStampFormat);
		timeStampFormatter.setTimeZone(TimeZone
				.getTimeZone(_timeStampTimeZoneID));

		String timeStamp = timeStampFormatter.format(time);

		String logMessage = String.format(
				"%s | latitude/longitude=%f/%f | accuracy=%f | Time=%s",
				provider, latitude, longitude, accuracy, timeStamp);
		
		return logMessage;
	}
	
	public static String FormatLocationInfo(Location location){
		String provider = location.getProvider();
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		float accuracy = location.getAccuracy();
		long time = location.getTime();
		
		return FormatLocationInfo(provider, latitude, longitude, accuracy, time);
	}
}
