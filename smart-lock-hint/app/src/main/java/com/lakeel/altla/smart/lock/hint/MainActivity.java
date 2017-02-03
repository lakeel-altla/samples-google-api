package com.lakeel.altla.smart.lock.hint;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_HINT = 1;

    private static final String USER_ID = "userId";

    private GoogleApiClient googleApiClient;

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

        Button button = (Button) findViewById(R.id.buttonHint);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HintRequest hintRequest = new HintRequest.Builder()
                        .setHintPickerConfig(new CredentialPickerConfig.Builder()
                                .setShowCancelButton(true)
                                .build())
                        .setEmailAddressIdentifierSupported(true)
                        .setAccountTypes(IdentityProviders.GOOGLE)
                        .build();

                PendingIntent intent =
                        Auth.CredentialsApi.getHintPickerIntent(googleApiClient, hintRequest);
                try {
                    startIntentSenderForResult(intent.getIntentSender(), REQUEST_CODE_HINT, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not start hint picker Intent", e);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                Intent intent;
                // Check for the user ID in your user database.
                if (isAlreadySignUpped(credential.getId())) {
                    intent = new Intent(this, SignInActivity.class);
                } else {
                    intent = new Intent(this, SignUpNewUserActivity.class);
                }
                intent.putExtra(getPackageName(), credential);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.toast_error_read_hint, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect.message=" + connectionResult.getErrorMessage());
    }

    private boolean isAlreadySignUpped(String id) {
        SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String userId = preferences.getString(USER_ID, "");
        return userId.length() != 0 && userId.equals(id);
    }
}