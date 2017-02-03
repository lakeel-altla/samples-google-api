package com.lakeel.altla.smart.lock.hint;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.credentials.Credential;

public final class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        setTitle(R.string.title_sign_in);

        // Get the credentials from intent.
        Intent intent = getIntent();
        Credential credential = intent.getParcelableExtra(getPackageName());

        EditText emailEditText = (EditText) findViewById(R.id.editTextEmail);
        emailEditText.setText(credential.getId());

        Button signInButton = (Button) findViewById(R.id.buttonSignIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartApp();
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        SignInActivity.this.finish();
    }
}
