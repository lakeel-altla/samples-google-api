package com.example.google_awareness;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.DetectedActivityFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public final class FenceFragment extends Fragment {

    private static final String TAG = FenceFragment.class.getSimpleName();

    public static final String FENCE_RECEIVER_ACTION = "android.intent.category.fence.receive";

    private static final String FENCE_KEY = "walkingWithHeadphoneFenceKey";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_fence, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle("Fence");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Awareness.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        registerFence();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterFence();
    }


    private void registerFence() {
        // When you are running with a headphone, Broadcast Receiver will be launched.

        // Register headphone fence.
        AwarenessFence headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN);

        // Register walking fence.
        AwarenessFence walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING);

        // Create AND condition.
        AwarenessFence walkingWithHeadphoneFence = AwarenessFence.and(
                headphoneFence, walkingFence
        );

        // Register fence.
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .addFence(FENCE_KEY, walkingWithHeadphoneFence, mPendingIntent)
                        .build())
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                });
    }

    private void unregisterFence() {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(FENCE_KEY)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.i(TAG, "Fence " + FENCE_KEY + " successfully removed.");
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.i(TAG, "Fence " + FENCE_KEY + " could NOT be removed.");
            }
        });
    }
}
