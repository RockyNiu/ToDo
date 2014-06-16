package com.rockyniu.todolist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.rockyniu.todolist.database.comparator.DueComparator;
import com.rockyniu.todolist.database.model.ToDoItem;
import com.rockyniu.todolist.todolist.ToDoFragment;
import com.rockyniu.todolist.todolist.ToGoFragment;
import com.rockyniu.todolist.todolist.ToDoFragment.OnDataPass;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TabsActivity extends Activity implements ActionBar.TabListener, OnDataPass{

	final String TAG = "TabsActivity";
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	String userName;
	String userId;
	ToDoItem alarmedItem;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tabs);

		// user information
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		userId = bundle.getString("_userid");
		userName = bundle.getString("_username");

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_tabs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		this.setResult(RESULT_OK);
		this.finish();
		super.onBackPressed();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment;
			switch (position) {
			case 0:
				fragment = ToDoFragment.newInstance(userId, userName);
				break;
			case 1:
				fragment = ToGoFragment.newInstance();
				break;
			default:
				fragment = ToDoFragment.newInstance(userId, userName);
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale locale = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_activity_to_do_list)
						.toUpperCase(locale);
			case 1:
				return getString(R.string.title_activity_to_go_list)
						.toUpperCase(locale);
			}
			return null;
		}

	}

	@Override
	public void onStop() {
//		doStopListening();
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		// sync();
//		setAlarmTime(this);
	}


	

	@Override
	public void onDataPass(ToDoItem toDoItem) {
		this.alarmedItem = toDoItem;
//		setAlarmTime(this);
	}
	
}
