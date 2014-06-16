package com.rockyniu.todolist.database.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.rockyniu.todolist.R;

public class ToDoItem {
	private String id; // uuid
	private String userId; // uuid
	private String title;
	private String notes;
	private Long dueTime;
	private boolean completed;
	private int priority;
	private Long modifiedTime;
	private boolean deleted;
	private Long completedTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return title;
	}

	public boolean isPastDue() {

		if (this.isCompleted() || this.getDueTime() == null) {
			return false;
		}

		return (dueTime < Calendar.getInstance().getTimeInMillis());
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Long getDueTime() {
		return dueTime;
	}

	public void setDueTime(Long dueTime) {
		this.dueTime = dueTime;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Long getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(Long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Long getCompletedTime() {
		return completedTime;
	}

	public void setCompletedTime(Long completedTime) {
		this.completedTime = completedTime;
	}

	public String toSmsMessage() {
		String smsMessage = title;

		SimpleDateFormat format = new SimpleDateFormat(
				"MM/dd/yyyy @ HH:mm a", Locale.getDefault());
		
		if (completed) {
			Calendar completedTime = Calendar.getInstance();
			if (completedTime == null) {

			} else {
				
				smsMessage = smsMessage + ", Completed on "
						+ format.format(completedTime.getTime());
			}
		} else {

			// Set the date due
			if (dueTime == null) {

			} else {
				Calendar due = Calendar.getInstance();
				due.setTimeInMillis(dueTime);
				smsMessage = smsMessage + ", Due on "
						+ format.format(due.getTime());
				if (notes == null || notes.isEmpty()) {

				} else {
					smsMessage = smsMessage + ", " + notes;
				}
			}
		}

		// set priority icon
		if (priority == 0) {
			smsMessage = "[LOW]" + smsMessage;
		} else if (priority == 1) {
			smsMessage = "[MED]" + smsMessage;
		} else {
			smsMessage = "[HIGH]" + smsMessage;
		}

		return smsMessage;
	}
}
