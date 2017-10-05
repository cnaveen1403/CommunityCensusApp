package com.zolipe.communitycensus.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class ReportsFragment extends Fragment {
    private final String TAG = ReportsFragment.class.getSimpleName();

    View rootView;
    PieChart pieChart;
    ValueLineChart cubiclinechart;
    List<ChartData> data;
    LinearLayout ll_no_data, ll_no_data_linechart, ll_indicator;
    TextView tv_total;
    Button btn_retry, btn_retry_linechart;
    Activity mActivity;
    Context mContext;
    String[] ITEMS = {"Overall", "Location"};
    String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    EditText et_search;
    private Spinner spinner_summary, spinner_months;

    public ReportsFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_reports, container, false);
        init();

        return rootView;
    }

    private void init() {
        mContext = this.getActivity();
        mActivity = getActivity();
        pieChart = (PieChart) rootView.findViewById(R.id.pie_total);
        cubiclinechart = (ValueLineChart) rootView.findViewById(R.id.cubiclinechart);
        tv_total = (TextView) rootView.findViewById(R.id.tv_total);
        ll_no_data = (LinearLayout) rootView.findViewById(R.id.ll_no_data);
        ll_no_data_linechart = (LinearLayout) rootView.findViewById(R.id.ll_no_data_linechart);
        ll_indicator = (LinearLayout) rootView.findViewById(R.id.ll_indicator);
        et_search = (EditText) rootView.findViewById(R.id.et_search);
        btn_retry = (Button) rootView.findViewById(R.id.btn_retry);
        btn_retry_linechart = (Button) rootView.findViewById(R.id.btn_retry_linechart);

        data = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_summary = (Spinner) rootView.findViewById(R.id.spinner_summary);
        spinner_summary.setAdapter(adapter);

        spinner_summary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    et_search.setVisibility(View.GONE);
                    new GetSummaryAsyncTask().execute();
                } else {
                    et_search.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_months = (Spinner) rootView.findViewById(R.id.spinner_months);
        spinner_months.setAdapter(monthsAdapter);

        spinner_months.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
//                int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
                int day = now.get(Calendar.DAY_OF_MONTH);
//                Log.e(TAG, "onItemSelected: position >>> " + position);
//
//                Log.e(TAG, "onItemSelected: " + getDates(year, position, day));
                new GetDayWiseSummaryAsyncTask().execute(getDates(year, position, day));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String search = et_search.getText().toString();
                    new GetSummaryByZipCodeAsyncTask().execute(search);
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
                String search = et_search.getText().toString();

                et_search.clearFocus();
//                    et_search.setText("");
                InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

                if (search.equals(""))
                    new GetSummaryAsyncTask().execute();
                else
                    new GetSummaryByZipCodeAsyncTask().execute(search);
            }
        });

        btn_retry_linechart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
//                int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
                int day = now.get(Calendar.DAY_OF_MONTH);

                int position = spinner_months.getSelectedItemPosition();

                Log.e(TAG, "onItemSelected: " + getDates(year, position, day));
                new GetDayWiseSummaryAsyncTask().execute(getDates(year, position, day));
            }
        });

        Calendar now = Calendar.getInstance();
//        int year = now.get(Calendar.YEAR);
//        int day = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH);
        spinner_months.setSelection(month);

        int position = spinner_months.getSelectedItemPosition();

//        new GetDayWiseSummaryAsyncTask().execute(getDates(year, position, day));
        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void setPieChartData(String total, String male, String female) {
        int tot = Integer.parseInt(total);
        if (tot > 0) {
            int female_percent = getPercent(tot, Integer.parseInt(female));
            int male_percent = getPercent(tot, Integer.parseInt(male));
            data.clear();
            if (female_percent != 0)
                data.add(new ChartData(female_percent + "%", female_percent, Color.WHITE, Color.parseColor("#03a9f5")));

            if (male_percent != 0)
                data.add(new ChartData(male_percent + "%", male_percent, Color.WHITE, Color.parseColor("#012b72")));

            if (male_percent == 0 && female_percent == 0) {
                pieChart.setAboutChart("0 Members");
                pieChart.setChartData(data);
                pieChart.partitionWithPercent(true);
                return;
            }

            tv_total.setText(total + " Members");
            pieChart.setChartData(data);
            pieChart.partitionWithPercent(true);
        } else {
            pieChart.setVisibility(View.GONE);
            ll_indicator.setVisibility(View.GONE);
            ll_no_data.setVisibility(View.VISIBLE);
            ((TextView) rootView.findViewById(R.id.tv_error)).setText("No data Available.");
        }
    }

    private int getPercent(float tot, int value) {
        return getRoundedValue((value * 100) / tot, 0);
    }

    public int getRoundedValue(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return Math.round(bd.floatValue());
    }

    public String getDates(int year, int month, int day) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        month++;
        int minDate = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        String beginning = year + "-" + month + "-" + minDate;
        String end = year + "-" + month + "-" + maxDate;

        return beginning + "," + end;
    }

    private void prepareLineChart(JSONArray jsonArray) throws JSONException {

        ValueLineChart mCubicValueLineChart = cubiclinechart;

        ValueLineSeries series = new ValueLineSeries();
        series.setColor(0xFF56B7F1);

        final Calendar calendar = Calendar.getInstance();
        int minDate = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDate = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = minDate; i < maxDate; i++) {
            if (!isDataPresentForTheDay(jsonArray, i, series)) {
                series.addPoint(new ValueLinePoint("" + i, 0.0f));
            }
        }

        mCubicValueLineChart.addSeries(series);
        mCubicValueLineChart.startAnimation();
    }

    private boolean isDataPresentForTheDay(JSONArray jsonArray, int date, ValueLineSeries series) throws JSONException {

        boolean isDayDataPresent = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject explrObject = jsonArray.getJSONObject(i);
            String count = explrObject.getString("member_count");
            String day = explrObject.getString("day");
            if (date == Integer.parseInt(day)) {
                isDayDataPresent = true;
                series.addPoint(new ValueLinePoint("" + date, Float.valueOf(count)));
            }
        }

        return isDayDataPresent;
    }

    public class GetSummaryAsyncTask extends AsyncTask<Void, Void, String> {
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
        protected String doInBackground(Void... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.GET_SUMMARY_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();
            Log.e(TAG, "on postexecute result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                String response = jsonObject.getString("response");

                if (status.equals("success") && status_code.equals("1003")) {
                    pieChart.setVisibility(View.GONE);
                    ll_indicator.setVisibility(View.GONE);
                    ll_no_data.setVisibility(View.GONE);
                    ((TextView) rootView.findViewById(R.id.tv_error)).setText(response);
                } else if (status.equals("success") && status_code.equals("1000")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    pieChart.setVisibility(View.VISIBLE);
                    ll_indicator.setVisibility(View.VISIBLE);
                    ll_no_data.setVisibility(View.GONE);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        String total = explrObject.getString("member_count");
                        String female = explrObject.getString("female_count");
                        String male = explrObject.getString("male_count");
                        setPieChartData(total, male, female);
                    }
                } else if (status.equals("success") && status_code.equals("1001")) {
                    pieChart.setVisibility(View.GONE);
                    ll_indicator.setVisibility(View.GONE);
                    ll_no_data.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error)).setText("No data Available.");
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                pieChart.setVisibility(View.GONE);
                ll_indicator.setVisibility(View.GONE);
                ll_no_data.setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.tv_error)).setText("Server not Responding, please try again after sometime.");
            }
        }
    }

    private class GetSummaryByZipCodeAsyncTask extends AsyncTask<String, Void, String> {
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
            String searchText = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(CensusConstants.search_text, searchText));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.GET_SUMMARY_BY_ZIPCODE_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();
            Log.e(TAG, "on postexecute result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                String response = jsonObject.getString("response");

                if (status.equals("success") && status_code.equals("1003")) {
                    pieChart.setVisibility(View.GONE);
                    ll_indicator.setVisibility(View.GONE);
                    ll_no_data.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error)).setText(response);
                } else if (status.equals("success") && status_code.equals("1000")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    pieChart.setVisibility(View.VISIBLE);
                    ll_indicator.setVisibility(View.VISIBLE);
                    ll_no_data.setVisibility(View.GONE);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject explrObject = jsonArray.getJSONObject(i);
                        String total = explrObject.getString("member_count");
                        String female = explrObject.getString("female_count");
                        String male = explrObject.getString("male_count");
                        setPieChartData(total, male, female);
                    }
                } else if (status.equals("success") && status_code.equals("1001")) {
                    pieChart.setVisibility(View.GONE);
                    ll_indicator.setVisibility(View.GONE);
                    ll_no_data.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error)).setText("No data Available.");
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                pieChart.setVisibility(View.GONE);
                ll_indicator.setVisibility(View.GONE);
                ll_no_data.setVisibility(View.GONE);
                ((TextView) rootView.findViewById(R.id.tv_error)).setText("Server not Responding, please try again after sometime.");
            }
        }
    }

    private class GetDayWiseSummaryAsyncTask extends AsyncTask<String, Void, String> {
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
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            String[] dates = params[0].split(",");
            String fromdate = dates[0];
            String todate = dates[1];
//            Log.e(TAG, "doInBackground: fromdate >> " + fromdate);
//            Log.e(TAG, "doInBackground: todate >> " + todate);
            parms.add(new BasicNameValuePair("fromdate", fromdate));
            parms.add(new BasicNameValuePair("todate", todate));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.GET_SUMMARY_DAYWISE_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();
            Log.e(TAG, "on postexecute result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                String response = jsonObject.getString("response");

                if (status.equals("success") && status_code.equals("1000")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    cubiclinechart.setVisibility(View.VISIBLE);
                    ll_no_data_linechart.setVisibility(View.GONE);
                    prepareLineChart(jsonArray);
                } else if (status.equals("success") && status_code.equals("1001")) {
                    cubiclinechart.setVisibility(View.GONE);
                    ll_no_data_linechart.setVisibility(View.VISIBLE);
                    ((TextView) rootView.findViewById(R.id.tv_error_linechart)).setText("No data Available.");
                } else if (status.equals("success") && status_code.equals("1003")) {
                    cubiclinechart.setVisibility(View.GONE);
                    ll_no_data_linechart.setVisibility(View.GONE);
                    ((TextView) rootView.findViewById(R.id.tv_error_linechart)).setText(response);
                } else {
                    cubiclinechart.setVisibility(View.GONE);
                    ll_no_data_linechart.setVisibility(View.GONE);
                    ((TextView) rootView.findViewById(R.id.tv_error_linechart)).setText(response);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                cubiclinechart.setVisibility(View.GONE);
                ll_no_data_linechart.setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.tv_error_linechart)).setText("Server not Responding, please try again after sometime.");
            }
        }
    }
}
