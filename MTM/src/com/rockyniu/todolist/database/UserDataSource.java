package com.rockyniu.todolist.database;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserDataSource {

	// Database fields
	private SQLiteDatabase database;
	private ToDoSQLiteHelper dbHelper;
	private String[] allColumns = { 
			ToDoSQLiteHelper.COLUMN_ID,
			ToDoSQLiteHelper.COLUMN_USERNAME, 
			ToDoSQLiteHelper.COLUMN_PASSWORD,
			};

	public UserDataSource(Context context) {
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
	
	public User createUsers(String name, String pwd) {
		String insertId = UUID.randomUUID().toString();
		return createUsers(name, pwd, insertId);
	}

	public User createUsers(String name, String pwd, String insertId) {
		String selection = ToDoSQLiteHelper.COLUMN_ID + " =  ? ";
		String[] selectionArgs = new String[]{insertId};
		
		ContentValues values = new ContentValues();
		values.put(ToDoSQLiteHelper.COLUMN_USERNAME, name);
		values.put(ToDoSQLiteHelper.COLUMN_PASSWORD, pwd);
		values.put(ToDoSQLiteHelper.COLUMN_ID, insertId);

		openWritableDatabase();
		database.insert(ToDoSQLiteHelper.TABLE_USERS, null, values);

		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_USERS,
				allColumns, selection,selectionArgs, null, null, null);
		cursor.moveToFirst();
		User newUser = cursorToUsers(cursor);
		cursor.close();
		closeDatabase();
		return newUser;
	}
	
	// if user doesn't exist, create a new one
	public User selectUser(String name) {
		String selection = ToDoSQLiteHelper.COLUMN_USERNAME + " =  ? ";
		String[] selectionArgs = new String[] { name };

		openReadableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_USERS,
				allColumns, selection, selectionArgs, null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			User newUser = createUsers(name, "");
			cursor.close();
			closeDatabase();
			return newUser;
		}
		User newUser = cursorToUsers(cursor);
		cursor.close();
		closeDatabase();
		return newUser;
	}
	
	
	
	// if user doesn't exist, create a new one
	public User selectUser(String name, String token) {
		String selection = ToDoSQLiteHelper.COLUMN_USERNAME + " =  ? ";
		String[] selectionArgs = new String[]{name};
		
		openReadableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_USERS,
				allColumns, selection, selectionArgs, null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()){
			if (token==null){
				token = "";
			}
			User newUser = createUsers(name, token);
			cursor.close();
			closeDatabase();
			return newUser;
		}
		User newUser = cursorToUsers(cursor);
		cursor.close();
		closeDatabase();
		return newUser;
	}
	
	public User getUserById(String id) {
		String selection = ToDoSQLiteHelper.COLUMN_ID + " =  ? ";
		String[] selectionArgs = new String[]{id};
		
		openReadableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_USERS,
				allColumns, selection, selectionArgs,
				null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()){
			cursor.close();
			closeDatabase();
			return null;
		}
			
		User newUser = cursorToUsers(cursor);
		cursor.close();
		closeDatabase();
		return newUser;
	}

	public int updateUsers(User user) {
		String id = user.getId();
		String selection = ToDoSQLiteHelper.COLUMN_ID + " =  ? ";
		String[] selectionArgs = new String[]{id};
		
		ContentValues values = new ContentValues();
		values.put(ToDoSQLiteHelper.COLUMN_USERNAME, user.getName());
		values.put(ToDoSQLiteHelper.COLUMN_PASSWORD, user.getPassword());

		openWritableDatabase();
		int rows = database.update(ToDoSQLiteHelper.TABLE_USERS, values,
				selection, selectionArgs);
		closeDatabase();
		return rows;
	}
	  
	public void deleteUser(User user) {
		String id = user.getId();
		String selection = ToDoSQLiteHelper.COLUMN_ID + " =  ? ";
		String[] selectionArgs = new String[]{id};
		openWritableDatabase();
		database.delete(ToDoSQLiteHelper.TABLE_USERS,
				selection, selectionArgs);
		closeDatabase();
		System.out.println("User deleted with id: " + id);
	}

	public List<User> getAllUsers() {
		List<User> allusers = new ArrayList<User>();
		openReadableDatabase();
		Cursor cursor = database.query(ToDoSQLiteHelper.TABLE_USERS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			User user = cursorToUsers(cursor);
			allusers.add(user);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		closeDatabase();
		return allusers;
	}

	private User cursorToUsers(Cursor cursor) {
		User user = new User();
		user.setId(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_ID)));
		user.setName(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_USERNAME)));
		user.setPassword(cursor.getString(cursor
				.getColumnIndex(ToDoSQLiteHelper.COLUMN_PASSWORD)));
		return user;
	}
}
