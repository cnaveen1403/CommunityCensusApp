package com.zolipe.communitycensus.app;

import android.app.Application;

import com.zolipe.communitycensus.util.CensusConstants;
import com.zolipe.communitycensus.util.CensusReceiver;

public class CensusApp extends Application {
    private static CensusApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized CensusApp getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(CensusReceiver.ConnectivityReceiverListener listener) {
        if (AppData.getBoolean(this, CensusConstants.isLoggedIn) == true)
            CensusReceiver.connectivityReceiverListener = listener;
    }
}
