package com.rockyniu.todolist.register;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rockyniu.todolist.LoginActivity;
import com.rockyniu.todolist.util.Constance;
import com.rockyniu.todolist.util.Utils;

public class Register {

	// for GCM
//	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String BACKEND_REG_STATUS = "registration_on_backend_";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	GoogleCloudMessaging gcm;
//	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	String regid;
	public static final String TAG = "register";

	public Register(Context context){
		this.context = context;
	}
	
	public void sendRegistrationIdToBackend(final String userName){
		sendRegistrationIdToBackend(userName, context);
	}
	
	public void sendRegistrationIdToGcm(){
		regid = getRegistrationId(context);

		if (regid == null || regid.isEmpty()) {
			registerInBackground();
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
					regid = gcm.register(Constance.GCM_SENDER_ID);
					// msg = "Device registered, registration ID=" + regid;
					msg = "Device registered to Google Cloud Messaging Server";
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
				// mDisplay.append(msg + "\n");
				Utils.showToastInternal((Activity)context, msg);
//				sendRegistrationIdToBackend();
			}

		}.execute(null, null, null);
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
		return context.getSharedPreferences(LoginActivity.class.getSimpleName(),
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

	private String getServerRegistrationStatus(Context context, String userName) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String status = prefs.getString(BACKEND_REG_STATUS + userName, "");
		if (status.isEmpty() || status.equals("NO")) {
			Log.i(TAG, userName + "is not registrated on backend server.");
			return "NO";
		}

		return "YES";
	}

	

//	/**
//	 * Sends the registration ID to your server over HTTP, so it can use
//	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
//	 * since the device sends upstream messages to a server that echoes back the
//	 * message using the 'from' address in the message.
//	 */
//	private void sendRegistrationIdToBackend() {
//		for (String userName : namesList) {
//			sendRegistrationIdToBackend(userName, context);
//		}
//
//	}

	/**
	 * Async send registration Id of single user to push server
	 * 
	 * @param userName
	 */
	private void sendRegistrationIdToBackend(final String userName, final Context context) {
		if (regid != null) {
			String status = getServerRegistrationStatus(context, userName);
			if (!status.equals("YES")) {
				new AsyncTask<String, Void, String>() {
					@Override
					protected String doInBackground(String... params) {
						return asyncRegistrationIdToBackend(params[0]);
					}

					@Override
					protected void onPostExecute(String msg) {
						// mDisplay.append(msg + "\n");
						if (msg.equals("YES")) {
							storeRegistrationServerId(context, userName, msg);
							msg = "Device registered to RockToDo Server";
						} else {
							storeRegistrationServerId(context, userName, "NO");
						}
						Utils.showToastInternal((Activity)context, msg);
					}

				}.execute(userName);
			}
		}
	}

	/**
	 * Send registration id of single user to push server
	 * 
	 * @param userName
	 * @return registration message
	 */
	private String asyncRegistrationIdToBackend(String userName) {
		String msg = "";
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("user", userName);
			jsonObj.put("type", "android");
			jsonObj.put("token", regid);

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Constance.TODOSERVER_URL_SUBSCRIBE);
			StringEntity stringEntity = new StringEntity(jsonObj.toString());
			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(stringEntity);
			httpClient.execute(httpPost);
			// msg = "Device registered to todo-server, registration ID="
			// + jsonObj.toString();
			msg = "YES";
		} catch (IOException | JSONException ex) {
			msg = "Error :" + ex.getMessage();
			// return false;
		}
		return msg;
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

	/**
	 * Store registration status of backend server
	 * 
	 * @param context
	 * @param userName
	 * @param status
	 *            , YES or NO
	 */
	private void storeRegistrationServerId(Context context, String userName,
			String status) {
		final SharedPreferences prefs = getGCMPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(BACKEND_REG_STATUS + userName, status);
		editor.commit();
	}

}
