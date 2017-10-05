package com.zolipe.communitycensus.database;

public interface DbAction {
	public void execPreDbAction();
	public void execPostDbAction();
}
