package com.example.firebase_crash_reporting;

import com.google.firebase.crash.FirebaseCrash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Creating a custom log
        // Custom log will be sent with report
        FirebaseCrash.log("Crash Date:" + new Date().toString());

        // Report error to firebase
        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Logcat only
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onResume");
    }

    @OnClick(R.id.button)
    public void onClick(View view) {
        // Generate uncaughtException
        throw new NullPointerException("");
    }
}
