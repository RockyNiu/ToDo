package com.rockyniu.todolist;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

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

	public static String formationLocationProvider(Context context,
			LocationProvider provider) {
		String name = provider.getName();
		int horizontalAccuracy = provider.getAccuracy();
		int powerRequirements = provider.getPowerRequirement();
		boolean hasMonetaryCost = provider.hasMonetaryCost();
		boolean requiresCell = provider.requiresCell();
		boolean requiresNetwork = provider.requiresCell();
		boolean requiresSatellite = provider.requiresSatellite();
		boolean supportsAltitude = provider.supportsAltitude();
		boolean supportsBearing = provider.supportsBearing();
		boolean supportsSpeed = provider.supportsSpeed();

		String enableMessage = "UNKNOWN";
		if (context != null) {
			LocationManager lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			enableMessage = yOrN(lm.isProviderEnabled(name));
		}

		String logMessage = String
				.format("%s | enable:%s | horizontal accuracy:%d | power:%d | "
						+ "cost:%s | uses cell:%s | uses network:%s | uses satellite:%s | "
						+ "has altitude:%s | has bearing:%s | has speed:%s",
						name, enableMessage, horizontalAccuracy,
						powerRequirements, yOrN(hasMonetaryCost),
						yOrN(requiresCell), yOrN(requiresNetwork),
						yOrN(requiresSatellite), yOrN(supportsAltitude),
						yOrN(supportsBearing), yOrN(supportsSpeed));
		return logMessage;
	}

	static String yOrN(boolean enable) {
		return enable ? "Y" : "N";
	}

	public static String FormatLocationInfo(Location location) {
		String provider = location.getProvider();
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		float accuracy = location.getAccuracy();
		long time = location.getTime();

		return FormatLocationInfo(provider, latitude, longitude, accuracy, time);
	}
}
