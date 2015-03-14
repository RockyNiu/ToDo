package com.rockyniu.todolist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rockyniu.todolist.database.UserDataSource;
import com.rockyniu.todolist.database.model.Converter;
import com.rockyniu.todolist.database.model.User;
import com.rockyniu.todolist.gcm.GcmBroadcastReceiver;
import com.rockyniu.todolist.register.Register;
import com.rockyniu.todolist.user.UserInformation;
import com.rockyniu.todolist.util.Constance;
import com.rockyniu.todolist.util.ToastHelper;

public class LoginActivity extends Activity {

	static final String TAG = "LoginActiviy";
	Register register;
	UserInformation userInformation;
	// private static final String TODOSERVER_URL_SUBSCRIBE =
	// "http://todo-server.cloudapp.net:8000/subscribe";
	//
	// // for GCM
	// // public static final String EXTRA_MESSAGE = "message";
	// public static final String PROPERTY_REG_ID = "registration_id";
	// public static final String BACKEND_REG_STATUS =
	// "registration_on_backend_";
	// private static final String PROPERTY_APP_VERSION = "appVersion";
	// String GCM_SENDER_ID = "215231197297"; // from api console
	// GoogleCloudMessaging gcm;
	// // AtomicInteger msgId = new AtomicInteger();
	// SharedPreferences prefs;
	// Context context;
	// String regid;

	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 911;
	UserDataSource userDataSource;
	AccountManager mAccountManager;
	List<String> namesList;
	ListView mUserNamesList;
	ProgressBar progressBar;

	// // for gcm service
	// Intent gcmServiceIntent;
	// GcmBroadcastReceiver gcmBroadcastReceiver;

	// user information
	String userId;
	String userName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		userDataSource = new UserDataSource(this);
		// GCM information
		// mDisplay = (TextView) findViewById(R.id.display);
		// context = getApplicationContext();

		// get account names
		mAccountManager = AccountManager.get(this);
		namesList = getAccounts(mAccountManager);

		mUserNamesList = (ListView) findViewById(R.id.namesList);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, namesList);
		mUserNamesList.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		// delete redundant user data
		deleteRedundantUsers();

		// set click listener to user name list
		mUserNamesList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View arg1,
							int pos, long id) {

						String userName = parent.getItemAtPosition(pos)
								.toString();
						goToToDoList(userName);
					}
				});

		register = new Register(LoginActivity.this);
		userInformation = new UserInformation(LoginActivity.this);
		checkGooglePlaySerViceAvailable();

		// get the user settings
		userId = userInformation.getUserId();
		userName = userInformation.getUserName();

		// ToastHelper.showToastInternal(this, getString(R.string.welcome), 2,
		// 0f,
		// 0.5f);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constance.REQUEST_USER_PICKER
				&& resultCode == RESULT_OK) {
			userName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			register.sendRegistrationIdToBackend(userName);
		} else {
			// TODO
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (userName != null) {
			goToToDoList(userName);
		} else {
			refreshView();
		}
		// checkGooglePlaySerViceAvailable(LoginActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_new_user:
			userInformation.onUserPicker();
			return true;
		case R.id.menu_exit:
			onExitClicked();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * click exit menu icon
	 */
	public void onExitClicked() {
		finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			toggleActionBar();
		}
		return true;
	}

	private void toggleActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			if (actionBar.isShowing()) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}

	private void checkGooglePlaySerViceAvailable() {
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(LoginActivity.this);
		if (status != ConnectionResult.SUCCESS) {
			Log.v(TAG, "GoolgePlayService is not available.");
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				GooglePlayServicesUtil.getErrorDialog(status,
						LoginActivity.this, REQUEST_CODE_RECOVER_PLAY_SERVICES)
						.show();
			} else {
				Toast.makeText(
						this,
						getResources().getString(R.string.device_not_supported),
						Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			// If check succeeds, proceed with GCM registration.
			// gcm = GoogleCloudMessaging.getInstance(this);
			// regid = getRegistrationId(context);
			//
			// if (regid == null || regid.isEmpty()) {
			// registerInBackground();
			// }
			register.sendRegistrationIdToGcm();
		}
	}

	private List<String> getAccounts(AccountManager mAccountManager) {
		Account[] accounts = mAccountManager
				.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		List<String> namesList = new ArrayList<String>();
		for (int i = 0; i < accounts.length; i++) {
			namesList.add(accounts[i].name);
		}
		return namesList;
	}

	private void goToToDoList(String userName) {
		register.sendRegistrationIdToBackend(userName);
		User user = userDataSource.selectUser(userName);
		String userId = user.getId();
		// String token = user.getPassword();

		// save user settings
		userInformation.saveUserInfo(userId, userName);

		// Sent the intent
		Intent intent = new Intent(LoginActivity.this, TabsActivity.class);
		startActivity(intent);
	}

	private void deleteRedundantUsers() {
		List<User> usersList = userDataSource.getAllUsers();
		for (User user : usersList) {
			if (!namesList.contains(user.getName())) {
				userDataSource.deleteUser(user);
			}
		}
	}

	private void refreshView() {
		namesList = getAccounts(mAccountManager);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, namesList);
		mUserNamesList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

}
