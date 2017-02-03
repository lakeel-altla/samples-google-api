package com.lakeel.altla.smart.lock.hint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;

public final class SignUpNewUserActivity extends AppCompatActivity {

    private static final String USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setTitle(R.string.title_sign_up);

        // Get the credentials from intent.
        Intent intent = getIntent();
        final Credential credential = intent.getParcelableExtra(getPackageName());

        EditText nameEditText = (EditText) findViewById(R.id.editTextName);
        EditText emailEditText = (EditText) findViewById(R.id.editTextEmail);

        nameEditText.setText(credential.getName());
        emailEditText.setText(credential.getId());

        Button signUpButton = (Button) findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), R.string.toast_success, Toast.LENGTH_SHORT).show();

                saveUserId(credential.getId());
                restartApp();
            }
        });
    }

    private void saveUserId(String id) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USER_ID, id);
        editor.apply();
    }

    private void restartApp() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        SignUpNewUserActivity.this.finish();
    }

}
