package com.zolipe.communitycensus.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zolipe.communitycensus.R;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        String message = getIntent().getExtras().getString("error_message");
        ((TextView)findViewById(R.id.tv_error)).setText(message);
    }
}
