package com.zolipe.communitycensus.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.adapter.StateListAdapter;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.model.State;
import com.zolipe.communitycensus.permissions.PermissionsActivity;
import com.zolipe.communitycensus.permissions.PermissionsChecker;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.SelectDocument;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.ganfra.materialspinner.MaterialSpinner;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class SignUpActivity extends AppCompatActivity {
    private static final String LOG_TAG = SignUpActivity.class.getSimpleName();

    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private static EditText et_first_name, et_last_name, et_dob, et_aadhaar, et_phone_no, et_email, et_address, et_zipcode;
    private static String mImageType = "jpeg";
    ToggleButton toggleButton_gender;
    private ImageView image_male, image_female;
    CircleImageView iv_add_image;
    Button btn_register;
    String mPhoneNumer, mGender = "male", mEncodedData = "", mStateId = "-1", mCityId = "-1";
    private PermissionsChecker checker;

    Uri mCapturedImageURI;
    private String realPath;
    String fileName = "temp.jpg";
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;

    Context mContext;

    MaterialSpinner spinner2, spinner_city;
    private static StateListAdapter mStateListAdapter;
    private static ArrayList<State> mStateList = new ArrayList<>();
    private static StateListAdapter mCityListAdapter;
    private static ArrayList<State> mCityList = new ArrayList<>();
    private String mUserRole = "admin";
//    private String mUserRole = "supervisor";

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_signup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Register");

        init();
    }

    private void init() {
        mContext = SignUpActivity.this;
        checker = new PermissionsChecker(this);
        new StatesListAsyncTask().execute();

        animation = new TranslateAnimation(0, 0, 300, 0);
        // set Animation for 0.5 sec
        animation.setDuration(500);
        //for button stops in the new position.
        animation.setFillAfter(true);

        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        et_dob = (EditText) findViewById(R.id.et_member_dob);
        et_phone_no = (EditText) findViewById(R.id.et_phone_no_signup);
        et_aadhaar = (EditText) findViewById(R.id.et_aadhaar_signup);
        et_email = (EditText) findViewById(R.id.et_email);
        et_address = (EditText) findViewById(R.id.et_address);
        et_zipcode = (EditText) findViewById(R.id.et_zipcode);
        toggleButton_gender = (ToggleButton) findViewById(R.id.toggleButton_gender);
        iv_add_image = (CircleImageView) findViewById(R.id.iv_add_image);
        image_female = (ImageView) findViewById(R.id.ivFemale);
        image_male = (ImageView) findViewById(R.id.ivMale);
        btn_register = (Button) findViewById(R.id.btn_register);

        iv_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    openFileChooserDialog();
                }
            }
        });

        et_dob.setInputType(InputType.TYPE_NULL);
        et_dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDOBDialogue(v);
                }
            }
        });

        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDOBDialogue(v);
            }
        });

        /*----- Toggle button clicked ----- */
        toggleButton_gender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    image_male.setImageResource(R.drawable.ic_male_grey);
                    image_female.setImageResource(R.drawable.ic_female_blue);
                    mGender = "female";
                } else {
                    image_female.setImageResource(R.drawable.ic_female_grey);
                    image_male.setImageResource(R.drawable.ic_male_blue);
                    mGender = "male";
                }
            }
        });

        image_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton_gender.setChecked(false);
                mGender = "male";
            }
        });

        image_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton_gender.setChecked(true);
                mGender = "female";
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerClicked();
            }
        });

        initStateSpinner();
        initCitySpinner();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SignUpActivity.this, LoginSignupActivity.class);
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

    private void initStateSpinner() {
        spinner2 = (MaterialSpinner) findViewById(R.id.spinner2);
        mStateListAdapter = new StateListAdapter(mContext, mStateList);
        spinner2.setAdapter(mStateListAdapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                Log.d(LOG_TAG, "Item On Position >> "+ position);
                if (position != -1) {
                    String state_id = mStateList.get(position).getId();
                    mStateId = state_id;
                    new GetCityListAsync().execute(state_id);
                    resetCitySpinner();
//                    Log.d(LOG_TAG, "Item on position " + position + " : " + state_id + " Selected");
//                    Log.d(LOG_TAG, "Item on position " + position + " : " + mStateList.get(position).getName() + " Selected");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                Toast.makeText(mContext, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetCitySpinner() {
        mCityId = "-1";
        mCityList.clear();
        mCityListAdapter.notifyDataSetChanged();
        spinner_city.setSelection(0);
//        spinner_city.setId(-1);
    }

    private void initCitySpinner() {
        spinner_city = (MaterialSpinner) findViewById(R.id.spinner_city);
        mCityListAdapter = new StateListAdapter(mContext, mCityList);
        spinner_city.setAdapter(mCityListAdapter);
        spinner_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                Log.d(LOG_TAG, "Item On Position >> "+ position);
                if (position != -1) {
                    String city_id = mCityList.get(position).getId();
                    mCityId = city_id;
//                    Log.d(LOG_TAG, "Item on position " + position + " : " + city_id + " Selected");
//                    Log.d(LOG_TAG, "Item on position " + position + " : " + mCityList.get(position).getName() + " Selected");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                Toast.makeText(mContext, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private void openFileChooserDialog() {
        final Dialog dialog = new Dialog(SignUpActivity.this);
        // Include dialog.xml file
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_gallery);
        // Set dialog title
//        dialog.setTitle("Add Picture");
        Button btn_camera = (Button) dialog.findViewById(R.id.btn_camera);
        Button btn_gallery = (Button) dialog.findViewById(R.id.btn_gallery);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCameraIntent();
                dialog.dismiss();
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGalleryIntent();
                dialog.dismiss();
            }
        });

        dialog.show();
        btn_camera.startAnimation(animation);
        btn_gallery.startAnimation(animation);
    }

    private void initCameraIntent() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void initGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == 0) {
            openFileChooserDialog();
        }

        Uri correctedUri = null;
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            //----- Correct Image Rotation ----//
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCapturedImageURI);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(SignUpActivity.this, mCapturedImageURI));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                iv_add_image.setImageURI(correctedUri);
                getEncoded64ImageStringFromBitmap(imageBitmap);
                getMimeType(realPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            realPath = getRealPathFromURI(selectedImageUri);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(SignUpActivity.this, selectedImageUri));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                iv_add_image.setImageURI(correctedUri);
                getEncoded64ImageStringFromBitmap(imageBitmap);
                getMimeType(realPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getEncoded64ImageStringFromBitmap(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        mEncodedData = Base64.encodeToString(ba, Base64.DEFAULT);
//        Log.d(LOG_TAG, "mEncodedData >> " + mEncodedData);
    }

    public static void getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        String[] imageType = type.split("/");
        mImageType = imageType[1];
//        Log.d(LOG_TAG, "mImageType >>> " + mImageType);
    }

    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            @SuppressWarnings("deprecation")
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        }
    }

    private Uri getImageUri(Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap imageOrientationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void showDOBDialogue(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);
        DialogFragment dFragment = new DatePickerFragment();
        dFragment.show(getFragmentManager(), "date picker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR)-18;
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), R.style.datepicker, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date
            String selectedDate = year + "-" + (month + 1) + "-" + day;
            et_dob.setText(selectedDate);
        }
    }

    private void registerClicked() {
        if (isValidated()) {
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
//Log.d(LOG_TAG, "Here came the control >>> " + ("+91" + getMobileNumber()).equals(Digits.getActiveSession().getPhoneNumber()));
                    /*if(!("+91" + getMobileNumber()).equals(Digits.getActiveSession().getPhoneNumber())){
                        Digits.clearActiveSession();
                    }*/

//                    authenticateDigits(getMobileNumber());
                }
            });

            // Setting Negative "NO" Button
            alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // User pressed No button. Write Logic Here
                    Toast.makeText(getApplicationContext(), "You need to verify your mobile number to continue with the app.", Toast.LENGTH_SHORT).show();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }
    }

    /*final AuthCallback digitsCallback = new AuthCallback() {
        @Override
        public void success(DigitsSession session, String phoneNumber) {
            // TODO: associate the session userID with your user model
Log.e("SignUPScuccess", "phoneNumber >>> " + phoneNumber.substring(1));
            mPhoneNumer = phoneNumber.substring(1);
            submitSignupForm();
        }

        @Override
        public void failure(DigitsException exception) {
            Log.d("Digits", "Sign in with Digits failure", exception);
        }
    };*/

    private void submitSignupForm() {
        //TODO : submit all the final values to the server
        new RegisterUser().execute();
    }

    /*public void authenticateDigits(String mobileNumber) {
        Log.d("AuthenticateDigits", "Mobile number >>> " + mobileNumber);
        AuthConfig.Builder authConfigBuilder = new AuthConfig.Builder()
                .withAuthCallBack(digitsCallback)
                .withEmailCollection(true)
                .withPhoneNumber("+91" + mobileNumber);

        Digits.authenticate(authConfigBuilder.build());
    }*/

    public String getFname() {
        return et_first_name.getText().toString();
    }

    public String getLname() {
        return et_last_name.getText().toString();
    }

    public String getGender() {
        return mGender;
    }

    public String getDateOfBirth() {
        return et_dob.getText().toString();
    }

    public String getAadhaar() {
        return et_aadhaar.getText().toString();
    }

    public String getMobileNumber() {
        return et_phone_no.getText().toString();
    }

    public String getEmail() {
        return et_email.getText().toString();
    }

    public String getAddress() {
        return et_address.getText().toString();
    }

    public String getZipcode() {
        return et_zipcode.getText().toString();
    }

    private boolean isValidAadhar (){
        boolean bStatus = true;
        if (et_aadhaar.getText().toString().length()<12) {
            bStatus = false;
        }else if (getAadhaar().equals("")){
            bStatus = false;
        }

        return  bStatus;
    }

    private boolean isValidPhoneNo (){
        boolean bStatus = true;
        if (et_phone_no.getText().toString().length()<10) {
            bStatus = false;
        }else if (getMobileNumber().equals("")){
            bStatus = false;
        }

        return  bStatus;
    }

    private boolean isValidated() {
        boolean bStatus = true;
        if (getFname().equals("")) {
            bStatus = false;
            et_first_name.requestFocus();
            et_first_name.setError("Please enter first name");
        } else if (getLname().equals("")) {
            bStatus = false;
            et_last_name.requestFocus();
            et_last_name.setError("Please enter last name");
        } else if (!isValidAadhar ()) {
            bStatus = false;
            et_aadhaar.requestFocus();
            et_aadhaar.setError("Please enter valid Aadhaar Number");
        } else if (!isValidPhoneNo ()) {
            bStatus = false;
            et_phone_no.requestFocus();
            et_phone_no.setError("Please enter valid mobile number");
        } else if (getDateOfBirth().equals("")) {
            bStatus = false;
            et_dob.requestFocus();
            et_dob.setError("Please enter Date of Birth");
        } else if (getEmail().equals("")) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        }else if (!getEmail().equals("") && !getEmail().matches(emailPattern)) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        } else if (getAddress().equals("")) {
            bStatus = false;
            et_address.requestFocus();
            et_address.setError("Please enter Address");
        } else if (mStateId.equals("-1")) {
            bStatus = false;
            spinner2.requestFocus();
            spinner2.setError("Please select State");
        } else if (mCityId.equals("-1")) {
            bStatus = false;
            spinner_city.requestFocus();
            spinner_city.setError("Please select City");
        } else if (getZipcode().equals("")) {
            bStatus = false;
            et_zipcode.requestFocus();
            et_zipcode.setError("Please enter Zipcode");
        } else if (mEncodedData.equals("")) {
            bStatus = false;
            showAddImageDialog();
        }

        return bStatus;
    }

    private void showAddImageDialog() {
        final Dialog dialog = new Dialog(SignUpActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.yes_no_alert);

        ((TextView) dialog.findViewById(R.id.tv_title_yes_no)).setText("Alert");
        ((TextView) dialog.findViewById(R.id.tv_message_yes_no)).setText("Please Add User Image, Do you wish to Add Profile Image ?");

        Button btn_no = (Button) dialog.findViewById(R.id.btn_cancel);

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mEncodedData = "";
                registerClicked();
            }
        });

        Button btn_send = (Button) dialog.findViewById(R.id.btn_send);
        btn_send.setText("Add");
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    openFileChooserDialog();
                }
            }
        });
        dialog.show();
    }

    private class RegisterUser extends AsyncTask<Void, Void, String> {
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
            // params comes from the execute() call: params[0] is the url.
            List<NameValuePair> parms = new LinkedList<NameValuePair>();

            parms.add(new BasicNameValuePair(CensusConstants.firstName, getFname()));
            parms.add(new BasicNameValuePair(CensusConstants.lastName, getLname()));
            parms.add(new BasicNameValuePair(CensusConstants.gender, getGender()));
            parms.add(new BasicNameValuePair(CensusConstants.dob, getDateOfBirth()));
            parms.add(new BasicNameValuePair(CensusConstants.phoneNumber, mPhoneNumer));
            parms.add(new BasicNameValuePair(CensusConstants.aadhaar, getAadhaar()));
            parms.add(new BasicNameValuePair(CensusConstants.emailId, getEmail()));
            parms.add(new BasicNameValuePair(CensusConstants.address, getAddress()));
            parms.add(new BasicNameValuePair(CensusConstants.city_id, mCityId));
            parms.add(new BasicNameValuePair(CensusConstants.state_id, mStateId));
            parms.add(new BasicNameValuePair(CensusConstants.country, "india"));
            parms.add(new BasicNameValuePair(CensusConstants.zipcode, getZipcode()));
            parms.add(new BasicNameValuePair(CensusConstants.userAvatar, mEncodedData));
            parms.add(new BasicNameValuePair(CensusConstants.imageType, mImageType));
            parms.add(new BasicNameValuePair(CensusConstants.userRole, mUserRole));

            /*Log.d(LOG_TAG, "params firstName >>> " + getFname());
            Log.d(LOG_TAG, "params lastName >>> " + getLname());
            Log.d(LOG_TAG, "params gender >>> " + getGender());
            Log.d(LOG_TAG, "params dob >>> " + getDateOfBirth());
            Log.d(LOG_TAG, "params phoneNumber >>> " + mPhoneNumer);
            Log.d(LOG_TAG, "params emailId >>> " + getEmail());
            Log.d(LOG_TAG, "params address >>> " + getAddress());
            Log.d(LOG_TAG, "params zipcode >>> " + getZipcode());
            Log.d(LOG_TAG, "params mEncodedData >>> " + mEncodedData);
            Log.d(LOG_TAG, "params image type >>> " + mImageType);*/

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.REGISTER_URL;
            url += "?";
            url += paramString;
            Log.e(LOG_TAG, "url sending is >>> " + url);*/

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.REGISTER_URL, parms);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.d(LOG_TAG, "result >>> " + result);
                progressDialog.dismiss();
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                if (status.equals("success")) {
                    String response = jsonObject.getString("response");
                    String status_code = jsonObject.getString("status_code");

                    if (status.equalsIgnoreCase(CensusConstants.SUCCESS) && status_code.equals("1000")) {
//                        Digits.logout();
//                        mLoggedIn = true;
                        String image_url = jsonObject.getString("image_url");
                        String user_id = jsonObject.getString("user_id");
                        String rolebased_user_id = jsonObject.getString("rolebased_user_id");

                        AppData.saveString(mContext, CensusConstants.userid, user_id);
                        AppData.saveString(mContext, CensusConstants.firstName, getFname());
                        AppData.saveString(mContext, CensusConstants.lastName, getLname());
                        AppData.saveString(mContext, CensusConstants.dob, getDateOfBirth());
                        AppData.saveString(mContext, CensusConstants.gender, getGender());
                        AppData.saveString(mContext, CensusConstants.phoneNumber, mPhoneNumer);
                        AppData.saveString(mContext, CensusConstants.emailId, getEmail());
                        AppData.saveString(mContext, CensusConstants.address, getAddress());
                        AppData.saveString(mContext, CensusConstants.zipcode, getZipcode());
                        AppData.saveString(mContext, CensusConstants.image_url, image_url);
                        AppData.saveString(mContext, CensusConstants.aadhaar, getAadhaar());
                        AppData.saveString(mContext, CensusConstants.city_id, mCityId);
                        AppData.saveString(mContext, CensusConstants.state_id, mStateId);
                        AppData.saveString(mContext, CensusConstants.country, "india");
                        AppData.saveString(mContext, CensusConstants.userRole, mUserRole);
                        AppData.saveString(mContext, CensusConstants.rolebased_user_id, rolebased_user_id);

                        final Dialog customDialog = new Dialog(SignUpActivity.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                                AppData.saveBoolean(mContext, CensusConstants.isLoggedIn, true);
                                finishAffinity();
                                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            }
                        });
                        customDialog.show();
                    } else if (status.equalsIgnoreCase(CensusConstants.SUCCESS) && status_code.equals("1002")) {
                        final Dialog customDialog = new Dialog(SignUpActivity.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Alert");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                                Intent intent = new Intent(SignUpActivity.this, LoginSignupActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            }
                        });
                        customDialog.show();
                    } else if (status.equals("error")) {
                        final Dialog customDialog = new Dialog(SignUpActivity.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                                Intent intent = new Intent(SignUpActivity.this, LoginSignupActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            }
                        });
                        customDialog.show();
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();

                final Dialog customDialog = new Dialog(SignUpActivity.this);
                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                customDialog.setContentView(R.layout.simple_alert);
                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Alert");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Server Not Responding Please try again Later.");
                TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                        Intent intent = new Intent(SignUpActivity.this, LoginSignupActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    }
                });
                customDialog.show();
            }
        }
    }

    public class StatesListAsyncTask extends AsyncTask<Void, Void, String> {
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
            return new ConnectToServer().getDataFromUrlGETMethod(CensusConstants.BASE_URL + CensusConstants.GET_STATES_URL);
        }

        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();
            Log.d(LOG_TAG, "on postexecute result >>> " + result);
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
                        String id = explrObject.getString("state_id");
                        String name = explrObject.getString("state_name");
                        mStateList.add(new State(id, name));
                    }

                    mStateListAdapter.notifyDataSetChanged();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Alert");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Server not Responding, please try again after sometime.");
                TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                    }
                });
                customDialog.show();
            }
        }
    }

    private class GetCityListAsync extends AsyncTask<String, Void, String> {
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
            String state_id = params[0];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(CensusConstants.state_id, state_id));
            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.GET_CITY_LIST_URL, parms);
        }

        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();
            Log.d(LOG_TAG, "on postexecute result >>> " + result);
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
                        String id = explrObject.getString("city_id");
                        String name = explrObject.getString("city_name");
                        mCityList.add(new State(id, name));
                    }

                    mCityListAdapter.notifyDataSetChanged();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Alert");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Server not Responding, please try again after sometime.");
                TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                    }
                });
                customDialog.show();
            }
        }
    }
}