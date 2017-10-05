package com.zolipe.communitycensus.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public class DbAsyncParameter {
	private DbAction dbAction = null;
	private int sqlResourceId = 0;
	private DbParameter dbParameter;
	private GDatabaseHelper dbHelper = null;
	private SQLiteDatabase database;
	private int queryType = 0;
	private int queryResult = 0;
	private Cursor queryCursor = null;
	private List<Cursor> listCursors = null;

	public DbAsyncParameter(/* SQLiteDatabase db, */ int sqlResId, int qryType, DbParameter dbParams, DbAction dbAction)
	{
    	this.sqlResourceId = sqlResId;
    	this.dbParameter = dbParams;
    	this.dbAction = dbAction;
    	this.queryType = qryType;
//    	this.database = db;
	}
	
    public int getQueryResult() {
		return queryResult;
	}

	public void setQueryResult(int queryResult) {
		this.queryResult = queryResult;
	}

	public Cursor getQueryCursor() {
		return queryCursor;
	}

	public void setQueryCursor(Cursor queryCursor) {
		this.queryCursor = queryCursor;
	}

	public int getSqlResourceId() {
		return sqlResourceId;
	}

	public void setSqlResourceId(int sqlResourceId) {
		this.sqlResourceId = sqlResourceId;
	}

	public int getQueryType() {
		return queryType;
	}

	public void setQueryType(int queryType) {
		this.queryType = queryType;
	}

	public DbAction getDbAction() {
		return dbAction;
	}

	public void setDbAction(DbAction dbAction) {
		this.dbAction = dbAction;
	}

	public DbParameter getDbParameter() {
		return dbParameter;
	}

	public void setDbParameter(DbParameter dbParameter) {
		this.dbParameter = dbParameter;
	}

	public GDatabaseHelper getDbHelper() {
		return dbHelper;
	}

	public void setDbHelper(GDatabaseHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	public List<Cursor> getListCursors() {
		return listCursors;
	}

	public void setListCursors(List<Cursor> listCursors) {
		this.listCursors = listCursors;
	}

}
