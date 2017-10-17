package com.zolipe.communitycensus.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.activity.FamilyDetailsActivity;
import com.zolipe.communitycensus.adapter.FamilyHeadAdapter;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.interfaces.FamilyHeadsListItemClickListener;
import com.zolipe.communitycensus.model.FamilyHead;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.CommonUtils;
import com.zolipe.communitycensus.util.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FamilyHeadsFragment extends Fragment implements FamilyHeadsListItemClickListener {

    private String TAG = FamilyHeadsFragment.class.getSimpleName();

    private List<FamilyHead> headsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FamilyHeadAdapter mAdapter;

    private EditText et_search;
    View rootView;
    Context mContext;
    Activity mActivity;
    Button btn_retry;
    RelativeLayout rl_data, rl_error;
    String mUserRole;

    public FamilyHeadsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_family_heads, container, false);

        mContext = this.getActivity();
        mActivity = getActivity();

        mUserRole = AppData.getString(mContext, CensusConstants.userRole);

        if (CommonUtils.isActiveNetwork(mContext)) {
            if(mUserRole.equals("admin"))
                getFamilyHeads();

            if (mUserRole.equals("supervisor"))
                getFamilyHeadsForSupervisor();

            new CommonUtils.GetFamilyHeadsListAsyncTask(mContext).execute("");
        } else {
            getFamilyHeads();
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        rl_data = (RelativeLayout) rootView.findViewById(R.id.rl_data);
        rl_error = (RelativeLayout) rootView.findViewById(R.id.rl_error);
        btn_retry = (Button) rootView.findViewById(R.id.btn_error_supervisor_list);

        mAdapter = new FamilyHeadAdapter(headsList, mContext);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(this);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        et_search = (EditText) rootView.findViewById(R.id.et_search);

        et_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });

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
                    if (CommonUtils.isActiveNetwork(mContext)) {
                        new CommonUtils.GetFamilyHeadsListAsyncTask(mContext).execute(search);
                        getFamilyHeads();
                    } else {
                        getFamilyHeads();
                    }
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
                    getFamilyHeads();
                    new CommonUtils.GetFamilyHeadsListAsyncTask(mContext).execute("");
                } else {
                    getFamilyHeads();
                }
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (CommonUtils.isActiveNetwork(mContext)) {
            getFamilyHeads();
            new CommonUtils.GetFamilyHeadsListAsyncTask(mContext).execute("");
        } else {
            getFamilyHeads();
        }
    }

    @Override
    public void onFamilyHeadClicked(View view, FamilyHead obj) {
        Intent intent = new Intent(mContext, FamilyDetailsActivity.class);
        intent.putExtra("family_head", obj);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void getFamilyHeads(){
        if(mUserRole.equals("admin"))
            getFamilyHeadsForAdmin();

        if (mUserRole.equals("supervisor"))
            getFamilyHeadsForSupervisor();
    }

    private void getFamilyHeadsForAdmin() {
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();
        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add("yes");
        dbParams.addParamterList(parms);

        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_family_heads,
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

                if (cur.getCount() == 0){
                    rl_data.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error_lbl)).setText("No data found, Please add Family Members.");
                }

                if (cur.moveToFirst()) {
                    rl_error.setVisibility(View.GONE);
                    rl_data.setVisibility(View.VISIBLE);
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

                            if (headsList.size() == 0) {
                                headsList.add(new FamilyHead(first_name, last_name,
                                        phone_number, aadhaar, email,
                                        address, gender, image_url, age, relationship, size, zipcode
                                        , dob, familyHeadId, isFamilyHead, isSynced,
                                        state_id, city_id));
                            } else {
                                boolean bStatus = true;
                                Iterator<FamilyHead> iter = headsList.iterator();
                                while (iter.hasNext()) {
                                    Log.d(TAG, "============ Inside if condition iterator ============= ");
                                    FamilyHead obj = iter.next();
                                    if (aadhaar.equals(obj.getAadhaar())) {
                                        bStatus = false;
                                    }
                                }
                                Log.d(TAG, "bStatus >>>> " + bStatus);
                                if (bStatus) {
                                    headsList.add(new FamilyHead(first_name, last_name,
                                            phone_number, aadhaar, email,
                                            address, gender, image_url, age, relationship, size, zipcode
                                            , dob, familyHeadId, isFamilyHead, isSynced,
                                            state_id, city_id));
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

    private void getFamilyHeadsForSupervisor() {
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();
        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add("yes");
        parms.add(AppData.getString(mContext, CensusConstants.userid));
        dbParams.addParamterList(parms);

        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_family_heads_for_supervisor,
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

                if (cur.getCount() == 0){
                    rl_data.setVisibility(View.GONE);
                    rl_error.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error_lbl)).setText("No data found, Please add Family Members.");
                }

                if (cur.moveToFirst()) {
                    rl_error.setVisibility(View.GONE);
                    rl_data.setVisibility(View.VISIBLE);
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
                            String state_id = cur.getString(cur.getColumnIndex("state_id"));
                            String city_id = cur.getString(cur.getColumnIndex("city_id"));

                            if (headsList.size() == 0) {
                                headsList.add(new FamilyHead(first_name, last_name,
                                        phone_number, aadhaar, email,
                                        address, gender, image_url, age, relationship, size, zipcode
                                        , dob, familyHeadId, isFamilyHead, isSynced, state_id, city_id));
                            } else {
                                boolean bStatus = true;
                                Iterator<FamilyHead> iter = headsList.iterator();
                                while (iter.hasNext()) {
                                    FamilyHead obj = iter.next();
                                    if (aadhaar.equals(obj.getAadhaar())) {
                                        bStatus = false;
                                    }
                                }
                                if (bStatus) {
                                    headsList.add(new FamilyHead(first_name, last_name,
                                            phone_number, aadhaar, email,
                                            address, gender, image_url, age, relationship, size, zipcode
                                            , dob, familyHeadId, isFamilyHead, isSynced, state_id, city_id));
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
