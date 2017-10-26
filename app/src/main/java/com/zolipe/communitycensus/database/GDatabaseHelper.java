package com.zolipe.communitycensus.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zolipe.communitycensus.R;

import java.util.ArrayList;
import java.util.List;

public class GDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "community_census.db";
    private static final int DATABASE_VERSION = 1;
    private Context context = null;
    private static GDatabaseHelper gDbHelper = null;

    // private static DbAsyncTask dbTask = null;

    public static GDatabaseHelper getInstance(Context context) {
        // dbTask = new DbAsyncTask((Activity) context);
        if (gDbHelper == null) {
            // SQLiteDatabase.loadLibs(context);
            gDbHelper = new GDatabaseHelper(context);
            SQLiteDatabase db = gDbHelper.getWritableDatabase();
            // db.close();
            // db = null;
        }

        return gDbHelper;
    }

    public GDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DbAsyncTask dbTask = new DbAsyncTask(this.context, true, null);
        DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_ddl,
                DbAsyncTask.QUERY_TYPE_UPDATE, null, null);
        dbAsyncParam.setDatabase(db);

        try {
            dbTask.execute(dbAsyncParam);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        String verDdlStrFormat = "ddl_v_%d";
        for (int i = oldVersion; i <= newVersion; i++) {
            String verDdlStr = String.format(verDdlStrFormat, i);
            int resId = context.getResources().getIdentifier(verDdlStr,
                    "string", context.getPackageName());
            String ResDdlStr = this.context.getResources().getString(resId);
            String[] ddl = ResDdlStr.split("\n");
            for (String ddlStr : ddl) {
                db.execSQL(ddlStr);
            }
        }
        db.endTransaction();
    }


    public Cursor executeSql(DbAsyncParameter dbAsyncParm) throws Exception {

        Cursor result = null;
        int sqlResourceId = dbAsyncParm.getSqlResourceId();
        DbParameter dbParameter = dbAsyncParm.getDbParameter();
        String ResDdlStr = this.context.getResources().getString(sqlResourceId);
        String[] ddl = ResDdlStr.split("\n");

        if (ddl.length > 1) {
            throw new Exception();
        }
        GDatabaseHelper dbHelper = this;
        SQLiteDatabase sqliteDb = dbHelper.getReadableDatabase();

        try {
            for (String ddlStr : ddl) {
                Object[] parms = dbParameter.getObjectArrayParameters(0);
                String[] strParms = null;
                int parmsSize = -1;
                if (parms != null) {
                    strParms = new String[parms.length];
                    parmsSize = parms.length;
                }

                for (int i = 0; i < parmsSize; i++) {
                    if (parms[i] instanceof String) {
                        strParms[i] = (String) parms[i];
                    } else {
                        strParms[i] = String.valueOf(parms[i]);
                    }
                }
                result = sqliteDb.rawQuery(ddlStr, strParms);

                dbAsyncParm.setQueryCursor(result);
                dbAsyncParm.setDatabase(sqliteDb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDb = null;
        }
        return result;
    }


    public Cursor executeDirectSql(DbAsyncParameter dbAsyncParm) throws Exception {

        Cursor result = null;
        int sqlResourceId = dbAsyncParm.getSqlResourceId();
        //DbParameter dbParameter = dbAsyncParm.getDbParameter();
        String ResDdlStr = this.context.getResources().getString(sqlResourceId);
        String[] ddl = ResDdlStr.split("\n");

        if (ddl.length > 1) {
            throw new Exception();
        }
        GDatabaseHelper dbHelper = this;
        SQLiteDatabase sqliteDb = dbHelper.getReadableDatabase();

        try {
            for (String ddlStr : ddl) {
                result = sqliteDb.rawQuery(ddlStr, null);
                dbAsyncParm.setQueryCursor(result);
                dbAsyncParm.setDatabase(sqliteDb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDb = null;
        }
        return result;
    }


    public void executeUpdateSql(DbAsyncParameter dbAsyncParm) // throws
    // Exception
    {

        int sqlResourceId = dbAsyncParm.getSqlResourceId();
        DbParameter dbParameter = dbAsyncParm.getDbParameter();
        String ResDdlStr = this.context.getResources().getString(sqlResourceId);
        String[] ddl = ResDdlStr.split("\n");
        GDatabaseHelper dbHelper = this;
        SQLiteDatabase sqliteDb = dbHelper.getWritableDatabase();
        sqliteDb.beginTransaction();
        int index = 0;
        try {
            for (String ddlStr : ddl) {
                Object[] parms;
                parms = (dbParameter != null) ? dbParameter.getObjectArrayParameters(index) : null;
                if (parms == null) {
                    sqliteDb.execSQL(ddlStr);
                } else {
                    sqliteDb.execSQL(ddlStr, parms);
                }
                index++;
            }
            sqliteDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDb.endTransaction();
            sqliteDb = null;
        }
        sqliteDb = null;
    }

    public void executeBulkUpdateSql(DbAsyncParameter dbAsyncParm) // throws
    // Exception
    {
        int sqlResourceId = dbAsyncParm.getSqlResourceId();
        DbParameter dbParameter = dbAsyncParm.getDbParameter();
        String ResDdlStr = this.context.getResources().getString(sqlResourceId);
        String[] ddl = ResDdlStr.split("\n");
        GDatabaseHelper dbHelper = this;
        SQLiteDatabase sqliteDb = dbHelper.getWritableDatabase();
        sqliteDb.beginTransaction();
        // int index = 0;
        try {
            String ddlStr = null;
            for (int index = 0; index < dbParameter.size(); index++) {
                Object[] parms;
                parms = (dbParameter != null) ? dbParameter
                        .getObjectArrayParameters(index) : null;
                for (int j = 0; j < ddl.length; j++) {
                    ddlStr = ddl[j];
                    if (parms == null) {
                        sqliteDb.execSQL(ddlStr);
                    } else {
                        sqliteDb.execSQL(ddlStr, parms);
                    }
                }
            }
            sqliteDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDb.endTransaction();
        }
        sqliteDb = null;
    }

    public List<Cursor> executeMultipleSql(DbAsyncParameter dbAsyncParm) throws Exception {
        ArrayList<Cursor> result = new ArrayList<Cursor>();
        Cursor tCursor = null;
        int sqlResourceId = dbAsyncParm.getSqlResourceId();
        DbParameter dbParameter = dbAsyncParm.getDbParameter();
        String ResDdlStr = this.context.getResources().getString(sqlResourceId);
        String[] ddl = ResDdlStr.split("\n");
//		Log.i("DB", "No of Queries: " + ddl.length);
        GDatabaseHelper dbHelper = this;
        SQLiteDatabase sqliteDb = dbHelper.getReadableDatabase();

        int index = 0;
        try {
            for (String ddlStr : ddl) {
                ddlStr = ddlStr.trim();
                if (ddlStr.length() == 0) {
                    continue;
                }
//				Log.i("DB", "No of Queries: index " + index);
                Object[] parms = dbParameter.getObjectArrayParameters(index);
                String[] strParms = null;
                int parmsSize = -1;
                if (parms != null) {
                    strParms = new String[parms.length];
                    parmsSize = parms.length;
                }

                for (int i = 0; i < parmsSize; i++) {
                    if (parms[i] instanceof String) {
                        strParms[i] = (String) parms[i];
                    } else {
                        strParms[i] = String.valueOf(parms[i]);
                    }
                }
                tCursor = sqliteDb.rawQuery(ddlStr, strParms);

                result.add(tCursor);
                index++;
            }
            dbAsyncParm.setDatabase(sqliteDb);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqliteDb = null;
        }
        return result;
    }

    public void executeSingleQueryBulkUpdateSql(DbAsyncParameter dbAsyncParm) // throws Exception
    {
        int sqlResourceId = dbAsyncParm.getSqlResourceId();
        DbParameter dbParameter = dbAsyncParm.getDbParameter();
        String ResDdlStr = this.context.getResources().getString(sqlResourceId);
        String[] ddl = ResDdlStr.split("\n");
        GDatabaseHelper dbHelper = this;
        SQLiteDatabase sqliteDb = dbHelper.getWritableDatabase();
        sqliteDb.beginTransaction();
        // int index = 0;
        try {
            String ddlStr = null;
            for (int index = 0; index < dbParameter.size(); index++) {
                Object[] parms;
                ddlStr = ddl[0];
                parms = (dbParameter != null) ? dbParameter.getObjectArrayParameters(index) : null;
                if (parms == null) {
                    sqliteDb.execSQL(ddlStr);
                } else {
                    sqliteDb.execSQL(ddlStr, parms);
                }
            }
            sqliteDb.setTransactionSuccessful();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            sqliteDb.endTransaction();
            //			sqliteDb.close();
            sqliteDb = null;
        }
        // sqliteDb.close();
        sqliteDb = null;
        // dbHelper.close();
        // dbHelper = null;
    }
}
