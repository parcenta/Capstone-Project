package com.peterarkt.customerconnect;

import android.app.Application;
import timber.log.Timber;

public class CustomerConnectApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set the Timber
        Timber.plant(new Timber.DebugTree());

    }
}