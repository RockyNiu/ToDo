package com.rockyniu.todolist.database;

import java.util.Comparator;


public class ToDoComparator implements Comparator<ToDoItem> {

	@Override
	public int compare(ToDoItem arg0, ToDoItem arg1) {
		
		//first status/completed, then PastDue;
		if (arg0.isCompleted() && !arg1.isCompleted())
			return 1;
		if (!arg0.isCompleted() && arg1.isCompleted())
			return -1;
		if (arg0.isPastDue() && !arg1.isPastDue()) {
			return -1;
		}
		if (!arg0.isPastDue() && arg1.isPastDue()){
			return 1;
		}
		return 0;
	}

}
