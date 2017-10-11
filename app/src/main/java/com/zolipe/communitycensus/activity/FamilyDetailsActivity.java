package com.zolipe.communitycensus.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.adapter.HorizantalListAdapter;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.database.GDatabaseHelper;
import com.zolipe.communitycensus.model.FamilyHead;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.CommonUtils;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.HorizontalListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FamilyDetailsActivity extends AppCompatActivity {

    private Context mContext;
    ArrayList<FamilyHead> familyHeadsList = new ArrayList<>();
    HorizontalListView familyMembersList;
    Button btn_add_member, btn_retry;
    ScrollView sv_head_detail;
    RelativeLayout rl_family_members, rl_error;
    TextView tv_no_data;
    FamilyHead mFamilyHead;
    CircleImageView iv_header_image;
    private String TAG = "FamilyDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContext = FamilyDetailsActivity.this;
        btn_add_member = (Button) findViewById(R.id.btn_add_member);
        btn_retry = (Button) findViewById(R.id.btn_retry);
        sv_head_detail = (ScrollView) findViewById(R.id.sv_head_detail);
        rl_family_members = (RelativeLayout) findViewById(R.id.rl_family_members);
        rl_error = (RelativeLayout) findViewById(R.id.rl_error);
        tv_no_data = (TextView) findViewById(R.id.tv_no_data);
        iv_header_image = (CircleImageView) findViewById(R.id.iv_header_image);

        familyMembersList = (HorizontalListView) findViewById(R.id.membersList);

        if (getIntent().getExtras() != null) {
            mFamilyHead = getIntent().getExtras().getParcelable("family_head");
            Log.e(TAG, "onCreate: head aadahaar >>> " + mFamilyHead.getAadhaar());
        }

        if (CommonUtils.isActiveNetwork(mContext)) {
            showFamilyProfiles (mFamilyHead.getAadhaar());
            new GetFamilyDetailsAsyncTask().execute(mFamilyHead.getAadhaar());
        }else {
            showFamilyProfiles (mFamilyHead.getAadhaar());
        }
        familyMembersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FamilyHead tempObj = familyHeadsList.get(position);
                Intent intent = new Intent(mContext, ViewMember.class);
                intent.putExtra("FamilyMember", tempObj);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
//                showMember (tempObj);
            }
        });

        btn_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FamilyDetailsActivity.this, AddFamilyMember.class);
                intent.putExtra("family_head", mFamilyHead);
                /*intent.putExtra("head_aadhaar", mHeadAddhaar);
                intent.putExtra("member_name", familyHeadName);
                intent.putExtra("member_url", familyHeadImgUrl);*/
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetFamilyDetailsAsyncTask().execute(mFamilyHead.getAadhaar());
            }
        });

        iv_header_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog nagDialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(false);
                nagDialog.setContentView(R.layout.preview_image);
                Button btnClose = (Button) nagDialog.findViewById(R.id.btnIvClose);
                ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
                Glide.with(mContext).load(mFamilyHead.getImage_url())
                        .crossFade()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_supervisor_list)
                        .into(ivPreview);
                ivPreview.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        nagDialog.dismiss();
                    }
                });
                nagDialog.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                Intent intent = new Intent(FamilyDetailsActivity.this, FamilyMembers.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CommonUtils.isActiveNetwork(mContext)) {
            showFamilyProfiles (mFamilyHead.getAadhaar());
            new GetFamilyDetailsAsyncTask().execute(mFamilyHead.getAadhaar());
        }else {
            showFamilyProfiles (mFamilyHead.getAadhaar());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                new GetFamilyDetailsAsyncTask().execute(mFamilyHead.getAadhaar());
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private class GetFamilyDetailsAsyncTask extends AsyncTask<String, Void, String> {
        final Dialog progressDialog = new Dialog(mContext, R.style.progress_dialog);

        @Override
        protected void onPreExecute() {
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // params comes from the execute() call: params[0] is the url.
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            String head_aadhaar = params[0];
            parms.add(new BasicNameValuePair("head_aadhar_number", head_aadhaar));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_WITH_FAMILY_MEMBERS_URL, parms);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "onPostExecute: result >>>> "  + result );
            progressDialog.dismiss();
            if (result.equals("") || result.equals(null) || result.isEmpty()) {
//                showServerError();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.getString("status");
                    String status_code = jsonObject.getString("status_code");
                    String response = jsonObject.getString("response");

                    if (status.equals("error") && status_code.equals("1003")) {
                        showFamilyProfiles(mFamilyHead.getAadhaar());
                    } else if (status.equals("success") && status_code.equals("1000")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject explrObject = jsonArray.getJSONObject(i);
//                            String isFamilyHead = explrObject.getString("isfamily_head");
//                            if (isFamilyHead.equals("no")) {
                                CommonUtils.saveMembersToLocalDB(mContext, explrObject);
//                            }
                        }

                        showFamilyProfiles (mFamilyHead.getAadhaar());
                    } else if (status.equals("success") && status_code.equals("1001")) {
                        /*sv_head_detail.setVisibility(View.GONE);
                        rl_family_members.setVisibility(View.GONE);
                        rl_error.setVisibility(View.VISIBLE);
                        tv_no_data.setText("Data Not available, Please add Members.");*/
                        showFamilyProfiles(mFamilyHead.getAadhaar());
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
//                    showServerError();
                }
            }
        }
    }

    private void showFamilyProfiles(String head_aadhaar) {
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();
        Log.e(TAG, "showFamilyProfiles: inside the offline fetch");
        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add(head_aadhaar);
        dbParams.addParamterList(parms);

        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_family_members,
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

                if(cur.getCount() > 0){
                    familyHeadsList.clear();
                    sv_head_detail.setVisibility(View.VISIBLE);
                    rl_family_members.setVisibility(View.VISIBLE);
                    rl_error.setVisibility(View.GONE);
                }else {
                    sv_head_detail.setVisibility(View.GONE);
                    rl_family_members.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    tv_no_data.setText("Data Not available, Please add Members.");
                }

                if (cur.moveToFirst()) {
                    do {
                        try {
                            String headId = cur.getString(cur.getColumnIndex("familyHeadId"));
                            String first_name = cur.getString(cur.getColumnIndex("first_name"));
                            String last_name = cur.getString(cur.getColumnIndex("last_name"));
                            String phone_number = cur.getString(cur.getColumnIndex("phone_number"));
                            String aadhaar = cur.getString(cur.getColumnIndex("aadhaar"));
                            String email = cur.getString(cur.getColumnIndex("email"));
                            String address = cur.getString(cur.getColumnIndex("address"));
                            String gender = cur.getString(cur.getColumnIndex("gender"));
                            String image_url = cur.getString(cur.getColumnIndex("image_url"));
                            String age = cur.getString(cur.getColumnIndex("age"));
                            String relationship = cur.getString(cur.getColumnIndex("relationship"));
                            String size = cur.getString(cur.getColumnIndex("family_size"));
                            String zipcode = cur.getString(cur.getColumnIndex("zipcode"));
                            String dob = cur.getString(cur.getColumnIndex("dob"));
                            String familyHeadId = cur.getString(cur.getColumnIndex("familyHeadId"));
                            String isFamilyHead = cur.getString(cur.getColumnIndex("isFamilyHead"));
                            String isSynced = cur.getString(cur.getColumnIndex("isSynced"));
                            String state_id = cur.getString(cur.getColumnIndex("state_id"));
                            String city_id = cur.getString(cur.getColumnIndex("city_id"));
//                            Log.e(TAG, "execPostDbAction: first_name >> " + first_name);
//                            Log.e(TAG, "execPostDbAction: last_name >> " + last_name);
//                            Log.e(TAG, "execPostDbAction: aadhaar >> " + aadhaar);

                            if (isFamilyHead.equalsIgnoreCase("yes")) {
                                ((TextView) findViewById(R.id.firstNameTV)).setText(first_name);
                                ((TextView) findViewById(R.id.lastNameTV)).setText(last_name);
                                ((TextView) findViewById(R.id.genderTV)).setText(gender);
                                ((TextView) findViewById(R.id.dobTV)).setText(age);
                                ((TextView) findViewById(R.id.phoneNumberTV)).setText(phone_number);
                                ((TextView) findViewById(R.id.emailTV)).setText(email);
                                ((TextView) findViewById(R.id.addressTV)).setText(address);
                                ((TextView) findViewById(R.id.pinCodeTV)).setText(zipcode);
                                Glide.with(mContext).load(image_url)
                                        .crossFade()
                                        .dontAnimate()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.ic_family_head)
                                        .into((ImageView) findViewById(R.id.iv_header_image));
                            } else {
                                String relation = getRelationName(relationship);
                                if (familyHeadsList.size() == 0) {
                                    familyHeadsList.add(new FamilyHead(first_name, last_name,
                                            phone_number, aadhaar, email, address, gender, image_url,
                                            age, relation, size, zipcode, dob, familyHeadId, isFamilyHead
                                            , isSynced, state_id, city_id));
                                } else {
                                    boolean bStatus = true;
                                    Iterator<FamilyHead> iter = familyHeadsList.iterator();
                                    while (iter.hasNext()) {
                                        Log.d(TAG, "============ Inside if condition iterator ============= ");
                                        FamilyHead obj = iter.next();
                                        if (aadhaar.equals(obj.getAadhaar())) {
                                            bStatus = false;
                                        }
                                    }
                                    Log.d(TAG, "bStatus >>>> " + bStatus);
                                    if (bStatus) {
                                        familyHeadsList.add(new FamilyHead(first_name, last_name,
                                                phone_number, aadhaar, email, address, gender, image_url,
                                                age, relation, size, zipcode, dob, familyHeadId, isFamilyHead
                                                , isSynced, state_id, city_id));
                                    }
                                }

                               /* familyHeadsList.add(new FamilyHead(headId, first_name, last_name,
                                        phone_number, aadhaar, email, address, gender, image_url,
                                        age, relation, size, zipcode, dob, familyHeadId, isFamilyHead, isSynced));*/
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    while (cur.moveToNext());
                    HorizantalListAdapter horizantalListAdapter = new HorizantalListAdapter(FamilyDetailsActivity.this, familyHeadsList);
                    familyMembersList.setAdapter(horizantalListAdapter);
                    horizantalListAdapter.notifyDataSetChanged();
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

    private void showServerError() {
        sv_head_detail.setVisibility(View.GONE);
        rl_family_members.setVisibility(View.GONE);
        rl_error.setVisibility(View.VISIBLE);
        tv_no_data.setText("Server Not Responding !!! Please try again later.");
    }

    private String getRelationName (String relation_id){
        String relation = "";
        GDatabaseHelper dbHelper = GDatabaseHelper.getInstance(FamilyDetailsActivity.this);
        String query = "SELECT relation_name FROM RelationsInfo WHERE relation_id = " + relation_id;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                relation = cursor.getString(cursor.getColumnIndex("relation_name"));
            }while(cursor.moveToNext());
        }
        cursor.close();

        // return count
        return relation;
    }
}
