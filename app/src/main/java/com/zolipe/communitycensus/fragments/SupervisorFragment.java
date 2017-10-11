package com.zolipe.communitycensus.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.activity.SupervisorDetail;
import com.zolipe.communitycensus.adapter.SupervisorAdapter;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.interfaces.SupervisorListItemClickListener;
import com.zolipe.communitycensus.model.SupervisorObj;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.CommonUtils;
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

public class SupervisorFragment extends Fragment implements SupervisorListItemClickListener {

    private String TAG = "SupervisorFragment";

    private List<SupervisorObj> supervisorList = new ArrayList<>();
    private RecyclerView recyclerView;

    private SupervisorAdapter mAdapter;
    private EditText et_search;
    Context mContext;
    Activity mActivity;
    View rootView;
    Button btn_retry;
    RelativeLayout rl_data, rl_error;

    public SupervisorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = this.getActivity();
        mActivity = getActivity();
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_supervisor, container, false);

        et_search = (EditText) rootView.findViewById(R.id.et_search);
        btn_retry = (Button) rootView.findViewById(R.id.btn_error_supervisor_list);
        rl_data = (RelativeLayout) rootView.findViewById(R.id.rl_data);
        rl_error = (RelativeLayout) rootView.findViewById(R.id.rl_error);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mAdapter = new SupervisorAdapter(supervisorList, mContext);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(this);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text [" + s + "]");
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

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String search = et_search.getText().toString();
                    prepareSupervisorList(search);
                    et_search.clearFocus();
//                    et_search.setText("");
                    InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isActiveNetwork(mContext)) {
                    prepareSupervisorList("");
                } else {
                    showSupervisors();
                }
            }
        });

        if (CommonUtils.isActiveNetwork(mContext))
            prepareSupervisorList("");
        else
            showSupervisors();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return rootView;
    }

    private void prepareSupervisorList(String search) {
        new GetSupervisorListAsyncTask().execute(search);
    }

    @Override
    public void onClick(View view, SupervisorObj obj) {
        Intent intent = new Intent(mContext, SupervisorDetail.class);
        intent.putExtra("SupervisorObj", obj);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public class GetSupervisorListAsyncTask extends AsyncTask<String, Void, String> {
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
            String search = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair("search_text", search));
            parms.add(new BasicNameValuePair(CensusConstants.userid, AppData.getString(mContext, CensusConstants.userid)));
//            parms.add(new BasicNameValuePair(CensusConstants.phoneNumber, search));
//            parms.add(new BasicNameValuePair(CensusConstants.firstName, search));
            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.GET_SUPERVISOR_LIST_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.GET_SUPERVISOR_LIST_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mActivity);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            Log.d(TAG, "on post execute result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                final String response = jsonObject.getString("response");

                if (status.equals("success") && status_code.equals("1003")) {
                    rl_data.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error_lbl)).setText(response);
                } else if (status.equals("success") && status_code.equals("1000")) {
                    CommonUtils.saveSupervisorsToLocalDB(mContext, jsonObject);
                } else if (status.equals("success") && status_code.equals("1001")) {
                    rl_data.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error_lbl)).setText("No data found, Please add supervisors.");
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                rl_data.setVisibility(View.GONE);
                rl_error.setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.tv_error_lbl)).setText("Server not Responding, please try again after sometime.");
            }

            progressDialog.dismiss();
            showSupervisors();
        }
    }

    private void showSupervisors() {

        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();
        Log.e(TAG, "showSupervisors: inside the offline fetch");
        ArrayList<Object> parms = new ArrayList<Object>();
        dbParams.addParamterList(parms);

        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_all_supervisors,
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

                if (cur.getCount() == 0) {
                    rl_data.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error_lbl)).setText("No data found, Please add supervisors.");
                }

                if (cur.moveToFirst()) {
                    rl_data.setVisibility(View.VISIBLE);
                    rl_error.setVisibility(View.GONE);

                    do {
                        try {
                            String sup_id = cur.getString(cur.getColumnIndex("aadhaar"));
                            String first_name = cur.getString(cur.getColumnIndex("first_name"));
                            String last_name = cur.getString(cur.getColumnIndex("last_name"));
                            String phone_number = cur.getString(cur.getColumnIndex("phone_number"));
                            String aadhaar = cur.getString(cur.getColumnIndex("aadhaar"));
                            String email = cur.getString(cur.getColumnIndex("email"));
                            String address = cur.getString(cur.getColumnIndex("address"));
                            String gender = cur.getString(cur.getColumnIndex("gender"));
                            String image_url = cur.getString(cur.getColumnIndex("image_url"));
                            String age = cur.getString(cur.getColumnIndex("age"));
                            String count = cur.getString(cur.getColumnIndex("member_count"));
                            String zipcode = cur.getString(cur.getColumnIndex("zipcode"));
                            String dob = cur.getString(cur.getColumnIndex("dob"));
                            String isSynced = cur.getString(cur.getColumnIndex("isSynced"));

                            if (supervisorList.size() == 0) {
                                supervisorList.add(new SupervisorObj(sup_id, first_name, last_name,
                                        phone_number, aadhaar, email, address,
                                        age, gender, image_url, zipcode, count, dob, isSynced));
                            } else {
                                boolean bStatus = true;
                                Iterator<SupervisorObj> iter = supervisorList.iterator();
                                while (iter.hasNext()) {
                                    Log.d(TAG, "============ Inside if condition iterator ============= ");
                                    SupervisorObj obj = iter.next();
                                    //Check if the List has Supervisor with same aadhar
                                    if (aadhaar.equals(obj.getAadhaar())) {
                                        bStatus = false;
                                    }
                                }
                                Log.d(TAG, "bStatus >>>> " + bStatus);
                                if (bStatus) {
//                                Log.d("SuperFragment", "************ Object Has been added successfully ************ ");
                                    supervisorList.add(new SupervisorObj(sup_id, first_name, last_name,
                                            phone_number, aadhaar, email, address,
                                            age, gender, image_url, zipcode, count, dob, isSynced));
                                }
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    while (cur.moveToNext());
                    mAdapter.notifyDataSetChanged();
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
}
