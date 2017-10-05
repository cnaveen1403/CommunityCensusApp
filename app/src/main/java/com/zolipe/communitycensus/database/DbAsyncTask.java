package com.zolipe.communitycensus.database;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import java.util.List;


public class DbAsyncTask extends
        AsyncTask<DbAsyncParameter, DbAsyncParameter, DbAsyncParameter> {
	// private DbAction dbAction = null;
	private Context activity = null;
	public static int QUERY_TYPE_UPDATE = 1;
	public static int QUERY_TYPE_CURSOR = 2;
	public static int QUERY_TYPE_BULK_UPDATE = 3;
	public static int QUERY_TYPE_MULTIPLE_CURSOR = 4;
	public static int QUERY_TYPE_SINGLE_QUERY_BULK_UPDATE = 5;
	public static int QUERY_TYPE_WITHOUT_CURSOR = 6;

	private boolean defaultAI = true;
	private View customAI = null;
	private GDatabaseHelper dbHelper = null;
	ProgressDialog pDialog;

	public DbAsyncTask(Context context, boolean tdAI, View cAI) {
		this.activity = context;
		this.defaultAI = tdAI;
		this.customAI = cAI;
	}

	@Override
	protected DbAsyncParameter doInBackground(DbAsyncParameter... params) {
		DbAsyncParameter dbAsyncParm = params[0];
	
		dbHelper = new GDatabaseHelper(this.activity);
		if (dbAsyncParm.getQueryType() == QUERY_TYPE_UPDATE) {
			try {
				dbHelper.executeUpdateSql(dbAsyncParm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (dbAsyncParm.getQueryType() == QUERY_TYPE_CURSOR) {
			try {
				dbAsyncParm.setQueryCursor(dbHelper.executeSql(dbAsyncParm));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(dbAsyncParm.getQueryType() == QUERY_TYPE_BULK_UPDATE) {
			try {
				dbHelper.executeBulkUpdateSql(dbAsyncParm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} else if(dbAsyncParm.getQueryType() == QUERY_TYPE_MULTIPLE_CURSOR) {
			try {
				List<Cursor> listCursors = dbHelper.executeMultipleSql(dbAsyncParm);
				dbAsyncParm.setListCursors(listCursors);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		} else if(dbAsyncParm.getQueryType() == QUERY_TYPE_SINGLE_QUERY_BULK_UPDATE) {
			try {
				dbHelper.executeSingleQueryBulkUpdateSql(dbAsyncParm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		}
		else if(dbAsyncParm.getQueryType() == QUERY_TYPE_WITHOUT_CURSOR) {
			try {
				dbHelper.executeDirectSql(dbAsyncParm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		}

		return dbAsyncParm;
	}

	@Override
	protected void onProgressUpdate(DbAsyncParameter... params) {
	}

	@Override
	protected void onPostExecute(DbAsyncParameter dbAsyncParm) {
		DbAction dbAction = dbAsyncParm.getDbAction();
		SQLiteDatabase sqlDatabase = dbAsyncParm.getDatabase();
		Cursor cur = dbAsyncParm.getQueryCursor();
		try{
			if (dbAction != null /* && !activity.isFinishing() */) {
				dbAction.execPostDbAction();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if(dbHelper != null /* && !activity.isFinishing() */) {
			dbHelper.close();
			dbHelper = null;			
		}

		/*
		if(cur != null)
			cur.close();
		cur = null;
		if(sqlDatabase != null)
			sqlDatabase.close();
		sqlDatabase = null;
		*/
		
		if(this.defaultAI == true && this.pDialog != null /* && !activity.isFinishing() */) {
			this.pDialog.dismiss();    		
		} else if(this.customAI != null /* && !activity.isFinishing() */) {
			this.customAI.setVisibility(View.GONE);
			this.customAI.setAnimation(null);
		}
	}

	@Override
	protected void onPreExecute() {
		if(this.defaultAI == true) {
			/*this.progressDialog=new GprogressDialog(activity,R.drawable.progress_spinner);
	    	this.progressDialog.show();		*/	

			pDialog = new ProgressDialog(activity);
			pDialog.setMessage("Loading...");
			pDialog.show();
		} else if(this.customAI != null) {
			this.customAI.setVisibility(View.VISIBLE);
			RotateAnimation anim = new RotateAnimation(0.0f, 360.0f , Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
			anim.setInterpolator(new LinearInterpolator());
			anim.setRepeatCount(Animation.INFINITE);
			anim.setDuration(1500);
			this.customAI.setAnimation(anim);
			this.customAI.startAnimation(anim);
		}
	}
}
