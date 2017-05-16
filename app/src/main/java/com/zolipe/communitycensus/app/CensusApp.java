package com.zolipe.communitycensus.app;

import android.app.Application;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

import static com.zolipe.communitycensus.util.CensusConstants.TWITTER_KEY;
import static com.zolipe.communitycensus.util.CensusConstants.TWITTER_SECRET;

public class CensusApp extends Application {
//    private AuthCallback authCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        /*TwitterAuthConfig authConfig =  new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());
        authCallback = new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                // Do something with the session
            }

            @Override
            public void failure(DigitsException exception) {
                // Do something on failure
            }
        };*/
    }

    /*public AuthCallback getAuthCallback(){
        return authCallback;
    }*/
}
