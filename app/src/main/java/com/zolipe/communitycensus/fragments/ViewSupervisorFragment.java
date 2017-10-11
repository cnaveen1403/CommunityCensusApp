package com.zolipe.communitycensus.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zolipe.communitycensus.R;
import com.zolipe.communitycensus.model.SupervisorObj;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewSupervisorFragment extends Fragment {

    View rootView;
    Context mContext;
    Activity mActivity;
    SupervisorObj mSupervisorObj;

    TextView tv_first_name, tv_last_name, tv_age, tv_gender, tv_phone, tv_aadhar, tv_email, tv_address, tv_zipcode;
    CircleImageView iv_image;

    public ViewSupervisorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_view_supervisor, container, false);

        init (rootView);
        mContext = this.getActivity();
        mActivity = getActivity();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mSupervisorObj = bundle.getParcelable("Supervisor");
            tv_first_name.setText(mSupervisorObj.getFirst_name());
            tv_last_name.setText(mSupervisorObj.getLast_name());
            tv_last_name.setText(mSupervisorObj.getLast_name());
            tv_gender.setText(mSupervisorObj.getGender());
            tv_age.setText(mSupervisorObj.getAge());
            tv_phone.setText(mSupervisorObj.getPhone_number());
            tv_aadhar.setText(mSupervisorObj.getAadhaar());
            tv_email.setText(mSupervisorObj.getEmail());
            tv_address.setText(mSupervisorObj.getAddress());
            tv_zipcode.setText(mSupervisorObj.getZipcode());

            Glide.with(this).load(mSupervisorObj.getImage_url())
                    .crossFade()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_supervisor_list)
                    .into(iv_image);
        }

        iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog nagDialog = new Dialog(mContext ,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(false);
                nagDialog.setContentView(R.layout.preview_image);
                Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
                ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
                Glide.with(mContext).load(mSupervisorObj.getImage_url())
                        .crossFade()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_supervisor_list)
                        .into(ivPreview);
                ivPreview.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        nagDialog.dismiss();
                    }
                });
                nagDialog.show();
            }
        });

        return rootView;
    }

    private void init (View rootView){
        tv_first_name= (TextView) rootView.findViewById(R.id.tv_first_name_value);
        tv_last_name= (TextView) rootView.findViewById(R.id.tv_last_name_value);
        tv_gender = (TextView) rootView.findViewById(R.id.tv_gender_value);
        tv_age = (TextView) rootView.findViewById(R.id.tv_age_value);
        tv_phone = (TextView) rootView.findViewById(R.id.tv_phone_value);
        tv_aadhar = (TextView) rootView.findViewById(R.id.tv_aadhaar_value);
        tv_email = (TextView) rootView.findViewById(R.id.tv_email_value);
        tv_address = (TextView) rootView.findViewById(R.id.tv_address_value);
        tv_zipcode = (TextView) rootView.findViewById(R.id.tv_zipcode_value);
        iv_image = (CircleImageView) rootView.findViewById(R.id.iv_image);
    }
}
