package com.rockyniu.todolist.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rockyniu.todolist.LoginActivity;
import com.rockyniu.todolist.R;
import com.rockyniu.todolist.TabsActivity;

// past due alarm receiver
public class AlarmReceiver extends BroadcastReceiver {
	
//	private NotificationManager mManager;
	final static String PASTDEUALARM_ACTION = "com.rockyniu.todolist.pastduealarm";
	
	@Override
	public void onReceive(Context context, Intent intent) {
//		if ("com.rockyniu.todolist.pastduealarm".equals(intent.getAction())) {
			Bundle bundle = intent.getExtras();

			String pastDueItemTitle = bundle.getString("_pastDueItemTitle");
			String pastDueItemNotes = bundle.getString("_pastDueItemNotes");
			String message = pastDueItemTitle + pastDueItemNotes;
			
			// Prepare intent which is triggered if the
		    // notification is selected
		    Intent intent1 = new Intent(context, LoginActivity.class);
		    PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent1, 0);

		    // Build notification
		    Notification notification = new Notification.Builder(context)
		        .setContentTitle("Past Due")
		        .setContentText(message).setSmallIcon(R.drawable.ic_launcher)
		        .setContentIntent(pIntent)
		        .build();
		    
		    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		    // hide the notification after its selected
		    notification.flags |= Notification.FLAG_AUTO_CANCEL;

		    notificationManager.notify(0, notification);
			
		}
//	}
}
