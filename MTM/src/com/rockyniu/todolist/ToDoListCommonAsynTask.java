package com.rockyniu.todolist;

import java.io.IOException;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.tasks.Tasks;
import com.rockyniu.todolist.database.ToDoItemDataSource;

abstract class ToDoListCommonAsynTask extends AsyncTask<Integer, Void, Boolean> {
	
	final ToDoListActivity activity;
//	private final View progressBar;
	protected Tasks service;
	protected ToDoItemDataSource toDoItemDataSource; 
	String userId;
	String taskListId;
	public ToDoListCommonAsynTask(ToDoListActivity toDoListActivity){
		this.activity = toDoListActivity;
//		progressBar = activity.findViewById(R.id.progressBar_connecting);
		service = activity.service;
		toDoItemDataSource = activity.toDoItemDataSource;
		userId = activity.userId;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		activity.numAsyncTasks++;
		activity.setProgressBarIndeterminateVisibility(true);
		
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if (0 == --activity.numAsyncTasks) {
			activity.setProgressBarIndeterminateVisibility(false);
		}
		if (0 == activity.numAsyncTasks && success){
			activity.clearDeletedItems();
			activity.refreshView();
		}else{
		}
//		activity.refreshView();
		
	}
	
	@Override
	protected Boolean doInBackground(Integer... requestCodes) {
		// get token
		try {
			return doInBackground(requestCodes[0]);
		} catch (OperationCanceledException e) {
			Utils.logAndShow(activity, activity.TAG, e);
			Log.e("OperationCanceledException", e.toString());
		} catch (AuthenticatorException e) {
			Utils.logAndShow(activity, activity.TAG, e);
			Log.e("AuthenticatorException", e.toString());
		} catch (GoogleJsonResponseException e){
			Utils.logAndShow(activity, activity.TAG, e);
			Log.e("GoogleJsonResponseException", e.toString());
		} catch (IOException e) {
			Utils.logAndShow(activity, activity.TAG, e);
			Log.e("IOException", e.toString());
		} catch (Exception e){
			Log.e("Exception", e.toString());
		}
		return false;
	}

	abstract protected boolean doInBackground(Integer requestCode) throws Exception;
}
