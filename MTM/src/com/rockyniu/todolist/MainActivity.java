package com.rockyniu.todolist;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.rockyniu.todolist.database.User;
import com.rockyniu.todolist.database.UserDataSource;

public class MainActivity extends Activity implements View.OnClickListener {

	static final String TAG = "MainActiviy";
	static final int REQUEST_ACCOUNT_PICKER = 114;
	static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 911;
	UserDataSource userDataSource;
	AccountManager mAccountManager;
	// String token;
	// int numAsyncTasks;

	// Users user;
	List<String> namesList;
	// String userName;
	// String userId;
	ListView mUserNamesList;
	SignInButton mAddUserButton;
	ProgressBar progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		userDataSource = new UserDataSource(this);

		mAddUserButton = (SignInButton) findViewById(R.id.add_account_button);
		// mAddUserButton.setSize(SignInButton.SIZE_WIDE);
		mAddUserButton.setOnClickListener(MainActivity.this);
		mAddUserButton.setContentDescription("Add User");

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

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK) {
//			String userName = data
//					.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//			goToToDoList(userName);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView();
		checkGooglePlaySerViceAvailable(MainActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void checkGooglePlaySerViceAvailable(Activity activity) {
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(MainActivity.this);
		if (status != ConnectionResult.SUCCESS) {
			Log.v(TAG, "GoolgePlayService is not available.");
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				GooglePlayServicesUtil.getErrorDialog(status, this,
						REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
			} else {
				Toast.makeText(this, "This device is not supported.",
						Toast.LENGTH_LONG).show();
				finish();
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
		Intent intent = new Intent(MainActivity.this, ToDoListActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("com.example.cs6300todolist.userid", userId);
		bundle.putString("com.example.cs6300todolist.username", userName);
		// bundle.putString("com.example.cs6300todolist.token", token);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void deleteRedundantUsers(){
		List<User> usersList = userDataSource.getAllUsers();
		for (User user : usersList) {
			if (!namesList.contains(user.getName())){
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

	@Override
	public void onClick(View view) {
		if (view == findViewById(R.id.add_account_button)) {

			Intent intent = AccountPicker.newChooseAccountIntent(null, null,
					new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, false,
					null, null, null, null);
			try {
				startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
			}  catch (Exception e) {
				checkGooglePlaySerViceAvailable(MainActivity.this);
			}
		}
	}

}
