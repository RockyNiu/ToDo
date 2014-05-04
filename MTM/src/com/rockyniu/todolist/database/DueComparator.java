package com.rockyniu.todolist.database;

public class DueComparator extends ToDoComparator {

	@Override
	public int compare(ToDoItem arg0, ToDoItem arg1) {
		int result = super.compare(arg0, arg1);
		if (result != 0)
			return result;

		// first due time, then priority;
		if (arg0.getDueTime() == null && arg1.getDueTime() != null)
			return 1;
		if (arg1.getDueTime() == null && arg0.getDueTime() != null)
			return -1;
		if (arg0.getDueTime() == arg1.getDueTime()) {
			return (int) (arg1.getPriority() - arg0.getPriority());
		}
		return (int) (arg0.getDueTime() - arg1.getDueTime());
	}

}
