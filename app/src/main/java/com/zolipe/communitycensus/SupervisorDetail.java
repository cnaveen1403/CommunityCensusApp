package com.zolipe.communitycensus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.model.SupervisorObj;

import de.hdodenhof.circleimageview.CircleImageView;

public class SupervisorDetail extends AppCompatActivity {

    TextView tv_name, tv_age, tv_gender, tv_phone, tv_aadhar, tv_email, tv_address, tv_zipcode;
    CircleImageView iv_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();
        Intent i = getIntent();
        if (i.getExtras() != null) {
            SupervisorObj tempObj = i.getExtras().getParcelable("SupervisorObj");
            tv_name.setText(tempObj.getFirst_name() + " " + tempObj.getLast_name());
            tv_age.setText(tempObj.getAge());
            tv_gender.setText(tempObj.getGender());
            tv_phone.setText(tempObj.getPhone_number());
            tv_aadhar.setText(tempObj.getAadhaar());
            tv_email.setText(tempObj.getEmail());
            tv_address.setText(tempObj.getAddress());
            tv_zipcode.setText(tempObj.getZipcode());

            Glide.with(this).load(tempObj.getImage_url())
                    .crossFade()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_supervisor_list)
                    .into(iv_image);
        }
    }

    private void init() {
        tv_name = (TextView) findViewById(R.id.tv_name_value);
        tv_age = (TextView) findViewById(R.id.tv_age_value);
        tv_gender = (TextView) findViewById(R.id.tv_gender_value);
        tv_phone = (TextView) findViewById(R.id.tv_phone_value);
        tv_aadhar = (TextView) findViewById(R.id.tv_aadhaar_value);
        tv_email = (TextView) findViewById(R.id.tv_email_value);
        tv_address = (TextView) findViewById(R.id.tv_address_value);
        tv_zipcode = (TextView) findViewById(R.id.tv_zipcode_value);
        iv_image = (CircleImageView)findViewById(R.id.iv_image);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
