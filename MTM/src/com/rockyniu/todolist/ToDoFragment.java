package com.rockyniu.todolist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.rockyniu.todolist.database.DueComparator;
import com.rockyniu.todolist.database.ToDoItem;
import com.rockyniu.todolist.database.ToDoItemDataSource;
import com.rockyniu.todolist.database.ToDoItemDataSource.SortType;
import com.rockyniu.todolist.database.ToDoItemDataSource.ToDoFlag;
import com.rockyniu.todolist.database.ToDoItemDataSource.ToDoStatus;
import com.rockyniu.todolist.database.UserDataSource;

/**
 * A todo fragment.
 */
public class ToDoFragment extends Fragment {
	static final String TAG = "TodoFragment";
	static final int REQUEST_EDIT_ITEM = 101;
	static final int REQUEST_TOKEN = 1000;
	static final int REQUEST_GOOGLE_TASKS_SERVICES = 1001;
	static final int REQUEST_REMOTE_DATABASE = 1002;
	static final int UPDATE_LOCAL_DATABASE = 1003;
	static final int UPDATE_REMOTE_DATABASE = 1004;

	// for local database
	ToDoItemDataSource toDoItemDataSource;
	UserDataSource userDataSource;
	private SortType sortType = SortType.DUE;
	private ToDoStatus status = ToDoStatus.ALL;
	static List<ToDoItem> localToDoItems;

	// for auth2.0
	String userName;
	String userId;
	String token;

	// for remote server
	final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
	GoogleCredential credential;
	int numAsyncTasks;
	Tasks service;
	// final static String GOOGLE_TASKS_API_KEY =
	// "AIzaSyCvtrY8Rvv7KXrAWN0UjgszWe6AUPc8EVc";
	final static String TODOLIST_NAME = "cs6300ToDoList";
	List<Task> tasksList;

	private static ListView toDoListView;
	private CheckBox checkBox;

	// for check pastDue
	AlarmReceiver pastDueAlarmReceiver;

	/**
	 * Pass the data to {@link com.rockyniu.todolist.TabsActivity}
	 */
	OnDataPass dataPasser;

	/**
	 * The fragment arguments
	 */
	private static final String ARG_USER_ID = "_userid";
	private static final String ARG_USER_NAME = "_username";

	/**
	 * Returns a new instance of this fragment with userid and username.
	 */
	public static ToDoFragment newInstance(String userid, String username) {
		ToDoFragment fragment = new ToDoFragment();
		Bundle args = new Bundle();
		args.putString(ARG_USER_ID, userid);
		args.putString(ARG_USER_NAME, username);
		fragment.setArguments(args);
		return fragment;
	}

	public ToDoFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_tab_todo, container,
				false);

		setHasOptionsMenu(true); // must for additional menu of fragment

		Bundle bundle = getArguments();
		userId = bundle.getString(ARG_USER_ID);
		userName = bundle.getString(ARG_USER_NAME);
		// token = bundle.getString("_token");

		// credential = (new GoogleCredential()).setAccessToken(token);
		// credential.setAccessToken(token);

		// String[] nameSplit = userName.split("@");
		// this.setTitle("ToDos for " + nameSplit[0]);
		this.getActivity().setTitle(userName);
		sortType = SortType.DUE;
		toDoItemDataSource = new ToDoItemDataSource(this.getActivity());
		// userDataSource = new UserDataSource(this);

		// set item adapter
		localToDoItems = getNewListFromLocal(ToDoFlag.UNDELETED, status);
		toDoListView = (ListView) rootView.findViewById(R.id.toDoListView);
		toDoListView.setEmptyView(rootView.findViewById(R.id.empty_list_item));
		ToDoListAdapter adapter = new ToDoListAdapter(this.getActivity(),
				localToDoItems);
		toDoListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		// set due time alarm Time
		setAlarmTime(this.getActivity());
		
		checkBox = (CheckBox) rootView.findViewById(R.id.checkBoxHide);
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView.isChecked()) {
					status = ToDoStatus.ACTIVE;
				} else {
					status = ToDoStatus.ALL;
				}
//				localToDoItems = getNewListFromLocal(ToDoFlag.UNDELETED, status);
				refreshView();
			}
		});

		// click action
		toDoListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// / mark the task either completed or uncompleted
				ToDoListAdapter tasksAdapter = (ToDoListAdapter) toDoListView
						.getAdapter();
				ToDoItem currentItem = (ToDoItem) tasksAdapter
						.getItem(position);
				long currentTime = Calendar.getInstance().getTimeInMillis();
				if (currentItem.isCompleted()) {
					currentItem.setCompleted(false);
					currentItem.setCompletedTime(null);
				} else {
					currentItem.setCompleted(true);
					currentItem.setCompletedTime(currentTime);
				}
				currentItem.setModifiedTime(currentTime);
				toDoItemDataSource.updateItem(currentItem);
//				localToDoItems.set(position, currentItem); // update localToDoItems
				// sync();
				refreshView();
			}
		});

		// long cilck action
		toDoListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ToDoListAdapter tasksAdapter = (ToDoListAdapter) toDoListView
						.getAdapter();
				ToDoItem currentItem = (ToDoItem) tasksAdapter
						.getItem(position);
				String itemId = currentItem.getId();
				editItem(itemId);
				return true;
			}
		});

		// swipe to delete tasks
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				toDoListView,
				new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {

						// Delete all dismissed tasks
						for (int position : reverseSortedPositions) {
							ToDoListAdapter tasksAdapter = (ToDoListAdapter) toDoListView
									.getAdapter();
							ToDoItem currentItem = (ToDoItem) tasksAdapter
									.getItem(position);
							// label delete
							currentItem.setModifiedTime(Calendar.getInstance()
									.getTimeInMillis());
							toDoItemDataSource
									.labelItemDeletedWithModifiedTime(currentItem);
//							localToDoItems.remove(position); // remove item from localToDoItems
						}
						Utils.showToastInternal(
								ToDoFragment.this.getActivity(),
								"Task deleted.");
						// refreshView();
						// sync();
						refreshView();
					}
				});
		toDoListView.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		toDoListView.setOnScrollListener(touchListener.makeScrollListener());
		sync();
		refreshView();
		return rootView;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_EDIT_ITEM
				&& resultCode == Activity.RESULT_OK) {
			// sync();
			refreshView();
		}
//		refreshView();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.activity_to_do_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_addItem:
			editItem(getString(R.string.new_item));
			return true;
		case R.id.menu_duedate:
			sortType = SortType.DUE;
			refreshView();
			return true;
		case R.id.menu_priority:
			sortType = SortType.PRIORITY;
			refreshView();
			return true;
		case R.id.menu_clear_completed:
			new AlertDialog.Builder(this.getActivity())
					.setTitle("Clear All")
					.setMessage("Do you want to clear all completed tasks?")
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// continue with delete
									clearCompleted();
									// refreshView();
									// sync();
									refreshView();
								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// do nothing
								}
							}).setIcon(android.R.drawable.ic_dialog_alert)
					.show();
			return true;
		case R.id.menu_sync:
			sync();
//			refreshView();
			return true;
		case android.R.id.home:

			NavUtils.navigateUpFromSameTask(this.getActivity());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		passData();
		super.onStart();
		// sync();
	}

	@Override
	public void onResume() {
//		refreshView();
		super.onResume();
		doListening();
		setAlarmTime(getActivity());
		// sync();
	}

	@Override
	public void onStop() {
		doStopListening();
		super.onStop();
	}

	// add or edit Item
	private void editItem(String itemId) {
		Intent myIntent = new Intent(this.getActivity(), EditItemActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("_userid", userId);
		bundle.putString("_itemid", itemId);
		myIntent.putExtras(bundle);
		startActivityForResult(myIntent, REQUEST_EDIT_ITEM);
	}

	// label items completed as deleted
	public void clearCompleted() {
		List<ToDoItem> completedItems = getNewListFromLocal(ToDoFlag.All,
				ToDoStatus.COMPLETED);
		for (int i = 0; i < completedItems.size(); i++) {
			ToDoItem item = completedItems.get(i);
			if (item.isCompleted()) {
				// label delete
				item.setModifiedTime(Calendar.getInstance().getTimeInMillis());
				toDoItemDataSource.labelItemDeletedWithModifiedTime(item);
			}
		}
	}

	// clear items labeled deleted
	public void clearDeletedItems() {
		List<ToDoItem> deletedItems = getNewListFromLocal(ToDoFlag.DELETED,
				ToDoStatus.ALL);
		for (int i = 0; i < deletedItems.size(); i++) {
			ToDoItem item = deletedItems.get(i);
			if (item.isDeleted()) {
				toDoItemDataSource.deleteItem(item);
			}
		}
	}

	private void sync() {
		ToDoListLoadAsynTask toDoListLoadAsynTask = new ToDoListLoadAsynTask(
				this);
		toDoListLoadAsynTask.execute(UPDATE_REMOTE_DATABASE);
	}

	public void refreshView() {
		refresh();
		// setAlarmTime(this.getActivity());
	}

	private void refresh() {
		ToDoListAdapter adapter = new ToDoListAdapter(this.getActivity(),
				localToDoItems);
		toDoListView.setAdapter(adapter);
		localToDoItems = getNewListFromLocal(ToDoFlag.UNDELETED,
				checkBox.isChecked() ? ToDoStatus.ACTIVE : ToDoStatus.ALL);
		adapter.updateList(localToDoItems);
		adapter.notifyDataSetChanged();
	}

	public List<ToDoItem> getNewListFromLocal(ToDoFlag deleted,
			ToDoStatus status) {
		return toDoItemDataSource.getNewListFromLocal(userId, status, sortType,
				deleted);
	}

	/**
	 * get the first being pastDue ToDoItem, otherwise return null
	 */
	private ToDoItem getFirstBeingPastDueItem() {
		List<ToDoItem> tempList = localToDoItems;
		Collections.sort(tempList, new DueComparator());
		for (int i = 0; i < tempList.size(); i++) {
			ToDoItem item = tempList.get(i);
			if (!item.isPastDue() && item.getDueTime() != null
					&& !item.isCompleted()) {
				return tempList.get(i);
			}
		}
		return null;
	}

	// set alarm for item
	private void setAlarmTime(Context context) {
//		refreshView();
		ToDoItem alarmedItem = getFirstBeingPastDueItem();
		long timeInMillis;
		String pastDueItemTitle;
		String pastDueItemNotes;
		if (alarmedItem == null) {
			timeInMillis = Long.MAX_VALUE;
			pastDueItemTitle = "The End Of World!";
			pastDueItemNotes = "The End Of World!";
		} else {
			timeInMillis = alarmedItem.getDueTime();
			pastDueItemTitle = alarmedItem.getTitle();
			Calendar dueTime = Calendar.getInstance();
			dueTime.setTimeInMillis(alarmedItem.getDueTime());
			SimpleDateFormat format = new SimpleDateFormat(
					"MM/dd/yyyy HH:mm a", Locale.getDefault());
			pastDueItemNotes = "@ " + format.format(dueTime.getTime());
		}

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent();
		intent.setAction("_pastduealarm");
		Bundle bundle = new Bundle();
		bundle.putString("_pastDueItemTitle", pastDueItemTitle);
		bundle.putString("_pastDueItemNotes", pastDueItemNotes);
		intent.putExtras(bundle);

		// trigger of sending intent to AlarmReceiver
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, timeInMillis, sender);
	}

	// past due alarm receiver
	private class AlarmReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, intent.getAction());
			if ("_pastduealarm".equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();

				String pastDueItemTitle = bundle.getString("_pastDueItemTitle");
				String pastDueItemNotes = bundle.getString("_pastDueItemNotes");
				String message = pastDueItemTitle + "\n\n" + pastDueItemNotes;

				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.alarm_dialog);
				dialog.setTitle("Task Past Due!");

				// set the custom dialog components - text, image and button
				TextView text = (TextView) dialog
						.findViewById(R.id.alarm_dialog_text);
				text.setText(message);
				ImageView image = (ImageView) dialog
						.findViewById(R.id.alarm_dialog_icon);
				image.setImageResource(R.drawable.warning);

				Button dialogButton = (Button) dialog
						.findViewById(R.id.alarm_dialog_button);
				// if button is clicked, close the custom dialog

				dialogButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						refreshView();
					}
				});
				dialog.show();
			}
		}
	}

	/**
	 * start alarmlistenning
	 */
	void doListening(){
		IntentFilter filter = new IntentFilter();
		filter.addAction("_pastduealarm");
		pastDueAlarmReceiver = new AlarmReceiver();
		this.getActivity().registerReceiver(pastDueAlarmReceiver, filter);
	}
	
	/**
	 * stop alarmlistening
	 */
	void doStopListening() {
		if (pastDueAlarmReceiver != null) {
			getActivity().unregisterReceiver(pastDueAlarmReceiver);
			pastDueAlarmReceiver = null;
		}
	}

	/**
	 * for passing the data back to activity
	 * 
	 * @author Lei Niu
	 * 
	 */
	public interface OnDataPass {
		public void onDataPass(ToDoItem toDoItem);
	}

	/**
	 * attach the fragment to activity
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			dataPasser = (OnDataPass) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnDataPass");
		}
	}

	/**
	 * pass the data
	 * 
	 * @param data
	 */
	public void passData() {
		ToDoItem toDoItem = getFirstBeingPastDueItem();
		dataPasser.onDataPass(toDoItem);
	}
}