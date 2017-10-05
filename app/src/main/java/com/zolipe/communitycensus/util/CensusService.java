package com.zolipe.communitycensus.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.database.GDatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CensusService extends Service {
    private static final String TAG = "CensusService";

    private boolean isRunning = false;

    @Override
    public void onCreate() {
        Log.e(TAG, "Service onCreate");
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e(TAG, "Service onStartCommand");
        if (CommonUtils.isActiveNetwork(CensusService.this)) {
            //Creating new thread for my service
            //Always write your long running tasks in a separate thread, to avoid ANR
            new Thread(new Runnable() {
                @Override
                public void run() {

                    /*//Your logic that service will perform will be placed here
                    //In this example we are just looping and waits for 1000 milliseconds in each loop.
                    for (int i = 0; i < 5; i++) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }

                        if (isRunning) {
                            Log.e(TAG, "Service running");
                        }
                    }*/
                    int sup_count = getOfflineRecordsCount("SupervisorInfo");
                    int mem_count = getOfflineRecordsCount("MemberInfo");
                    if (sup_count > 0)
                        uploadOfflineSupervisors();

                    if (mem_count > 0)
                        uploadOfflineMembersToServer();

                    //Stop service once it finishes its task
                    stopSelf();
                }
            }).start();
        }

        return Service.START_STICKY;
    }

    public int getOfflineRecordsCount(String table_name) {
        GDatabaseHelper dbHelper = GDatabaseHelper.getInstance(CensusService.this);
        String countQuery = "SELECT  * FROM " + table_name + " WHERE `isSynced`='no'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    private void uploadOfflineSupervisors() {
        final DbAsyncTask dbATask = new DbAsyncTask(CensusService.this, false, null);
        DbParameter dbParams = new DbParameter();
        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add("no");
        dbParams.addParamterList(parms);
        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_offline_supervisors,
                DbAsyncTask.QUERY_TYPE_CURSOR, dbParams, null);

        DbAction dbAction = new DbAction() {

            @Override
            public void execPreDbAction() {
            }

            @Override
            public void execPostDbAction() {
                Log.e(TAG, "uploadOfflineSupervisors : execPostDbAction: indide >>>>>>>>>>>>. ");
                Cursor cur = dbAsyncParam.getQueryCursor();
                if (cur == null) {
                    Log.e(TAG, "execPostDbAction: Cursor is null  <<<<<<<<<<<<<<<<....>>>>>>>>>>>>>>>>>>");
                    return;
                }

                if (cur.moveToFirst()) {
                    JSONArray jsonArray = new JSONArray();

                    do {
                        try {
                            JSONObject member = new JSONObject();
                            member.put("offline_id", cur.getString(cur.getColumnIndex("id")));
                            member.put("first_name", cur.getString(cur.getColumnIndex("first_name")));
                            member.put("last_name", cur.getString(cur.getColumnIndex("last_name")));
                            member.put("gender", cur.getString(cur.getColumnIndex("gender")));
                            member.put("dob", cur.getString(cur.getColumnIndex("dob")));
                            member.put("phone_number", cur.getString(cur.getColumnIndex("phone_number")));
                            member.put("aadhar_number", cur.getString(cur.getColumnIndex("aadhaar")));
                            member.put("email", cur.getString(cur.getColumnIndex("email")));
                            member.put("address", cur.getString(cur.getColumnIndex("address")));
                            member.put("city_id", cur.getString(cur.getColumnIndex("city_id")));
                            member.put("state_id", cur.getString(cur.getColumnIndex("state_id")));
                            member.put("country", cur.getString(cur.getColumnIndex("country")));
                            member.put("zipcode", cur.getString(cur.getColumnIndex("zipcode")));
                            member.put("image_type", cur.getString(cur.getColumnIndex("image_type")));
                            member.put("created_by", cur.getString(cur.getColumnIndex("created_by")));
                            member.put("user_avatar", cur.getString(cur.getColumnIndex("image")));
                            //add the member to array
                            jsonArray.put(member);

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    while (cur.moveToNext());

                    JSONObject finalObj = new JSONObject();
                    try {
                        finalObj.put("supervisor_data", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String jsonStr = finalObj.toString();
                    Log.e(TAG, "execPostDbAction: jsonString : >>>>>>>>>>>>> " + jsonStr);
                    new CommonUtils.uploadOfflineSupervisorAsyncTask(CensusService.this).execute(jsonStr);
                }

                cur.close();
            }
        };

        dbAsyncParam.setDbAction(dbAction);

        try {
            dbATask.execute(dbAsyncParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadOfflineMembersToServer() {
        Log.e(TAG, "uploadOfflineMembers : Inside >>>>>>>>>>>>>>>>>>>>>>>>>>>>. ");
        final DbAsyncTask dbATask = new DbAsyncTask(CensusService.this, false, null);
        DbParameter dbParams = new DbParameter();
        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add("no");
        dbParams.addParamterList(parms);
        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_offline_members,
                DbAsyncTask.QUERY_TYPE_CURSOR, dbParams, null);

        DbAction dbAction = new DbAction() {

            @Override
            public void execPreDbAction() {}

            @Override
            public void execPostDbAction() {
                Log.e(TAG, "uploadOfflineMembers : execPostDbAction: indide >>>>>>>>>>>>. ");
                Cursor cur = dbAsyncParam.getQueryCursor();
                if (cur == null) {
                    Log.e(TAG, "uploadOfflineMembers : execPostDbAction: Cursor is null  <<<<<<<<<<<<<<<<....>>>>>>>>>>>>>>>>>>");
                    return;
                }

                if (cur.moveToFirst()) {
                    JSONArray jsonArray = new JSONArray();

                    do {
                        try {
                            JSONObject member = new JSONObject();
                            member.put("offline_id", cur.getString(cur.getColumnIndex("offline_id")));
                            member.put("isfamily_head", cur.getString(cur.getColumnIndex("isFamilyHead")));
                            member.put("relationship_id", cur.getString(cur.getColumnIndex("relationship_id")));
                            member.put("first_name", cur.getString(cur.getColumnIndex("first_name")));
                            member.put("last_name", cur.getString(cur.getColumnIndex("last_name")));
                            member.put("gender", cur.getString(cur.getColumnIndex("gender")));
                            member.put("dob", cur.getString(cur.getColumnIndex("dob")));
                            member.put("phone_number", cur.getString(cur.getColumnIndex("phone_number")));
                            member.put("head_aadhar_number", cur.getString(cur.getColumnIndex("head_aadhar_number")));
                            member.put("aadhar_number", cur.getString(cur.getColumnIndex("aadhaar")));
                            member.put("email", cur.getString(cur.getColumnIndex("email")));
                            member.put("address", cur.getString(cur.getColumnIndex("address")));
                            member.put("city_id", cur.getString(cur.getColumnIndex("city_id")));
                            member.put("state_id", cur.getString(cur.getColumnIndex("state_id")));
                            member.put("country", cur.getString(cur.getColumnIndex("country")));
                            member.put("zipcode", cur.getString(cur.getColumnIndex("zipcode")));
                            member.put("user_avatar", cur.getString(cur.getColumnIndex("user_avatar")));
                            member.put("image_type", cur.getString(cur.getColumnIndex("image_type")));
                            member.put("user_role", cur.getString(cur.getColumnIndex("user_role")));
                            member.put("rolebased_user_id", cur.getString(cur.getColumnIndex("role_based_user_id")));
                            member.put("created_by", cur.getString(cur.getColumnIndex("created_by")));
//                            member.put("isSynced", cur.getString(cur.getColumnIndex("isSynced")));

                            //add the member to array
                            jsonArray.put(member);

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    while (cur.moveToNext());

                    JSONObject finalObj = new JSONObject();
                    try {
                        finalObj.put("members_data", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String jsonStr = finalObj.toString();
                    Log.e(TAG, "execPostDbAction: " + "jsonString: " + jsonStr);
                    new CommonUtils.uploadOfflineMembersAsyncTask(CensusService.this).execute(jsonStr);
                }

                cur.close();
            }
        };

        dbAsyncParam.setDbAction(dbAction);

        try {
            Log.e(TAG, "getOfllineMembers: inside try ");
            dbATask.execute(dbAsyncParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.e(TAG, "Service onDestroy");
    }
}
