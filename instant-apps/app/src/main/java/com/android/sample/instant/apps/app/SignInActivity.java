package com.android.sample.instant.apps.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.sample.instant.apps.feature.Feature1Activity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ResultCodes;

import java.util.Collections;

public final class SignInActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Button signInButton = findViewById(R.id.buttonSignIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch the google activity.
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
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                Intent intent = new Intent(this, Feature1Activity.class);
                startActivity(intent);
            } else {
                // Sign in failed.
                Toast.makeText(SignInActivity.this.getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
