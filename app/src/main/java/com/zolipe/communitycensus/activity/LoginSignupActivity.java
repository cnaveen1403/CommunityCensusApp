package com.zolipe.communitycensus.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.database.GDatabaseHelper;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LoginSignupActivity extends Activity {

    private static final String TAG = LoginSignupActivity.class.getSimpleName();

    AccessToken accessToken = AccountKit.getCurrentAccessToken();
    public static int APP_REQUEST_CODE = 99;
    Button btn_login, btn_signup, mPhoneSignInButton;
    Context mContext;
    EditText et_username, et_password;
    TextView tv_admin_login_hint, tv_member_login_hint, tv_member_supervisor;
    RelativeLayout rl_member_login, rl_admin_login;
    Animation animation;
    String mUserRole = "supervisor";

    private static final String[] WHITE_LIST = new String[]{
            "IN"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = LoginSignupActivity.this;
        GDatabaseHelper mDatabaseHelper = new GDatabaseHelper(LoginSignupActivity.this);
        mDatabaseHelper.getInstance(LoginSignupActivity.this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login_signup);

        btn_login = (Button) findViewById(R.id.btn_login);
        rl_member_login = (RelativeLayout) findViewById(R.id.rl_member_login);
        rl_admin_login = (RelativeLayout) findViewById(R.id.rl_admin_login);
        tv_admin_login_hint = (TextView) findViewById(R.id.tv_admin_login_hint);
        tv_member_login_hint = (TextView) findViewById(R.id.tv_member_login_hint);
        tv_member_supervisor = (TextView) findViewById(R.id.tv_member_supervisor);
//        tv_member_login = (TextView) findViewById(R.id.tv_member_login);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        rl_member_login.setVisibility(View.VISIBLE);
        mPhoneSignInButton = (Button) findViewById(R.id.account_kit_button_login);
//        btn_signup = (Button) findViewById(R.id.sign_up_btn);
//        btn_signup.setVisibility(View.GONE);

        //set position TranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta
        animation = new TranslateAnimation(0, 0, 300, 0);
        // set Animation for 0.5 sec
        animation.setDuration(500);
        //for button stops in the new position.
        animation.setFillAfter(true);
        mPhoneSignInButton.startAnimation(animation);
        tv_member_login_hint.startAnimation(animation);
//        btn_signup.startAnimation(animation);

        /*btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignupActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
*/

        try {
            if (AppData.getBoolean(mContext, CensusConstants.isLoggedIn)) {
                finishAffinity();
                Intent intent = new Intent(LoginSignupActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*tv_member_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_member_login.setPaintFlags(tv_member_login.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                String loginText = tv_member_login.getText().toString();
                if (loginText.equals(getResources().getString(R.string.lbl_member_login_text))) {
                    mUserRole = "member";
                    tv_member_login.setText(getResources().getString(R.string.lbl_supervisor_login_text));
                    tv_member_supervisor.setText("You are logging in as Member");
                    memberLoginUIenable();
                } else if (loginText.equals(getResources().getString(R.string.lbl_supervisor_login_text))) {
                    mUserRole = "supervisor";
                    tv_member_login.setText(getResources().getString(R.string.lbl_member_login_text));
                    tv_member_supervisor.setText("You are logging in as Supervisor");
                    memberLoginUIenable();
                }
            }
        });*/

        tv_admin_login_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memberLoginUIenable();
            }
        });

        tv_member_login_hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserRole = "admin";
                adminLoginUIenable();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidParams()) {
                    new AdminLoginAsyncTask().execute();
                }
            }
        });


        mPhoneSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneLogin(view);
            }
        });

        if (accessToken != null) {
            //Handle Returning User
        } else {
            //Handle new or logged out user
        }

        final Button supervisor_login = (Button)findViewById(R.id.btn_supervisor_login);
        final Button member_login = (Button)findViewById(R.id.btn_member_login);

        View.OnClickListener topButtonsListener  = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_supervisor_login){
                    supervisor_login.setSelected(true);
                    supervisor_login.setTextColor(getResources().getColor(R.color.white));
                    member_login.setTextColor(getResources().getColor(R.color.hint_color));
                    member_login.setSelected(false);

                    mUserRole = "supervisor";
                    tv_member_supervisor.setText("You are logging in as Supervisor");
                }
                else{
                    supervisor_login.setSelected(false);
                    member_login.setSelected(true);
                    supervisor_login.setTextColor(getResources().getColor(R.color.hint_color));
                    member_login.setTextColor(getResources().getColor(R.color.white));

                    mUserRole = "member";
                    tv_member_supervisor.setText("You are logging in as Member");
                }
            }
        };

        supervisor_login.setOnClickListener(topButtonsListener);
        member_login.setOnClickListener(topButtonsListener);
        supervisor_login.performClick();
    }

    public void phoneLogin(final View view) {
        final Intent intent = new Intent(LoginSignupActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        configurationBuilder.setReceiveSMS(true);
        configurationBuilder.setSMSWhitelist(WHITE_LIST);
        configurationBuilder.setDefaultCountryCode("IN");
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
//        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = "Success: 123 " + loginResult.getAccessToken().getAccountId();
                } else {
                    toastMessage = String.format(
                            "Success:9999 %s...",
                            loginResult.getAuthorizationCode().substring(0, 10));
                    goToMyLoggedInActivity();
                }

                // If you have an authorization code, retrieve it from
                // loginResult.getAuthorizationCode()
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...
                goToMyLoggedInActivity();
            }

            // Surface the result to your user in an appropriate way.
//            Toast.makeText(this,toastMessage,Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorActivity(AccountKitError error) {
        String message = error.getUserFacingMessage();
        Intent intent = new Intent(LoginSignupActivity.this, ErrorActivity.class);
        intent.putExtra("error_message", message);
        startActivity(intent);
    }

    private void goToMyLoggedInActivity() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                // Get Account Kit ID
//                String accountKitId = account.getId();

                // Get phone number
                PhoneNumber phoneNumber = account.getPhoneNumber();
                String phoneNumberString = phoneNumber.toString();
                Log.e(TAG, "onSuccess: phoneNumberString >>> " + phoneNumberString);
                // Get email
//                String email = account.getEmail();

                afterLoginSuccess(phoneNumberString);
            }

            @Override
            public void onError(final AccountKitError error) {
                // Handle Error
                Log.e(TAG, "onError: INSIDE ERROR ");
            }
        });
    }

    private boolean isValidParams() {
        boolean bStatus = true;
        if (getUserName().equals("")) {
            bStatus = false;
            et_username.setError("Please enter username");
        } else if (getPassword().equals("")) {
            bStatus = false;
            et_password.setError("Please enter password");
        }

        return bStatus;
    }

    private String getUserName() {
        return et_username.getText().toString();
    }

    private String getPassword() {
        return et_password.getText().toString();
    }

    private void memberLoginUIenable() {
        rl_admin_login.clearAnimation();
        tv_admin_login_hint.clearAnimation();

        rl_admin_login.setVisibility(View.GONE);
        rl_member_login.setVisibility(View.VISIBLE);

        rl_member_login.startAnimation(animation);
        tv_member_login_hint.startAnimation(animation);
    }

    private void adminLoginUIenable() {
        rl_member_login.clearAnimation();
        tv_member_login_hint.clearAnimation();

        rl_member_login.setVisibility(View.GONE);
        rl_admin_login.setVisibility(View.VISIBLE);

        rl_admin_login.startAnimation(animation);
        tv_admin_login_hint.startAnimation(animation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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

    private void afterLoginSuccess(String phoneNumber) {
        // TODO : submit the credentials to server and validate the user
        if (ConnectToServer.isNetworkAvailable(mContext)) {
            new LoginTask().execute(CensusConstants.loginType, "digits", phoneNumber);
        } else {
            Toast.makeText(mContext, getString(R.string.no_network), Toast.LENGTH_LONG).show();
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
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
            String login_value = params[1];
            String phoneNum = params[2];
//Log.e(TAG, "phoneNum >> " + phoneNum.substring(1));
            parms.add(new BasicNameValuePair(params[0], params[1]));
            parms.add(new BasicNameValuePair(login_value, phoneNum.substring(1)));
            parms.add(new BasicNameValuePair(CensusConstants.userRole, mUserRole));

            //Print Full URL
            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.LOGIN_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.LOGIN_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.e(TAG, "result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                String status = jsonObject.getString(CensusConstants.STATUS);

                if (status.equals("error")) {
//                    Digits.logout();
                    String response = jsonObject.getString("response");
                    final Dialog dialog = new Dialog(LoginSignupActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.simple_alert);
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
                    // set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else if (status.equals("success")) {
                    String statusCode = jsonObject.getString(CensusConstants.STATUS_CODE);

                    if (statusCode.equals("1001")) {
//                        Digits.logout();
                        String response = jsonObject.getString("response");
                        final Dialog dialog = new Dialog(LoginSignupActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    } else if (statusCode.equals("1000")) {
//                        Digits.logout();

                        JSONArray jsonArrayProfileDetails = jsonObject.getJSONArray("profile_details");
                        JSONObject jsonProfileObj = jsonArrayProfileDetails.getJSONObject(0);

                        AppData.saveBoolean(mContext, CensusConstants.isLoggedIn, true);

                        saveUserProfileDetails(jsonProfileObj);

                        finishAffinity();
                        Intent intent = new Intent(LoginSignupActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                final Dialog dialog = new Dialog(LoginSignupActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.simple_alert);
                ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Something gone wrong please try again after sometime !!!");
                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }

    public void saveUserProfileDetails(JSONObject jsonObject) {
        for (Iterator<String> iter = jsonObject.keys(); iter.hasNext(); ) {
            String key = iter.next();
            try {
                //Save each key value pair received from the Server
//                Log.e(TAG, "key >> " + key + " >> value >> " + jsonObject.getString(key));
                if (key.equals("dob")) {
                    String date = jsonObject.getString(key);
                    String[] array = date.split("-");
                    String formattedDate = array[2] + "-" + array[1] + "-" + array[0];
                    Log.e(TAG, "saveUserProfileDetails: new date " + formattedDate);
                    AppData.saveString(LoginSignupActivity.this, key, formattedDate);
                } else {
                    AppData.saveString(LoginSignupActivity.this, key, jsonObject.getString(key));
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }

    private class AdminLoginAsyncTask extends AsyncTask<Void, Void, String> {
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
            parms.add(new BasicNameValuePair("username", getUserName()));
            parms.add(new BasicNameValuePair("password", getPassword()));

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.ADMIN_LOGIN_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.ADMIN_LOGIN_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            Log.d(TAG, "result >>> " + result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                String status = jsonObject.getString(CensusConstants.STATUS);

                if (status.equals("error")) {
                    String response = jsonObject.getString("response");
                    final Dialog dialog = new Dialog(LoginSignupActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.simple_alert);
                    ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                    ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
                    // set the custom dialog components - text, image and button
                    TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else if (status.equals("success")) {
                    String statusCode = jsonObject.getString(CensusConstants.STATUS_CODE);

                    if (statusCode.equals("1001")) {
//                        Digits.logout();
                        String response = jsonObject.getString("response");
                        final Dialog dialog = new Dialog(LoginSignupActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.simple_alert);
                        ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) dialog.findViewById(R.id.dialogMessage)).setText(response);
                        // set the custom dialog components - text, image and button
                        TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    } else if (statusCode.equals("1000")) {
                        JSONArray jsonArrayProfileDetails = jsonObject.getJSONArray("profile_details");
                        JSONObject jsonProfileObj = jsonArrayProfileDetails.getJSONObject(0);
                        AppData.saveBoolean(mContext, CensusConstants.isLoggedIn, true);

                        saveUserProfileDetails(jsonProfileObj);

                        finishAffinity();
                        Intent intent = new Intent(LoginSignupActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                final Dialog dialog = new Dialog(LoginSignupActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.simple_alert);
                ((TextView) dialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                ((TextView) dialog.findViewById(R.id.dialogMessage)).setText("Something gone wrong please try again after sometime !!!");
                // set the custom dialog components - text, image and button
                TextView text = (TextView) dialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }
}
