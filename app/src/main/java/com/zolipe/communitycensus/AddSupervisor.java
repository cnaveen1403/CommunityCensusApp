package com.zolipe.communitycensus;

import android.Manifest;
import android.app.AlertDialog;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.ganfra.materialspinner.MaterialSpinner;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class AddSupervisor extends AppCompatActivity {

    private static String TAG = "AddSupervisor";

    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    Context mContext;
    ImageView image_male, image_female;
    CircleImageView ivAddProfileImage;
    EditText et_first_name;
    EditText et_last_name;
    EditText et_phone_no;
    static EditText et_email;
    EditText et_address;
    EditText et_zipcode;
    EditText et_aadhaar;
    static EditText et_supervisor_dob;
    ToggleButton toggleButton_gender;
    MaterialSpinner spinner_state, spinner_city;

    Button btn_add_supervisor;
    private PermissionsChecker checker;
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    Uri mCapturedImageURI;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private String realPath, mStateId = "-1", mCityId = "-1";
    private String mGender = "male";
    private String mEncodedData = "";

    private static String mImageType;
    private static StateListAdapter mStateListAdapter;
    private static ArrayList<State> mStateList = new ArrayList<>();
    private static StateListAdapter mCityListAdapter;
    private static ArrayList<State> mCityList = new ArrayList<>();
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_supervisor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        init();
    }

    private void init() {
        mContext = AddSupervisor.this;
        new StatesListAsyncTask().execute();

        animation = new TranslateAnimation(0, 0, 300, 0);
        // set Animation for 0.5 sec
        animation.setDuration(500);
        //for button stops in the new position.
        animation.setFillAfter(true);

        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        et_phone_no = (EditText) findViewById(R.id.et_phone_no);
        et_aadhaar = (EditText) findViewById(R.id.et_aadhaar_signup);
        et_supervisor_dob = (EditText) findViewById(R.id.et_supervisor_dob);
        et_email = (EditText) findViewById(R.id.et_email);
        et_address = (EditText) findViewById(R.id.et_address);
        et_zipcode = (EditText) findViewById(R.id.et_zipcode);
        toggleButton_gender = (ToggleButton) findViewById(R.id.toggleButton_gender);
        image_female = (ImageView) findViewById(R.id.ivFemale);
        image_male = (ImageView) findViewById(R.id.ivMale);

        initStateSpinner();
        initCitySpinner();

        ivAddProfileImage = (CircleImageView) findViewById(R.id.ivAddProfileImage);
        btn_add_supervisor = (Button) findViewById(R.id.btn_add_supervisor);
        checker = new PermissionsChecker(this);

        ivAddProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    openFileChooserDialog();
                }
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

        et_supervisor_dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDOBDialogue(v);
                    hideKeyboard();
                }
            }
        });

        et_supervisor_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDOBDialogue(v);
            }
        });

        btn_add_supervisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidated()) {
                    new AddSupervisorAsyncTask().execute();
                }
            }
        });

        et_supervisor_dob.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideKeyboard();
                et_supervisor_dob.clearFocus();
                et_email.requestFocus();
                return true;
            }
        });

        et_address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard();
//                    et_address.clearFocus();
                    spinner_state.requestFocus();
                    spinner_state.performClick();
                }
                return true;
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(AddSupervisor.this, HomeActivity.class);
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
        spinner_state = (MaterialSpinner) findViewById(R.id.spinner_state);
        mStateListAdapter = new StateListAdapter(mContext, mStateList);
        spinner_state.setAdapter(mStateListAdapter);
        spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private void openFileChooserDialog() {
        final Dialog dialog = new Dialog(AddSupervisor.this);
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
        String fileName = "temp.jpg";
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
        Uri correctedUri = null;
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            //----- Correct Image Rotation ----//
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCapturedImageURI);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(AddSupervisor.this, mCapturedImageURI));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                ivAddProfileImage.setImageURI(correctedUri);
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
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(AddSupervisor.this, selectedImageUri));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                ivAddProfileImage.setImageURI(correctedUri);
                getEncoded64ImageStringFromBitmap(imageBitmap);
                getMimeType(realPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        mEncodedData = Base64.encodeToString(ba, Base64.DEFAULT);
    }

    public static void getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        String[] imageType = type.split("/");
        mImageType = imageType[1];
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

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private void showDOBDialogue(View v) {
        hideKeyboard();
        DialogFragment dFragment = new DatePickerFragment();
        dFragment.show(getFragmentManager(), "date picker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR) - 18;
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), R.style.datepicker, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date

            String selectedDate = year + "-" + ((month + 1)<10?"0"+(month+1):(month+1)) + "-" + day;
            et_supervisor_dob.setError(null);
            et_supervisor_dob.setText(selectedDate);
            validateDOB ();
        }
    }

    private static void validateDOB() {
        if (!isValidDateOfBirth(getSupervisorDOB())) {
            et_supervisor_dob.requestFocus();
            et_supervisor_dob.setError("Please enter valid Date of Birth.");
        }else{
            et_email.requestFocus();
        }
    }

    private void showAddImageDialog() {
        final Dialog dialog = new Dialog(AddSupervisor.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.yes_no_alert);
        ((TextView) dialog.findViewById(R.id.tv_title_yes_no)).setText("Alert");
        ((TextView) dialog.findViewById(R.id.tv_message_yes_no)).setText("Please Add User Image, Do you wish to Add Profile Image ?");

        Button btn_no = (Button) dialog.findViewById(R.id.btn_cancel);

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mEncodedData = CensusConstants.mBasicAvatarData;
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

    private boolean isValidAadhar() {
        boolean bStatus = true;
        if (et_aadhaar.getText().toString().length() < 12) {
            bStatus = false;
        } else if (getSupervisorAadhaar().equals("")) {
            bStatus = false;
        }

        return bStatus;
    }

    private String getSupervisorAadhaar() {
        return et_aadhaar.getText().toString();
    }

    private boolean isValidPhoneNo() {
        boolean bStatus = true;
        if (getSupervisorPhoneNo().length() < 10) {
            bStatus = false;
        } else if (getSupervisorPhoneNo().equals("")) {
            bStatus = false;
        }

        return bStatus;
    }

    private boolean isValidated() {
        boolean bStatus = true;

        if (gerSupervisorFirstName().equals("")) {
            bStatus = false;
            et_first_name.setError("Please enter first name");
        } else if (getSupervisorLastName().equals("")) {
            bStatus = false;
            et_last_name.setError("Please enter last name");
        } else if (!isValidAadhar()) {
            bStatus = false;
            et_aadhaar.requestFocus();
            et_aadhaar.setError("Please enter valid Aadhaar Number");
        } else if (!isValidPhoneNo()) {
            bStatus = false;
            et_phone_no.requestFocus();
            et_phone_no.setError("Please enter valid mobile number");
        } else if (getSupervisorDOB().equals("")) {
            bStatus = false;
            et_supervisor_dob.requestFocus();
            et_supervisor_dob.setError("Please enter Date of Birth");
        } else if (!isValidDateOfBirth(getSupervisorDOB())) {
            bStatus = false;
            et_supervisor_dob.requestFocus();
            et_supervisor_dob.setError("Please enter valid Date of Birth.");
        } else if (getSupervisorEmail().equals("")) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        } else if (!getSupervisorEmail().equals("") && !getSupervisorEmail().matches(emailPattern)) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        } else if (getSupervisorAddress().equals("")) {
            bStatus = false;
            et_address.requestFocus();
            et_address.setError("Please enter Address");
        } else if (mStateId.equals("-1")) {
            bStatus = false;
            spinner_state.requestFocus();
            spinner_state.setError("Please select State");
        } else if (mCityId.equals("-1")) {
            bStatus = false;
            spinner_city.requestFocus();
            spinner_city.setError("Please select City");
        } else if (getSupervisorZipcode().equals("")) {
            bStatus = false;
            et_zipcode.requestFocus();
            et_zipcode.setError("Please enter Zipcode");
        } else if (mEncodedData.equals("")) {
            bStatus = false;
            showAddImageDialog();
        }

        return bStatus;
    }

    private static boolean isValidDateOfBirth(String supervisorDOB) {
        Date todaysDate = new Date();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(todaysDate);

//        Log.e(TAG, "isValidDateOfBirth: formattedDate >>>> " + formattedDate);
//        Log.e(TAG, "isValidDateOfBirth: supervisorDOB >>>> " + supervisorDOB);
//        Log.e(TAG, "isValidDateOfBirth: todays date >>> " + todaysDate);
        Log.e(TAG, "isValidDateOfBirth: after comparision" + formattedDate.compareTo(supervisorDOB) );
        return ((formattedDate.compareTo(supervisorDOB) < 0) ? false:true );
    }

    public String gerSupervisorFirstName() {
        return et_first_name.getText().toString();
    }

    public String getSupervisorLastName() {
        return et_last_name.getText().toString();
    }

    public String getSupervisorGender() {
        return mGender;
    }

    public String getSupervisorPhoneNo() {
        return et_phone_no.getText().toString();
    }

    public static String getSupervisorDOB() {
        return et_supervisor_dob.getText().toString();
    }

    public String getSupervisorEmail() {
        return et_email.getText().toString();
    }

    public String getSupervisorAddress() {
        return et_address.getText().toString();
    }

    public String getSupervisorZipcode() {
        return et_zipcode.getText().toString();
    }

    private class AddSupervisorAsyncTask extends AsyncTask<Void, Void, String> {
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

            parms.add(new BasicNameValuePair(CensusConstants.firstName, gerSupervisorFirstName()));
            parms.add(new BasicNameValuePair(CensusConstants.lastName, getSupervisorLastName()));
            parms.add(new BasicNameValuePair(CensusConstants.gender, getSupervisorGender()));
            parms.add(new BasicNameValuePair(CensusConstants.dob, getSupervisorDOB()));
            parms.add(new BasicNameValuePair(CensusConstants.phoneNumber, "91" + getSupervisorPhoneNo()));
            parms.add(new BasicNameValuePair(CensusConstants.aadhaar, getSupervisorAadhaar()));
            parms.add(new BasicNameValuePair(CensusConstants.emailId, getSupervisorEmail()));
            parms.add(new BasicNameValuePair(CensusConstants.address, getSupervisorAddress()));
            parms.add(new BasicNameValuePair(CensusConstants.city_id, mCityId));
            parms.add(new BasicNameValuePair(CensusConstants.state_id, mStateId));
            parms.add(new BasicNameValuePair(CensusConstants.country, "india"));
            parms.add(new BasicNameValuePair(CensusConstants.zipcode, getSupervisorZipcode()));
            parms.add(new BasicNameValuePair(CensusConstants.userAvatar, mEncodedData));
            parms.add(new BasicNameValuePair(CensusConstants.imageType, mImageType));
            parms.add(new BasicNameValuePair(CensusConstants.createdBy, AppData.getString(mContext, CensusConstants.userid)));
//            Log.d(TAG, "params firstName >>> " + gerSupervisorFirstName());
//            Log.d(TAG, "params lastName >>> " + getSupervisorLastName());
//            Log.d(TAG, "params gender >>> " + getSupervisorGender());
//            Log.d(TAG, "params dob >>> " + getSupervisorDOB());
//            Log.d(TAG, "params phoneNumber >>> " + getSupervisorPhoneNo());
//            Log.d(TAG, "params aadhaar >>> " + getSupervisorAadhaar());
//            Log.d(TAG, "params emailId >>> " + getSupervisorEmail());
//            Log.d(TAG, "params address >>> " + getSupervisorAddress());
//            Log.d(TAG, "params zipcode >>> " + getSupervisorZipcode());
//            Log.d(TAG, "params mEncodedData >>> " + mEncodedData);
//            Log.d(TAG, "params image type >>> " + mImageType);

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.ADD_SUPERVISOR_URL, parms);//HttpUtils.doPost(map, BureauConstants.BASE_URL+BureauConstants.REGISTER_URL);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(AddSupervisor.this);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);

            progressDialog.dismiss();

            try {
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                String status_code = jsonObject.getString("status_code");
                String response = jsonObject.getString("response");

                if (status.equals("success") && status_code.equals("1000")) {

                    ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                    ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(response);
                    TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                    text.setText("OK");

                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            customDialog.dismiss();
                            finishAffinity();
                            Intent intent = new Intent(AddSupervisor.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        }
                    });
                    customDialog.show();
                }else if (status.equals("success") && status_code.equals("1003")) {
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
                } else if (status.equals("error") && status_code.equals("1004")) {
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
                }else if (status.equals("error") && status_code.equals("1005")) {
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
                }else {
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
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
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
            Log.d(TAG, "on postexecute result >>> " + result);
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

            Log.d(TAG, "on postexecute result >>> " + result);
            progressDialog.dismiss();
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
