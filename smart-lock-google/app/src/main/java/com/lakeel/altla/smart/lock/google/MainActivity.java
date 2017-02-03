package com.lakeel.altla.smart.lock.google;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_GOOGLE_SIGN_IN = 1;

    private static final int REQUEST_CODE_SAVE = 2;

    private static final int REQUEST_CODE_RETRIEVE = 3;

    private Button removeCredentialButton;

    private GoogleApiClient googleApiClient;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .enableAutoManage(MainActivity.this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        // Google sign in.
        SignInButton signInButton = (SignInButton) findViewById(R.id.buttonSignIn);
        signInButton.setOnClickListener(view1 -> {
            progressDialog.show();
            signInWithGoogle();
        });

        // Request credentials.
        Button requestCredentialsButton = (Button) findViewById(R.id.buttonRequestCredential);
        requestCredentialsButton.setOnClickListener(view -> requestCredentials());

        removeCredentialButton = (Button) findViewById(R.id.buttonRemoveCredential);
        removeCredentialButton.setVisibility(View.INVISIBLE);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.progress_message_sign_in));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect.message=" + connectionResult.getErrorMessage());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (progressDialog.isShowing()) {
            progressDialog.hide();
        }

        if (requestCode == REQUEST_GOOGLE_SIGN_IN) {
            // Handle Google sign in.
            if (RESULT_OK == resultCode) {
                // Sign in success.
                GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                saveCredential(signInResult);
            } else {
                Log.e(TAG, "Failed to sign in with google.");
            }
        } else if (requestCode == REQUEST_CODE_SAVE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.toast_save_credentials, Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Canceled to save the credentials by user.");
            }
        } else if (requestCode == REQUEST_CODE_RETRIEVE) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                onCredentialRetrieved(credential);
            } else {
                Toast.makeText(this, R.string.toast_error_read_credentials, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    private void requestCredentials() {
        CredentialRequest credentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();

        Auth.CredentialsApi.request(googleApiClient, credentialRequest).setResultCallback(
                credentialRequestResult -> {
                    if (credentialRequestResult.getStatus().isSuccess()) {
                        onCredentialRetrieved(credentialRequestResult.getCredential());
                    } else {
                        resolveRetrieveResult(credentialRequestResult.getStatus());
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN);
    }

    private void silentSignInWithGoogle(Credential credential) {
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .setAccountName(credential.getId())
                        .build();

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 1, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        OptionalPendingResult<GoogleSignInResult> silentSignInResult =
                Auth.GoogleSignInApi.silentSignIn(googleApiClient);

        if (silentSignInResult != null) {
            if (silentSignInResult.isDone()) {
                Toast.makeText(MainActivity.this, R.string.toast_silent_sign_in, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.toast_error_silent_sign_in, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveCredential(GoogleSignInResult signInResult) {
        GoogleSignInAccount gsa = signInResult.getSignInAccount();
        Credential credential = new Credential.Builder(gsa.getEmail())
                .setAccountType(IdentityProviders.GOOGLE)
                .setName(gsa.getDisplayName())
                .setProfilePictureUri(gsa.getPhotoUrl())
                .build();

        Auth.CredentialsApi.save(googleApiClient, credential).setResultCallback(status -> {
            if (status.isSuccess()) {
                Log.d(TAG, "Saved:userId=" + credential.getId());
                Toast.makeText(MainActivity.this, R.string.toast_save_credentials, Toast.LENGTH_SHORT).show();
                showRemoveButton(credential);
            } else {
                if (status.hasResolution()) {
                    // Try to resolve the save request. This will prompt the user if
                    // the credential is new.
                    try {
                        status.startResolutionForResult(this, REQUEST_CODE_SAVE);
                    } catch (IntentSender.SendIntentException e) {
                        // Could not resolve the request
                        Toast.makeText(this, R.string.toast_error_save_credentials, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Request has no resolution
                    Toast.makeText(this, R.string.toast_error_save_credentials, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onCredentialRetrieved(Credential credential) {
        showRemoveButton(credential);

        String accountType = credential.getAccountType();
        if (IdentityProviders.GOOGLE.equals(accountType)) {
            // Sign the user in with information from the Credential.
            silentSignInWithGoogle(credential);
        }
    }

    private void resolveRetrieveResult(Status status) {
        if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
            // Prompt the user to choose a saved credential; do not show the hint
            // selector.
            try {
                status.startResolutionForResult(this, REQUEST_CODE_RETRIEVE);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Failed to send resolution.", e);
            }
        } else {
            // The user must create an account or sign in manually.
            Toast.makeText(getApplicationContext(), R.string.toast_message_credentials_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Unsuccessful credential request.");
        }
    }

    private void showRemoveButton(Credential credential) {
        removeCredentialButton.setVisibility(View.VISIBLE);

        removeCredentialButton.setOnClickListener(view ->
                Auth.CredentialsApi.delete(googleApiClient, credential)
                        .setResultCallback(status -> {
                            if (status.isSuccess()) {
                                Toast.makeText(this, R.string.toast_remove_credentials, Toast.LENGTH_SHORT).show();
                                Auth.CredentialsApi.disableAutoSignIn(googleApiClient);
                                removeCredentialButton.setVisibility(View.INVISIBLE);
                            } else {
                                Log.e(TAG, "Failed to remove the credentials.");
                            }
                        }));
    }
}
