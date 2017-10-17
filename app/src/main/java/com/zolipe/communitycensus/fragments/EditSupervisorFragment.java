package com.zolipe.communitycensus.fragments;


import android.Manifest;
import android.app.Activity;
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
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.database.DbAction;
import com.zolipe.communitycensus.database.DbAsyncParameter;
import com.zolipe.communitycensus.database.DbAsyncTask;
import com.zolipe.communitycensus.database.DbParameter;
import com.zolipe.communitycensus.model.SupervisorObj;
import com.zolipe.communitycensus.permissions.PermissionsActivity;
import com.zolipe.communitycensus.permissions.PermissionsChecker;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.CommonUtils;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.SelectDocument;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditSupervisorFragment extends Fragment {

    private static String TAG = "EditSupervisorFragment";

    View rootView;
    static EditText et_first_name;
    static EditText et_last_name;
    static EditText et_member_dob;
    static EditText et_phone;
    static EditText et_aadhaar;
    static EditText et_email;
    static EditText et_address;
    static EditText et_zipcode;
    static ToggleButton toggleButton_gender;

    static Context mContext;
    Activity mActivity;

    static SupervisorObj mSupervisorObj;
    static String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static String mGender = "male", mEncodedData = "";
    private ImageView image_male, image_female;
    CircleImageView ivProfileImage;

    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private PermissionsChecker checker;
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    Uri mCapturedImageURI;
    String fileName = "temp.jpg";
    private static String mImageType = "jpeg", realPath;
    Animation animation;

    public EditSupervisorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_edit_supervisor, container, false);
        init(rootView);
        return rootView;
    }

    private void init(View rootView) {
        mContext = this.getActivity();
        mActivity = getActivity();
        checker = new PermissionsChecker(mContext);

        animation = new TranslateAnimation(0, 0, 300, 0);
        // set Animation for 0.5 sec
        animation.setDuration(500);
        //for button stops in the new position.
        animation.setFillAfter(true);

        ivProfileImage = (CircleImageView) rootView.findViewById(R.id.ivProfileImage);
        et_first_name = (EditText) rootView.findViewById(R.id.et_first_name);
        et_last_name = (EditText) rootView.findViewById(R.id.et_last_name);
        et_member_dob = (EditText) rootView.findViewById(R.id.et_member_dob);
        et_aadhaar = (EditText) rootView.findViewById(R.id.et_aadhaar);
        et_phone = (EditText) rootView.findViewById(R.id.et_phone_no);
        et_email = (EditText) rootView.findViewById(R.id.et_email);
        et_address = (EditText) rootView.findViewById(R.id.et_address);
        et_zipcode = (EditText) rootView.findViewById(R.id.et_zipcode);
        image_male = (ImageView) rootView.findViewById(R.id.ivMale);
        image_female = (ImageView) rootView.findViewById(R.id.ivFemale);

        toggleButton_gender = (ToggleButton) rootView.findViewById(R.id.toggleButton_gender);

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

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    openFileChooserDialog();
                }
            }
        });

        et_member_dob.setInputType(InputType.TYPE_NULL);
        et_member_dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDOBDialogue(v);
                }
            }
        });

        et_member_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDOBDialogue(v);
            }
        });

        setProfileData();
    }

    private void showDOBDialogue(View v) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 1);
        DialogFragment dFragment = new DatePickerFragment();
        dFragment.show(mActivity.getFragmentManager(), "date picker");
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
            et_member_dob.setText(selectedDate);
        }
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(mActivity, 0, permission);
    }

    private void openFileChooserDialog() {
        final Dialog dialog = new Dialog(mContext);
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
        mCapturedImageURI = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mCapturedImageURI);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(mContext, mCapturedImageURI));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                ivProfileImage.setImageURI(correctedUri);
                getEncoded64ImageStringFromBitmap(imageBitmap);
                getMimeType(realPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            realPath = getRealPathFromURI(selectedImageUri);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), selectedImageUri);
                imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(mContext, selectedImageUri));
                correctedUri = getImageUri(imageBitmap);
                realPath = getRealPathFromURI(correctedUri);

                ivProfileImage.setImageURI(correctedUri);
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
            Cursor cursor = mActivity.managedQuery(contentUri, proj, null, null, null);
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
        String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), image, "Title", null);
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

    public static void updateProfile() {
        if (isValidData()) {
            if (CommonUtils.isActiveNetwork(mContext))
                new UpdateAsyncTask().execute();
            else {
                saveToLocalDB(true, "", "No Internet Connectivity !!! meanwhile we saved your data in local database.");
            }
        }
    }

    public static String getFname() {
        return et_first_name.getText().toString();
    }

    public static String getLname() {
        return et_last_name.getText().toString();
    }

    public static String getDateOfBirth() {
        return et_member_dob.getText().toString();
    }

    public static String getAadhaar() {
        return et_aadhaar.getText().toString();
    }

    public static String getPhoneNumber() {
        return et_phone.getText().toString();
    }

    public static String getEmail() {
        return et_email.getText().toString();
    }

    public static String getAddress() {
        return et_address.getText().toString();
    }

    public static String getZipcode() {
        return et_zipcode.getText().toString();
    }

    private static boolean isValidPhoneNumber() {
        boolean bStatus = true;
        if (getPhoneNumber().length() < 10) {
            bStatus = false;
        } else if (getPhoneNumber().equals("")) {
            bStatus = false;
        }

        return bStatus;
    }

    private static boolean isValidData() {
        boolean bStatus = true;
        if (getFname().equals("")) {
            bStatus = false;
            et_first_name.requestFocus();
            et_first_name.setError("Please enter first name");
        } else if (getLname().equals("")) {
            bStatus = false;
            et_last_name.requestFocus();
            et_last_name.setError("Please enter last name");
        } else if (!isValidPhoneNumber()) {
            bStatus = false;
            et_aadhaar.requestFocus();
            et_aadhaar.setError("Please enter valid Phone Number");
        } else if (getDateOfBirth().equals("")) {
            bStatus = false;
            et_member_dob.requestFocus();
            et_member_dob.setError("Please enter Date of Birth");
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
        } else if (getZipcode().equals("")) {
            bStatus = false;
            et_zipcode.requestFocus();
            et_zipcode.setError("Please enter zipcode");
        }

        return bStatus;
    }

    private void setProfileData() {
        Bundle bundle = this.getArguments();
        mSupervisorObj = bundle.getParcelable("Supervisor");

        et_first_name.setText(mSupervisorObj.getFirst_name());
        et_last_name.setText(mSupervisorObj.getLast_name());

        String date = mSupervisorObj.getDob();
        String[] array = date.split("-");
        String formattedDate = array[2] + "-" + array[1] + "-" + array[0];

        et_member_dob.setText(formattedDate);
        et_phone.setText(mSupervisorObj.getPhone_number());
        et_aadhaar.setText(mSupervisorObj.getAadhaar());
        et_email.setText(mSupervisorObj.getEmail());
        et_address.setText(mSupervisorObj.getAddress());
        et_zipcode.setText(mSupervisorObj.getZipcode());

        Glide.with(this).load(mSupervisorObj.getImage_url())
                .crossFade()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_supervisor_list)
                .into(ivProfileImage);

        String gender = mSupervisorObj.getGender();

        if (gender.equals("male")) {
            toggleButton_gender.setChecked(false);
        } else {
            toggleButton_gender.setChecked(true);
        }

        et_aadhaar.setEnabled(false);
    }

    private static void saveToLocalDB(final Boolean isOffline, final String img_url, final String message) {

        final DbAsyncTask dbATask = new DbAsyncTask(mContext, false, null);
        DbParameter dbParams_duty = new DbParameter();

        ArrayList<Object> parms = new ArrayList<Object>();
        Log.e(TAG, "saveToLocalDB: mSupervisorObj.getFamilyHeadId() >>> " + mSupervisorObj.getAadhaar());
        parms.add(getFname());
        parms.add(getLname());
        parms.add("91" + getPhoneNumber());
        parms.add(getEmail());
        parms.add(getAddress());
        parms.add(mGender);
        parms.add(img_url);
        parms.add(isOffline ? mEncodedData:"");
        parms.add(getZipcode());
        parms.add(getDateOfBirth());
        parms.add(isOffline ? "no" : "yes");
        parms.add(mImageType);
        parms.add(AppData.getString(mContext, CensusConstants.rolebased_user_id));
        parms.add(getAadhaar());

        dbParams_duty.addParamterList(parms);

        final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_update_supervisor,
                DbAsyncTask.QUERY_TYPE_BULK_UPDATE, dbParams_duty, null);

        DbAction dbAction_duty = new DbAction() {
            @Override
            public void execPreDbAction() {
            }

            @Override
            public void execPostDbAction() {
                final Dialog customDialog = new Dialog(mContext);
                customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                customDialog.setContentView(R.layout.simple_alert);
                customDialog.setCancelable(false);

                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(message);
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
        };

        dbAsyncParam_duty.setDbAction(dbAction_duty);

        try {
            dbATask.execute(dbAsyncParam_duty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class UpdateAsyncTask extends AsyncTask<Void, Void, String> {
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
            parms.add(new BasicNameValuePair(CensusConstants.gender, mGender));
            parms.add(new BasicNameValuePair(CensusConstants.dob, getDateOfBirth()));
            parms.add(new BasicNameValuePair(CensusConstants.phoneNumber, mSupervisorObj.getPhone_number()));
            parms.add(new BasicNameValuePair(CensusConstants.aadhaar, getAadhaar()));
            parms.add(new BasicNameValuePair(CensusConstants.emailId, getEmail()));
            parms.add(new BasicNameValuePair(CensusConstants.address, getAddress()));
            parms.add(new BasicNameValuePair(CensusConstants.city_id, mSupervisorObj.getCity_id()));
            parms.add(new BasicNameValuePair(CensusConstants.state_id, mSupervisorObj.getState_id()));
            parms.add(new BasicNameValuePair(CensusConstants.country, "india"));
            parms.add(new BasicNameValuePair(CensusConstants.zipcode, getZipcode()));
            parms.add(new BasicNameValuePair(CensusConstants.userAvatar, mEncodedData));
            parms.add(new BasicNameValuePair("image_type", mImageType));
            parms.add(new BasicNameValuePair("updated_by", AppData.getString(mContext, CensusConstants.rolebased_user_id)));

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.UPDATE_SUPERVISOR_INFO_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/

            String URL = CensusConstants.BASE_URL + CensusConstants.UPDATE_SUPERVISOR_INFO_URL;

            return new ConnectToServer().getDataFromUrl(URL, parms);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                Log.e(TAG, "result >>> " + result);
                progressDialog.dismiss();
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");

                String response = jsonObject.getString("response");
                String status_code = jsonObject.getString("status_code");

                if (status.equals("success")) {
                    if (status.equalsIgnoreCase(CensusConstants.SUCCESS) && status_code.equals("1000")) {
                        saveToLocalDB(false, jsonObject.getString("image_url"), "Supervisor info has been updated successfully");
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
                            }
                        });
                        customDialog.show();
                    }
                } else if (status.equals("error")) {
                    saveToLocalDB(true, "", "Somethingwent wrong !!! meanwhile we saved updated supervisor info successfully in local db");
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                saveToLocalDB(true, "", "something gone wrong!!! your data has been saved in local Database");
            }
        }
    }
}