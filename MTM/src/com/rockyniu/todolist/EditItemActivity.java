package com.rockyniu.todolist;

import java.util.Calendar;
import java.util.UUID;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.rockyniu.todolist.database.ToDoItemDataSource;
import com.rockyniu.todolist.database.UserDataSource;
import com.rockyniu.todolist.database.model.ToDoItem;
import com.rockyniu.todolist.database.model.User;
import com.rockyniu.todolist.register.Register;
import com.rockyniu.todolist.user.UserInformation;
import com.rockyniu.todolist.util.Constance;
import com.rockyniu.todolist.util.DialogHelper;
import com.rockyniu.todolist.util.ToastHelper;

//@TargetApi(9)
public class EditItemActivity extends BaseActivity {

	private final static int DUE_IS_EARLIER = 1004;
	private final static int UPDATE_DONE = -1001;
	private final int MAX_LENGTH = 500; // max length of name

	private UserInformation userInformation;
	private ToDoItemDataSource itemdatasource;
	private ToDoItem toDoItem;
	private String userId;
	private String itemId;
	private boolean completed;
	private Long completedTime;
	private EditText itemNameEditText;
	private CheckBox setDueTimeCheckBox;
	private DatePicker dueDatePicker;
	private TimePicker dueTimePicker;
	private SeekBar priorityBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_item);

		itemdatasource = new ToDoItemDataSource(this);
		itemNameEditText = (EditText) findViewById(R.id.edit_name_edittext);
		priorityBar = (SeekBar) findViewById(R.id.edit_priority_seekbar);
		setDueTimeCheckBox = (CheckBox) findViewById(R.id.edit_due_checkbox);
		dueDatePicker = (DatePicker) findViewById(R.id.edit_due_datepicker);
		dueTimePicker = (TimePicker) findViewById(R.id.edit_due_timepicker);

		userInformation = new UserInformation(EditItemActivity.this);

		SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",
				Context.MODE_PRIVATE);
		userId = sharedPreferences.getString("UserId", "");
		itemId = getString(R.string.new_item);

		Intent intent = getIntent();
		String type = intent.getType();

		Bundle bundle = intent.getExtras();
		if (type != null && type.equals("text/plain")
				&& intent.getClipData() != null) {
			ClipData data = intent.getClipData();
			itemNameEditText.setText(data.getItemAt(0).getText());
		} else if (bundle != null) {
			if (bundle.keySet().contains(Intent.EXTRA_TEXT)) { // for Baidu
																// browser
				String content = bundle.getString(Intent.EXTRA_TEXT);
				itemNameEditText.setText(content);
			} else {
				userId = bundle.getString("_userid");
				itemId = bundle.getString("_itemid");
			}
		}

		toDoItem = new ToDoItem();

		if (itemId.equals(getString(R.string.new_item))) {
			this.setTitle("Add New Item");

			// date and time
			setDueTimeCheckBox.setChecked(false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 2);
			// date
			dueDatePicker.updateDate(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			dueDatePicker.setVisibility(View.GONE);
			// time
			dueTimePicker.setCurrentHour(0);
			dueTimePicker.setCurrentMinute(0);
			dueTimePicker.setVisibility(View.GONE);

			priorityBar.setProgress(1);

		} else {
			this.setTitle("Edit Item");
			toDoItem = itemdatasource.getItemByItemId(itemId);
			if (toDoItem == null) {
				ToastHelper.showErrorToast(EditItemActivity.this,
						"Task does not exits.");
				finish();
				this.setResult(RESULT_CANCELED);
				return;
			}
			itemNameEditText.setText(toDoItem.getTitle());

			setDueTimeCheckBox.setChecked(toDoItem.getDueTime() == null ? false
					: true);
			completed = toDoItem.isCompleted();
			completedTime = toDoItem.getCompletedTime();
			Calendar cal = Calendar.getInstance();
			if (toDoItem.getDueTime() == null) {
				dueDatePicker.setVisibility(View.GONE);
				dueTimePicker.setVisibility(View.GONE);
				cal.add(Calendar.DAY_OF_MONTH, 2);
			} else {
				dueDatePicker.setVisibility(View.VISIBLE);
				dueTimePicker.setVisibility(View.VISIBLE);
				cal.setTimeInMillis(toDoItem.getDueTime());
			}
			dueDatePicker.updateDate(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			dueTimePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			dueTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
			priorityBar.setProgress((int) toDoItem.getPriority());
		}
		setDueTimeCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (buttonView.isChecked()) {
							dueDatePicker.setVisibility(View.VISIBLE);
							dueTimePicker.setVisibility(View.VISIBLE);
						} else {
							dueDatePicker.setVisibility(View.GONE);
							dueTimePicker.setVisibility(View.GONE);
						}
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_edit_item, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.menu_sendSmsMessage:
			if (saveItem()) {
				String smsMessage = toDoItem.toSmsMessage();
				Intent smsIntent = new Intent(Intent.ACTION_VIEW);
				smsIntent.putExtra("sms_body", smsMessage);
				smsIntent.setType("vnd.android-dir/mms-sms");
				startActivity(smsIntent);
			} else {
				ToastHelper.showToastInternal(this,
						"Error happened when saving task.");
			}
			return true;
		case R.id.menu_sendEmail:
			if (saveItem()) {
				String emailContent = toDoItem.toSmsMessage();
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				String[] recipients = new String[] { "", "", };
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						recipients);
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"ToDo");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						emailContent);
				emailIntent.setType("text/plain");
				startActivity(Intent.createChooser(emailIntent, "Share"));
			} else {
				ToastHelper.showToastInternal(this,
						"Error happened when saving task.");
			}
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constance.REQUEST_USER_PICKER
				&& resultCode == Activity.RESULT_OK) {
			String userName = data
					.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			User user = (new UserDataSource(this)).selectUser(userName);
			userId = user.getId();
			userInformation.saveUserInfo(userId, userName);
			Register register = new Register(EditItemActivity.this);
			register.sendRegistrationIdToBackend(userName);
			finish();
			startActivity(getIntent());
		} else {
			// TODO
		}

	}

	public void onSaveClick(View view) {
		if (saveItem()) {
			this.setResult(RESULT_OK);
			this.finish();
		}
	}

	public void onCancelClick(View view) {
		ToastHelper.showToastInternal(this, "Cancel Editing.");
		this.setResult(RESULT_CANCELED);
		this.finish();
	}

	@Override
	public void onBackPressed() {
		ToastHelper.showToastInternal(this, "Cancel Editing.");
		this.setResult(RESULT_CANCELED);
		this.finish();
		super.onBackPressed();
	}

	private boolean saveItem() {
		String name = itemNameEditText.getText().toString().trim()
				.replaceAll("\\s+", " ");
		if (name.isEmpty()) {
			DialogHelper.showItemNameIsEmptyDialog(EditItemActivity.this);
			return false;
		}
		if (itemId.equals(getString(R.string.new_item))) {
			// Add item
			int updateResult = addItem(name);
			if (updateResult == UPDATE_DONE) {
				ToastHelper.showToastInternal(this, "Task created.");
			} else if (updateResult == DUE_IS_EARLIER) {
				DialogHelper.showDueTimeIsEarlierDialog(EditItemActivity.this);
				return false;
			}
		} else {
			// Update item
			int updateResult = updateItem(name);
			if (updateResult == UPDATE_DONE) {
				ToastHelper.showToastInternal(this, "Task updated.");
			} else if (updateResult == DUE_IS_EARLIER) {
				DialogHelper.showDueTimeIsEarlierDialog(EditItemActivity.this);
				return false;
			}
		}
		return true;
	}

	private int addItem(String name) {
		Calendar cal = Calendar.getInstance();
		// cal.set(Calendar.MILLISECOND, 1);
		// cal.set(dueDatePicker.getYear(), dueDatePicker.getMonth(),
		// dueDatePicker.getDayOfMonth(), 23, 59, 59);
		cal.set(dueDatePicker.getYear(), dueDatePicker.getMonth(),
				dueDatePicker.getDayOfMonth());
		if (setDueTimeCheckBox.isChecked()) {
			Calendar nowcal = Calendar.getInstance();
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, dueTimePicker.getCurrentMinute());
			cal.set(Calendar.HOUR_OF_DAY, dueTimePicker.getCurrentHour());
			if (nowcal.after(cal)) {
				return DUE_IS_EARLIER;
			}
		}

		// create item and save into database
		ToDoItem newItem = new ToDoItem();
		newItem.setId(UUID.randomUUID().toString());
		if (userId == "") {
			userInformation.onUserPicker();
		}
		newItem.setUserId(userId);
		if (name.length() > MAX_LENGTH) {
			name = name.substring(0, MAX_LENGTH);
			ToastHelper.showToastInternal(this, "Name is truncated to "
					+ MAX_LENGTH + " 140 characters.");
		}
		newItem.setTitle(name);
		newItem.setNotes("");

		if (setDueTimeCheckBox.isChecked()) {
			newItem.setDueTime(cal.getTimeInMillis());
		} else {
			newItem.setDueTime(null);
		}

		newItem.setCompleted(false);
		newItem.setCompletedTime((long) 0);
		newItem.setPriority(priorityBar.getProgress());
		newItem.setModifiedTime(Calendar.getInstance().getTimeInMillis());
		itemdatasource.insertItemWithId(newItem);
		toDoItem = newItem;
		return UPDATE_DONE;
	}

	private int updateItem(String name) {

		Calendar cal = Calendar.getInstance();
		// cal.set(Calendar.MILLISECOND, 1);
		// cal.set(dueDatePicker.getYear(), dueDatePicker.getMonth(),
		// dueDatePicker.getDayOfMonth(), 23, 59, 59);
		cal.set(dueDatePicker.getYear(), dueDatePicker.getMonth(),
				dueDatePicker.getDayOfMonth());
		if (setDueTimeCheckBox.isChecked()) {
			Calendar nowcal = Calendar.getInstance();
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, dueTimePicker.getCurrentMinute());
			cal.set(Calendar.HOUR_OF_DAY, dueTimePicker.getCurrentHour());
			if (nowcal.after(cal)) {
				return DUE_IS_EARLIER;
			}
		}

		// update item and database
		ToDoItem item = new ToDoItem();
		if (name.length() > MAX_LENGTH) {
			name = name.substring(0, MAX_LENGTH);
			ToastHelper.showToastInternal(this, "Name is truncated to "
					+ MAX_LENGTH + " 140 characters.");
		}
		item.setTitle(name);
		item.setNotes("");

		if (setDueTimeCheckBox.isChecked()) {
			item.setDueTime(cal.getTimeInMillis());
		} else {
			item.setDueTime(null);
		}

		item.setId(itemId);
		if (userId == "") {
			userInformation.onUserPicker();
		}
		item.setUserId(userId);
		item.setCompleted(completed);
		item.setCompletedTime(completedTime);
		item.setPriority(priorityBar.getProgress());
		item.setModifiedTime(Calendar.getInstance().getTimeInMillis());
		itemdatasource.updateItem(item);
		toDoItem = item;
		return UPDATE_DONE;

	}
}
