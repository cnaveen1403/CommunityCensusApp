package com.zolipe.communitycensus.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.AddFamilyMember;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.ViewMember;
import com.zolipe.communitycensus.adapter.HorizantalListAdapter;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.model.FamilyHead;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.HorizontalListView;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private String mFamilyHeadId;

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

        new GetFamilyDetailsAsyncTask().execute(AppData.getString(mContext, CensusConstants.rolebased_user_id));
        familyMembersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FamilyHead tempObj = familyHeadsList.get(position);
                Intent intent = new Intent(mContext, ViewMember.class);
                intent.putExtra("Member", tempObj);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                //showMember (tempObj);
            }
        });

        btn_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AddFamilyMember.class);
                String name = AppData.getString(mContext, CensusConstants.firstName) + " " + AppData.getString(mContext, CensusConstants.lastName);
                intent.putExtra("member_id", mFamilyHeadId);
                intent.putExtra("member_name", name);
                intent.putExtra("member_url", AppData.getString(mContext, CensusConstants.image_url));
                startActivityForResult(intent, 1);
                mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetFamilyDetailsAsyncTask().execute(AppData.getString(mContext, CensusConstants.rolebased_user_id));
            }
        });

        return rootView;
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
                    sv_head_detail.setVisibility(View.GONE);
                    rl_family_members.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_no_data)).setText(response);
                } else if (status.equals("success") && status_code.equals("1000")) {
                    sv_head_detail.setVisibility(View.VISIBLE);
                    rl_family_members.setVisibility(View.VISIBLE);
                    rl_error.setVisibility(View.GONE);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        String headId = explrObject.getString("member_id");
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

                        if(isFamilyHead.equalsIgnoreCase("yes")){
                            mFamilyHeadId = headId;
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
                                    .placeholder(R.drawable.app_icon)
                                    .into((ImageView)rootView.findViewById(R.id.iv_header_image));
                        }else{
                            familyHeadsList.add(new FamilyHead(headId, first_name, last_name,
                                    phone_number, aadhaar, email,
                                    address, gender, image_url, age, relationship, size, zipcode
                                    , dob, familyHeadId, isFamilyHead));
                        }
                    }
                    familyMembersList.setAdapter(new HorizantalListAdapter(mContext, familyHeadsList));
                } else if (status.equals("success") && status_code.equals("1001")) {
                    sv_head_detail.setVisibility(View.GONE);
                    rl_family_members.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    tv_no_data.setText("Data Not available, Please add Members.");
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                sv_head_detail.setVisibility(View.GONE);
                rl_family_members.setVisibility(View.GONE);
                rl_error.setVisibility(View.VISIBLE);
                tv_no_data.setText("Server Not Responding !!! Please try again later.");
            }
        }
    }
}
