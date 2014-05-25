package com.rockyniu.todolist.test;

import java.util.Calendar;
import java.util.TimeZone;

import android.test.AndroidTestCase;

import com.rockyniu.todolist.database.ToDoItem;
import com.rockyniu.todolist.database.ToDoItemDataSource;
import com.rockyniu.todolist.database.UserDataSource;

public class ToDoItemDataSourceTest extends AndroidTestCase {

	private static final String TAG = "Test for ToDoItemDataSource{}: ";

	private String id;
	private String title;
	private String notes;
	private Long dueTime;
	private boolean completed;
	private int priority;
	private Long modifiedTime;
	private boolean deleted;
	private Long completedTime;
	private ToDoItemDataSource toDoItemDataSource;
	private UserDataSource userDataSource;

	// @Before
	public void setUp() throws Exception {

		id = "-1";
		title = "test1";
		notes = "";
		dueTime = Calendar.getInstance().getTimeInMillis();
		completed = false;
		priority = 1;
		modifiedTime = Calendar.getInstance().getTimeInMillis();
		deleted = false;
		toDoItemDataSource = new ToDoItemDataSource(this.getContext());
	}

	// @After
	public void tearDown() throws Exception {
	}

	void printItem(ToDoItem item) {
		System.out.println("**** print content of task ****" + "\nclass: "
				+ item.getClass() + "\nid: " + item.getId() + "\nuser id: "
				+ item.getUserId() + "\ntitle: " + item.getTitle()
				+ "\nnotes: " + item.getNotes() + "\ndue time: "
				+ item.getDueTime() + "\nstatus: " + item.isCompleted()
				+ "\npriority: " + item.getPriority() + "\ndeleted: "
				+ item.isDeleted() + "\nmodified time "
				+ item.getModifiedTime() + "\ncompleted time: "
				+ item.getCompletedTime() + "\npastDue: " + item.isPastDue());
	}

	/**
	 * insertItemWithId() updateItem() labelItemDeletedWithModifiedTime()
	 * deleteItem()
	 */
	public void testCaseTask() {
		System.out.println(TAG);

		// Test insertItemWithId()
		ToDoItem item1 = new ToDoItem();
		toDoItemDataSource.insertItemWithId(item1);
		printItem(item1);
		assert (id.equals(toDoItemDataSource.getItemByItemId(id).getId()));

		// Test updateItem()
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.set(2100, 0, 1, 0, 0, 0);
		long time = calendar.getTimeInMillis()
				+ TimeZone.getDefault().getRawOffset();
		calendar.setTimeInMillis(time);
		item1.setTitle("test2");
		item1.setDueTime(time);
		item1.setPriority(2);
		item1.setCompleted(true);
		toDoItemDataSource.updateItem(item1);
		printItem(item1);
		ToDoItem item2 = toDoItemDataSource.getItemByItemId(id);
		assert (id.equals(item2.getId()));
		assert ("test2".equals(item2.getTitle()));
		assert (time == item2.getDueTime());
		assert (2 == item2.getPriority());
		assert (true == item2.isCompleted());

		// Test labelItemDeletedWithModifiedTime()
		toDoItemDataSource.labelItemDeletedWithModifiedTime(item1);
		item2 = toDoItemDataSource.getItemByItemId(id);
		assert (true == item2.isDeleted());

		// Test deleteItem()
		toDoItemDataSource.deleteItem(item1);
		item2 = toDoItemDataSource.getItemByItemId(id);
		assert (null == item2);
	}
}
