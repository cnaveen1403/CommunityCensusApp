package com.zolipe.communitycensus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.zolipe.communitycensus.model.UserProfile;

public class SignUpActivity extends Activity {
    TextInputEditText email, fname, lname, mobile;
    TextInputLayout tilEmail ,tilFName ,tilLName ,tilMobile;
    String mPhoneNumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_up);

        init ();



        ((Button) findViewById(R.id.sign_up_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidated ()){
                    mPhoneNumer = getMobileNumber();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignUpActivity.this);
                    // Setting Dialog Title
                    alertDialog.setTitle("Verify Your Mobile");

                    // Setting Dialog Message
                    alertDialog.setMessage("Please verify your mobile number.");

                    // Setting Positive "Yes" Button
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // User pressed YES button. Write Logic Here
                            authenticateDigits (getMobileNumber());
                        }
                    });

                    // Setting Negative "NO" Button
                    alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // User pressed No button. Write Logic Here
                            Toast.makeText(getApplicationContext(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Showing Alert Message
                    alertDialog.show();
                }
                /*Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();*/
                /*if (isAppInstalled(SignUpActivity.this, "com.whatsapp")) {
                    String whatsAppMessage = "https://play.google.com/store/apps/details?id=com.bureau.bureauapp&hl=en";

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
                    sendIntent.setType("text/plain");

                    // Do not forget to add this to open whatsApp App specifically
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(SignUpActivity.this, "Whatsapp is not installed", Toast.LENGTH_SHORT).show();
                }*/
            }
        });
    }

    private void init() {
        email = (TextInputEditText)findViewById(R.id.emailET);
        fname = (TextInputEditText)findViewById(R.id.firstNameET);
        lname = (TextInputEditText)findViewById(R.id.lastNameET);
        mobile = (TextInputEditText)findViewById(R.id.mobileNumberET);

        tilEmail = (TextInputLayout)findViewById(R.id.input_layout_email);

        tilFName = (TextInputLayout)findViewById(R.id.input_layout_first_name);
        tilLName = (TextInputLayout)findViewById(R.id.input_layout_lname);
        tilMobile = (TextInputLayout)findViewById(R.id.input_layout_mobile_number);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!getEmail().equals("")) {
                    tilEmail.setError(null);
                    tilEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!getFname().equals("")) {
                    tilFName.setError(null);
                    tilFName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!getLname().equals("")) {
                    tilLName.setError(null);
                    tilLName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!getMobileNumber().equals("")) {
                    tilMobile.setError(null);
                    tilMobile.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    final AuthCallback digitsCallback = new AuthCallback() {
        @Override
        public void success(DigitsSession session, String phoneNumber) {
            // TODO: associate the session userID with your user model
            Toast.makeText(SignUpActivity.this, "Authentication successful for "
                    + phoneNumber, Toast.LENGTH_LONG).show();
            mPhoneNumer = phoneNumber;
            submitSignupForm ();
        }

        @Override
        public void failure(DigitsException exception) {
            Log.d("Digits", "Sign in with Digits failure", exception);
        }
    };

    private void submitSignupForm() {
        //TODO : submit all the final values to the server
//        Digits.clearActiveSession();
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(getFname());
        userProfile.setLastName(getLname());
        userProfile.setPhoneNumber(mPhoneNumer);
        userProfile.setEmail(getEmail());

        finish();
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void authenticateDigits (String mobileNumber){
        AuthConfig.Builder authConfigBuilder = new AuthConfig.Builder()
                .withAuthCallBack(digitsCallback)
                .withEmailCollection(true)
                .withPhoneNumber("+91" + mobileNumber);

        Digits.authenticate(authConfigBuilder.build());
    }

    public String getEmail() {
        return email.getText().toString();
    }

    public String getFname() {
        return fname.getText().toString();
    }

    public String getLname() {
        return lname.getText().toString();
    }

    public String getMobileNumber() {
        return mobile.getText().toString();
    }

    private boolean isValidated() {
        boolean bStatus = true;

        if(getEmail().equals("")){
            bStatus = false;
            tilEmail.setErrorEnabled(true);
            tilEmail.setError("Please enter valid email");
//            email.setError("Please enter valid email");
        }else if(getFname().equals("")){
            bStatus = false;
            tilFName.setError("Please enter first name");
        }else if(getLname().equals("")){
            bStatus = false;
            tilLName.setError("Please enter last name");
        }else if(getMobileNumber().equals("")){
            bStatus = false;
            tilMobile.setError("Please enter mobile number");
        }

        return bStatus;
    }

    public boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
