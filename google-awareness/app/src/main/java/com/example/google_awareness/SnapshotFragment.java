package com.example.google_awareness;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.BeaconStateResult;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.BeaconState;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class SnapshotFragment extends Fragment {

    private static final String TAG = SnapshotFragment.class.getSimpleName();

    // NOTE: You must set this filter if you want to detect beacons in gradle.properties.
    // See https://developers.google.com/beacons/dashboard
    private static final BeaconState.TypeFilter BEACON_TYPE_FILTER = BeaconState.TypeFilter.with(
            BuildConfig.beaconAttachmentNamespace, // namespace
            BuildConfig.beaconAttachmentType); // type

    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_snapshot, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle("Snapshot");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Awareness.API)
                .build();
        mGoogleApiClient.connect();
    }


    @OnClick(R.id.button_detect)
    public void fetchActivityState(View view) {
        if (isConnected()) {
            Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<DetectedActivityResult>() {

                        @Override
                        public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                            if (!detectedActivityResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get the current activity.");
                                return;
                            }

                            ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                            DetectedActivity probableActivity = ar.getMostProbableActivity();
                            Log.i(TAG, probableActivity.toString());
                        }
                    });
        }
    }

    @OnClick(R.id.button_beacon)
    public void fetchBeaconState(View view) {
        if (isConnected()) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Awareness.SnapshotApi.getBeaconState(mGoogleApiClient, BEACON_TYPE_FILTER)
                        .setResultCallback(new ResultCallback<BeaconStateResult>() {
                            @Override
                            public void onResult(@NonNull BeaconStateResult beaconStateResult) {
                                if (!beaconStateResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Could not get beacon state.");
                                    return;
                                }

                                BeaconState beaconState = beaconStateResult.getBeaconState();
                                if (beaconState == null) {
                                    Log.d(TAG, "No beacon found");
                                    return;
                                }

                                Log.d(TAG, beaconState.toString());
                            }
                        });
            }
        }
    }

    @OnClick(R.id.button_headphone)
    public void fetchHeadphoneState(View view) {
        Awareness.SnapshotApi.getHeadphoneState(mGoogleApiClient)
                .setResultCallback(new ResultCallback<HeadphoneStateResult>() {
                    @Override
                    public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                        if (!headphoneStateResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get headphone state.");
                            return;
                        }

                        HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                        if (headphoneState.getState() == HeadphoneState.PLUGGED_IN) {
                            Log.i(TAG, "Headphones are plugged in.\n");
                        } else {
                            Log.i(TAG, "Headphones are NOT plugged in.\n");
                        }
                    }
                });
    }

    @OnClick(R.id.button_location)
    public void fetchLocationState(View view) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            if (!locationResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get location.");
                                return;
                            }

                            Location location = locationResult.getLocation();
                            Log.i(TAG, "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                        }
                    });
        }
    }

    @OnClick(R.id.button_places)
    public void fetchPlaces(View view) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getPlaces(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<PlacesResult>() {
                        @Override
                        public void onResult(@NonNull PlacesResult placesResult) {
                            if (!placesResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get places.");
                                return;
                            }

                            // Show locations
                            List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                            if (placeLikelihoodList != null && 0 > placeLikelihoodList.size()) {
                                for (PlaceLikelihood placeLikelihood : placeLikelihoodList) {
                                    Log.i(TAG, placeLikelihood.getPlace().getName().toString() + ", likelihood: " + placeLikelihood.getLikelihood());
                                }
                            } else {
                                Log.i(TAG, "Not found locations");
                            }
                        }
                    });
        }
    }

    @OnClick(R.id.button_weather)
    public void fetchWeatherState(View view) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<WeatherResult>() {
                        @Override
                        public void onResult(@NonNull WeatherResult weatherResult) {
                            if (!weatherResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Could not get weather.");
                                return;
                            }

                            Weather weather = weatherResult.getWeather();
                            Log.i(TAG, "Weather: " + weather);
                        }
                    });
        }
    }

    private boolean isConnected() {
        boolean isConnected = mGoogleApiClient.isConnected();
        if (!isConnected) {
            Log.i(TAG, "Not connected to google api.");
        }
        return isConnected;
    }
}
