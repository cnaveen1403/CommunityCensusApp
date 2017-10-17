package com.zolipe.communitycensus.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.activity.AddFamilyMember;
import com.zolipe.communitycensus.activity.ViewMember;
import com.zolipe.communitycensus.adapter.HorizantalListAdapter;
import com.zolipe.communitycensus.app.AppData;
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

public class FamilyDetailsFragment extends Fragment {

    View rootView;
    Context mContext;
    Activity mActivity;
    ArrayList<FamilyHead> familyHeadsList = new ArrayList<>();
    HorizontalListView familyMembersList;
    TextView tv_no_data;
    Button btn_add_member, btn_retry;
    ScrollView sv_head_detail;
    RelativeLayout rl_family_members, rl_error;
    private String TAG = "FamilyDetailsFragment";
    private String mAadhaar;
    FamilyHead mFamilyHead;

    public FamilyDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_family_details, container, false);
        mContext = this.getActivity();
        mActivity = getActivity();

        btn_add_member = (Button)rootView.findViewById(R.id.btn_add_member);
        btn_retry = (Button)rootView.findViewById(R.id.btn_retry);
        sv_head_detail = (ScrollView)rootView.findViewById(R.id.sv_head_detail);
        rl_family_members = (RelativeLayout)rootView.findViewById(R.id.rl_family_members);
        rl_error = (RelativeLayout)rootView.findViewById(R.id.rl_error);
        tv_no_data = (TextView) rootView.findViewById(R.id.tv_no_data);

        familyMembersList = (HorizontalListView)rootView.findViewById(R.id.membersList);

        mAadhaar = AppData.getString(mContext, CensusConstants.aadhaar);

        if(CommonUtils.isActiveNetwork(mContext)){
            showFamilyProfiles(mAadhaar);
            new GetFamilyDetailsAsyncTask().execute(mAadhaar);
        }else {
            showFamilyProfiles(mAadhaar);
        }

        familyMembersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FamilyHead tempObj = familyHeadsList.get(position);
                Intent intent = new Intent(mContext, ViewMember.class);
                intent.putExtra("FamilyMember", tempObj);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                //showMember (tempObj);
            }
        });

        btn_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddFamilyMember.class);
                intent.putExtra("family_head", mFamilyHead);
//                String name = AppData.getString(mContext, CensusConstants.firstName) + " " + AppData.getString(mContext, CensusConstants.lastName);
//                intent.putExtra("member_id", mFamilyHeadId);
//                intent.putExtra("member_name", name);
//                intent.putExtra("member_url", AppData.getString(mContext, CensusConstants.image_url));
                startActivityForResult(intent, 1);
                mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetFamilyDetailsAsyncTask().execute(mAadhaar);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                new GetFamilyDetailsAsyncTask().execute(mAadhaar);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CommonUtils.isActiveNetwork(mContext)) {
            showFamilyProfiles (mAadhaar);
            new GetFamilyDetailsAsyncTask().execute(mAadhaar);
        }else {
            showFamilyProfiles (mAadhaar);
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
            String aadhar_num = params[0];
            Log.e(TAG, "doInBackground: aadhar_num >>> " + aadhar_num);
            parms.add(new BasicNameValuePair("head_aadhar_number", aadhar_num));

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_WITH_FAMILY_MEMBERS_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_WITH_FAMILY_MEMBERS_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.e("FamilyDetailsActivity", "family head list result naveen >> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                String response = jsonObject.getString("response");

                if (status.equals("error") && status_code.equals("1003")) {
                    /*sv_head_detail.setVisibility(View.GONE);
                    rl_family_members.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_no_data)).setText(response);*/
                    showFamilyProfiles(mAadhaar);
                } else if (status.equals("success") && status_code.equals("1000")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        CommonUtils.saveMembersToLocalDB(mContext, explrObject);
                    }

                    showFamilyProfiles(mAadhaar); //familyMembersList.setAdapter(new HorizantalListAdapter(mContext, familyHeadsList));
                } else if (status.equals("success") && status_code.equals("1001")) {
                    showFamilyProfiles(mAadhaar);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                /*sv_head_detail.setVisibility(View.GONE);
                rl_family_members.setVisibility(View.GONE);
                rl_error.setVisibility(View.VISIBLE);
                tv_no_data.setText("Server Not Responding !!! Please try again later.");*/
            }
        }
    }

    private void showFamilyProfiles(String head_aadhaar) {
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();
        Log.e(TAG, "getFamilyProfiles: inside the offline fetch");
        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add(head_aadhaar);
        dbParams.addParamterList(parms);
        Log.e(TAG, "showFamilyProfiles: head_aadhaar >>> " + head_aadhaar);
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
                    tv_no_data.setText("No Data Available !!! Please add members.");
                }

                if (cur.moveToFirst()) {
                    do {
                        try {
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
                            String city_id = cur.getString(cur.getColumnIndex("city_id"));
                            String state_id = cur.getString(cur.getColumnIndex("state_id"));
//                            Log.e(TAG, "execPostDbAction: first_name >> " + first_name);
//                            Log.e(TAG, "execPostDbAction: last_name >> " + last_name);
//                            Log.e(TAG, "execPostDbAction: aadhaar >> " + aadhaar);

                            if (isFamilyHead.equalsIgnoreCase("yes")) {
                                ((TextView) rootView.findViewById(R.id.firstNameTV)).setText(first_name);
                                ((TextView) rootView.findViewById(R.id.lastNameTV)).setText(last_name);
                                ((TextView) rootView.findViewById(R.id.genderTV)).setText(gender);
                                ((TextView) rootView.findViewById(R.id.dobTV)).setText(age);
                                ((TextView) rootView.findViewById(R.id.phoneNumberTV)).setText(phone_number);
                                ((TextView) rootView.findViewById(R.id.emailTV)).setText(email);
                                ((TextView) rootView.findViewById(R.id.addressTV)).setText(address);
                                ((TextView) rootView.findViewById(R.id.pinCodeTV)).setText(zipcode);
                                Glide.with(mContext).load(image_url)
                                        .crossFade()
                                        .dontAnimate()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .placeholder(R.drawable.ic_family_head)
                                        .into((ImageView)rootView.findViewById(R.id.iv_header_image));

                                mFamilyHead = new FamilyHead(first_name, last_name,
                                        phone_number, aadhaar, email, address, gender, image_url,
                                        age, "Self", size, zipcode, dob, familyHeadId, isFamilyHead,
                                        isSynced, city_id, state_id);
                            } else {
                                String relation = getRelationName(relationship);
                                if (familyHeadsList.size() == 0) {
                                    familyHeadsList.add(new FamilyHead(first_name, last_name,
                                            phone_number, aadhaar, email, address, gender, image_url,
                                            age, relation, size, zipcode, dob, familyHeadId, isFamilyHead,
                                            isSynced, city_id, state_id));
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
                                                age, relation, size, zipcode, dob, familyHeadId, isFamilyHead,
                                                isSynced, city_id, state_id));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    while (cur.moveToNext());
                    HorizantalListAdapter horizantalListAdapter = new HorizantalListAdapter(mContext, familyHeadsList);
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

    private String getRelationName (String relation_id){
        String relation = "";
        GDatabaseHelper dbHelper = GDatabaseHelper.getInstance(mContext);
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
