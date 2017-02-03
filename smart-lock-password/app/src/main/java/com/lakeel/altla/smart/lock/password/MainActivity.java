package com.lakeel.altla.smart.lock.password;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_SAVE = 1;

    private static final int REQUEST_CODE_RETRIEVE = 2;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        CredentialRequest credentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        Auth.CredentialsApi.request(googleApiClient, credentialRequest).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(@NonNull CredentialRequestResult credentialRequestResult) {
                        if (credentialRequestResult.getStatus().isSuccess()) {
                            // See "Handle successful credential requests"
                            onCredentialRetrieved(credentialRequestResult.getCredential());
                        } else {
                            // See "Handle unsuccessful and incomplete credential requests"
                            resolveRetrieveResult(credentialRequestResult.getStatus());
                        }
                    }
                });


        final EditText emailEditText = (EditText) findViewById(R.id.editTextEmail);
        final EditText passwordEditText = (EditText) findViewById(R.id.editTextPassword);

        Button signInButton = (Button) findViewById(R.id.buttonSignIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                signInWithEmailPassword(email, password);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to Google API Client.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended:" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google API Client:message=" + connectionResult.getErrorMessage());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SAVE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "SAVE: OK");
                Toast.makeText(this, "Credentials saved.", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "SAVE: Canceled by user.");
            }
        } else if (requestCode == REQUEST_CODE_RETRIEVE) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                onCredentialRetrieved(credential);
            } else {
                Log.e(TAG, "Credential Read: NOT OK");
                Toast.makeText(this, "Credential Read Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signInWithEmailPassword(String email, String password) {
        if (checkUserAuthentication(email, password)) {
            Credential credential = new Credential.Builder(email)
                    .setPassword(password)
                    .build();

            Auth.CredentialsApi.save(googleApiClient, credential).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Toast.makeText(MainActivity.this, "Sign in success.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (status.hasResolution()) {
                            // Try to resolve the save request. This will prompt the user if
                            // the credential is new.
                            try {
                                status.startResolutionForResult(MainActivity.this, REQUEST_CODE_SAVE);
                            } catch (IntentSender.SendIntentException e) {
                                // Could not resolve the request.
                                Log.e(TAG, "STATUS: Failed to send resolution.", e);
                                Toast.makeText(MainActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Request has no resolution.
                            Toast.makeText(MainActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Log.e(TAG, "Failed to sign in.");
            Toast.makeText(MainActivity.this, "Sign in failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkUserAuthentication(String email, String password) {
        if ("xxx@example.com".equals(email) && "000000".equals(password)) {
            return true;
        }
        if ("yyy@example.com".equals(email) && "111111".equals(password)) {
            return true;
        }
        return true;
    }

    private void onCredentialRetrieved(Credential credential) {
        String accountType = credential.getAccountType();
        if (accountType == null) {
            // Sign the user in with information from the Credential.
            signInWithEmailPassword(credential.getId(), credential.getPassword());
        }
    }

    private void resolveRetrieveResult(Status status) {
        if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
            // Prompt the user to choose a saved credential; do not show the hint
            // selector.
            try {
                status.startResolutionForResult(this, REQUEST_CODE_RETRIEVE);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "STATUS: Failed to send resolution.", e);
            }
        } else {
            // The user must create an account or sign in manually.
            Log.e(TAG, "STATUS: Unsuccessful credential request.");
        }
    }
}
