package com.rockyniu.todolist;

import java.util.Calendar;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.Toast;

import com.rockyniu.todolist.database.ToDoItem;
import com.rockyniu.todolist.database.ToDoItemDataSource;


//@TargetApi(9)
public class EditItemActivity extends Activity {

	private final static int DUE_IS_EARLIER = 404;
	private final static int UPDATE_DONE = 0;
	private final int MAX_LENGTH = 140; // max length of name
	
	private ToDoItemDataSource itemdatasource;
	private String userId;
	private String itemId;
	private boolean completed;
	private Long completedTime;
	private EditText itemNameEditText;
//	private EditText itemNoteEditText;
	private CheckBox setDueTimeCheckBox;
//	private TextView dueTitle;
	private DatePicker dueDatePicker;
	private TimePicker dueTimePicker;
	private SeekBar priorityBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_item);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		userId = bundle.getString("_userid");
		itemId = bundle.getString("_itemid");

		itemdatasource = new ToDoItemDataSource(this);
		itemNameEditText = (EditText) findViewById(R.id.edit_name_edittext);
		priorityBar = (SeekBar) findViewById(R.id.edit_priority_seekbar);
		setDueTimeCheckBox = (CheckBox) findViewById(R.id.edit_due_checkbox);
		dueDatePicker = (DatePicker) findViewById(R.id.edit_due_datepicker);
		dueTimePicker = (TimePicker) findViewById(R.id.edit_due_timepicker);
		
		
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
			ToDoItem item = itemdatasource.getItemByItemId(itemId);
			if (item == null) {
				Utils.showErrorToast(this, "Task does not exits.");
//				Toast toast = Toast.makeText(this, "Task does not exist.",
//						Toast.LENGTH_LONG);
//				toast.show();
				finish();
				this.setResult(RESULT_CANCELED);
				return;
			}
			itemNameEditText.setText(item.getTitle());
			
			setDueTimeCheckBox.setChecked(item.getDueTime()==null?false:true);
			completed = item.isCompleted();
			completedTime = item.getCompletedTime();
			Calendar cal = Calendar.getInstance();
			if (item.getDueTime()==null) {
				dueDatePicker.setVisibility(View.GONE);
				dueTimePicker.setVisibility(View.GONE);
				cal.add(Calendar.DAY_OF_MONTH, 2);
			} else {
				dueDatePicker.setVisibility(View.VISIBLE);
				dueTimePicker.setVisibility(View.VISIBLE);
				cal.setTimeInMillis(item.getDueTime());
			}
			dueDatePicker.updateDate(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			dueTimePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			dueTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
			priorityBar.setProgress((int) item.getPriority());
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSaveClick(View view) {
		saveItem();
		this.setResult(RESULT_OK);
		this.finish();
	}

	public void onCancelClick(View view) {
		Utils.showToastInternal(this, "Cancel Editing.");
		this.setResult(RESULT_CANCELED);
		this.finish();
	}

	@Override
	public void onBackPressed() {
		saveItem();
		this.setResult(RESULT_OK);
		this.finish();
		super.onBackPressed();
	}
	
	private void saveItem(){
		String name = itemNameEditText.getText().toString().trim()
				.replaceAll("\\s+", " ");
		if (name.isEmpty()) {
			Utils.showItemNameIsEmptyDialog(EditItemActivity.this);
			return;
		}
		if (itemId.equals(getString(R.string.new_item))) {
			// Add item
			int updateResult = addItem(name);
			if (updateResult == UPDATE_DONE) {
				Utils.showToastInternal(this, "Task created.");
			} else if (updateResult == DUE_IS_EARLIER) {
				Utils.showDueTimeIsEarlierDialog(EditItemActivity.this);
				return;
			}
		} else {
			// Update item
			int updateResult = updateItem(name);
			if (updateResult == UPDATE_DONE) {
				Utils.showToastInternal(this, "Task updated.");
			} else if (updateResult == DUE_IS_EARLIER) {
				Utils.showDueTimeIsEarlierDialog(EditItemActivity.this);
				return;
			}
		}
	}
	
	private int addItem (String name){
		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MILLISECOND, 1);
//		cal.set(dueDatePicker.getYear(), dueDatePicker.getMonth(),
//				dueDatePicker.getDayOfMonth(), 23, 59, 59);
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
		newItem.setUserId(userId);
		if (name.length() > MAX_LENGTH){
			name = name.substring(0,MAX_LENGTH);
			Utils.showToastInternal(this, "Name is truncated to 140 characters.");
		}
		newItem.setTitle(name);
		newItem.setNotes("");
		
		if (setDueTimeCheckBox.isChecked()){
			newItem.setDueTime(cal.getTimeInMillis());
		}else{
			newItem.setDueTime(null);
		}
		
		newItem.setCompleted(false);
		newItem.setCompletedTime((long)0);
		newItem.setPriority(priorityBar.getProgress());
		newItem.setModifiedTime(Calendar.getInstance().getTimeInMillis());
		itemdatasource.insertItemWithId(newItem);
		return UPDATE_DONE;
	}
	
	private int updateItem(String name){

		Calendar cal = Calendar.getInstance();
//		cal.set(Calendar.MILLISECOND, 1);
//		cal.set(dueDatePicker.getYear(), dueDatePicker.getMonth(),
//				dueDatePicker.getDayOfMonth(), 23, 59, 59);
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
		if (name.length() > MAX_LENGTH){
			name = name.substring(0,MAX_LENGTH);
			Utils.showToastInternal(this, "Name is truncated to 140 characters.");
		}
		item.setTitle(name);		
		item.setNotes("");
		
		if (setDueTimeCheckBox.isChecked()){
			item.setDueTime(cal.getTimeInMillis());
		}else{
			item.setDueTime(null);
		}
		
		item.setId(itemId);
		item.setUserId(userId);
		item.setCompleted(completed);
		item.setCompletedTime(completedTime);
		item.setPriority(priorityBar.getProgress());
		item.setModifiedTime(Calendar.getInstance().getTimeInMillis());
		itemdatasource.updateItem(item);
		return UPDATE_DONE;
		
	}
}
