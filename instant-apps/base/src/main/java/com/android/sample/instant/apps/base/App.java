package com.android.sample.instant.apps.base;

import android.app.Application;
import android.util.Log;

import com.android.sample.instant.apps.BuildConfig;
import com.google.firebase.FirebaseApp;

public final class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "application onCreate");
        Log.i(TAG, "version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        FirebaseApp.initializeApp(this);
    }
}