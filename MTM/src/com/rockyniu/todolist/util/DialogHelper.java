package com.rockyniu.todolist.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.rockyniu.todolist.R;

public class DialogHelper {

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
	static void showNeedClickDialog(Activity activity, String message,
			String title) {
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(activity).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity
				.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
		return;
	}

	static void showTaskCreationErrorDialog(Activity activity, String message) {
		showNeedClickDialog(activity, message, activity.getResources()
				.getString(R.string.fail_to_create_new_task));
	}

	public static void showItemNameIsEmptyDialog(Activity activity) {
		showTaskCreationErrorDialog(activity, activity.getResources()
				.getString(R.string.empty_name));
	}

	public static void showDueTimeIsEarlierDialog(Activity activity) {
		showTaskCreationErrorDialog(activity, activity.getResources()
				.getString(R.string.due_earlier_than_now));
	}

}
