package com.rockyniu.todolist.todolist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rockyniu.todolist.R;
import com.rockyniu.todolist.location.MyLocationListener;
import com.rockyniu.todolist.map.JSONParser;
import com.rockyniu.todolist.util.LogHelper;
import com.rockyniu.todolist.util.Utils;

/**
 * To Go Fragment.
 */
public class ToGoFragment extends Fragment {

	final String TAG = "ToGoGragment";
	final String _logTag = "Monitor Location";
	LocationListener _networkListener;
	LocationListener _gpsListener;
	Location currentLocation;
	LatLng currentLatLng;
	private static GoogleMap googleMap;

	private LatLng destLatLng;
	private List<Polyline> polylines;
	private EditText mapSearchEditText;
	private ImageButton mapSearchImageButton;

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

		// setTabTitle(getResources().getString(R.string.title_activity_to_go_list),
		// 1);

		// onStartListening();
		currentLocation = getLastKnowLocation();

		// Statue of Liberty
		// String here = "Statue of Liberty";
		// String snippet =
		// "The colossal neoclassical sculpture on Liberty Island in the middle of New York Harbor, in Manhattan, New York City.";
		// currentLatLng = new LatLng(40.689249, -74.0445);

		// WallStreet
		// String here = "WallStreet";
		// String snippet = "WallStreet, New York City.";
		// LatLng wallStreetLatLng = new LatLng(40.7064, -74.0094);
		// currentLatLng = wallStreetLatLng;

		// Manhattan
		String here = "Manhattan";
		String snippet = "Manhattan, New York City.";
		LatLng manhattanLatLng = new LatLng(40.7903, -73.9597);
		currentLatLng = manhattanLatLng;

		 if (currentLocation != null) {
		 here = "Here";
		 snippet = "My feet step here.";
		 currentLatLng = new LatLng(currentLocation.getLatitude(),
		 currentLocation.getLongitude());
		 }

		mapSearchEditText = (EditText) rootView
				.findViewById(R.id.map_search_edittext);
		mapSearchEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_SEARCH
								|| actionId == EditorInfo.IME_ACTION_DONE
								|| actionId == EditorInfo.IME_ACTION_GO
								|| event.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

							// hide virtual keyboard
							InputMethodManager imm = (InputMethodManager) getActivity()
									.getSystemService(
											Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									mapSearchEditText.getWindowToken(), 0);

							new SearchClicked(mapSearchEditText.getText()
									.toString().trim()).execute();
							mapSearchEditText.setText("",
									TextView.BufferType.EDITABLE);
							return true;
						}
						return false;
					}
				});

		mapSearchImageButton = (ImageButton) rootView
				.findViewById(R.id.map_search_button);
		mapSearchImageButton
				.setOnClickListener(new ImageButton.OnClickListener() {

					@Override
					public void onClick(View v) {
						new SearchClicked(mapSearchEditText.getText()
								.toString().trim()).execute();
					}
				});

		// Get a handle to the Map Fragment
		googleMap = ((MapFragment) getActivity().getFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		googleMap.setMyLocationEnabled(true);
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
				13));

		googleMap.addMarker(new MarkerOptions().title(here).snippet(snippet)
				.position(currentLatLng));

		googleMap.getUiSettings().setCompassEnabled(true);
		googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);

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

		if (gpsLocation == null) {
			Log.d(_logTag, "Monitor Location: GPS Location is NULL");
			return networkLocation;
		} else {
			Log.d(_logTag, "Monitor Location: Network Location is NULL");
			return gpsLocation;
		}
	}

	/**
	 * Make map path request url
	 * 
	 * @param sourcelat
	 * @param sourcelog
	 * @param destlat
	 * @param destlog
	 * @return
	 */
	public String makeURL(double sourcelat, double sourcelog, double destlat,
			double destlog) {
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");// from
		urlString.append(Double.toString(sourcelat));
		urlString.append(",");
		urlString.append(Double.toString(sourcelog));
		urlString.append("&destination=");// to
		urlString.append(Double.toString(destlat));
		urlString.append(",");
		urlString.append(Double.toString(destlog));
		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}

	/**
	 * Draw path on google map
	 * 
	 * @param result
	 */
	public void drawPath(String result) {

		try {
			// clear the previous lines
			if (polylines != null) {
				for (Polyline line : polylines) {
					line.remove();
				}
				polylines.clear();
			}

			// Tranform the string into a json object
			final JSONObject json = new JSONObject(result);
			JSONArray routeArray = json.getJSONArray("routes");
			JSONObject routes = routeArray.getJSONObject(0);
			JSONObject overviewPolylines = routes
					.getJSONObject("overview_polyline");
			String encodedString = overviewPolylines.getString("points");
			List<LatLng> list = decodePoly(encodedString);
			List<Polyline> lines = new ArrayList<Polyline>();

			for (int z = 0; z < list.size() - 1; z++) {
				LatLng src = list.get(z);
				LatLng dest = list.get(z + 1);
				Polyline line = googleMap.addPolyline(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude),
								new LatLng(dest.latitude, dest.longitude))
						.width(5).color(Color.BLUE).geodesic(true));
				lines.add(line);
			}
			polylines = lines;

		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			Utils.showErrorToast(getActivity(), "Fail to parse path");
		}
	}

	/**
	 * Decode the poly lines from encoded path String
	 * 
	 * @param encoded
	 * @return List<LatLng>
	 */
	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}

	/**
	 * Async get the path
	 */
	private class ConnectAsyncTask extends AsyncTask<Void, Void, String> {
		private ProgressDialog progressDialog;
		String url;

		ConnectAsyncTask(String urlPass) {
			url = urlPass;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(ToGoFragment.this.getActivity());
			progressDialog.setMessage("Fetching route, Please wait...");
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			JSONParser jParser = new JSONParser();
			String json = jParser.getJSONFromUrl(url);
			return json;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progressDialog.hide();
			if (result != null) {
				drawPath(result);
			}
		}
	}

	/**
	 * Draw path between two LatLngs;
	 * 
	 * @param sourceLatLng
	 * @param destLatLng
	 */
	private void drawPath(LatLng sourceLatLng, LatLng destLatLng) {
		String urlPass = makeURL(sourceLatLng.latitude, sourceLatLng.longitude,
				destLatLng.latitude, destLatLng.longitude);
		(new ConnectAsyncTask(urlPass)).execute();
	}

	private class SearchClicked extends AsyncTask<Void, Void, LatLng> {
		private ProgressDialog progressDialog;
		private String toSearch;
		private Address address;

		public SearchClicked(String toSearch) {
			this.toSearch = toSearch;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(ToGoFragment.this.getActivity());
			progressDialog.setMessage("Get the address, Please wait...");
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected LatLng doInBackground(Void... voids) {
			LatLng destLatLng = null;
			try {
				Geocoder geocoder = new Geocoder(ToGoFragment.this
						.getActivity().getApplicationContext(),
						Locale.getDefault());
				List<Address> results = geocoder.getFromLocationName(toSearch,
						1);

				if (results.size() == 0) {
					return null;
				}

				address = results.get(0);
				destLatLng = new LatLng(address.getLatitude(),
						address.getLongitude());

			} catch (Exception e) {
				Log.e("", "Something went wrong: ", e);
				return null;
			}
			return destLatLng;
		}

		protected void onPostExecute(LatLng result) {
			super.onPostExecute(result);
			progressDialog.hide();
			if (result != null) {
				destLatLng = result;
				drawPath(currentLatLng, destLatLng);
			}
		}

	}

}