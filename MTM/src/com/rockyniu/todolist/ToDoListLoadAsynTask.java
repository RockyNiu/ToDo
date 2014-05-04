package com.rockyniu.todolist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
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
import com.rockyniu.todolist.database.SyncResult;
import com.rockyniu.todolist.database.ToDoItem;
import com.rockyniu.todolist.database.ToDoItemDataSource;
import com.rockyniu.todolist.database.User;
import com.rockyniu.todolist.database.UserDataSource;
import com.rockyniu.todolist.database.ToDoItemDataSource.ToDoFlag;
import com.rockyniu.todolist.database.ToDoItemDataSource.ToDoStatus;

class ToDoListLoadAsynTask extends ToDoListCommonAsynTask {

	ToDoListLoadAsynTask(ToDoListActivity toDoListActivity) {
		super(toDoListActivity);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean doInBackground(Integer requestCode) throws Exception {

		// get token
		// if (activity.token==null || activity.token.isEmpty()){
		getToken();

		// }
		if (activity.token == null && activity.token == null) {
			return false;
		} else if (requestCode == ToDoListActivity.REQUEST_TOKEN) {
			return true;
		}

		// get Google Tasks service
		getGoogleTasksService();
		if (service == null) {
			return false;
		} else if (requestCode == ToDoListActivity.REQUEST_GOOGLE_TASKS_SERVICES) {
			return true;
		}

		// get tasks list from Google Tasks service
		Object[] resultsGetTasksFromRemote = getTasksFromRemote(ToDoListActivity.listName);
		taskListId = (String) resultsGetTasksFromRemote[0];
		List<Task> tasks = (List<Task>) resultsGetTasksFromRemote[1];
		if (requestCode == ToDoListActivity.REQUEST_REMOTE_DATABASE) {
			return true;
		}

		// synchronize local database
		Object[] sync = syncLocalDatabase(tasks);
		if (requestCode == ToDoListActivity.UPDATE_LOCAL_DATABASE) {
			return true;
		}
		// synchronize remote database
		tasks = syncRemoteDatabase(sync);
		if (tasks == null) {
			return false;
		} else if (requestCode == ToDoListActivity.UPDATE_REMOTE_DATABASE) {
			// managerDataSource.updateLastSynTime(userId,
			// lastSynDateTime.getValue());
			activity.tasksList = tasks;
			return true;
		}
		// activity.refreshView();
		return true;
	}

	// Get token
	protected void getToken() throws OperationCanceledException,
			AuthenticatorException, IOException {
		// activity.token = null;
		Account account = new Account(activity.userName,
				GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		AccountManagerFuture<Bundle> accountManagerFuture = AccountManager.get(
				activity).getAuthToken(account, "oauth2:" + TasksScopes.TASKS,
				null, activity, null, null);
		activity.token = accountManagerFuture.getResult().getString(
				AccountManager.KEY_AUTHTOKEN);
		// Users user = activity.userDataSource.selectUser(activity.userName);
		// user.setPwd(activity.token);
		if (activity.token != null && !activity.token.isEmpty()) {
			// activity.userDataSource.updateUsers(user);
			activity.credential = (new GoogleCredential())
					.setAccessToken(activity.token);
		}
	}

	// Setting up the Tasks API Service
	protected void getGoogleTasksService() {
		try {
			service = new Tasks.Builder(activity.httpTransport,
					activity.jsonFactory, activity.credential)
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
					).setApplicationName("Gatech-cs6300-Team6-ToDoList/1.0")
					.build();
		} catch (Exception e) {
			service = null;
		}
	}

	// get Task list from remote database
	protected Object[] getTasksFromRemote(String listName) throws IOException {
		Object[] results = new Object[2];
		String taskListId = null;
		List<TaskList> taskLists;
		taskLists = service.tasklists().list().execute().getItems();

		List<Task> tasks;
		if (taskLists == null) {
			return results;
		} else {
			for (TaskList taskList : taskLists) {
				if (taskList.getTitle().equals(listName)) {
					taskListId = taskList.getId();
				}
			}
			// if list dosen't exist
			if (taskListId == null) {
				TaskList taskList = new TaskList()
						.setTitle(ToDoListActivity.listName);
				taskList = service.tasklists().insert(taskList).execute();
				taskListId = taskList.getId();
			}
			if (taskListId != null) {
				tasks = service.tasks()
						.list(taskListId)
						// .setFields("items")
						.setFields(
								"items/id, items/title, items/notes, items/due"
										+ ", items/status, items/updated, items/deleted, items/completed")
						// .setFields("items/id, title, notes, due, status, updated, deleted")
						.execute().getItems();
				results[0] = taskListId;
				results[1] = tasks;
				return results;
			} else {
				return null;
			}
		}
	}

	// synchronize with the local database
	@SuppressWarnings("unchecked")
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
									activity.httpTransport,
									activity.jsonFactory, activity.credential)
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
							for (int k = 0; k < tasksShowDeleted.size(); k++) {
								Task taskShowDeleted = tasksShowDeleted.get(k);
								if (taskShowDeleted.getId().equals(taskId)) {
									isDeleted = true;
									if (taskShowDeleted.getDeleted()) {
										Long deletedTime = taskShowDeleted
												.getUpdated().getValue();
										if (deletedTime >= task.getUpdated()
												.getValue()) {
											// item is deleted in remote
											// database after updated in local
											// database
											ToDoItem item = Utils
													.convertTaskToToDoItem(
															userId, task);
											item.setModifiedTime(deletedTime);
											toDoItemDataSource
													.labelItemDeletedWithModifiedTime(item);
										} else {
											// item is deleted in remote
											// database before updated in local
											// database
											service.tasks()
													.update(taskListId, taskId,
															task).execute();
										}
									}
									break;
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

			Log.e(ToDoListActivity.TAG, e.getMessage());
		}
		return null;
	}

	// static void run(ToDoListActivity toDoListActivity) {
	// new ToDoListLoadAsynTask(toDoListActivity).execute();
	// }
}
