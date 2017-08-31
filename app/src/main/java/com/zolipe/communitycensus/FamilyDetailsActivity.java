package com.zolipe.communitycensus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import com.zolipe.communitycensus.adapter.HorizantalListAdapter;
import com.zolipe.communitycensus.model.FamilyHead;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.HorizontalListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FamilyDetailsActivity extends AppCompatActivity {

    private Context mContext;
    ArrayList<FamilyHead> familyHeadsList = new ArrayList<>();
    HorizontalListView familyMembersList;
    Button btn_add_member, btn_retry;
    String member_id;
    ScrollView sv_head_detail;
    RelativeLayout rl_family_members, rl_error;
    TextView tv_no_data;
    private String TAG = "FamilyDetails";
    private String familyHeadName;
    private String familyHeadImgUrl;
    private String mFamilyHeadId;
    CircleImageView iv_header_image;

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
        btn_add_member = (Button)findViewById(R.id.btn_add_member);
        btn_retry = (Button)findViewById(R.id.btn_retry);
        sv_head_detail = (ScrollView)findViewById(R.id.sv_head_detail);
        rl_family_members = (RelativeLayout)findViewById(R.id.rl_family_members);
        rl_error = (RelativeLayout)findViewById(R.id.rl_error);
        tv_no_data = (TextView)findViewById(R.id.tv_no_data);
        iv_header_image = (CircleImageView) findViewById(R.id.iv_header_image);

        familyMembersList = (HorizontalListView) findViewById(R.id.membersList);

        if (getIntent().getExtras() != null) {
            member_id = getIntent().getExtras().getString("member_id");
        }

        new GetFamilyDetailsAsyncTask().execute(member_id);
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
                intent.putExtra("member_id", mFamilyHeadId);
                intent.putExtra("member_name", familyHeadName);
                intent.putExtra("member_url", familyHeadImgUrl);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetFamilyDetailsAsyncTask().execute(member_id);
            }
        });

        iv_header_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog nagDialog = new Dialog(mContext ,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(false);
                nagDialog.setContentView(R.layout.preview_image);
                Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
                ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
                Glide.with(mContext).load(familyHeadImgUrl)
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                member_id = data.getStringExtra("member_id");
                new GetFamilyDetailsAsyncTask().execute(member_id);
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
            String member_id = params[0];
            parms.add(new BasicNameValuePair(CensusConstants.member_id, member_id));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.FAMILY_HEADS_LIST_WITH_FAMILY_MEMBERS_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
//            Log.e("FamilyDetailsActivity", "result.isEmpty() >> " + result.isEmpty());
            if (result.equals("") || result.equals(null) || result.isEmpty()){
                showServerError();
            }else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.getString("status");
                    String status_code = jsonObject.getString("status_code");
                    String response = jsonObject.getString("response");

                    if (status.equals("error") && status_code.equals("1003")) {
                        sv_head_detail.setVisibility(View.GONE);
                        rl_family_members.setVisibility(View.GONE);
                        rl_error.setVisibility(View.VISIBLE);
                        tv_no_data.setText(response);
                    } else if (status.equals("success") && status_code.equals("1000")) {
                        familyHeadsList.clear();
                        sv_head_detail.setVisibility(View.VISIBLE);
                        rl_family_members.setVisibility(View.VISIBLE);
                        rl_error.setVisibility(View.GONE);
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject explrObject = jsonArray.getJSONObject(i);
                            String memberId = explrObject.getString("member_id");
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

                            if (isFamilyHead.equalsIgnoreCase("yes")) {
                                mFamilyHeadId = memberId;
                                familyHeadName = first_name + " " + last_name;
                                familyHeadImgUrl = image_url;
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
                                familyHeadsList.add(new FamilyHead(memberId, first_name, last_name,
                                        phone_number, aadhaar, email, address, gender, image_url,
                                        age, relationship, size, zipcode, dob, familyHeadId, isFamilyHead));
                            }
                        }
                        familyMembersList.setAdapter(new HorizantalListAdapter(FamilyDetailsActivity.this, familyHeadsList));
                    } else if (status.equals("success") && status_code.equals("1001")) {
                        sv_head_detail.setVisibility(View.GONE);
                        rl_family_members.setVisibility(View.GONE);
                        rl_error.setVisibility(View.VISIBLE);
                        tv_no_data.setText("Data Not available, Please add Members.");
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                    showServerError();
                }
            }
        }
    }

    private void showServerError() {
        sv_head_detail.setVisibility(View.GONE);
        rl_family_members.setVisibility(View.GONE);
        rl_error.setVisibility(View.VISIBLE);
        tv_no_data.setText("Server Not Responding !!! Please try again later.");
    }
}
