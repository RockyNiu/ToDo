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

//		ToastHelper.showToastInternal(this, getString(R.string.welcome), 2, 0f,
//				0.5f);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constance.REQUEST_USER_PICKER
				&& resultCode == RESULT_OK) {
			String userName = data
					.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			register.sendRegistrationIdToBackend(userName);
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
				Toast.makeText(this, "This device is not supported.",
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
		// Bundle bundle = new Bundle();
		// bundle.putString("_userid", userId);
		// bundle.putString("_username", userName);
		// // bundle.putString("_token", token);
		// intent.putExtras(bundle);
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

	// /**
	// * Gets the current registration ID for application on GCM service.
	// * <p>
	// * If result is empty, the app needs to register.
	// *
	// * @return registration ID, or empty string if there is no existing
	// * registration ID.
	// */
	// private String getRegistrationId(Context context) {
	// final SharedPreferences prefs = getGCMPreferences(context);
	// String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	// if (registrationId.isEmpty()) {
	// Log.i(TAG, "Registration not found.");
	// return "";
	// }
	// // Check if app was updated; if so, it must clear the registration ID
	// // since the existing regID is not guaranteed to work with the new
	// // app version.
	// int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
	// Integer.MIN_VALUE);
	// int currentVersion = getAppVersion(context);
	// if (registeredVersion != currentVersion) {
	// Log.i(TAG, "App version changed.");
	// return "";
	// }
	// return registrationId;
	// }
	//
	// /**
	// * @return Application's {@code SharedPreferences}.
	// */
	// private SharedPreferences getGCMPreferences(Context context) {
	// return getSharedPreferences(LoginActivity.class.getSimpleName(),
	// Context.MODE_PRIVATE);
	// }
	//
	// /**
	// * @return Application's version code from the {@code PackageManager}.
	// */
	// private static int getAppVersion(Context context) {
	// try {
	// PackageInfo packageInfo = context.getPackageManager()
	// .getPackageInfo(context.getPackageName(), 0);
	// return packageInfo.versionCode;
	// } catch (NameNotFoundException e) {
	// // should never happen
	// throw new RuntimeException("Could not get package name: " + e);
	// }
	// }
	//
	// private String getServerRegistrationStatus(Context context, String
	// userName) {
	// final SharedPreferences prefs = getGCMPreferences(context);
	// String status = prefs.getString(BACKEND_REG_STATUS + userName, "");
	// if (status.isEmpty() || status.equals("NO")) {
	// Log.i(TAG, userName + "is not registrated on backend server.");
	// return "NO";
	// }
	//
	// return "YES";
	// }
	//
	// /**
	// * Registers the application with GCM servers asynchronously.
	// * <p>
	// * Stores the registration ID and app versionCode in the application's
	// * shared preferences.
	// */
	// private void registerInBackground() {
	// new AsyncTask<Void, Void, String>() {
	// @Override
	// protected String doInBackground(Void... params) {
	// String msg = "";
	// try {
	// if (gcm == null) {
	// gcm = GoogleCloudMessaging.getInstance(context);
	// }
	// regid = gcm.register(GCM_SENDER_ID);
	// // msg = "Device registered, registration ID=" + regid;
	// msg = "Device registered to Google Cloud Messaging Server";
	//
	// // You should send the registration ID to your server over
	// // HTTP,
	// // so it can use GCM/HTTP or CCS to send messages to your
	// // app.
	// // The request to your server should be authenticated if
	// // your app
	// // is using accounts.
	// // sendRegistrationIdToBackend();
	//
	// // For this demo: we don't need to send it because the
	// // device
	// // will send upstream messages to a server that echo back
	// // the
	// // message using the 'from' address in the message.
	//
	// // Persist the regID - no need to register again.
	// storeRegistrationId(context, regid);
	// } catch (IOException ex) {
	// msg = "Error :" + ex.getMessage();
	// // If there is an error, don't just keep trying to register.
	// // Require the user to click a button again, or perform
	// // exponential back-off.
	// }
	// return msg;
	// }
	//
	// @Override
	// protected void onPostExecute(String msg) {
	// // mDisplay.append(msg + "\n");
	// Utils.showToastInternal(LoginActivity.this, msg);
	// // sendRegistrationIdToBackend();
	// }
	//
	// }.execute(null, null, null);
	// }
	//
	// // /**
	// // * Sends the registration ID to your server over HTTP, so it can use
	// // * GCM/HTTP or CCS to send messages to your app. Not needed for this
	// demo
	// // * since the device sends upstream messages to a server that echoes
	// back the
	// // * message using the 'from' address in the message.
	// // */
	// // private void sendRegistrationIdToBackend() {
	// // for (String userName : namesList) {
	// // sendRegistrationIdToBackend(userName, context);
	// // }
	// //
	// // }
	//
	// /**
	// * Async send registration Id of single user to push server
	// *
	// * @param userName
	// */
	// private void sendRegistrationIdToBackend(final String userName, final
	// Context context) {
	// if (regid != null) {
	// String status = getServerRegistrationStatus(context, userName);
	// if (!status.equals("YES")) {
	// new AsyncTask<String, Void, String>() {
	// @Override
	// protected String doInBackground(String... params) {
	// return asyncRegistrationIdToBackend(params[0]);
	// }
	//
	// @Override
	// protected void onPostExecute(String msg) {
	// // mDisplay.append(msg + "\n");
	// if (msg.equals("YES")) {
	// storeRegistrationServerId(context, userName, msg);
	// msg = "Device registered to RockToDo Server";
	// } else {
	// storeRegistrationServerId(context, userName, "NO");
	// }
	// Utils.showToastInternal(LoginActivity.this, msg);
	// }
	//
	// }.execute(userName);
	// }
	// }
	// }
	//
	// /**
	// * Send registration id of single user to push server
	// *
	// * @param userName
	// * @return registration message
	// */
	// private String asyncRegistrationIdToBackend(String userName) {
	// String msg = "";
	// try {
	// JSONObject jsonObj = new JSONObject();
	// jsonObj.put("user", userName);
	// jsonObj.put("type", "android");
	// jsonObj.put("token", regid);
	//
	// HttpClient httpClient = new DefaultHttpClient();
	// HttpPost httpPost = new HttpPost(TODOSERVER_URL_SUBSCRIBE);
	// StringEntity stringEntity = new StringEntity(jsonObj.toString());
	// stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
	// "application/json"));
	// httpPost.setHeader("Accept", "application/json");
	// httpPost.setHeader("Content-type", "application/json");
	// httpPost.setEntity(stringEntity);
	// httpClient.execute(httpPost);
	// // msg = "Device registered to todo-server, registration ID="
	// // + jsonObj.toString();
	// msg = "YES";
	// } catch (IOException | JSONException ex) {
	// msg = "Error :" + ex.getMessage();
	// // return false;
	// }
	// return msg;
	// }
	//
	// /**
	// * Stores the registration ID and app versionCode in the application's
	// * {@code SharedPreferences}.
	// *
	// * @param context
	// * application's context.
	// * @param regId
	// * registration ID
	// */
	// private void storeRegistrationId(Context context, String regId) {
	// final SharedPreferences prefs = getGCMPreferences(context);
	// int appVersion = getAppVersion(context);
	// Log.i(TAG, "Saving regId on app version " + appVersion);
	// SharedPreferences.Editor editor = prefs.edit();
	// editor.putString(PROPERTY_REG_ID, regId);
	// editor.putInt(PROPERTY_APP_VERSION, appVersion);
	// editor.commit();
	// }
	//
	// /**
	// * Store registration status of backend server
	// *
	// * @param context
	// * @param userName
	// * @param status
	// * , YES or NO
	// */
	// private void storeRegistrationServerId(Context context, String userName,
	// String status) {
	// final SharedPreferences prefs = getGCMPreferences(context);
	// SharedPreferences.Editor editor = prefs.edit();
	// editor.putString(BACKEND_REG_STATUS + userName, status);
	// editor.commit();
	// }

}
