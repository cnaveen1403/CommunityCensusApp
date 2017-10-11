package com.zolipe.communitycensus.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.adapter.StateListAdapter;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.model.State;
import com.zolipe.communitycensus.permissions.PermissionsActivity;
import com.zolipe.communitycensus.permissions.PermissionsChecker;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.CommonUtils;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.ganfra.materialspinner.MaterialSpinner;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class EditProfile extends AppCompatActivity {

    Context mContext;

    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private PermissionsChecker checker;
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    Uri mCapturedImageURI;
    String fileName = "temp.jpg";
    private static String mImageType = "jpeg";
    private String realPath, mStateId = "0", mCityId = "-1", mGender = "male", mEncodedData = "";

    private static EditText et_first_name, et_last_name, et_dob, et_aadhaar, et_email, et_address, et_zipcode;
    ToggleButton toggleButton_gender;
    private ImageView image_male, image_female;
    CircleImageView iv_add_image;
    Button btn_register;
    MaterialSpinner spinner2, spinner_city;
    private static StateListAdapter mStateListAdapter;
    private static ArrayList<State> mStateList = new ArrayList<>();
    private static StateListAdapter mCityListAdapter;
    private static ArrayList<State> mCityList = new ArrayList<>();

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static String TAG = "EditProfile";

    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_signup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        init();
    }

    private void init() {
        mContext = EditProfile.this;
        checker = new PermissionsChecker(this);

        animation = new TranslateAnimation(0, 0, 300, 0);
        // set Animation for 0.5 sec
        animation.setDuration(500);
        //for button stops in the new position.
        animation.setFillAfter(true);

        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        et_dob = (EditText) findViewById(R.id.et_member_dob);
//        et_phone_no = (EditText) findViewById(R.id.et_phone_no_signup);
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
                if (isValidated()) {
                    if (CommonUtils.isActiveNetwork(mContext))
                        updateProfile();
                    else
                        Toast.makeText(mContext, "Check your internet connectivity to update profile", Toast.LENGTH_LONG).show();
                }
            }
        });

        initStateSpinner();
        initCitySpinner();
        setProfileData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;

            default:
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void setProfileData() {
        String gender = AppData.getString(mContext, CensusConstants.gender);

        spinner2.setSelection(Integer.parseInt(AppData.getString(mContext, CensusConstants.state_id)));
        et_first_name.setText(AppData.getString(mContext, CensusConstants.firstName));
        et_last_name.setText(AppData.getString(mContext, CensusConstants.lastName));
//        Log.e(TAG, "setProfileData: Date >>>>>>>>> " + AppData.getString(mContext, CensusConstants.dob));
        et_dob.setText(AppData.getString(mContext, CensusConstants.dob));
//        et_phone_no.setText(AppData.getString(mContext, CensusConstants.phoneNumber));
        et_aadhaar.setText(AppData.getString(mContext, CensusConstants.aadhaar));
        et_email.setText(AppData.getString(mContext, CensusConstants.emailId));
        et_address.setText(AppData.getString(mContext, CensusConstants.address));
        et_zipcode.setText(AppData.getString(mContext, CensusConstants.zipcode));

        if (gender.equals("male")) {
            toggleButton_gender.setChecked(false);
        } else {
            toggleButton_gender.setChecked(true);
        }

        Glide.with(mContext).load(AppData.getString(mContext, CensusConstants.image_url))
//                .thumbnail(0.5f)
                .crossFade()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.app_icon)
                .into(iv_add_image);

        new Thread(new Runnable() {
            @Override
            public void run() {
                    //Your logic that service will perform will be placed here
                    //In this example we are just looping and waits for 1000 milliseconds in each loop.
                try {
                    Thread.sleep(3000);
                    spinner_city.setSelection(Integer.parseInt(AppData.getString(mContext, CensusConstants.city_id)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initStateSpinner() {
        mStateList = CommonUtils.getStatesList(mContext);
        spinner2 = (MaterialSpinner) findViewById(R.id.spinner2);
        mStateListAdapter = new StateListAdapter(mContext, mStateList);
        spinner2.setAdapter(mStateListAdapter);
        mStateListAdapter.notifyDataSetChanged();
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                Log.d(LOG_TAG, "Item On Position >> "+ position);
                if (position != -1) {
                    String state_id = mStateList.get(position).getId();
                    mStateId = state_id;
                    resetCitySpinner();
                    prepareCityList();
                    mCityListAdapter.notifyDataSetChanged();
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
        prepareCityList();
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

    private void prepareCityList() {
        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams = new DbParameter();

        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add(mStateId);
        dbParams.addParamterList(parms);
        final DbAsyncParameter dbAsyncParam = new DbAsyncParameter(R.string.sql_select_cities, DbAsyncTask.QUERY_TYPE_CURSOR, dbParams, null);

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

                if (cur.moveToFirst()) {
                    do {
                        String id = cur.getString(cur.getColumnIndex("city_id"));
                        String name = cur.getString(cur.getColumnIndex("city_name"));

                        if (mCityList.size() == 0) {
                            mCityList.add(new State(id, name));
                        } else {
                            boolean bStatus = true;
                            Iterator<State> iter = mCityList.iterator();
                            while (iter.hasNext()) {
//                                    Log.d(TAG, "============ Inside if condition iterator ============= ");
                                State obj = iter.next();
                                if (id.equals(obj.getId())) {
                                    bStatus = false;
                                }
                            }
                            Log.d(TAG, "bStatus >>>> " + bStatus);
                            if (bStatus) {
//                                Log.d("SuperFragment", "************ Object Has been added successfully ************ ");
                                mCityList.add(new State(id, name));
                            }
                        }
                    }
                    while (cur.moveToNext());
                    mCityListAdapter.notifyDataSetChanged();
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

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private void openFileChooserDialog() {
        final Dialog dialog = new Dialog(EditProfile.this);
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
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(EditProfile.this, mCapturedImageURI));
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
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(EditProfile.this, selectedImageUri));
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
            int year = calendar.get(Calendar.YEAR) - 18;
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

    public String getFname() {
        return et_first_name.getText().toString();
    }

    public String getLname() {
        return et_last_name.getText().toString();
    }

    public String getDateOfBirth() {
        return et_dob.getText().toString();
    }

    public String getAadhaar() {
        return et_aadhaar.getText().toString();
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

    private boolean isValidAadhar() {
        boolean bStatus = true;
        if (et_aadhaar.getText().toString().length() < 12) {
            bStatus = false;
        } else if (getAadhaar().equals("")) {
            bStatus = false;
        }

        return bStatus;
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
        } else if (!isValidAadhar()) {
            bStatus = false;
            et_aadhaar.requestFocus();
            et_aadhaar.setError("Please enter valid Aadhaar Number");
        } else if (getDateOfBirth().equals("")) {
            bStatus = false;
            et_dob.requestFocus();
            et_dob.setError("Please enter Date of Birth");
        } else if (getEmail().equals("")) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        } else if (!getEmail().equals("") && !getEmail().matches(emailPattern)) {
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
            et_zipcode.setError("Please enter zipcode");
        }

        return bStatus;
    }

    private int getPosition(ArrayList<State> mStateList, String state_id) {
        int pos = 0;
        for (int i = 0; i < mStateList.size(); i++) {
            String temp_id = mStateList.get(i).getId();
            if (temp_id.equals(state_id)) {
                pos = i + 1;
                break;
            }
        }

        return pos;
    }

    private void updateProfile() {
        new UpdateProfile().execute();
    }

    private class UpdateProfile extends AsyncTask<Void, Void, String> {
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

            String user_role = AppData.getString(mContext, CensusConstants.userRole);

            parms.add(new BasicNameValuePair(CensusConstants.firstName, getFname()));
            parms.add(new BasicNameValuePair(CensusConstants.lastName, getLname()));
            parms.add(new BasicNameValuePair(CensusConstants.gender, mGender));
            parms.add(new BasicNameValuePair(CensusConstants.dob, getDateOfBirth()));
            parms.add(new BasicNameValuePair(CensusConstants.phoneNumber, AppData.getString(mContext, CensusConstants.phoneNumber)));
            parms.add(new BasicNameValuePair(CensusConstants.aadhaar, getAadhaar()));
            parms.add(new BasicNameValuePair(CensusConstants.emailId, getEmail()));
            parms.add(new BasicNameValuePair(CensusConstants.address, getAddress()));
            parms.add(new BasicNameValuePair(CensusConstants.city_id, mCityId));
            parms.add(new BasicNameValuePair(CensusConstants.state_id, mStateId));
            parms.add(new BasicNameValuePair(CensusConstants.country, AppData.getString(mContext, CensusConstants.country)));
            parms.add(new BasicNameValuePair(CensusConstants.zipcode, getZipcode()));
            parms.add(new BasicNameValuePair(CensusConstants.userAvatar, mEncodedData));
            parms.add(new BasicNameValuePair(CensusConstants.imageType, mImageType));
            parms.add(new BasicNameValuePair(CensusConstants.userRole, user_role));

            /*Log.d(LOG_TAG, "params firstName >>> " + getFname());
            Log.d(LOG_TAG, "params lastName >>> " + getLname());
            Log.d(LOG_TAG, "params gender >>> " + mGender);
            Log.d(LOG_TAG, "params dob >>> " + getDateOfBirth());
            Log.d(LOG_TAG, "params phoneNumber >>> " + AppData.getString(mContext, CensusConstants.phoneNumber));
            Log.d(LOG_TAG, "params emailId >>> " + getEmail());
            Log.d(LOG_TAG, "params address >>> " + getAddress());
            Log.d(LOG_TAG, "params zipcode >>> " + getZipcode());
            Log.d(LOG_TAG, "params mEncodedData >>> " + mEncodedData);
            Log.d(LOG_TAG, "params image type >>> " + mImageType);*/

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.UPDATE_SUPERVISOR_INFO_URL;
            url += "?";
            url += paramString;
            Log.e(LOG_TAG, "url sending is >>> " + url);*/

            String URL = "";

            String userRole = AppData.getString(mContext, CensusConstants.userRole);
            if (userRole.equals("admin")) {
                URL = CensusConstants.BASE_URL + CensusConstants.UPDATE_ADMIN_INFO_URL;
                parms.add(new BasicNameValuePair("user_id", AppData.getString(mContext, CensusConstants.rolebased_user_id)));
            } else if (userRole.equals("supervisor")) {
                URL = CensusConstants.BASE_URL + CensusConstants.UPDATE_SUPERVISOR_INFO_URL;
                parms.add(new BasicNameValuePair("supervisor_id", AppData.getString(mContext, CensusConstants.rolebased_user_id)));
            } else if (userRole.equals("member")) {
                URL = CensusConstants.BASE_URL + CensusConstants.UPDATE_MEMBER_INFO_URL;
                parms.add(new BasicNameValuePair("member_id", AppData.getString(mContext, CensusConstants.rolebased_user_id)));
            }

            return new ConnectToServer().getDataFromUrl(URL, parms);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.d(TAG, "result >>> " + result);
                progressDialog.dismiss();
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                String response = jsonObject.getString("response");
                String status_code = jsonObject.getString("status_code");

                if (status.equals("success")) {

                    if (status.equalsIgnoreCase(CensusConstants.SUCCESS) && status_code.equals("1000")) {
                        final Dialog customDialog = new Dialog(mContext);
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
                                saveAppData();
                                finishAffinity();
                                Intent intent = new Intent(mContext, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            }
                        });
                        customDialog.show();
                    } else if (status.equalsIgnoreCase(CensusConstants.SUCCESS) && status_code.equals("1001")) {
                        final Dialog customDialog = new Dialog(mContext);
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
                                Intent intent = new Intent(mContext, HomeActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                            }
                        });
                        customDialog.show();
                    }
                } else if (status.equals("error")) {
                    final Dialog customDialog = new Dialog(mContext);
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
                            Intent intent = new Intent(mContext, HomeActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        }
                    });
                    customDialog.show();
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();

                final Dialog customDialog = new Dialog(mContext);
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
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    }
                });
                customDialog.show();
            }
        }
    }

    private void saveAppData() {
        AppData.saveString(mContext, CensusConstants.firstName, getFname());
        AppData.saveString(mContext, CensusConstants.lastName, getLname());
        AppData.saveString(mContext, CensusConstants.gender, mGender);
        AppData.saveString(mContext, CensusConstants.dob, getDateOfBirth());
        AppData.saveString(mContext, CensusConstants.aadhaar, getAadhaar());
        AppData.saveString(mContext, CensusConstants.emailId, getEmail());
        AppData.saveString(mContext, CensusConstants.address, getAddress());
        AppData.saveString(mContext, CensusConstants.city_id, mCityId);
        AppData.saveString(mContext, CensusConstants.state_id, mStateId);
        AppData.saveString(mContext, CensusConstants.zipcode, getZipcode());
        AppData.saveString(mContext, CensusConstants.userAvatar, mEncodedData);
        AppData.saveString(mContext, CensusConstants.imageType, mImageType);
    }
}
