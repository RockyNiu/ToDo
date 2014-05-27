package com.rockyniu.todolist;

import com.rockyniu.todolist.database.ToDoItemDataSource.SortType;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToGoFragment extends Fragment {

	final String _logTag = "Monitor Location";
	LocationListener _networkListener;
	LocationListener _gpsListener;

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
		
		TextView textView = (TextView) rootView
				.findViewById(R.id.section_label);
		textView.setText(Integer.toString(getArguments().getInt(
				ARG_SECTION_NUMBER)));
		return rootView;
	}
	
	@Override
	public void onStop(){
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
		case R.id.menu_StartListening:
			onStartListening(item);
			return true;
		case R.id.menu_StopListening:
			onStopListening(item);
			return true;
		case R.id.menu_RecentLocation:
			onRecentLocation(item);
			return true;
		case R.id.menu_SingleLocation:
			onSingleLocation(item);
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this.getActivity());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onStartListening(MenuItem item) {
		Log.d(_logTag, "Monitor Location - Start Listening");

		try {
			LocationManager lm = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			_networkListener = new MyLocationListener();
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					_networkListener);
			
//			_gpsListener = new MyLocationListener();
//			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
//					_gpsListener);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onStopListening(MenuItem item) {
		Log.d(_logTag, "Monitor Location - Stop Listening");
	}

	public void onRecentLocation(MenuItem item) {
		Log.d(_logTag, "Monitor - Recent Location");
	}

	public void onSingleLocation(MenuItem item) {
		Log.d(_logTag, "Monitor - Single Location");
	}

	public void onExit(MenuItem item) {
//		Log.d(_logTag, "Monitor Location Exit");
//		doStopListening();
		getActivity().finish();
	}

	void doStopListening() {
		LocationManager lm = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		
		if (_networkListener != null) {
			lm.removeUpdates(_networkListener);
			_networkListener = null;
		}
		Log.d(_logTag, "Monitor Location Exit");
	}
}