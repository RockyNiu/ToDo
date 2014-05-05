package com.rockyniu.todolist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.rockyniu.todolist.database.ToDoItem;

public class Utils {

	public static List<ToDoItem> convertTasksToToDoItems(String userId,
			List<Task> tasks) {
		if (tasks == null) {
			return null;
		}
		List<ToDoItem> items = new ArrayList<ToDoItem>();
		for (int i = 0; i < tasks.size(); i++) {
			items.add(convertTaskToToDoItem(userId, tasks.get(i)));
		}
		return items;
	}

	public static List<Task> convertToDoItemsToTasks(List<ToDoItem> items) {
		if (items == null) {
			return null;
		}
		List<Task> tasks = new ArrayList<Task>();
		for (int i = 0; i < items.size(); i++) {
			tasks.add(convertToDoItemToTask(items.get(i)));
		}
		return tasks;
	}

	public static ToDoItem convertTaskToToDoItem(String userId, Task task) {
		if (task == null)
			return null;

		ToDoItem item = new ToDoItem();
		// set id
		item.setId(task.getId());

		// set userId
		item.setUserId(userId);

		// set modifiedTime
		item.setModifiedTime(task.getUpdated().getValue());

		// set title and priority
		String title = task.getTitle().trim();
		if (title.endsWith("!!")) {
			title = title.substring(0, title.length() - 2);
			item.setPriority(2);
		} else if (title.endsWith("!")) {
			title = title.substring(0, title.length() - 1);
			item.setPriority(1);
		} else if (title.endsWith(".")) {
			title = title.substring(0, title.length() - 1);
			item.setPriority(0);
		} else {
			item.setPriority(1); // default
		}
		if (title.isEmpty()) {
			item.setTitle("To be done"); // default
		} else {
			item.setTitle(title);
		}

		// set completed and completedTime
		if (task.getStatus().equals("completed")) {
			item.setCompleted(true);
			long time = task.getCompleted().getValue();
			// int offset = TimeZone.getDefault().getOffset(time);
			item.setCompletedTime(time);
		} else {
			item.setCompleted(false);
			item.setCompletedTime(null);
		}
		;

		// set dueTime
		DateTime due = task.getDue();
		String notes = task.getNotes();
		if (due == null && notes == null) {
			item.setDueTime(null);
		} else if (notes != null && notes.matches("^Due .*")) {
			// set dueTime from notes
			notes = notes.replace("Due ", "");
			long dueTime = DateTime.parseRfc3339(notes).getValue();
			// int offset = TimeZone.getDefault().getOffset(dueTime);
			item.setDueTime(dueTime);
		} else {
			// set dueTime from due
			long time = due.getValue();
			int offset = TimeZone.getDefault().getOffset(time);
			item.setDueTime(time - offset);
		}

		// set notes
		item.setNotes("");

		// set deleted
		if (task.getDeleted() == null || !task.getDeleted()) {
			item.setDeleted(false);
		} else {
			item.setDeleted(true);
		}

		return item;
	}

	public static Task convertToDoItemToTask(ToDoItem item) {
		Task task = new Task();
		if (item == null)
			return null;
		// set id
		task.setId(item.getId());

		// set title
		String priority = "";
		switch (item.getPriority()) {
		case 2:
			priority = "!!";
			break;
		case 1:
			priority = "!";
			break;
		case 0:
			priority = ".";
			break;
		default:
			priority = "!";
			break;
		}
		task.setTitle(item.getTitle() + priority);

		// set notes and due
		if (item.getDueTime() == null) {
			task.setDue(null);
			task.setNotes(null);
		} else {
			long time = item.getDueTime();
			// int offset = TimeZone.getDefault().getOffset(time);
			DateTime dueTime = new DateTime(time);
			task.setDue(dueTime);
			task.setNotes("Due " + dueTime.toStringRfc3339());
		}

		// set status and completed
		if (item.isCompleted()) {
			task.setStatus("completed");
			long time = item.getCompletedTime();
			// int offset = TimeZone.getDefault().getOffset(time);
			DateTime completedTime = new DateTime(time);
			task.setCompleted(completedTime);
		} else {
			task.setStatus("needsAction");
			task.setCompleted(null);
		}

		// set updated
		task.setUpdated(new DateTime(item.getModifiedTime()));

		// set deleted
		task.setDeleted(item.isDeleted());

		return task;
	}

	/**
	 * Logs the given throwable and shows an error alert dialog with its
	 * message.
	 * 
	 * @param activity
	 *            activity
	 * @param tag
	 *            log tag to use
	 * @param t
	 *            throwable to log and show
	 */
	public static void logAndShow(Activity activity, String tag, Throwable t) {
		Log.e(tag, "Error", t);
		String message = t.getMessage();
		if (t instanceof GoogleJsonResponseException) {
			GoogleJsonError details = ((GoogleJsonResponseException) t)
					.getDetails();
			if (details != null) {
				message = details.getMessage();
			}
		} else if (t.getCause() instanceof OperationCanceledException) {
			message = ((OperationCanceledException) t.getCause()).getMessage();
		} else if (t.getCause() instanceof GoogleAuthException) {
			message = ((GoogleAuthException) t.getCause()).getMessage();
		} else if (t instanceof IOException) {
			if (t.getMessage() == null) {
				message = "IOException";
			}
		}
		showErrorToast(activity, message);
	}

	/**
	 * Shows an toast message with the given message.
	 * 
	 * @param activity
	 *            activity
	 * @param message
	 *            message to show or {@code null} for none
	 */
	public static void showErrorToast(Activity activity, String message) {
		String errorMessage = getErrorMessage(activity, message);
		showToastInternal(activity, errorMessage);
	}

	private static String getErrorMessage(Activity activity, String message) {
		Resources resources = activity.getResources();
		if (message == null) {
			return resources.getString(R.string.error);
		}
		return resources.getString(R.string.error_format, message);
	}

	public static void showToastInternal(final Activity activity,
			final String toastMessage) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(activity, toastMessage, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	/**
	 * Shows an alert dialog with the given message. click is need to dismiss
	 * the message.
	 * 
	 * @param activity
	 *            activity
	 * @param message
	 *            message to show or {@code null} for none
	 * @param title
	 *            title of dialog
	 */
	private static void showNeedClickDialog(Activity activity, String message,
			String title) {
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
		return;
	}

	// public static void showPastDueDialog(Activity activity, String message){
	// showNeedClickDialog(activity, message, "Task Past Due!");
	// }

	private static void showTaskCreationErrorDialog(Activity activity,
			String message) {
		showNeedClickDialog(activity, message, "Task Creation Error");
	}

	public static void showItemNameIsEmptyDialog(Activity activity) {
		showTaskCreationErrorDialog(activity, "Task name cannot be empty.");
	}

	public static void showDueTimeIsEarlierDialog(Activity activity) {
		showTaskCreationErrorDialog(activity,
				"Due time cannot be earlier than now.");
	}
}