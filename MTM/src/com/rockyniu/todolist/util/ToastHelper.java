package com.rockyniu.todolist.util;

import java.io.IOException;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.rockyniu.todolist.R;

public class ToastHelper {

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
		ToastHelper.showErrorToast(activity, message);
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
		String errorMessage = ToastHelper.getErrorMessage(activity, message);
		ToastHelper.showToastInternal(activity, errorMessage);
	}

	static String getErrorMessage(Activity activity, String message) {
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

	public static void showToastInternal(final Activity activity,
			final String toastMessage, final int duration,
			final float horizontalMargin, final float verticalMargin) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = Toast.makeText(activity, toastMessage,
						Toast.LENGTH_LONG);
				toast.setMargin(horizontalMargin, verticalMargin);
				toast.show();
			}
		});
	}

}
