package com.zolipe.communitycensus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.zolipe.communitycensus.adapter.FamilyHeadAdapter;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.interfaces.FamilyHeadsListItemClickListener;
import com.zolipe.communitycensus.model.FamilyHead;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.DividerItemDecoration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FamilyMembers extends AppCompatActivity implements FamilyHeadsListItemClickListener {

    private static Context mContext;
    private String LOG_TAG = FamilyMembers.class.getSimpleName();
    private List<FamilyHead> familyHeadses = new ArrayList<>();
    private RecyclerView recyclerView;
    private FamilyHeadAdapter mAdapter;
    private EditText et_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_members);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContext = FamilyMembers.this;
        getFamilyHeadList("");

        et_search = (EditText)findViewById(R.id.et_search);
//        iv_search = (Button)findViewById(R.id.iv_search);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mAdapter = new FamilyHeadAdapter(familyHeadses, FamilyMembers.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(FamilyMembers.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(FamilyMembers.this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        mAdapter.setClickListener(this);


        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text ["+s+"]");
                mAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(FamilyMembers.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void getFamilyHeadList(String search) {
        new GetFamilyHeadsAsyncTask().execute(search);
    }

    @Override
    public void onClick(View view, FamilyHead obj) {
        Intent intent = new Intent(FamilyMembers.this, FamilyDetailsActivity.class);
        intent.putExtra("member_id",obj.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private class GetFamilyHeadsAsyncTask extends AsyncTask<String, Void, String> {
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
            String searchText = params[0];
            parms.add(new BasicNameValuePair(CensusConstants.search_text, searchText));
            parms.add(new BasicNameValuePair(CensusConstants.userid, AppData.getString(mContext, CensusConstants.userid)));
            parms.add(new BasicNameValuePair("rolebased_user_id", AppData.getString(mContext, CensusConstants.rolebased_user_id)));
            parms.add(new BasicNameValuePair("user_role", AppData.getString(mContext, CensusConstants.userRole)));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();
            Log.d(LOG_TAG, "family head list result >> " + result);
//            et_search.setText(""+result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                String response = jsonObject.getString("response");

                if (status.equals("success") && status_code.equals("1003")) {

                    ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                    ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
                    TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customDialog.dismiss();
                        }
                    });
                    customDialog.show();
                } else if (status.equals("success") && status_code.equals("1000")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        String member_id = explrObject.getString("member_id");
                        String first_name = explrObject.getString("first_name");
                        String last_name = explrObject.getString("last_name");
                        String phone_number = explrObject.getString("phone_number");
                        String aadhaar = explrObject.getString("aadhar_number");
                        String email = explrObject.getString("email");
                        String address = explrObject.getString("address");
                        String gender = explrObject.getString("gender");
                        String image_url = explrObject.getString("image_url");
                        String age = explrObject.getString("age");
                        String zipcode = explrObject.getString("zipcode");
                        String relationship = explrObject.getString("relationship");
                        String size = explrObject.getString("member_count");
                        String dob = explrObject.getString("dob");
                        String familyHeadId = explrObject.getString("family_head_id");
                        String isFamilyHead = explrObject.getString("isfamily_head");

                        if (familyHeadses.size() == 0) {
                            familyHeadses.add(new FamilyHead(member_id, first_name, last_name,
                                    phone_number, aadhaar, email,
                                    address, gender, image_url, age, relationship, size, zipcode
                                    , dob, familyHeadId, isFamilyHead));
                        } else {
                            boolean bStatus = true;
                            Iterator<FamilyHead> iter = familyHeadses.iterator();
                            while (iter.hasNext()) {
                                Log.d(LOG_TAG, "============ Inside if condition iterator ============= ");
                                FamilyHead obj = iter.next();
//                                Log.d(LOG_TAG, "supervisorId >>>>>>> " + headId);
//                                Log.d(LOG_TAG, "obj.getId() >>>>>>>> " + obj.getId());
//                                Log.d(LOG_TAG, "Compraring Id >>>>>> " + headId.equals(obj.getId()));
                                if (member_id.equals(obj.getId())) {
                                    bStatus = false;
                                }
                            }
                            Log.d(LOG_TAG, "bStatus >>>> " + bStatus);
                            if (bStatus) {
//                                Log.d("SuperFragment", "************ Object Has been added successfully ************ ");
                                familyHeadses.add(new FamilyHead(member_id, first_name, last_name,
                                        phone_number, aadhaar, email,
                                        address, gender, image_url, age, relationship, size, zipcode
                                        , dob, familyHeadId, isFamilyHead));
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }else if (status.equals("success") && status_code.equals("1001")) {
                    familyHeadses.clear();
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                Log.e(LOG_TAG, "family head list result >>>>>> " + jsonException.getLocalizedMessage());
                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Failure");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("failed to fetch Family head list. Server not responding please try again after sometime.");
                TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                        finishAffinity();
                        Intent intent = new Intent(FamilyMembers.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    }
                });
                customDialog.show();
            }
        }
    }

}
