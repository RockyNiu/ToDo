package com.rockyniu.todolist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class ToDoSQLiteHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_USERS = "users";
	public static final String TABLE_TODOLIST = "todolist";

	public static final String COLUMN_ID = "uuid";

	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_PASSWORD = "pwd";

	public static final String COLUMN_USERID = "userid";
	public static final String COLUMN_TODOTITLE = "title";
	public static final String COLUMN_TODONOTES = "notes";
	public static final String COLUMN_DUETIME = "duetime";
	public static final String COLUMN_PRIORITY = "priority";
	public static final String COLUMN_COMPLETED = "completed";
	public static final String COLUMN_MODIFIEDTIME = "modifiedtime"; // last modified time
	public static final String COLUMN_DELETED = "deleted";
	public static final String COLUMN_COMPLETEDTIME = "completedtime"; 
	
	public static final String DATABASE_NAME = "todlist.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String USER_DATABASE_CREATE = "create table "
			+ TABLE_USERS + "(" 
			+ COLUMN_ID + " text primary key, " 
			+ COLUMN_USERNAME + " text not null, " 
			+ COLUMN_PASSWORD + " text default ''" 
			+ ");";

	private static final String TODOLIST_DATABASE_CREATE = "create table "
			+ TABLE_TODOLIST + "(" 
			+ COLUMN_ID + " text primary key, " 
			+ COLUMN_USERID + " integer default 0, " 
			+ COLUMN_TODOTITLE + " text default '', " 
			+ COLUMN_TODONOTES + " text default '', "
			+ COLUMN_DUETIME + " integer default null, " 
			+ COLUMN_PRIORITY + " integer default 1, " 
			+ COLUMN_COMPLETED + " integer default 0, " 
			+ COLUMN_MODIFIEDTIME + " integer default 0, " 
			+ COLUMN_DELETED + " integer default 0, " 
			+ COLUMN_COMPLETEDTIME + " integer default null, " 
			+ " FOREIGN KEY (" + COLUMN_USERID + ") REFERENCES " 
			+ TABLE_USERS + "(" + COLUMN_ID + ")" + " ON DELETE CASCADE ON UPDATE CASCADE"			
			+ ");";

	public ToDoSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public ToDoSQLiteHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public ToDoSQLiteHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public ToDoSQLiteHelper(Context context, String name) {
		this(context, name, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		if (!database.isReadOnly()) {
			// Enable foreign key constraints
			database.execSQL("PRAGMA foreign_keys=ON;");
		}
		database.execSQL(USER_DATABASE_CREATE);
		database.execSQL(TODOLIST_DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ToDoSQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOLIST);
		onCreate(db);
	}

}
