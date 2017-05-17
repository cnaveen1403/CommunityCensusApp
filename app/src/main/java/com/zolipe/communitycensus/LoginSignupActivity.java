package com.zolipe.communitycensus;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

import static com.zolipe.communitycensus.util.CensusConstants.TWITTER_KEY;
import static com.zolipe.communitycensus.util.CensusConstants.TWITTER_SECRET;

public class LoginSignupActivity extends Activity {

    private static final String LOG_TAG = LoginSignupActivity.class.getSimpleName();
    Button btn_login, btn_signup;
    Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = LoginSignupActivity.this;
        /*TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().withTheme(R.style.CustomDigitsTheme).build());*/

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY,
                TWITTER_SECRET);
        Digits.Builder digitsBuilder = new Digits.Builder().withTheme(R.style.CustomDigitsTheme);
        Fabric.with(this, new TwitterCore(authConfig), digitsBuilder.build());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login_signup);

        btn_login = (Button)findViewById(R.id.auth_button_login) ;
        btn_signup = (Button)findViewById(R.id.sign_up_btn) ;

        //set position TranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta
        final Animation animation = new TranslateAnimation(0,0,300,0);
        // set Animation for 0.5 sec
        animation.setDuration(1000);
        //for button stops in the new position.
        animation.setFillAfter(true);
        btn_login.startAnimation(animation);
        btn_signup.startAnimation(animation);

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginSignupActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button_login);
        digitsButton.setText(R.string.login_digits_label);
        digitsButton.setTextSize(17);
        digitsButton.setTextColor(getResources().getColor(R.color.white));
        digitsButton.setBackgroundResource(R.drawable.button_bg);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // TODO: associate the session userID with your user model
                /*Toast.makeText(getApplicationContext(), "Authentication successful for "
                        + phoneNumber, Toast.LENGTH_LONG).show();*/
//                Digits.clearActiveSession();
                afterLoginSuccess (phoneNumber);
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
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
        String phoneNum;
//        Dialog dialog;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            phoneNum = params[2];
            parms.add(new BasicNameValuePair(CensusConstants.loginType, params[0]));
            parms.add(new BasicNameValuePair(params[1], phoneNum));

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.LOGIN_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
//            dialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
Log.d(LOG_TAG, "result >>> " + result);

                String status = jsonObject.getString(CensusConstants.STATUS);

                String response = jsonObject.getString("response");
                if (status.equals("error")) {
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
                } else {
                    finishAffinity();
                    Intent intent = new Intent(LoginSignupActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }
    }
}
