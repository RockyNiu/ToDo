package com.rockyniu.todolist.database;

public class PriorityComparator extends ToDoComparator {

	@Override
	public int compare(ToDoItem arg0, ToDoItem arg1) {
		int result = super.compare(arg0, arg1);
		if (result != 0)
			return result;
		//first priority, then duetime;
		if (arg1.getPriority()==arg0.getPriority()) {
			if (arg0.getDueTime()==null)
				return 1;
			if (arg1.getDueTime()==null)
				return -1;
			return (int)(arg0.getDueTime()-arg1.getDueTime());
		}
		//inverse sorting
		return (int)(arg1.getPriority()-arg0.getPriority());
	}

}
