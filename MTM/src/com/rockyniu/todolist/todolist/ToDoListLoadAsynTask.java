package com.rockyniu.todolist.todolist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.Tasks.Tasklists;
import com.google.api.services.tasks.TasksRequest;
import com.google.api.services.tasks.TasksRequestInitializer;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.rockyniu.todolist.database.ToDoItemDataSource;
import com.rockyniu.todolist.database.UserDataSource;
import com.rockyniu.todolist.database.ToDoItemDataSource.ToDoFlag;
import com.rockyniu.todolist.database.ToDoItemDataSource.ToDoStatus;
import com.rockyniu.todolist.database.model.SyncResult;
import com.rockyniu.todolist.database.model.ToDoItem;
import com.rockyniu.todolist.database.model.User;
import com.rockyniu.todolist.util.Utils;

class ToDoListLoadAsynTask extends ToDoListCommonAsynTask {

	ToDoListLoadAsynTask(ToDoFragment toDoFragment) {
		super(toDoFragment);
	}

	private final String TASK_LIST_ID = "task_list_id_";
	Context context = toDoFragment.getActivity().getApplicationContext();

	@SuppressWarnings("unchecked")
	@Override
	protected boolean doInBackground(Integer requestCode) throws Exception {

		// get token
		// if (activity.token==null || activity.token.isEmpty()){
		getToken();

		// }
		if (toDoFragment.token == null && toDoFragment.token == null) {
			return false;
		} else if (requestCode == ToDoFragment.REQUEST_TOKEN) {
			return true;
		}

		// get Google Tasks service
		getGoogleTasksService();
		if (service == null) {
			return false;
		} else if (requestCode == ToDoFragment.REQUEST_GOOGLE_TASKS_SERVICES) {
			return true;
		}

		// get tasks list from Google Tasks service
		Object[] resultsGetTasksFromRemote = getTasksFromRemote(ToDoFragment.TODOLIST_NAME);
		taskListId = (String) resultsGetTasksFromRemote[0];
		List<Task> tasks = (List<Task>) resultsGetTasksFromRemote[1];
		if (requestCode == ToDoFragment.REQUEST_REMOTE_DATABASE) {
			return true;
		}

		// synchronize local database
		Object[] sync = syncLocalDatabase(tasks);
		if (requestCode == ToDoFragment.UPDATE_LOCAL_DATABASE) {
			return true;
		}
		// synchronize remote database
		tasks = syncRemoteDatabase(sync);
		if (tasks == null) {
			return false;
		} else if (requestCode == ToDoFragment.UPDATE_REMOTE_DATABASE) {
			// managerDataSource.updateLastSynTime(userId,
			// lastSynDateTime.getValue());
			toDoFragment.tasksList = tasks;
			return true;
		}
		// activity.refreshView();
		return true;
	}

	// Get token
	protected void getToken() throws OperationCanceledException,
			AuthenticatorException, IOException {
		// activity.token = null;
		Account account = new Account(toDoFragment.userName,
				GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		AccountManagerFuture<Bundle> accountManagerFuture = AccountManager.get(
				toDoFragment.getActivity()).getAuthToken(account,
				"oauth2:" + TasksScopes.TASKS, null,
				toDoFragment.getActivity(), null, null);
		toDoFragment.token = accountManagerFuture.getResult().getString(
				AccountManager.KEY_AUTHTOKEN);
		// Users user = activity.userDataSource.selectUser(activity.userName);
		// user.setPwd(activity.token);
		if (toDoFragment.token != null && !toDoFragment.token.isEmpty()) {
			// activity.userDataSource.updateUsers(user);
			toDoFragment.credential = (new GoogleCredential())
					.setAccessToken(toDoFragment.token);
		}
	}

	// Setting up the Tasks API Service
	protected void getGoogleTasksService() {
		try {
			service = new Tasks.Builder(toDoFragment.httpTransport,
					toDoFragment.jsonFactory, toDoFragment.credential)
					.setTasksRequestInitializer(new TasksRequestInitializer()
					/*
					 * {
					 * 
					 * @Override public void initializeTasksRequest(
					 * TasksRequest<?> request) throws IOException {
					 * 
					 * @SuppressWarnings("rawtypes") TasksRequest tasksRequest =
					 * (TasksRequest) request; //
					 * tasksRequest.set("showDeleted", true); //
					 * tasksRequest.setKey
					 * (ToDoListActivity.GOOGLE_TASKS_API_KEY); } }
					 */
					).setApplicationName("RockyNiu-ToDo").build();
		} catch (Exception e) {
			service = null;
		}
	}

	// get Task list from remote database
	protected Object[] getTasksFromRemote(String listName) throws Exception,
			IOException {
		Object[] results = new Object[2];
		String taskListId = null;
		List<Task> tasks = null;

		// get the task list id from local db first
		taskListId = getTaskListId(context, userName);

		// if task list id dosen't exist
		if (taskListId == null) {
			taskListId = getTaskListIdFromServer(listName);
			if (taskListId == null){
				taskListId = insertTaskList(); // insert new task list to server
			}
			storeListId(context, userName, taskListId); // store list id in
														// local db
		}

		if (taskListId != null) {
			try {
				tasks = getTasksFromServer(taskListId);
			} catch (Exception e) {
				// if taskListId in local is different with taskListId in server

				// get task list id from server
				taskListId = getTaskListIdFromServer(listName);
				if (taskListId == null){
					taskListId = insertTaskList(); // insert new task list to server
				}
				
				// store task list in local database
				storeListId(context, userName, taskListId);
				tasks = getTasksFromServer(taskListId);
			}

			// successfully get the tasks list
			if (taskListId != null && tasks != null) {
				results[0] = taskListId;
				results[1] = tasks;
			} else{
				results = null;
			}
			return results;
		} else {
			return null;
		}
	}

	/**
	 * get tasks from server
	 * 
	 * @param taskListId
	 * @return
	 * @throws IOException
	 */
	private List<Task> getTasksFromServer(String taskListId) throws Exception {
		return service.tasks()
				.list(taskListId)
				// .setFields("items")
				.setFields(
						"items/id, items/title, items/notes, items/due"
								+ ", items/status, items/updated, items/deleted, items/completed")
				// .setFields("items/id, title, notes, due, status, updated, deleted")
				.execute().getItems();
	}

	/**
	 * insert new task list into user's Google Tasks
	 * 
	 * @return
	 * @throws IOException
	 */
	private String insertTaskList() throws IOException {
		TaskList taskList = new TaskList().setTitle(ToDoFragment.TODOLIST_NAME);
		taskList = service.tasklists().insert(taskList).execute();
		return taskList.getId();
	}

	/**
	 * get task list id from server
	 * 
	 * @param listName
	 * @throws IOException
	 */
	private String getTaskListIdFromServer(String listName) throws IOException {
		List<TaskList> taskLists = service.tasklists().list().execute()
				.getItems();
		String taskListId = null;
		for (TaskList taskList : taskLists) {
			if (taskList.getTitle().equals(listName)) {
				taskListId = taskList.getId();
			}
		}
		return taskListId;
	}

	/**
	 * get task list id from local database
	 * 
	 * @param context
	 * @param userName
	 * @return taskListId, a String
	 */
	private String getTaskListId(Context context, String userName) {
		final SharedPreferences prefs = getToDoPreferences(context);
		String taskListId = prefs.getString(TASK_LIST_ID + userName, null);
		if (taskListId == null) {
			Log.i(ToDoFragment.TAG, userName + "has no list id in local db.");
			return null;
		}
		return taskListId;
	}

	/**
	 * Store task list id of specific user
	 * 
	 * @param context
	 * @param userName
	 * @param taskListId
	 */
	private void storeListId(Context context, String userName, String taskListId) {
		if (taskListId != null | !taskListId.isEmpty()) {
			final SharedPreferences prefs = getToDoPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(TASK_LIST_ID + userName, taskListId);
			editor.commit();
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getToDoPreferences(Context context) {
		return toDoFragment.getActivity().getSharedPreferences(
				ToDoFragment.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	// synchronize with the local database
	protected Object[] syncLocalDatabase(List<Task> tasks) {

		List<ToDoItem> remoteItems = Utils.convertTasksToToDoItems(userId,
				tasks);
		List<ToDoItem> localItems = toDoItemDataSource.getToDoListBothStatus(
				userId, ToDoFlag.All);

		Object[] syncResults = toDoItemDataSource.synWithRemote(localItems,
				remoteItems);

		// activity.localToDoItems = (List<ToDoItem>) syncResults[0];
		// activity.refreshView();
		return syncResults;
	}

	// synchronize with the remote database
	protected List<Task> syncRemoteDatabase(Object[] sync) throws IOException {
		List<Task> tasks = Utils
				.convertToDoItemsToTasks((List<ToDoItem>) sync[0]);
		List<SyncResult> syncResults = (List<SyncResult>) sync[1];
		try {
			// general synchronization
			for (int i = 0; i < tasks.size(); i++) {
				Task task = tasks.get(i);
				String taskId = task.getId();
				for (int j = 0; j < syncResults.size(); j++) {
					if (taskId.equals(syncResults.get(j).getSyncId())) {
						// insert new item into remote database
						if (syncResults.get(j).getSyncValue() == 8) {
							Tasks serviceShowDeleted = new Tasks.Builder(
									toDoFragment.httpTransport,
									toDoFragment.jsonFactory,
									toDoFragment.credential)
									.setTasksRequestInitializer(
											new TasksRequestInitializer() {
												@Override
												public void initializeTasksRequest(
														TasksRequest<?> request)
														throws IOException {
													@SuppressWarnings("rawtypes")
													TasksRequest tasksRequest = (TasksRequest) request;
													tasksRequest
															.set("showDeleted",
																	true);
													// tasksRequest.setKey(ToDoListActivity.GOOGLE_TASKS_API_KEY);
												}

											})
									.setApplicationName(
											"Gatech-cs6300-Team6-ToDoList/1.0")
									.build();

							List<Task> tasksShowDeleted = serviceShowDeleted
									.tasks()
									.list(taskListId)
									.setFields(
											"items/id, items/title, items/deleted, items/updated")
									.execute().getItems();
							boolean isDeleted = false;
							if (tasksShowDeleted != null) {
								for (int k = 0; k < tasksShowDeleted.size(); k++) {
									Task taskShowDeleted = tasksShowDeleted
											.get(k);
									if (taskShowDeleted.getId().equals(taskId)) {
										isDeleted = true;
										if (taskShowDeleted.getDeleted()) {
											Long deletedTime = taskShowDeleted
													.getUpdated().getValue();
											if (deletedTime >= task
													.getUpdated().getValue()) {
												// item is deleted in remote
												// database after updated in
												// local
												// database
												ToDoItem item = Utils
														.convertTaskToToDoItem(
																userId, task);
												item.setModifiedTime(deletedTime);
												toDoItemDataSource
														.labelItemDeletedWithModifiedTime(item);
											} else {
												// item is deleted in remote
												// database before updated in
												// local
												// database
												service.tasks()
														.update(taskListId,
																taskId, task)
														.execute();
											}
										}
										break;
									}
								}
							}

							if (!isDeleted) {
								task.setId(null);
								Task newTask = service.tasks()
										.insert(taskListId, task).execute();
								tasks.set(i, newTask);
								// task.setUpdated(newTask.getUpdated());
								// update the item id in local database
								ToDoItem item = Utils.convertTaskToToDoItem(
										userId, task);
								toDoItemDataSource.updateItem(item);
								toDoItemDataSource.changeItemId(taskId,
										newTask.getId());
								taskId = newTask.getId();
							}

						}

						// update items from local database to remote database
						else if (syncResults.get(j).getSyncValue() == 4) {
							service.tasks().update(taskListId, taskId, task)
									.execute();
						}
						if (task.getDeleted()) {
							service.tasks().delete(taskListId, taskId);
						}
						break;
					}
				}
			}

			return tasks;
		} catch (Exception e) {

			Log.e(ToDoFragment.TAG, e.getMessage());
		}
		return null;
	}

	// static void run(ToDoListActivity toDoListActivity) {
	// new ToDoListLoadAsynTask(toDoListActivity).execute();
	// }
}
