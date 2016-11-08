package com.example.google_places;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PLACE_PICKER = 1;

    public static final String PLACE_REPORT_REVIEW = "review";

    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.textView)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(MainActivity.this);

        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        // Set google api client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Show selected place from the place picker
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended:code=" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:Result=" + connectionResult.toString());
    }

    @OnClick(R.id.button_place_picker)
    public void launchPlacePicker(View view) {
        if (mGoogleApiClient.isConnected()) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(MainActivity.this), REQUEST_PLACE_PICKER);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.button_fetch)
    public void fetchCurrentPlaces(View view) {

        // Fetch current places

        if (mGoogleApiClient.isConnected()) {
            // Check permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                        .getCurrentPlace(mGoogleApiClient, null);

                result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                        StringBuilder builder = new StringBuilder();

                        // The likelihood provides a relative probability of the place being the best match
                        // within the list of returned places for a single request.
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            String formattedString = String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood());

                            builder.append(formattedString);
                            builder.append(System.getProperty("line.separator"));
                        }
                        mTextView.setText(builder.toString());

                        likelyPlaces.release();
                    }
                });
            } else {
                Log.w(TAG, "Permission not granted");
            }
        }
    }

    @OnClick(R.id.button_add)
    public void addPlace(View view) {

        // Add place
        // Newly-added places enter a moderation queue to be considered for addition to the Google Places database

        // Set added place
        final AddPlaceRequest place =
                new AddPlaceRequest(
                        "Manly Sea Life Sanctuary", // Name
                        new LatLng(-33.7991, 151.2813), // Latitude and longitude
                        "W Esplanade, Manly NSW 2095", // Address
                        Collections.singletonList(Place.TYPE_AQUARIUM), // Place types
                        "+61 1800 199 742", // Phone number
                        Uri.parse("http://www.manlysealifesanctuary.com.au/") // Website
                );

        Places.GeoDataApi.addPlace(mGoogleApiClient, place)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer placeBuffer) {
                        Log.d(TAG, "Place add result: " + placeBuffer.getStatus().toString());

                        for (Place place : placeBuffer) {
                            Log.d(TAG, "Added place: " + place.getName().toString());
                            Log.d(TAG, "Place ID: " + place.getId());
                        }

                        Toast.makeText(MainActivity.this, R.string.toast_message_added, Toast.LENGTH_LONG).show();
                        placeBuffer.release();
                    }
                });
    }

    @OnClick(R.id.button_report)
    public void reportPlace(View view) {

        // Report place to help Google build a local model of the world

        // First, fetch current places
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        String placeId = placeLikelihood.getPlace().getId();

                        // Second, report current place
                        PlaceReport report = PlaceReport.create(placeId, PLACE_REPORT_REVIEW);
                        Places.PlaceDetectionApi.reportDeviceAtPlace(mGoogleApiClient, report)
                                .setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        if (status.isSuccess()) {
                                            Log.d(TAG, "Report place result: " + status.toString());
                                            Toast.makeText(MainActivity.this, R.string.toast_message_reported, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }

                    likelyPlaces.release();
                }
            });
        } else {
            Log.w(TAG, "Permission not granted");
        }
    }
}