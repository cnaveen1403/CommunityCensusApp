package com.zolipe.communitycensus.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.app.AppData;
import com.zolipe.communitycensus.permissions.PermissionsActivity;
import com.zolipe.communitycensus.permissions.PermissionsChecker;
import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.ConnectToServer;
import com.zolipe.communitycensus.util.SelectDocument;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class TelegramBroadcast extends AppCompatActivity implements View.OnClickListener {

    PermissionsChecker checker;
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private LinearLayout mRevealView;
    private boolean hidden = true;
    private ImageButton gallery_btn, photo_btn, audio_btn;
    EditText et_input;
    ImageButton sendMessageButton;
    CheckBox chk_is_group_message;
    private static final int IMAGE_PICKER_SELECT = 7, REQUEST_CAMERA = 2, REQUEST_AUDIO = 1;
    String selectedFilePath, selectedFilePathAudio,
            selectedFilePathDoc;
    private Context mContext;
    private Uri mCapturedImageURI;
    FrameLayout fl_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram_broadcast);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        checker = new PermissionsChecker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
            startPermissionsActivity(PERMISSIONS_READ_STORAGE);
        } else {
            initView();
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = TelegramBroadcast.this;
        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.GONE);

        gallery_btn = (ImageButton) findViewById(R.id.gallery_img_btn);
        photo_btn = (ImageButton) findViewById(R.id.photo_img_btn);
        audio_btn = (ImageButton) findViewById(R.id.audio_img_btn);
        et_input = (EditText) findViewById(R.id.et_input);
        sendMessageButton = (ImageButton) findViewById(R.id.sendMessageButton);
        chk_is_group_message = (CheckBox) findViewById(R.id.chk_is_group_message);
        fl_background = (FrameLayout) findViewById(R.id.fl_background);
//        video_btn = (ImageButton) findViewById(R.id.video_img_btn);
//        location_btn = (ImageButton) findViewById(R.id.location_img_btn);
//        contact_btn = (ImageButton) findViewById(R.id.contact_img_btn);

        gallery_btn.setOnClickListener(this);
        photo_btn.setOnClickListener(this);
        audio_btn.setOnClickListener(this);
        sendMessageButton.setOnClickListener(this);
        fl_background.setOnClickListener(this);
//        audio_btn.setOnClickListener(this);
//        location_btn.setOnClickListener(this);
//        contact_btn.setOnClickListener(this);
    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onClick(View v) {
        hideRevealView();
        switch (v.getId()) {

            case R.id.gallery_img_btn:
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
                break;
            case R.id.photo_img_btn:
                String fileName = "temp.jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, fileName);
                mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                startActivityForResult(intent, REQUEST_CAMERA);
                break;
            case R.id.audio_img_btn:
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, REQUEST_AUDIO);
                break;
            case R.id.sendMessageButton:
                String sms_text = et_input.getText().toString();
                if (sms_text.equals("")) {
                    et_input.setError("Please enter your message !!!");
                    return;
                }
                String URL = CensusConstants.BASE_URL + CensusConstants.SEND_TELEGRAM_URL;
                String key = "message";
                if (chk_is_group_message.isChecked()) {
                    key = "text_msg";
                    URL = CensusConstants.BASE_URL + CensusConstants.SEND_GROUP_TELEGRAM_URL;
                }

                new sendTelegramAsyncTask().execute(sms_text, URL, key);
                break;
            case R.id.fl_background:
                hideRevealView();
                break;
           /* case R.id.audio_img_btn:

                break;
            case R.id.location_img_btn:

                break;
            case R.id.contact_img_btn:

                break;*/
        }
    }

    private void hideRevealView() {
        if (mRevealView.getVisibility() == View.VISIBLE) {
            mRevealView.setVisibility(View.GONE);
            hidden = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.broadcast_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clip:

                int cx = (mRevealView.getLeft() + mRevealView.getRight());
                int cy = mRevealView.getTop();
                int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

                //Below Android LOLIPOP Version
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    SupportAnimator animator =
                            ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(700);

                    SupportAnimator animator_reverse = animator.reverse();

                    if (hidden) {
                        mRevealView.setVisibility(View.VISIBLE);
                        animator.start();
                        hidden = false;
                    } else {
                        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                            @Override
                            public void onAnimationStart() {

                            }

                            @Override
                            public void onAnimationEnd() {
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;

                            }

                            @Override
                            public void onAnimationCancel() {

                            }

                            @Override
                            public void onAnimationRepeat() {

                            }
                        });
                        animator_reverse.start();
                    }
                }
                // Android LOLIPOP And ABOVE Version
                else {
                    if (hidden) {
                        Animator anim = android.view.ViewAnimationUtils.
                                createCircularReveal(mRevealView, cx, cy, 0, radius);
                        mRevealView.setVisibility(View.VISIBLE);
                        anim.start();
                        hidden = false;
                    } else {
                        Animator anim = android.view.ViewAnimationUtils.
                                createCircularReveal(mRevealView, cx, cy, radius, 0);
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;
                            }
                        });
                        anim.start();
                    }
                }
                return true;

            case android.R.id.home:
                supportFinishAfterTransition();
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class sendTelegramAsyncTask extends AsyncTask<String, Void, String> {
        final Dialog progressDialog = new Dialog(TelegramBroadcast.this, R.style.progress_dialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("Please wait ...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Toast.makeText(mContext, "Your message has been sent succesfully !!!", Toast.LENGTH_SHORT).show();
            et_input.setText("");
            if (chk_is_group_message.isChecked())
                chk_is_group_message.setChecked(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String sms_text = params[0];
            String url = params[1];
            String key = params[2];
            List<NameValuePair> parms = new LinkedList<NameValuePair>();
            parms.add(new BasicNameValuePair(key, sms_text));
            return new ConnectToServer().getDataFromUrl(url, parms);
        }
    }

    private void SendImageMultiPart(String caption) throws Exception {
        final Dialog progressDialog = new Dialog(TelegramBroadcast.this, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("Please wait ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        File file = new File(selectedFilePath);
        String contentType = getMimeType(selectedFilePath);
        if (contentType == null) {
            contentType = "image/jpeg";
//              contentType = "video/mp4";
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        final String filename = "file_" + System.currentTimeMillis() / 1000L;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //        candidate_id =>candidate id which you will receive while log in
                //        scheduled_question_id => question id you will receive while calling interview question
                //        video_file => it supports mp4 and file size upto 100mb

                .addFormDataPart("caption", caption)
                .addFormDataPart("file", filename + ".jpg", fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://zovis.in/telegramphoto/image")
//                  .header("x-api-key", "230ad774-e7c3-42ef-afff-d0f96018e0d7")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        final Dialog customDialog = new Dialog(TelegramBroadcast.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Error occurred !! Please try again.");
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                        customDialog.show();
                        Log.d("NewFarm", "nah it is not done ya");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, "Your image has been sent successfully.", Toast.LENGTH_SHORT).show();
                        Log.d("Response ", "Response == >>> " + response.toString());
                        Log.d("Response ", "Response == >>> " + response.message().toString());

                    }
                });
            }
        });
    }

    private void execMultipartPostAudio() throws Exception {
        final Dialog progressDialog = new Dialog(TelegramBroadcast.this, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("Please wait ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        File file = new File(selectedFilePathAudio);
        String contentType = getMimeType(selectedFilePathAudio);
        if (contentType == null) {
            contentType = "audio/mpeg";
//              contentType = "video/mp4";
        }

        Log.e("UploadToServerActivity", "file: " + file.getPath());
        Log.e("UploadToServerActivity", "contentType: " + contentType);

        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        final String filename = "file_" + System.currentTimeMillis() / 1000L;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //        candidate_id =>candidate id which you will receive while log in
                //        scheduled_question_id => question id you will receive while calling interview question
                //        video_file => it supports mp4 and file size upto 100mb

//                  .addFormDataPart("caption", "Pavan")
                .addFormDataPart("file", filename + ".mp3", fileBody)
                .build();

        Request request = new Request.Builder()
                .url("http://zovis.in/telegramaudio/audio")
//                  .header("x-api-key", "230ad774-e7c3-42ef-afff-d0f96018e0d7")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("UploadToServerActivity", "Error: " + e.getLocalizedMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        final Dialog customDialog = new Dialog(TelegramBroadcast.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Error occurred !! Please try again.");
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                        customDialog.show();
                        Log.d("NewFarm", "nah it is not done ya");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.d("Response ", "Response == >>> " + response.toString());
                        Log.d("Response ", "Response == >>> " + response.message().toString());

                    }
                });
            }
        });
    }


    private void execMultipartPostDoc() throws Exception {
        final Dialog progressDialog = new Dialog(TelegramBroadcast.this, R.style.progress_dialog);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
        msg.setText("Please wait ...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        File file = new File(selectedFilePathDoc);
        String contentType = getMimeType(selectedFilePathDoc);
        if (contentType == null) {
            contentType = "docx/pdf";
//              contentType = "video/mp4";
        }

        Log.e("UploadToServerActivity", "file: " + file.getPath());
        Log.e("UploadToServerActivity", "contentType: " + contentType);

        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        final String filename = "file_" + System.currentTimeMillis() / 1000L;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //        candidate_id =>candidate id which you will receive while log in
                //        scheduled_question_id => question id you will receive while calling interview question
                //        video_file => it supports mp4 and file size upto 100mb

//                  .addFormDataPart("caption", "Pavan")
                .addFormDataPart("file", "words.pdf", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(" http://zovis.in/telegramdocument/document")
//                  .header("x-api-key", "230ad774-e7c3-42ef-afff-d0f96018e0d7")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("UploadToServerActivity", "Error: " + e.getLocalizedMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        final Dialog customDialog = new Dialog(TelegramBroadcast.this);
                        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        customDialog.setContentView(R.layout.simple_alert);
                        ((TextView) customDialog.findViewById(R.id.dialogTitleTV)).setText("Error");
                        ((TextView) customDialog.findViewById(R.id.dialogMessage)).setText("Error occurred !! Please try again.");
                        TextView text = (TextView) customDialog.findViewById(R.id.cancelTV);
                        text.setText("OK");

                        text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                customDialog.dismiss();
                            }
                        });
                        customDialog.show();
                        Log.d("NewFarm", "nah it is not done ya");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.d("Response ", "Response == >>> " + response.toString());
                        Log.d("Response ", "Response == >>> " + response.message().toString());

                    }
                });
            }
        });
    }

    private String getMimeType(String realPath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(realPath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICKER_SELECT) {
                Uri selectedMediaUri = data.getData();
                if (selectedMediaUri.toString().contains("image")) {
                    //handle video
                    File f = new File(getRealPathFromURI(getApplicationContext(), selectedMediaUri));
                    selectedFilePath = f.getPath();
                    showImageSendDialog(selectedMediaUri);
                    /*try {
                        SendImageMultiPart();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                }
            } else if (requestCode == REQUEST_CAMERA) {
                Uri selectedMediaUri = mCapturedImageURI;
                //handle video
                File f = new File(getRealPathFromURI(getApplicationContext(), selectedMediaUri));
                selectedFilePath = f.getPath();
                showImageSendDialog(selectedMediaUri);
                /*try {
                    SendImageMultiPart();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            } else if (requestCode == REQUEST_AUDIO) {
                //the selected audio.
                Uri uri = data.getData();
                File f = new File(getRealPathFromURI(getApplicationContext(), uri));
                selectedFilePathAudio = f.getPath();
                try {
                    execMultipartPostAudio();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }

    private void showImageSendDialog(final Uri selectedMediaUri) {
        final Dialog dialog = new Dialog(mContext, R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.telegram_send_image);

        Button btn_no = (Button) dialog.findViewById(R.id.btn_cancel);
        Button btn_yes = (Button) dialog.findViewById(R.id.btn_send);
        final ImageView image = (ImageView) dialog.findViewById(R.id.ivAddProfileImage);
        EditText editText = (EditText)dialog.findViewById(R.id.et_caption);
        final String caption = editText.getText().toString();

        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), selectedMediaUri);
            imageBitmap = imageOrientationValidator(imageBitmap, SelectDocument.getPath(mContext, selectedMediaUri));
            Uri correctedUri = getImageUri(imageBitmap);
            image.setImageURI(correctedUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                mLoggedIn = false;
//                mDigitsSession = null;
                AppData.saveBoolean(getApplicationContext(), CensusConstants.isLoggedIn, false);
                AppData.clearPreferences(getApplicationContext());
                finishAffinity();
                Intent intent = new Intent(getApplicationContext(), LoginSignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
        });

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    SendImageMultiPart(caption);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();
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

    private Uri getImageUri(Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Context ctx, Uri contentUri) {
        String res = null;
       /* String[] proj = {MediaStore.Images.Media.DATA};*/
       /* Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);*/
        Cursor cursor = ctx.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor.moveToFirst()) {

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        } else {
            res = contentUri.getPath();
        }
        cursor.close();
        return res;
    }

    private class SendLocationAsyncTask extends AsyncTask<Void, Void, String> {
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
            parms.add(new BasicNameValuePair("latitude", "12.9716"));
            parms.add(new BasicNameValuePair("longitude", "77.5946"));
            return new ConnectToServer().getDataFromUrl("http://zovis.in/telegramlocation/location", parms);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
//            Log.e(LOG_TAG, "Result LAVA " + result);
        }
    }
}

