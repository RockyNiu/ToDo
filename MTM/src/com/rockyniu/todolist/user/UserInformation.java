package com.rockyniu.todolist.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.rockyniu.todolist.R;
import com.rockyniu.todolist.util.Constance;
import com.rockyniu.todolist.util.ToastHelper;

public class UserInformation {

	Context context;
	SharedPreferences settings;
	
	public UserInformation(Context context){
		this.context = context;
		settings = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
	}
	
	public void onUserPicker(){
		onUserPicker((Activity) context);
	}
	
	/**
	 * pick a user account with the request id {@link com.rockyniu.todolist.util.Constance.REQUEST_USER_PICKER}
	 */
	private void onUserPicker(Activity activity) {
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, true,
				null, null, null, null);
		try {
			activity.startActivityForResult(intent, Constance.REQUEST_USER_PICKER);
		} catch (Exception e) {
			ToastHelper.showErrorToast(activity, activity.getResources().getString(R.string.fail_to_add_new_user));
		}
	}

	/**
	 * Save user info into {@link android.content.SharedPreferences}
	 * @param userId
	 * @param userName
	 */
	public void saveUserInfo(String userId, String userName){
		// save user settings
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("UserId", userId);
		editor.putString("UserName", userName);
		editor.commit();
	}
	
	public String getUserId(){
		return settings.getString("UserId", null);
	}
	
	public String getUserName(){
		return settings.getString("UserName", null);
	}
}
