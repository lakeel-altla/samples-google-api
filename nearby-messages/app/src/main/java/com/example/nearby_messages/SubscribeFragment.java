package com.example.nearby_messages;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

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
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This fragment class is used to subscribe the beacon messages.
 */
public final class SubscribeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.editText_namespace)
    EditText mNameSpace;

    @BindView(R.id.editText_type)
    EditText mType;

    private static final String TAG = SubscribeFragment.class.getSimpleName();

    // This value is google project id.
    // See "https://console.developers.google.com/apis/dashboard".
    private static final String NAMESPACE = BuildConfig.BEACON_ATTACHMENT_NAMESPACE;

    private GoogleApiClient mGoogleApiClient;

    private MessageListener mMessageListener;

    public static SubscribeFragment newInstance() {
        return new SubscribeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subscribe, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNameSpace.setText(NAMESPACE);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(getActivity(), this)
                .build();

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                // Check my namespace.
                if (NAMESPACE.equals(message.getNamespace())) {
                    Log.d(TAG, "Message string: " + new String(message.getContent()));
                    Log.d(TAG, "Message namespaced type: " + message.getNamespace() +
                            "/" + message.getType());
                }
            }

            @Override
            public void onLost(Message message) {
                // Called when a message is no longer detectable nearby.
                String messageAsString = new String(message.getContent());
                Log.d(TAG, "Lost sight of message: " + messageAsString);
            }

            @Override
            public void onBleSignalChanged(final Message message, final BleSignal bleSignal) {
                // Called when the Bluetooth Low Energy (BLE) signal associated with a message changes.
                // This will only be called if we discover that the message is nearby via a BLE signal.
                // For messages discovered in other ways (e.g. audio), this will not be called.
                Log.i(TAG, "Message: " + message + " has new BLE signal information: " + bleSignal);
            }

            @Override
            public void onDistanceChanged(final Message message, final Distance distance) {
                // Called when Nearby's estimate of the distance to a message changes.
                // For example, this is called when we first gather enough information to make a distance estimate;
                // or when the message remains nearby, but gets closer or further away.
                Log.i(TAG, "Distance changed, message: " + message + ", new distance: " + distance);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(TAG);
    }

    @OnClick(R.id.button_subscribe_in_foreground)
    public void onSubscribeInForeground() {
        // Subscribe in foreground.

        if (mGoogleApiClient.isConnected()) {
            String namespaceFilter = mNameSpace.getText().toString();
            String typeFilter = mType.getText().toString();
            if (0 < namespaceFilter.length() && 0 < typeFilter.length()) {
                // Filter settings.
                // When you want to change filter settings, once need to unsubscribe.
                MessageFilter messageFilter = new MessageFilter.Builder()
                        .includeNamespacedType(namespaceFilter, typeFilter)
                        .build();

                SubscribeOptions options = new SubscribeOptions.Builder()
                        .setStrategy(Strategy.BLE_ONLY)
                        .setFilter(messageFilter)
                        .build();
                Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options);
            } else {
                // No filter.
                SubscribeOptions options = new SubscribeOptions.Builder()
                        .setStrategy(Strategy.BLE_ONLY)
                        .build();
                Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener);
            }
        } else {
            Toast.makeText(getContext(), "Not connected to google api client", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_subscribe_in_background)
    public void onSubscribeInBackground() {
        // Subscribe in background.
        // When your app subscribes to beacon messages in the background, low-power scans are triggered at screen-on events (or when Bluetooth turns on),
        // even when your app is not currently active.
        if (mGoogleApiClient.isConnected()) {
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(Strategy.BLE_ONLY)
                    .build();
            // Set BroadcastReceiver.
            Nearby.Messages.subscribe(mGoogleApiClient, getPendingIntent(), options);
        } else {
            Toast.makeText(getContext(), "Not connected to google api client", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.unsubscribe)
    public void onUnsubscribe() {
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed");
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getBroadcast(getContext(), 0, new Intent(getContext(), BeaconMessageReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
