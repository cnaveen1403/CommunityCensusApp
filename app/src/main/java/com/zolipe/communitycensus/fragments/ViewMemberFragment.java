package com.zolipe.communitycensus.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.zolipe.communitycensus.model.FamilyHead;

public class ViewMemberFragment extends Fragment {

    private final String TAG = "ViewMemberFragment";
    View rootView;
    ImageView iv_profileImage;
    TextView tv_first_name, tv_last_name, tv_gender, tv_dob, tv_phone_num, tv_aadhar,
            tv_email, tv_address, tv_zipcode;
    FamilyHead member;
    Context mContext;
    Activity mActivity;

    public ViewMemberFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_view_member, container, false);

        init (rootView);
        mContext = this.getActivity();
        mActivity = getActivity();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            member = bundle.getParcelable("Member");
            tv_first_name.setText(member.getFirst_name() + " " + member.getLast_name());
            tv_last_name.setText(member.getLast_name());
            tv_gender.setText(member.getGender());
            tv_dob.setText(member.getAge());
            tv_phone_num.setText(member.getPhone_number());
            tv_aadhar.setText(member.getAadhaar());
            tv_email.setText(member.getEmail());
            tv_address.setText(member.getAddress());
            tv_zipcode.setText(member.getZipcode());

            Glide.with(this).load(member.getImage_url())
                    .crossFade()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_supervisor_list)
                    .into(iv_profileImage);
        }

        iv_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog nagDialog = new Dialog(mContext ,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                nagDialog.setCancelable(false);
                nagDialog.setContentView(R.layout.preview_image);
                Button btnClose = (Button)nagDialog.findViewById(R.id.btnIvClose);
                ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
                Glide.with(mContext).load(member.getImage_url())
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
        tv_first_name = (TextView) rootView.findViewById(R.id.tv_first_name);
        tv_last_name = (TextView) rootView.findViewById(R.id.tv_last_name);
        tv_gender = (TextView) rootView.findViewById(R.id.tv_gender);
        tv_dob = (TextView) rootView.findViewById(R.id.tv_dob);
        tv_phone_num = (TextView) rootView.findViewById(R.id.tv_phone_num);
        tv_aadhar = (TextView) rootView.findViewById(R.id.tv_aadhar);
        tv_email = (TextView) rootView.findViewById(R.id.tv_email);
        tv_address = (TextView) rootView.findViewById(R.id.tv_address);
        tv_zipcode = (TextView) rootView.findViewById(R.id.tv_zipcode);
        iv_profileImage = (ImageView) rootView.findViewById(R.id.iv_profileImage);
    }
}
