package com.rockyniu.todolist.database.model;

public class SyncResult {
	private String syncId;
	private int syncValue;
	public String getSyncId() {
		return syncId;
	}
	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}
	public int getSyncValue() {
		return syncValue;
	}
	public void setSyncValue(int syncValue) {
		this.syncValue = syncValue;
	}
}
