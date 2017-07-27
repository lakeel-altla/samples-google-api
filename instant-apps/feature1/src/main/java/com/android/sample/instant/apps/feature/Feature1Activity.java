package com.android.sample.instant.apps.feature;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.instantapps.InstantApps;

import java.util.Collections;

public final class Feature1Activity extends AppCompatActivity {

    private static final String TAG = Feature1Activity.class.getSimpleName();

    private static final int REQUEST_CODE_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature1);

        setTitle("Feature1");

        if (InstantApps.isInstantApp(this)) {
            // Instant app.
            Log.d(TAG, "Instant app.");

            Intent intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(
                            Collections.singletonList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                                            .build()
                            )
                    )
                    .build();
            startActivityForResult(intent, REQUEST_CODE_SIGN_IN);
        } else {
            // Installed app.
            Log.d(TAG, "Installed app.");
        }

        Button buttonShowFeature2Activity = findViewById(R.id.buttonShowFeature2Activity);
        buttonShowFeature2Activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show Feature2Activity.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/feature2"));
                intent.setPackage(getPackageName());
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                Log.d(TAG, "Signed in.");
            } else {
                Log.e(TAG, "Not signed in.");
            }
        }
    }
}