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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rockyniu.todolist.database.User;
import com.rockyniu.todolist.database.UserDataSource;

public class LoginActivity extends Activity {

	static final String TAG = "LoginActiviy";

	private static final String TODOSERVER_URL_SUBSCRIBE = "http://192.168.1.4:8000/subscribe";

	// for GCM
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	String SENDER_ID = "215231197297"; // from api console

	static final int REQUEST_ACCOUNT_PICKER = 114;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 911;
	UserDataSource userDataSource;
	AccountManager mAccountManager;
	List<String> namesList;
	ListView mUserNamesList;
	ProgressBar progressBar;

	// for GCM
	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	String regid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		userDataSource = new UserDataSource(this);

		// get GCM textview
		mDisplay = (TextView) findViewById(R.id.display);
		context = getApplicationContext();

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

		checkGooglePlaySerViceAvailable();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
			// String userName = data
			// .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			// goToToDoList(userName);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView();
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
			onAddUserClicked();
			return true;
		case R.id.menu_exit:
			onExitClicked();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/*
	 * click add_new_user menu icon
	 */
	public void onAddUserClicked() {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, true,
				null, null, null, null);
		try {
			startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
		} catch (Exception e) {
			// checkGooglePlaySerViceAvailable(LoginActivity.this);
			Utils.showErrorToast(this, "Fail to add new user.");
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
				Toast.makeText(this, "This device is not supported.",
						Toast.LENGTH_LONG).show();
				finish();
			}
		} else {
			// If check succeeds, proceed with GCM registration.
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			}
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
		User user = userDataSource.selectUser(userName);
		String userId = user.getId();
		String token = user.getPassword();
		Intent intent = new Intent(LoginActivity.this, TabsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("_userid", userId);
		bundle.putString("_username", userName);
		// bundle.putString("_token", token);
		intent.putExtras(bundle);
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

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This app persists the registration ID in shared preferences, but how
		// you store the regID in your app is up to you.
		return getSharedPreferences(LoginActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your
					// app.
					// The request to your server should be authenticated if
					// your app
					// is using accounts.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device
					// will send upstream messages to a server that echo back
					// the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				mDisplay.append(msg + "\n");
			}

		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private Boolean sendRegistrationIdToBackend() {
		// String msg = "";

		try {
			JSONObject registration = new JSONObject();
			JSONArray jsonArray = new JSONArray();

			for (String userName : namesList) {
				User user = userDataSource.selectUser(userName);
				String userId = user.getId();
				JSONObject jsonObj = new JSONObject();

				jsonObj.put("user", userName);
				jsonObj.put("type", "android");
				jsonObj.put("token", userId);
				jsonArray.put(jsonObj);
			}

			// registration.put("datastreams", jsonArray);
			// registration.put("version", version);
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(TODOSERVER_URL_SUBSCRIBE);
			StringEntity se = new StringEntity(jsonArray.get(0).toString());
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-type", "application/json");
			post.setEntity(se);
			client.execute(post);

			// Bundle data = new Bundle();
			// data.putString("my_message", "Hello World");
			// data.putString("my_action",
			// "com.google.android.gcm.demo.app.ECHO_NOW");
			// String id = Integer.toString(msgId.incrementAndGet());
			// gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
			// msg = "Sent message";
		} catch (IOException | JSONException ex) {
			// msg = "Error :" + ex.getMessage();
			return false;
		}
		return true;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}
}
