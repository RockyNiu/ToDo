package com.rockyniu.todolist.todolist;

import java.io.IOException;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.tasks.Tasks;
import com.rockyniu.todolist.database.ToDoItemDataSource;
import com.rockyniu.todolist.util.Utils;

abstract class ToDoListCommonAsynTask extends AsyncTask<Integer, Void, Boolean> {
	
	final ToDoFragment toDoFragment;
//	private final View progressBar;
	protected Tasks service;
	protected ToDoItemDataSource toDoItemDataSource; 
	String userId;
	String userName;
	String taskListId;
	public ToDoListCommonAsynTask(ToDoFragment toDoFragment){
		this.toDoFragment = toDoFragment;
//		progressBar = activity.findViewById(R.id.progressBar_connecting);
		service = toDoFragment.service;
		toDoItemDataSource = toDoFragment.toDoItemDataSource;
		userId = toDoFragment.userId;
		userName = toDoFragment.userName;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		toDoFragment.numAsyncTasks++;
		toDoFragment.getActivity().setProgressBarIndeterminateVisibility(true);
		
	}

	@Override
	protected void onPostExecute(Boolean success) {
		super.onPostExecute(success);
		if (0 == --toDoFragment.numAsyncTasks) {
			toDoFragment.getActivity().setProgressBarIndeterminateVisibility(false);
		}
		if (0 == toDoFragment.numAsyncTasks && success){
			toDoFragment.clearDeletedItems();
//			activity.refreshView();
		}else{
		}
		toDoFragment.refreshView();
		
	}
	
	@Override
	protected Boolean doInBackground(Integer... requestCodes) {
		// get token
		try {
			return doInBackground(requestCodes[0]);
		} catch (OperationCanceledException e) {
			Utils.logAndShow(toDoFragment.getActivity(), ToDoFragment.TAG, e);
			Log.e("OperationCanceledException", e.toString());
		} catch (AuthenticatorException e) {
			Utils.logAndShow(toDoFragment.getActivity(), ToDoFragment.TAG, e);
			Log.e("AuthenticatorException", e.toString());
		} catch (GoogleJsonResponseException e){
			Utils.logAndShow(toDoFragment.getActivity(), ToDoFragment.TAG, e);
			Log.e("GoogleJsonResponseException", e.toString());
		} catch (IOException e) {
			Utils.logAndShow(toDoFragment.getActivity(), ToDoFragment.TAG, e);
			Log.e("IOException", e.toString());
		} catch (Exception e){
			Log.e("Exception", e.toString());
		}
		return false;
	}

	abstract protected boolean doInBackground(Integer requestCode) throws Exception;
}
