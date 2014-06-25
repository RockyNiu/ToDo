package com.rockyniu.todolist.database.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;


import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

public class Converter {
	
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

	
}