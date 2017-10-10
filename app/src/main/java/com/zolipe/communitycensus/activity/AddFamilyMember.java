package com.zolipe.communitycensus.activity;

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
import com.zolipe.communitycensus.model.FamilyHead;
import com.zolipe.communitycensus.model.State;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.ganfra.materialspinner.MaterialSpinner;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class AddFamilyMember extends AppCompatActivity {

    private static String TAG = "AddFamilyMember";

    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static StateListAdapter mStateListAdapter;
    private static ArrayList<State> mStateList = new ArrayList<>();
    private static StateListAdapter mRelationsAdapter;
    private static ArrayList<State> mRelationsList = new ArrayList<>();
    private static StateListAdapter mCityListAdapter;

    private static ArrayList<State> mCityList = new ArrayList<>();
    MaterialSpinner sp_relations, spinner_state, spinner_city;
    private Context mContext;

    TextView tv_fh_name;
    private static EditText et_first_name, et_last_name, et_member_dob, et_phone_no, et_aadhaar,
            et_email, et_address, et_zipcode;
    ImageView image_female, image_male;
    CircleImageView iv_add_image_member, ivFamilyHeadImage;
    ToggleButton toggleButton_gender;
    Button btn_add_member;
    private PermissionsChecker checker;
    private final CharSequence[] items = {"Take Photo", "From Gallery"};
    Uri mCapturedImageURI;

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;

    String mGender = "male", mStateId = "-1", mCityId = "-1", mRelationshipId = "-1",
            emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+", mEncodedData = "";
    FamilyHead mFamilyHead;

    private static String mImageType;

    Animation animation;
    private String head_aadhaar, family_head_name, family_head_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_family_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mContext = AddFamilyMember.this;

        mFamilyHead = getIntent().getExtras().getParcelable("family_head");
        head_aadhaar = mFamilyHead.getAadhaar();
        family_head_name = mFamilyHead.getFirst_name() + " " + mFamilyHead.getLast_name();
        family_head_url = mFamilyHead.getImage_url();
        initRelationsSpinner();
        initStateSpinner();
        initCitySpinner();

        init();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /*Intent intent = new Intent(AddFamilyMember.this, FamilyDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
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

    private void init() {
        checker = new PermissionsChecker(this);

        animation = new TranslateAnimation(0, 0, 300, 0);
        // set Animation for 0.5 sec
        animation.setDuration(500);
        //for button stops in the new position.
        animation.setFillAfter(true);

        tv_fh_name = (TextView) findViewById(R.id.tv_fh_name);
        et_first_name = (EditText) findViewById(R.id.et_first_name);
        et_last_name = (EditText) findViewById(R.id.et_last_name);
        et_phone_no = (EditText) findViewById(R.id.et_phone_no);
        et_aadhaar = (EditText) findViewById(R.id.et_aadhaar_signup);
        et_member_dob = (EditText) findViewById(R.id.et_member_dob);
        et_email = (EditText) findViewById(R.id.et_email);
        et_address = (EditText) findViewById(R.id.et_address);
        et_zipcode = (EditText) findViewById(R.id.et_zipcode);
        image_female = (ImageView) findViewById(R.id.ivFemale);
        image_male = (ImageView) findViewById(R.id.ivMale);
        iv_add_image_member = (CircleImageView) findViewById(R.id.ivAddProfileImage);
        ivFamilyHeadImage = (CircleImageView) findViewById(R.id.ivFamilyHeadImage);
        toggleButton_gender = (ToggleButton) findViewById(R.id.toggleButton_gender);
        btn_add_member = (Button) findViewById(R.id.btn_add_member);

        tv_fh_name.setText(family_head_name);
        Glide.with(mContext).load(family_head_url)
                .crossFade()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_family_head)
                .into((ImageView) findViewById(R.id.ivFamilyHeadImage));

        et_member_dob.setInputType(InputType.TYPE_NULL);
        et_member_dob.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dobClicked();
                    hideKeyboard();
                }
            }
        });

        et_member_dob.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideKeyboard();
                et_member_dob.clearFocus();
                et_aadhaar.requestFocus();
                return true;
            }
        });

        et_member_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dobClicked();
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

        iv_add_image_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
                    startPermissionsActivity(PERMISSIONS_READ_STORAGE);
                } else {
                    openFileChooserDialog();
                }
            }
        });

        btn_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidForm()) {
                    if (CommonUtils.isActiveNetwork(mContext)) {
                        new AddMemberAsyncTask().execute();
                    } else {
                        saveToLocalDB(true, "");
                    }
                }
            }
        });

        et_address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard();
                    et_address.clearFocus();
                    spinner_state.requestFocus();
                    spinner_state.performClick();
                }
                return true;
            }
        });
    }

    private void saveToLocalDB(final boolean isOffline, String img_url) {
        String isFamilyHead = "no";
        String familyHeadId = mFamilyHead.getAadhaar();
        String relationshipId = mRelationshipId;
        String roleBasedUserId = AppData.getString(AddFamilyMember.this, CensusConstants.rolebased_user_id);
        String createdBy = AppData.getString(AddFamilyMember.this, CensusConstants.userid);

        final DbAsyncTask dbATask = new DbAsyncTask(AddFamilyMember.this, false, null);
        DbParameter dbParams_duty = new DbParameter();

        ArrayList<Object> parms = new ArrayList<Object>();
        parms.add(getMemberFirstName());
        parms.add(getMemberLastName());
        parms.add(getMemberPhoneNumber());
        parms.add(getMemberEmailId());
        parms.add(getMemberAddress());
        parms.add(getGender());
        parms.add(CommonUtils.calculateAge(getMemberDateOfBirth()));
        parms.add(img_url);
        parms.add((isOffline) ? mEncodedData : "");
        parms.add(relationshipId);
        //TODO : Calculate Family Size and update
        parms.add("1");
        parms.add(getMemberZipcode());
        parms.add(getMemberDateOfBirth());
        parms.add(familyHeadId);
        parms.add(isFamilyHead);
        parms.add(isOffline ? "no" : "yes");
        parms.add(mCityId);
        parms.add(mStateId);
        parms.add("india");
        parms.add(mImageType);
        parms.add("member");
        parms.add(roleBasedUserId);
        parms.add(createdBy);
        parms.add(getMemberAadhaar());

        dbParams_duty.addParamterList(parms);

        final DbAsyncParameter dbAsyncParam_duty = new DbAsyncParameter(R.string.sql_insert_members,
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

                String message = isOffline ? "No Internet Connectivity !!! your data saved successfully in local db."
                        : "Member has been added successfully.";
                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Success");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText(message);
                TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                text.setText("OK");

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                        Intent intent = new Intent(mContext, FamilyDetailsActivity.class);
                        intent.putExtra("family_head", mFamilyHead);
                        setResult(Activity.RESULT_CANCELED, intent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

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

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void dobClicked() {
        hideKeyboard();
        DialogFragment dFragment = new DatePickerFragment();
        dFragment.show(getFragmentManager(), "date picker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), R.style.datepicker, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the chosen date
            String selectedDate = year + "-" + ((month + 1) < 10 ? "0" + (month + 1) : (month + 1)) + "-" + (day < 10 ? "0" + day : day);
            et_member_dob.setError(null);
            et_member_dob.setText(selectedDate);
            validateDOB();
        }
    }

    private static void validateDOB() {
        if (!isValidDOB(getMemberDateOfBirth())) {
            et_member_dob.requestFocus();
            et_member_dob.setError("Please enter valid Date of Birth.");
        } else {
            et_aadhaar.requestFocus();
        }
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    private void openFileChooserDialog() {
        final Dialog dialog = new Dialog(AddFamilyMember.this);
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

        if (requestCode == 0 && resultCode == 0) {
            openFileChooserDialog();
        }

        Uri correctedUri = null;
        if (resultCode == RESULT_OK) {
            String realPath;
            if (requestCode == REQUEST_CAMERA) {
//                mEncodedData = "";

                //----- Correct Image Rotation ----//
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCapturedImageURI);
                    imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(mContext, mCapturedImageURI));
                    correctedUri = getImageUri(imageBitmap);
                    realPath = getRealPathFromURI(correctedUri);

                    iv_add_image_member.setImageURI(correctedUri);
                    getEncoded64ImageStringFromBitmap(imageBitmap);
                    getMimeType(realPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_GALLERY) {
//                mEncodedData = "";
                Uri selectedImageUri = data.getData();

//                realPath = getRealPathFromURI(selectedImageUri);
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(mContext, selectedImageUri));
                    correctedUri = getImageUri(imageBitmap);
                    realPath = getRealPathFromURI(correctedUri);

                    iv_add_image_member.setImageURI(correctedUri);
                    getEncoded64ImageStringFromBitmap(imageBitmap);
                    getMimeType(realPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bao);
        byte[] ba = bao.toByteArray();
        mEncodedData = Base64.encodeToString(ba, Base64.DEFAULT);
        Log.d(TAG, "mEncodedData >> " + mEncodedData);
    }

    public static void getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        String[] imageType = type.split("/");
        mImageType = imageType[1];
        Log.d(TAG, "mImageType >>> " + mImageType);
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

    private void initRelationsSpinner() {
        sp_relations = (MaterialSpinner) findViewById(R.id.spinner_relations);
        mRelationsList = CommonUtils.getRelationsList(mContext);
//        prepareRelationsList(mRelationsList);
        mRelationsAdapter = new StateListAdapter(mContext, mRelationsList);
        sp_relations.setAdapter(mRelationsAdapter);
        sp_relations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                Log.d(LOG_TAG, "Item On Position >> "+ position);
                if (position != -1) {
                    String state_id = mRelationsList.get(position).getId();
                    mRelationshipId = state_id;
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

    private void initStateSpinner() {
        spinner_state = (MaterialSpinner) findViewById(R.id.spinner_state);
//        prepareStateList();
        mStateList = CommonUtils.getStatesList(mContext);
        mStateListAdapter = new StateListAdapter(mContext, mStateList);
        spinner_state.setAdapter(mStateListAdapter);
        spinner_state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                Log.d(LOG_TAG, "Item On Position >> "+ position);
                if (position != -1) {
                    String state_id = mStateList.get(position).getId();
                    resetCitySpinner();
                    mStateId = state_id;
                    prepareCityList();
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

    private void resetCitySpinner() {
        mCityId = "-1";
        mCityList.clear();
        mCityListAdapter.notifyDataSetChanged();
        spinner_city.setSelection(0);
    }

    private void initCitySpinner() {
        spinner_city = (MaterialSpinner) findViewById(R.id.spinner_city);
        mCityListAdapter = new StateListAdapter(mContext, mCityList);
        spinner_city.setAdapter(mCityListAdapter);
        spinner_city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != -1) {
                    String city_id = mCityList.get(position).getId();
                    mCityId = city_id;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private String getMemberFirstName() {
        return et_first_name.getText().toString();
    }

    private String getMemberLastName() {
        return et_last_name.getText().toString();
    }

    private String getGender() {
        return mGender;
    }

    private static String getMemberDateOfBirth() {
        return et_member_dob.getText().toString();
    }

    private String getMemberAadhaar() {
        return et_aadhaar.getText().toString();
    }

    private String getMemberPhoneNumber() {
        return et_phone_no.getText().toString();
    }

    private String getMemberEmailId() {
        return et_email.getText().toString();
    }

    private String getMemberAddress() {
        return et_address.getText().toString();
    }

    private String getMemberZipcode() {
        return et_zipcode.getText().toString();
    }

    private boolean isValidForm() {
        boolean bStatus = true;

        if (mRelationshipId.equals("-1")) {
            bStatus = false;
            sp_relations.requestFocus();
            Toast.makeText(this, "Please select Relationship.", Toast.LENGTH_SHORT).show();
        } else if (getMemberFirstName().equals("")) {
            bStatus = false;
            et_first_name.requestFocus();
            et_first_name.setError("Please enter first name");
        } else if (getMemberLastName().equals("")) {
            bStatus = false;
            et_last_name.requestFocus();
            et_last_name.setError("Please enter last name");
        } else if (getMemberDateOfBirth().equals("")) {
            bStatus = false;
            et_member_dob.requestFocus();
            et_member_dob.setError("Please enter Date of Birth");
        } else if (!isValidDOB(getMemberDateOfBirth())) {
            bStatus = false;
            et_member_dob.requestFocus();
            et_member_dob.setError("Please select valid Date of Birth");
        } else if (!isValidAadhar()) {
            bStatus = false;
            et_aadhaar.requestFocus();
            et_aadhaar.setError("Please enter valid Aadhaar Number");
        } else if (!isValidPhoneNo()) {
            bStatus = false;
            et_phone_no.requestFocus();
            et_phone_no.setError("Please enter valid mobile number");
        } else if (getMemberEmailId().equals("")) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        } else if (!getMemberEmailId().equals("") && !getMemberEmailId().matches(emailPattern)) {
            bStatus = false;
            et_email.requestFocus();
            et_email.setError("Please enter valid email");
        } else if (getMemberAddress().equals("")) {
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
        } else if (getMemberZipcode().equals("")) {
            bStatus = false;
            et_zipcode.requestFocus();
            et_zipcode.setError("Please enter Zipcode");
        }

        return bStatus;
    }

    private static boolean isValidDOB(String memberDateOfBirth) {
        Date todaysDate = new Date();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(todaysDate);
        return ((formattedDate.compareTo(memberDateOfBirth) < 0) ? false : true);
    }

    private boolean isValidAadhar() {
        boolean bStatus = true;
        if (et_aadhaar.getText().toString().length() < 12) {
            bStatus = false;
        } else if (getMemberAadhaar().equals("")) {
            bStatus = false;
        }

        return bStatus;
    }

    private boolean isValidPhoneNo() {
        boolean bStatus = true;
        if (getMemberPhoneNumber().length() < 10) {
            bStatus = false;
        } else if (getMemberPhoneNumber().equals("")) {
            bStatus = false;
        }

        return bStatus;
    }

    private class AddMemberAsyncTask extends AsyncTask<Void, Void, String> {
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

            String userRole = AppData.getString(mContext, CensusConstants.userRole);

            parms.add(new BasicNameValuePair(CensusConstants.isFamilyHead, "no"));
//            parms.add(new BasicNameValuePair(CensusConstants.familyHeadId, family_head_id));
            parms.add(new BasicNameValuePair(CensusConstants.relationshipId, mRelationshipId));
            parms.add(new BasicNameValuePair(CensusConstants.firstName, getMemberFirstName()));
            parms.add(new BasicNameValuePair(CensusConstants.lastName, getMemberLastName()));
            parms.add(new BasicNameValuePair(CensusConstants.gender, getGender()));
            parms.add(new BasicNameValuePair(CensusConstants.dob, getMemberDateOfBirth()));
            parms.add(new BasicNameValuePair(CensusConstants.phoneNumber, "91" + getMemberPhoneNumber()));
            parms.add(new BasicNameValuePair(CensusConstants.head_aadhar_number, mFamilyHead.getAadhaar()));
            parms.add(new BasicNameValuePair(CensusConstants.aadhaar, getMemberAadhaar()));
            parms.add(new BasicNameValuePair(CensusConstants.emailId, getMemberEmailId()));
            parms.add(new BasicNameValuePair(CensusConstants.address, getMemberAddress()));
            parms.add(new BasicNameValuePair(CensusConstants.city_id, mCityId));
            parms.add(new BasicNameValuePair(CensusConstants.state_id, mStateId));
            parms.add(new BasicNameValuePair(CensusConstants.country, "india"));
            parms.add(new BasicNameValuePair(CensusConstants.zipcode, getMemberZipcode()));
            parms.add(new BasicNameValuePair(CensusConstants.userAvatar, mEncodedData));
            parms.add(new BasicNameValuePair(CensusConstants.imageType, mImageType));
            parms.add(new BasicNameValuePair(CensusConstants.userRole, "member"));
            parms.add(new BasicNameValuePair(CensusConstants.rolebased_user_id, AppData.getString(mContext, CensusConstants.rolebased_user_id)));
            parms.add(new BasicNameValuePair(CensusConstants.createdBy, AppData.getString(mContext, CensusConstants.userid)));

            /*String paramString = URLEncodedUtils.format(parms, "utf-8");
            String url = CensusConstants.BASE_URL + CensusConstants.ADD_MEMBER_URL;
            url += "?";
            url += paramString;
            Log.e(TAG, "url sending is >>> " + url);*/

            return new ConnectToServer().getDataFromUrl(CensusConstants.BASE_URL + CensusConstants.ADD_MEMBER_URL, parms);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            final Dialog customDialog = new Dialog(mContext);
            customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            customDialog.setContentView(R.layout.simple_alert);
            customDialog.setCancelable(false);

            progressDialog.dismiss();

            Log.d(TAG, "on post execute result >>> " + result);
            if (result.equals("")) {
                ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Alert");
                ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Server not Responding, please try again after sometime.");
                TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                text.setText("OK");
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        customDialog.dismiss();
                        Intent intent = new Intent(mContext, FamilyDetailsActivity.class);
                        intent.putExtra("family_head", mFamilyHead);
                        setResult(Activity.RESULT_CANCELED, intent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                    }
                });
                customDialog.show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.getString("status");
                    String status_code = jsonObject.getString("status_code");
                    String response = jsonObject.getString("response");

                    if (status.equals("success") && status_code.equals("1000")) {
                        final String image_url = jsonObject.getString("image_url");
                        saveToLocalDB(false, image_url);
                    } else if (status.equals("success") && status_code.equals("1003")) {
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
                    } else if (status.equals("error")) {
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
}
