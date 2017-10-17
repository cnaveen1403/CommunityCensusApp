package com.zolipe.communitycensus.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.activity.HomeActivity;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.database.GDatabaseHelper;
import com.zolipe.communitycensus.model.State;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class CommonUtils {
    public static String TAG = "CommonUtils";
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static boolean isActiveNetwork(Context context) {
        Boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                isConnected = true;
            }
        }
        return isConnected;
    }

    public static String calculateAge(String dateString) {
        String age = "0";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // use SimpleDateFormat to define how to PARSE the INPUT
            Date date = sdf.parse(dateString);

            // at this point you have a Date-Object with the value of
            // 1437059241000 milliseconds
            // It doesn't have a format in the way you think

            // use SimpleDateFormat to define how to FORMAT the OUTPUT
//            System.out.println( sdf.format(date) );
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            Date todaysDate = new Date();
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(todaysDate);
            int nowMonth = cal2.get(Calendar.MONTH) + 1;
            int nowYear = cal2.get(Calendar.YEAR);
            int result = nowYear - year;

            if (month > nowMonth) {
                result--;
            } else if (month == nowMonth) {
                int nowDay = cal2.get(Calendar.DAY_OF_MONTH);

                if (day > nowDay) {
                    result--;
                }
            }

            age = result + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age;
    }

    public static ArrayList<State> getRelationsList(Context mContext) {
        final ArrayList<State> mRelationsList = new ArrayList<>();
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();

        ArrayList<Object> parms = new ArrayList<Object>();
        dbParams.addParamterList(parms);
        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_relations_info,
                DbAsyncTask.QUERY_TYPE_CURSOR, dbParams, null);

        DbAction dbAction = new DbAction() {
            @Override
            public void execPreDbAction() {
            }

            @Override
            public void execPostDbAction() {
                Cursor cur = dbAsyncParam.getQueryCursor();
                if (cur == null) {
                    return;
                }

                if (cur.moveToFirst()) {
                    do {
                        String id = cur.getString(cur.getColumnIndex("relation_id"));
                        String name = cur.getString(cur.getColumnIndex("relation_name"));
                        if (mRelationsList.size() == 0) {
                            mRelationsList.add(new State(id, name));
                        } else {
                            boolean bStatus = true;
                            Iterator<State> iter = mRelationsList.iterator();
                            while (iter.hasNext()) {
//                                    Log.d(TAG, "============ Inside if condition iterator ============= ");
                                State obj = iter.next();
                                if (id.equals(obj.getId())) {
                                    bStatus = false;
                                }
                            }
                            Log.d(TAG, "bStatus >>>> " + bStatus);
                            if (bStatus) {
//                                Log.d("SuperFragment", "************ Object Has been added successfully ************ ");
                                mRelationsList.add(new State(id, name));
                            }
                        }
                    }
                    while (cur.moveToNext());
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

        return mRelationsList;
    }

    public static ArrayList<State> getStatesList(Context mContext) {
        final ArrayList<State> mStatesList = new ArrayList<>();
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();

        ArrayList<Object> parms = new ArrayList<Object>();
        dbParams.addParamterList(parms);
        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_states_info,
                DbAsyncTask.QUERY_TYPE_CURSOR, dbParams, null);

        DbAction dbAction = new DbAction() {
            @Override
            public void execPreDbAction() {
            }

            @Override
            public void execPostDbAction() {
                Cursor cur = dbAsyncParam.getQueryCursor();
                if (cur == null) {
                    return;
                }

                if (cur.moveToFirst()) {
                    do {
                        String id = cur.getString(cur.getColumnIndex("state_id"));
                        String name = cur.getString(cur.getColumnIndex("state_name"));
                        if (mStatesList.size() == 0) {
                            mStatesList.add(new State(id, name));
                        } else {
                            boolean bStatus = true;
                            Iterator<State> iter = mStatesList.iterator();
                            while (iter.hasNext()) {
//                                    Log.d(TAG, "============ Inside if condition iterator ============= ");
                                State obj = iter.next();
                                if (id.equals(obj.getId())) {
                                    bStatus = false;
                                }
                            }
                            Log.d(TAG, "bStatus >>>> " + bStatus);
                            if (bStatus) {
//                                Log.d("SuperFragment", "************ Object Has been added successfully ************ ");
                                mStatesList.add(new State(id, name));
                            }
                        }
                    }
                    while (cur.moveToNext());
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

        return mStatesList;
    }

    public static class GetFamilyHeadsListAsyncTask extends AsyncTask<String, Void, String> {
        Context mContext;

        public GetFamilyHeadsListAsyncTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String doInBackground(String... params) {
            // params comes from the execute() call: params[0] is the url.
            String search = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(CensusConstants.userid, AppData.getString(mContext, CensusConstants.userid)));
            parms.add(new BasicNameValuePair("rolebased_user_id", AppData.getString(mContext, CensusConstants.rolebased_user_id)));
            parms.add(new BasicNameValuePair("user_role", AppData.getString(mContext, CensusConstants.userRole)));
            parms.add(new BasicNameValuePair("search_text", search));

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");

                if (status.equals("success") && status_code.equals("1000")) {
                    saveFamilyHeadsToLocalDB(mContext, jsonObject);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private static void saveFamilyHeadsToLocalDB(Context context, JSONObject jsonObject) {
        final DbAsyncTask dbATask = new DbAsyncTask(context, false, null);
        DbParameter dbParams_duty = new DbParameter();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                ArrayList<Object> parms = new ArrayList<Object>();

                parms.add(explrObject.getString("first_name"));
                parms.add(explrObject.getString("last_name"));
                parms.add(explrObject.getString("phone_number"));
                parms.add(explrObject.getString("email"));
                parms.add(explrObject.getString("address"));
                parms.add(explrObject.getString("gender"));
                parms.add(explrObject.getString("age"));
                parms.add(explrObject.getString("image_url"));
                parms.add("");//encoded data
                parms.add(explrObject.getString("relationship_id"));
                parms.add(explrObject.getString("member_count"));
                parms.add(explrObject.getString("zipcode"));
                parms.add(explrObject.getString("dob"));
                parms.add(explrObject.getString("aadhar_number"));
                parms.add(explrObject.getString("isfamily_head"));
                parms.add("yes");
                parms.add(explrObject.getString("city_id"));
                parms.add(explrObject.getString("state_id"));
                parms.add(explrObject.getString("country"));
                parms.add("");//Image type
                parms.add("member");
                parms.add(explrObject.getString("rolebased_user_id"));
                parms.add(explrObject.getString("created_by"));
                parms.add(explrObject.getString("aadhar_number"));

                dbParams_duty.addParamterList(parms);
            }

            final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_members,
                    DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

            DbAction dbAction_duty = new DbAction() {
                @Override
                public void execPreDbAction() {
                }

                @Override
                public void execPostDbAction() {
                }
            };

            dbAsyncParam_duty.setDbAction(dbAction_duty);

            try {
                dbATask.execute(dbAsyncParam_duty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void saveMembersToLocalDB(Context context, JSONObject explrObject) {

        final DbAsyncTask dbATask = new DbAsyncTask(context, false, null);
        DbParameter dbParams_duty = new DbParameter();
        try {
            ArrayList<Object> parms = new ArrayList<Object>();
            String relationId = getRelationShipId(context, explrObject.getString("relationship"));
            parms.add(explrObject.getString("first_name"));
            parms.add(explrObject.getString("last_name"));
            parms.add(explrObject.getString("phone_number"));
            parms.add(explrObject.getString("email"));
            parms.add(explrObject.getString("address"));
            parms.add(explrObject.getString("gender"));
            parms.add(explrObject.getString("age"));
            parms.add(explrObject.getString("image_url"));
            parms.add("");//encoded data
            parms.add(relationId);
            parms.add(explrObject.getString("member_count"));
            parms.add(explrObject.getString("zipcode"));
            parms.add(explrObject.getString("dob"));
            parms.add(explrObject.getString("head_aadhar_number"));
            parms.add(explrObject.getString("isfamily_head"));
            parms.add("yes");
            parms.add(explrObject.getString("city_id"));
            parms.add(explrObject.getString("state_id"));
            parms.add(explrObject.getString("country"));
            parms.add("");//Image type
            parms.add("member");
            parms.add(explrObject.getString("member_id"));
            parms.add(explrObject.getString("created_by"));
            parms.add(explrObject.getString("aadhar_number"));

            dbParams_duty.addParamterList(parms);

            final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_members,
                    DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

            DbAction dbAction_duty = new DbAction() {
                @Override
                public void execPreDbAction() {
                }

                @Override
                public void execPostDbAction() {

                }
            };

            dbAsyncParam_duty.setDbAction(dbAction_duty);

            try {
                dbATask.execute(dbAsyncParam_duty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String getRelationShipId(Context context, String relation_name) {
        String relationId = "";
        GDatabaseHelper dbHelper = GDatabaseHelper.getInstance(context);
        String query = "SELECT relation_id FROM RelationsInfo WHERE relation_name = '" + relation_name + "'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                relationId = cursor.getString(cursor.getColumnIndex("relation_id"));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // return count
        return relationId;
    }

    public static class uploadOfflineSupervisorAsyncTask extends AsyncTask<String, Void, String> {
        Context context;

        uploadOfflineSupervisorAsyncTask(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(String... params) {
            String sresponse = "empty response !!!!!!!!!";
            // params comes from the execute() call: params[0] is the url.
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(CensusConstants.BASE_URL + CensusConstants.UPLOAD_OFFLINE_SUPERVISORS_URL);
                httpPost.setEntity(new StringEntity(params[0], "UTF-8"));

                httpPost.setHeader("Content-Type", "application/json");
                HttpResponse response = null;
                response = httpClient.execute(httpPost);
//                sresponse = response.getEntity().toString();
                sresponse = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sresponse;
//            List<NameValuePair> parms = new LinkedList<NameValuePair>();
//            parms.add(new BasicNameValuePair("", ));
//            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.UPLOAD_OFFLINE_SUPERVISORS_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                //{"status":"success","status_code":"1000","offline_ids":"12","response":"Add offline Supervisors Successful"}
                if (status.equals("success") && status_code.equals("1000")) {
                    String ids = jsonObject.getString("offline_ids");
                    updateOfflineRecords(context, ids, "supervisor");
                } else {
                    Log.e(TAG, "onPostExecute: Some error in uploading the Profiles : " + result);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    public static class uploadOfflineMembersAsyncTask extends AsyncTask<String, Void, String> {
        Context context;

        uploadOfflineMembersAsyncTask(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(String... params) {
            String sresponse = "empty response !!!!!!!!!";
            // params comes from the execute() call: params[0] is the url.
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(CensusConstants.BASE_URL + CensusConstants.UPLOAD_OFFLINE_MEMBERS_URL);
                httpPost.setEntity(new StringEntity(params[0], "UTF-8"));

                httpPost.setHeader("Content-Type", "application/json");
                HttpResponse response = null;
                response = httpClient.execute(httpPost);
                sresponse = EntityUtils.toString(response.getEntity());

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(TAG, "doInBackground: sresponse for members >>> " + sresponse);
            return sresponse;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                //{"status":"success","status_code":"1000","offline_ids":"4","response":"Add offline Members Successful"}
                if (status.equals("success") && status_code.equals("1000")) {
                    Log.e(TAG, "onPostExecute: Inside Success uploadOfflineMembersAsyncTask [][][][][][][][[][][][][][][][][][][][ ");
                    String ids = jsonObject.getString("offline_ids");
                    updateOfflineRecords(context, ids, "members");
                } else {
                    Log.e(TAG, "onPostExecute: Some error in uploading the Profiles : " + result);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private static void updateOfflineRecords(Context context, String ids, String userDataType) {
        String[] array = ids.split(",");

        if (userDataType.equals("supervisor")) {
            for (int i = 0; i < array.length; i++) {
                updateSupervisorRecordInLocalDB(context, Integer.parseInt(array[i]));
            }
            showNotification(context, "Your offline data of supervisor has been synced successfully", userDataType);
        } else if (userDataType.equals("members")) {
            for (int i = 0; i < array.length; i++) {
                updateMemberRecordInLocalDB(context, Integer.parseInt(array[i]));
            }
            showNotification(context, "offline Members data has been synced successfully", userDataType);
        }
    }

    private static void updateSupervisorRecordInLocalDB(Context context, int id) {
        GDatabaseHelper dbHelper = GDatabaseHelper.getInstance(context);
        String query = "UPDATE  SupervisorInfo SET `isSynced`='yes' WHERE id=" + id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.close();
    }

    private static void updateMemberRecordInLocalDB(Context context, int id) {
        GDatabaseHelper dbHelper = GDatabaseHelper.getInstance(context);
        String query = "UPDATE  MemberInfo SET `isSynced`='yes' WHERE id=" + id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.close();
    }

    public static void showNotification(Context context, String message, String userDataType) {
        Log.e(TAG, "showNotification: INSIDE THIS METHOD ");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.logo_blue);
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int UNIQUE_INTEGER_NUMBER = 1;

        if (userDataType.equals("supervisor"))
            intent.putExtra("selected_tab", userDataType);

        if (userDataType.equals("members")) {
            intent.putExtra("selected_tab", userDataType);
            UNIQUE_INTEGER_NUMBER = 2;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_blue));
        builder.setContentTitle("Community Census");
        builder.setContentText(message);
        builder.setSubText("Tap to relaod the view");
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(UNIQUE_INTEGER_NUMBER, builder.build());
    }

    public static void saveSupervisorsToLocalDB(Context context, JSONObject jsonObject) {

        final DbAsyncTask dbATask = new DbAsyncTask(context, false, null);
        DbParameter dbParams_duty = new DbParameter();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Log.e(TAG, "saveSupervisorsToLocalDB: jsonArray.length() >> " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                ArrayList<Object> parms = new ArrayList<Object>();

                parms.add(explrObject.getString("first_name"));
                parms.add(explrObject.getString("last_name"));
                parms.add(explrObject.getString("phone_number"));
                parms.add(explrObject.getString("email"));
                parms.add(explrObject.getString("address"));
                parms.add(explrObject.getString("gender"));
                parms.add(explrObject.getString("age"));
                parms.add(explrObject.getString("image_url"));
                parms.add("");//encoded data
                parms.add(explrObject.getString("zipcode"));
                parms.add(explrObject.getString("dob"));
                parms.add(explrObject.getString("member_count"));
                parms.add("yes");
                parms.add(explrObject.getString("city_id"));
                parms.add(explrObject.getString("state_id"));
                parms.add(explrObject.getString("country"));
                parms.add("");//Image type
                parms.add("supervisor");
                parms.add(explrObject.getString("created_by"));
                parms.add(explrObject.getString("aadhar_number"));

                dbParams_duty.addParamterList(parms);
            }

            final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_supervisor,
                    DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

            DbAction dbAction_duty = new DbAction() {
                @Override
                public void execPreDbAction() {
                }

                @Override
                public void execPostDbAction() {

                }
            };

            dbAsyncParam_duty.setDbAction(dbAction_duty);

            try {
                dbATask.execute(dbAsyncParam_duty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static class getHelperTableAsyncTask extends AsyncTask<Void, Void, String> {
        Context mContext;

        public getHelperTableAsyncTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            // params comes from the execute() call: params[0] is the url.

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.ADD_MEMBER_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/
            return new ConnectToServer().getDataFromUrlGETMethod(CensusConstants.BASE_URL + CensusConstants.GET_HELPER_DATA_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "on postexecute result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");

                if (status.equals("success") && status_code.equals("1000")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    //Save Cities
                    JSONObject citiesObj = jsonArray.getJSONObject(0);
                    saveCitiesInfo(mContext, citiesObj);

                    JSONObject statesObj = jsonArray.getJSONObject(1);
                    saveStatesInfo(mContext, statesObj);

                    JSONObject relationsObj = jsonArray.getJSONObject(2);
                    saveRelationsInfo(mContext, relationsObj);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    public static void saveCitiesInfo(Context context, JSONObject jsonObject) {
        final DbAsyncTask dbATask = new DbAsyncTask(context, false, null);
        DbParameter dbParams_duty = new DbParameter();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("city_data");
            Log.e(TAG, "saveCitiesInfo: jsonArray.length() >> " + jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                ArrayList<Object> parms = new ArrayList<Object>();
                parms.add(explrObject.getString("city_name"));
                parms.add(explrObject.getString("state_id"));
                parms.add(explrObject.getString("city_id"));
                dbParams_duty.addParamterList(parms);
            }

            final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_city_info,
                    DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

            DbAction dbAction_duty = new DbAction() {
                @Override
                public void execPreDbAction() {
                }

                @Override
                public void execPostDbAction() {
                }
            };

            dbAsyncParam_duty.setDbAction(dbAction_duty);

            try {
                dbATask.execute(dbAsyncParam_duty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void saveStatesInfo(Context context, JSONObject jsonObject) {
        final DbAsyncTask dbATask = new DbAsyncTask(context, false, null);
        DbParameter dbParams_duty = new DbParameter();

        try {
            JSONArray statesArray = jsonObject.getJSONArray("state_data");
//            JSONArray jsonArray = jsonObject.getJSONArray("data");
            Log.e(TAG, "saveStatesInfo: jsonArray.length() >> " + statesArray.length());

            for (int i = 0; i < statesArray.length(); i++) {
                JSONObject explrObject = statesArray.getJSONObject(i);
                ArrayList<Object> parms = new ArrayList<Object>();
                parms.add(explrObject.getString("state_name"));
                parms.add(explrObject.getString("state_id"));
                dbParams_duty.addParamterList(parms);
            }

            final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_states_info,
                    DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

            DbAction dbAction_duty = new DbAction() {
                @Override
                public void execPreDbAction() {
                }

                @Override
                public void execPostDbAction() {

                }
            };

            dbAsyncParam_duty.setDbAction(dbAction_duty);

            try {
                dbATask.execute(dbAsyncParam_duty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void saveRelationsInfo(Context context, JSONObject jsonObject) {
        final DbAsyncTask dbATask = new DbAsyncTask(context, false, null);
        DbParameter dbParams_duty = new DbParameter();

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("realtionship_data");
            Log.e(TAG, "saveRelationsInfo: jsonArray.length() >> " + jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                ArrayList<Object> parms = new ArrayList<Object>();
                parms.add(explrObject.getString("relation_name"));
                parms.add(explrObject.getString("relation_id"));
                dbParams_duty.addParamterList(parms);
            }

            final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_relations_info,
                    DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

            DbAction dbAction_duty = new DbAction() {
                @Override
                public void execPreDbAction() {
                }

                @Override
                public void execPostDbAction() {

                }
            };

            dbAsyncParam_duty.setDbAction(dbAction_duty);

            try {
                dbATask.execute(dbAsyncParam_duty);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
