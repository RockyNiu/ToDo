package com.rockyniu.todolist.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ToDoItemDataSource {

	// Database fields
	private SQLiteDatabase database;
	private ToDoSQLiteHelper dbHelper;
	private String[] allColumns = { ToDoSQLiteHelper.COLUMN_ID,
			ToDoSQLiteHelper.COLUMN_USERID, ToDoSQLiteHelper.COLUMN_TODOTITLE,
			ToDoSQLiteHelper.COLUMN_TODONOTES, ToDoSQLiteHelper.COLUMN_DUETIME,
			ToDoSQLiteHelper.COLUMN_PRIORITY,
			ToDoSQLiteHelper.COLUMN_COMPLETED,
			ToDoSQLiteHelper.COLUMN_MODIFIEDTIME,
			ToDoSQLiteHelper.COLUMN_DELETED, 
			ToDoSQLiteHelper.COLUMN_COMPLETEDTIME};
	private String[] onlyColumnId = { ToDoSQLiteHelper.COLUMN_ID, };

	public ToDoItemDataSource(Context context) {
		dbHelper = new ToDoSQLiteHelper(context);
	}

	private void openWritableDatabase() throws SQLException {
		database = dbHelper.getWritableDatabase();
		database.execSQL("PRAGMA foreign_keys=ON;");
	}

	private void openReadableDatabase() throws SQLException {
		database = dbHelper.getReadableDatabase();
	}

	private void closeDatabase() {
		database.close();
	}

	public enum ToDoStatus {
		ALL(-1), 
		ACTIVE(0), 
		COMPLETED(1);
		private final int status;

		private ToDoStatus(int status) {
			this.status = status;
		}

		public int getStatus() {
			return status;
		}
	}

	public enum ToDoPriority {
		ALL(-1), 
		LOW(0), 
		MEDIUM(1), 
		HIGH(2);
		private final int priority;

		private ToDoPriority(int priority) {
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}
	}

	public enum ToDoFlag {
		All(-1), 
		UNDELETED(0), 
		DELETED(1);
		private final int flag;

		private ToDoFlag(int flag) {
			this.flag = flag;
		}

		public int getFlag() {
			return flag;
		}
	}

	public enum SortType{
		DUE(0),
		PRIORITY(1);
		private final int sortType;
		
		private SortType(int sortType){
			this.sortType = sortType;
		}
		
		private int getSortType (){
			return sortType;
		}
	}
	
	private boolean itemIdExsit(String id) {
		openReadableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_TODOLIST,
				onlyColumnId, ToDoSQLiteHelper.COLUMN_ID + " = ? ",
				new String[] { id }, null, null, null);
		boolean exsit = cursor.getCount() > 0 ? true : false;
		cursor.close();
		closeDatabase();
		return exsit;
	}

	public ToDoItem getItemByItemId(String id) {

		openWritableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_TODOLIST,
				allColumns, ToDoSQLiteHelper.COLUMN_ID + " = ? ",
				new String[] { id }, null, null, null);

		cursor.moveToFirst();
		if (cursor.isAfterLast())
			return null;
		ToDoItem newItem = cursorToToDoItem(cursor);

		closeDatabase();
		return newItem;
	}

	// remember to create an id for item:
	// item.setId(UUID.randomUUID().toString());
	// remember to set localOnly the value 0 for item came from remote
	public boolean insertItemWithId(ToDoItem item) {
		if (item == null || item.getId() == null || itemIdExsit(item.getId())) {
			return false;
		}
		ContentValues values = new ContentValues();
		values.put(ToDoSQLiteHelper.COLUMN_MODIFIEDTIME, item.getModifiedTime());
		values.put(ToDoSQLiteHelper.COLUMN_ID, item.getId());
		values.put(ToDoSQLiteHelper.COLUMN_USERID, item.getUserId());
		// if (item.getTitle().isEmpty()){
		// values.put(ToDoSQLiteHelper.COLUMN_TODOTITLE, "To be done");
		// values.put(ToDoSQLiteHelper.COLUMN_MODIFIEDTIME,
		// Calendar.getInstance().getTimeInMillis());
		// }else{
		values.put(ToDoSQLiteHelper.COLUMN_TODOTITLE, item.getTitle());
		// }
		values.put(ToDoSQLiteHelper.COLUMN_TODONOTES, item.getNotes());
		if (item.getDueTime() == null) {
			values.putNull(ToDoSQLiteHelper.COLUMN_DUETIME);
		} else {
			values.put(ToDoSQLiteHelper.COLUMN_DUETIME, item.getDueTime());
		}
		values.put(ToDoSQLiteHelper.COLUMN_PRIORITY, item.getPriority());
		values.put(ToDoSQLiteHelper.COLUMN_COMPLETED, item.isCompleted());
		values.put(ToDoSQLiteHelper.COLUMN_DELETED, item.isDeleted());
		if (item.getCompletedTime() == null) {
			values.putNull(ToDoSQLiteHelper.COLUMN_COMPLETEDTIME);
		} else {
			values.put(ToDoSQLiteHelper.COLUMN_COMPLETEDTIME, item.getCompletedTime());
		}
		
		openWritableDatabase();
		database.insert(ToDoSQLiteHelper.TABLE_TODOLIST, null, values);
		closeDatabase();
		return true;
	}

	public boolean deleteItem(ToDoItem item) {
		String id = item.getId();
		openWritableDatabase();
		int rows = database.delete(ToDoSQLiteHelper.TABLE_TODOLIST,
				ToDoSQLiteHelper.COLUMN_ID + " = ?", new String[] { id });
		closeDatabase();
		return rows > 0 ? true : false;
	}

	// remember to set modifiedTime before use this method
	public boolean labelItemDeletedWithModifiedTime(ToDoItem item) {
		String id = item.getId();

		openWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ToDoSQLiteHelper.COLUMN_DELETED, 1);
		values.put(ToDoSQLiteHelper.COLUMN_MODIFIEDTIME, item.getModifiedTime());

		int rows = database.update(ToDoSQLiteHelper.TABLE_TODOLIST, values,
				ToDoSQLiteHelper.COLUMN_ID + " = ?", new String[] { id });

		closeDatabase();
		return rows > 0 ? true : false;
	}

	public int changeItemId(String oldId, String newId) {
		int rows = 0;
		openWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ToDoSQLiteHelper.COLUMN_ID, newId);
		rows = database.update(ToDoSQLiteHelper.TABLE_TODOLIST, values,
				ToDoSQLiteHelper.COLUMN_ID + " = ?", new String[] { oldId });
		closeDatabase();
		return rows;
	}

	public int updateItem(ToDoItem item) {
		String id = item.getId();
		int rows = 0;

		openWritableDatabase();
		/*
		 * Cursor cursor = database.query(TodoSQLiteHelper.TABLE_TODOLIST, null,
		 * TodoSQLiteHelper.COLUMN_ID + " = ? ", new String[] {
		 * Long.toString(id) }, null, null, null); if (cursor.getCount() > 0) {
		 */
		ContentValues values = new ContentValues();
		values.put(ToDoSQLiteHelper.COLUMN_MODIFIEDTIME, item.getModifiedTime());
		// if (item.getTitle().isEmpty()){
		// values.put(ToDoSQLiteHelper.COLUMN_TODOTITLE, "To be done");
		// values.put(ToDoSQLiteHelper.COLUMN_MODIFIEDTIME,
		// Calendar.getInstance().getTimeInMillis());
		// }else{
		values.put(ToDoSQLiteHelper.COLUMN_TODOTITLE, item.getTitle());
		// }
		values.put(ToDoSQLiteHelper.COLUMN_TODONOTES, item.getNotes());
		if (item.getDueTime() == null) {
			values.putNull(ToDoSQLiteHelper.COLUMN_DUETIME);
		} else {
			values.put(ToDoSQLiteHelper.COLUMN_DUETIME, item.getDueTime());
		}
		values.put(ToDoSQLiteHelper.COLUMN_PRIORITY, item.getPriority());
		values.put(ToDoSQLiteHelper.COLUMN_COMPLETED, item.isCompleted());
		values.put(ToDoSQLiteHelper.COLUMN_DELETED, item.isDeleted());
		values.put(ToDoSQLiteHelper.COLUMN_COMPLETED, item.isCompleted());
		if (item.getCompletedTime() == null) {
			values.putNull(ToDoSQLiteHelper.COLUMN_COMPLETEDTIME);
		} else {
			values.put(ToDoSQLiteHelper.COLUMN_COMPLETEDTIME, item.getCompletedTime());
		}
		
		rows = database.update(ToDoSQLiteHelper.TABLE_TODOLIST, values,
				ToDoSQLiteHelper.COLUMN_ID + " = ?", new String[] { id });
		/*
		 * }else{ // if this id dosen't exist in database rows = -1; }
		 */
		// cursor.close();
		closeDatabase();
		return rows;

	}

	// return all data if (userId == null)
	// return all data if (status < 0), unchecked if (status == 0), checked if
	// (status == 1)
	// return all data if (priority < 0)
	// return all data if (synTime == null)
	// return all data if (deleted < 0)
	public List<ToDoItem> getToDoList(String userId, ToDoStatus status,
			ToDoPriority priority, ToDoFlag deleted) {
		List<ToDoItem> list = new ArrayList<ToDoItem>();

		String orderBy =
		 ToDoSQLiteHelper.COLUMN_COMPLETED + " ASC, "
		 + ToDoSQLiteHelper.COLUMN_DUETIME + " DESC, "
		 + ToDoSQLiteHelper.COLUMN_PRIORITY + " DESC, "
		 + ToDoSQLiteHelper.COLUMN_MODIFIEDTIME + " DESC ";
		String selection = ToDoSQLiteHelper.COLUMN_COMPLETED;
		String[] selectionArgs;

		// Status filter
		switch (status.getStatus()) {
		case -1: // all
			selection = selection + " > ? ";
			selectionArgs = new String[] { "-1" };
			break;
		case 0: // unchecked
			selection = selection + " = ? ";
			selectionArgs = new String[] { "0" };
			break;
		case 1: // checked
			selection = selection + " = ? ";
			selectionArgs = new String[] { "1" };
			break;
		default: // all
			selection = selection + " > ? ";
			selectionArgs = new String[] { "-1" };
			break;
		}

		// Priority filter
		if (priority.getPriority() >= 0) {
			selection = selection + " AND " + ToDoSQLiteHelper.COLUMN_PRIORITY
					+ " = ? ";
			selectionArgs = Arrays.copyOf(selectionArgs,
					selectionArgs.length + 1);
			selectionArgs[selectionArgs.length - 1] = Integer.toString(priority
					.getPriority());
		}

		// Deleted filter
		if (deleted.getFlag() == 0 || deleted.getFlag() == 1) {
			selection = selection + " AND " + ToDoSQLiteHelper.COLUMN_DELETED
					+ " = ? ";
			selectionArgs = Arrays.copyOf(selectionArgs,
					selectionArgs.length + 1);
			selectionArgs[selectionArgs.length - 1] = Integer.toString(deleted
					.getFlag());
		}

		// userId filter
		if (userId == null) {
			orderBy = ToDoSQLiteHelper.COLUMN_USERNAME + " ASC, " + orderBy;
		} else {
			selection = selection + " AND " + ToDoSQLiteHelper.COLUMN_USERID
					+ " = ? ";
			selectionArgs = Arrays.copyOf(selectionArgs,
					selectionArgs.length + 1);
			selectionArgs[selectionArgs.length - 1] = userId;
		}

		openReadableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_TODOLIST,
				allColumns, selection, selectionArgs, null, null, orderBy);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ToDoItem item = cursorToToDoItem(cursor);
			list.add(item);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		closeDatabase();
		return list;
	}

	// all priority
	private List<ToDoItem> getToDoList(String userId, ToDoStatus status,
			ToDoFlag deleted) {
		return getToDoList(userId, status, ToDoPriority.ALL, deleted);
	}

	// all priority and all status
	public List<ToDoItem> getToDoListBothStatus(String userId, ToDoFlag deleted) {
		return getToDoList(userId, ToDoStatus.ALL, deleted);
	}

	private List<ToDoItem> getToDoListActive(String userId, ToDoFlag deleted) {
		return getToDoList(userId, ToDoStatus.ACTIVE, deleted);
	}

	private List<ToDoItem> getToDoListCompleted(String userId, ToDoFlag deleted) {
		return getToDoList(userId, ToDoStatus.COMPLETED, deleted);
	}

	public List<ToDoItem> getAllToDo(ToDoFlag deleted) {
		return getToDoList(null, ToDoStatus.ALL, ToDoPriority.ALL, deleted);
	}

	// hide completed
	// sortType : SORT_DUEDATE = 0; SORT_PRIORITY = 1;
	public List<ToDoItem> getNewListFromLocal(String userId, ToDoStatus status,
			SortType sortType, ToDoFlag deleted) {
		List<ToDoItem> newList = null;
		switch (status) {
		case ALL:
			newList = getToDoListBothStatus(userId, deleted);
			break;
		case ACTIVE:
			newList = getToDoListActive(userId, deleted);
			break;
		case COMPLETED:
			newList = getToDoListCompleted(userId, deleted);
			break;
		default:
			newList = getToDoListBothStatus(userId, deleted);
			break;
		};
		
		switch (sortType) {
		case DUE:
			Collections.sort(newList, new DueComparator());
			break;
		case PRIORITY:
			Collections.sort(newList, new PriorityComparator());
			break;
		default:
			Collections.sort(newList, new DueComparator());
			break;
		}
		return newList;
	}

	// synchronized local database with remote database
	// return updated remote tasks list and changed records
	// record-value : remote-insert remote-update local-insert local-update
	// 8 : 1 0 0 0
	// 4 : 0 1 0 0
	// 2 : 0 0 1 0
	// 1 : 0 0 0 1
	public Object[] synWithRemote(List<ToDoItem> localItems,
			List<ToDoItem> remoteItems) {
		Object[] results = new Object[2];
		List<String> syncIds = new ArrayList<String>();
		List<SyncResult> syncResults = new ArrayList<SyncResult>();
		results[0] = remoteItems;
		results[1] = syncResults;
		if (remoteItems == null && localItems == null) {
			// results[0] = localItems;
			return results;
		} else if (localItems != null && remoteItems != null) {
			boolean[] syn = new boolean[localItems.size()];

			for (int i = 0; i < remoteItems.size(); i++) {
				boolean isNew = true;
				ToDoItem remote = remoteItems.get(i);
				String remoteId = remote.getId();
				for (int j = 0; j < localItems.size(); j++) {
					ToDoItem local = localItems.get(j);
					if (local.getId().equals(remoteId)) {

						if (local.getModifiedTime() > remote.getModifiedTime()) {
							// prepare to update remote;
							remoteItems.set(i, local);
							syncIds.add(remoteId);
							SyncResult syncResult = new SyncResult();
							syncResult.setSyncId(remoteId);
							syncResult.setSyncValue(4);
							syncResults.add(syncResult);
						} else if (local.getModifiedTime() < remote
								.getModifiedTime() || !local.equals(remote)) {
							// update local;
							updateItem(remote);
							SyncResult syncResult = new SyncResult();
							syncResult.setSyncId(remoteId);
							syncResult.setSyncValue(1);
							syncResults.add(syncResult);
						} else {
							// equals
						}
						isNew = false;
						syn[j] = true;
						break;
					}
				}
				// add "new" updated item into local
				if (isNew) {
					SyncResult syncResult = new SyncResult();
					syncResult.setSyncId(remoteId);
					if (itemIdExsit(remoteId)) {
						updateItem(remote);
						syncResult.setSyncValue(1); // update local
					} else {
						insertItemWithId(remote);
						syncResult.setSyncValue(2); // insert new into local
					}
					syncResults.add(syncResult);
				}
			}

			// add "new" updated items into remote database
			for (int i = 0; i < syn.length; i++) {
				if (!syn[i]) {
					ToDoItem insertItem = localItems.get(i);
					String insertItemId = insertItem.getId();
					remoteItems.add(insertItem);
					SyncResult syncResult = new SyncResult();
					syncResult.setSyncId(insertItemId);
					syncResult.setSyncValue(8); // prepare to insert new into
												// remote
					syncResults.add(syncResult);
				}
			}
		} else if (localItems == null) { // add "new" updated items into local
											// database
			for (int i = 0; i < remoteItems.size(); i++) {
				ToDoItem item = remoteItems.get(i);
				String id = item.getId();
				SyncResult syncResult = new SyncResult();
				syncResult.setSyncId(id);
				if (itemIdExsit(id)) {
					updateItem(item);
					syncResult.setSyncValue(1); // update local
				} else {
					insertItemWithId(item);
					syncResult.setSyncValue(2); // insert new into local
				}
				syncResults.add(syncResult);
			}
		} else { // add "new" updated items into remote database
			for (int i = 0; i < localItems.size(); i++) {
				ToDoItem item = localItems.get(i);
				String id = item.getId();
				SyncResult syncResult = new SyncResult();
				syncResult.setSyncId(id);
				syncResult.setSyncValue(8); // prepare to insert new into remote
				syncResults.add(syncResult);
			}
			results[0] = localItems;
		}

		// results[0] = remoteItems;
		return results;
	}

	// sort by modifiedTime
	public List<ToDoItem> sort(List<ToDoItem> items) {

		return items;
	}

	private ToDoItem cursorToToDoItem(Cursor cursor) {
		ToDoItem item = new ToDoItem();
		item.setId(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_ID)));
		item.setUserId(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_USERID)));
		item.setTitle(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_TODOTITLE)));
		item.setNotes(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_TODONOTES)));

		if (cursor.isNull(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_DUETIME))) {
			item.setDueTime(null);
		} else {
			item.setDueTime(cursor.getLong(cursor
					.getColumnIndex(ToDoSQLiteHelper.COLUMN_DUETIME)));
		}
		item.setPriority(cursor.getInt(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_PRIORITY)));
		item.setCompleted(cursor.getInt(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_COMPLETED)) == 1 ? true
				: false);
		item.setModifiedTime(cursor.getLong(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_MODIFIEDTIME)));
		item.setDeleted(cursor.getInt(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_DELETED)) == 1 ? true
				: false);
		if (cursor.isNull(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_COMPLETEDTIME))) {
			item.setCompletedTime(null);
		} else {
			item.setCompletedTime(cursor.getLong(cursor
					.getColumnIndex(ToDoSQLiteHelper.COLUMN_COMPLETEDTIME)));
		}
		return item;
	}
}
