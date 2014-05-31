package com.rockyniu.todolist;

import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToGoFragment extends Fragment {

	final String _logTag = "Monitor Location";
	LocationListener _networkListener;
	LocationListener _gpsListener;
	Location currentLocation;
	private static GoogleMap googleMap;

	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for 1.
	 */
	public static ToGoFragment newInstance() {
		ToGoFragment fragment = new ToGoFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, 1);
		fragment.setArguments(args);
		return fragment;
	}

	public ToGoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_tab_togo, container,
				false);

		setHasOptionsMenu(true);

		setTabTitle("Where I am", 1);
		// TextView textView = (TextView) rootView
		// .findViewById(R.id.section_label);
		// textView.setText(Integer.toString(getArguments().getInt(
		// ARG_SECTION_NUMBER)));

		// onStartListening();
		currentLocation = getLastKnowLocation();
		String here = "Statue of Liberty";
		String snippet = "The colossal neoclassical sculpture on Liberty Island in the middle of New York Harbor, in Manhattan, New York City.";
		LatLng currentLatLng = new LatLng(40.689249, -74.0445);
		if (currentLocation != null) {
			here = "Here";
			snippet = "My feet step here.";
			currentLatLng = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());
		}

		// Get a handle to the Map Fragment
		googleMap = ((MapFragment) getActivity().getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		googleMap.setMyLocationEnabled(true);
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
				13));

		googleMap.addMarker(new MarkerOptions().title(here).snippet(snippet)
				.position(currentLatLng));

		return rootView;
	}

	@Override
	public void onStop() {
		doStopListening();
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_to_go_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case R.id.menu_StartListening:
		// onStartListening();
		// return true;
		// case R.id.menu_StopListening:
		// onStopListening();
		// return true;
		// case R.id.menu_RecentLocation:
		// onRecentLocation();
		// return true;
		// case R.id.menu_SingleLocation:
		// onSingleLocation();
		// return true;
		// case R.id.menu_AccurateProvider:
		// onAccurateProvider();
		// return true;
		// case R.id.menu_LowPowerProvider:
		// onLowPowerProvider();
		// return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this.getActivity());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onStartListening() {
		Log.d(_logTag, "Monitor Location - Start Listening");

		try {
			LocationManager lm = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			_networkListener = new MyLocationListener();
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					_networkListener);

			_gpsListener = new MyLocationListener();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					_gpsListener);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onStopListening() {
		Log.d(_logTag, "Monitor Location - Stop Listening");

		doStopListening();
	}

	public void onRecentLocation() {
		Log.d(_logTag, "Monitor - Recent Location");

		Location networkLocation;
		Location gpsLocation;

		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		networkLocation = lm
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (networkLocation == null) {
			Log.d(_logTag, "Monitor Location: Network Location is NULL");
		} else {
			String networkLogMessage = LogHelper
					.FormatLocationInfo(networkLocation);
			Log.d(_logTag, "Monitor Location " + networkLogMessage);
		}

		if (gpsLocation == null) {
			Log.d(_logTag, "Monitor Location: GPS Location is NULL");
		} else {
			String gpsLogMessage = LogHelper.FormatLocationInfo(gpsLocation);
			Log.d(_logTag, "Monitor Location " + gpsLogMessage);
		}
	}

	public void onSingleLocation() {
		Log.d(_logTag, "Monitor - Single Location");

		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		_networkListener = new MyLocationListener();
		lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
				_networkListener, null);

		_gpsListener = new MyLocationListener();
		lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, _gpsListener, null);

	}

	public void onAccurateProvider() {
		Criteria criteria = new Criteria();

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setSpeedRequired(true);
		criteria.setAltitudeRequired(true);

		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		List<String> matchingProvideNames = lm.getProviders(criteria, false);
		for (String providerName : matchingProvideNames) {
			LocationProvider provider = lm.getProvider(providerName);
			String logMessage = LogHelper.formationLocationProvider(
					getActivity(), provider);
			Log.d(_logTag, logMessage);
		}
	}

	public void onLowPowerProvider() {
		Criteria criteria = new Criteria();

		criteria.setPowerRequirement(Criteria.POWER_LOW);

		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		List<String> matchingProvideNames = lm.getProviders(criteria, false);
		for (String providerName : matchingProvideNames) {
			LocationProvider provider = lm.getProvider(providerName);
			String logMessage = LogHelper.formationLocationProvider(
					getActivity(), provider);
			Log.d(_logTag, logMessage);
		}
	}

	public void onExit() {
		// Log.d(_logTag, "Monitor Location Exit");
		// doStopListening();
		getActivity().finish();
	}

	void doStopListening() {
		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		if (_networkListener != null) {
			lm.removeUpdates(_networkListener);
			_networkListener = null;
		}

		if (_gpsListener != null) {
			lm.removeUpdates(_gpsListener);
			_gpsListener = null;
		}
		Log.d(_logTag, "Monitor Location Exit");
	}

	// set TabTitle
	public void setTabTitle(String title, int tabIndex) {
		String str = title.toUpperCase(Locale.getDefault());
		getActivity().getActionBar().getTabAt(tabIndex).setText(str);
	}

	// get position
	public Location getLastKnowLocation() {
		Log.d(_logTag, "Monitor - Recent Location");

		Location networkLocation;
		Location gpsLocation;

		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);

		networkLocation = lm
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (networkLocation == null && gpsLocation == null) {
			Log.d(_logTag,
					"Monitor Location: Network and GPS Location both are NULL");
			return null;
		}

		if (networkLocation != null && gpsLocation != null) {
			if (networkLocation.getTime() > gpsLocation.getTime()) {
				return networkLocation;
			} else {
				return gpsLocation;
			}
		}

		if (networkLocation == null) {
			Log.d(_logTag, "Monitor Location: Network Location is NULL");
			return gpsLocation;
		} else {
			Log.d(_logTag, "Monitor Location: GPS Location is NULL");
			return networkLocation;
		}
	}
}