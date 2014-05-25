package com.rockyniu.todolist.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.rockyniu.todolist.database.User;
import com.rockyniu.todolist.database.UserDataSource;

public class UserDataSourceTest extends AndroidTestCase {

	private static final String TAG = "Test for UserDataSource{}: ";
	String userName1;
	String userName2;
	UserDataSource userDataSource;

	// @Before
	public void setUp() throws Exception {
		userName1 = "User 1";
		userName2 = "User 2";
		userDataSource = new UserDataSource(this.getContext());
	}

	// @After
	public void tearDown() throws Exception {
	}

	/**
	 * Test selectUser(String name) Test getUserById(String id), Test
	 * deleteUser(User user) Test getAllUsers()
	 */
	public void testCaseUser() {
		System.out.println(TAG);

		// Test selectUser(String name), getAllUsers(), and deleteUser(User
		// user)
		List<User> users = userDataSource.getAllUsers();
		int num = users.size();
		for (int i = 0; i < num; i++) {
			userDataSource.deleteUser(users.get(i));
			assertTrue(userDataSource.getAllUsers().size() == num - i - 1);
		}

		userDataSource.selectUser(userName1);
		assertTrue(userDataSource.getAllUsers().size() == 1);
		userDataSource.selectUser(userName2);
		assertTrue(userDataSource.selectUser(userName1).getName()
				.equals(userName1));

		// Test getUserById(String id)
		String id = userDataSource.selectUser(userName1).getId();
		assertTrue(id.equals(userDataSource.getUserById(id).getId()));

		// Test deleteUser(User user)
		User user = userDataSource.selectUser(userName1);
		userDataSource.deleteUser(user);
		assertTrue(userDataSource.getAllUsers().size() == 1);
		user = userDataSource.selectUser(userName2);
		userDataSource.deleteUser(user);
		assertTrue(userDataSource.getAllUsers().size() == 0);
	}
}
