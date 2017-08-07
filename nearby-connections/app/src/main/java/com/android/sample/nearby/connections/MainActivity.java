package com.android.sample.nearby.connections;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING;
import static com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING;
import static com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED;
import static com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_OK;
import static com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_OUT_OF_ORDER_API_CALL;

public final class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1000;

    private final List<String> endpointIds = new ArrayList<>();

    private final Map<String, String> devices = new HashMap<>();

    private String serviceId;

    private TextView textViewConnectedDevices;

    private TextView textViewStatus;

    private TextView textViewLog;

    private Button buttonPayload;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Must set the same value between the devices.
        serviceId = getPackageName();

        // Request the location permission.
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        debugLog("Connected to google play services.");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        debugLog("Connection suspended.");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        debugLog("Failed to connect to google play services.");
                    }
                })
                .addApi(Nearby.CONNECTIONS_API)
                .build();

        textViewConnectedDevices = (TextView) findViewById(R.id.textViewConnectedDevices);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewLog = (TextView) findViewById(R.id.textViewLog);

        Button buttonAdvertise = (Button) findViewById(R.id.buttonAdvertise);
        buttonAdvertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdvertising();
            }
        });

        Button buttonDiscovery = (Button) findViewById(R.id.buttonDiscovery);
        buttonDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDiscovery();
            }
        });

        buttonPayload = (Button) findViewById(R.id.buttonSend);
        buttonPayload.setEnabled(false);
        buttonPayload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nearby.Connections.sendPayload(googleApiClient, endpointIds, Payload.fromBytes("Test".getBytes()));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Result of the runtime permission.
        if (requestCode == REQUEST_PERMISSION_ACCESS_FINE_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(this, "Can not do anything.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            endpointIds.clear();
        }
    }

    private void startAdvertising() {
        AdvertisingOptions options = new AdvertisingOptions(Strategy.P2P_CLUSTER);
        Nearby
                .Connections
                .startAdvertising(
                        googleApiClient,
                        Build.MODEL,
                        serviceId, // Same value in the devices. App package name is set.
                        connectionLifecycleCallback,
                        options
                )
                .setResultCallback(new ResultCallback<Connections.StartAdvertisingResult>() {
                    @Override
                    public void onResult(@NonNull Connections.StartAdvertisingResult result) {
                        switch (result.getStatus().getStatusCode()) {
                            case STATUS_OK:
                                debugLog("Advertising...");
                                break;
                            case STATUS_ALREADY_ADVERTISING:
                                debugLog("Already advertising.");
                                break;
                            case STATUS_OUT_OF_ORDER_API_CALL:
                                debugLog("Currently connected to remote endpoints.");
                                Nearby.Connections.stopAdvertising(googleApiClient);
                                startAdvertising();
                                break;
                            default:
                                debugLog("Unknown status code:" + result.getStatus().getStatusCode());
                        }
                    }
                });
    }

    private void startDiscovery() {
        DiscoveryOptions options = new DiscoveryOptions(Strategy.P2P_CLUSTER);
        Nearby
                .Connections
                .startDiscovery(
                        googleApiClient,
                        serviceId,
                        endpointDiscoveryCallback,
                        options
                )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        switch (status.getStatusCode()) {
                            case STATUS_OK:
                                debugLog("Discovering...");
                                break;
                            case STATUS_ALREADY_DISCOVERING:
                                debugLog("Already discovering.");
                                break;
                            case STATUS_OUT_OF_ORDER_API_CALL:
                                debugLog("Currently connected to remote endpoints.");
                                Nearby.Connections.stopDiscovery(googleApiClient);
                                startDiscovery();
                                break;
                            default:
                                debugLog("Unknown status code:" + status.getStatusCode());
                        }
                    }
                });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {

        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo endpointInfo) {
            debugLog("Endpoint found:id=" + endpointId + " name:" + endpointInfo.getEndpointName());

            Nearby.Connections
                    .requestConnection(
                            googleApiClient,
                            Build.MODEL,
                            endpointId,
                            connectionLifecycleCallback
                    );
        }

        @Override
        public void onEndpointLost(String endpointId) {
            debugLog("Endpoint lost:id=" + endpointId);
        }
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {

        @Override
        public void onConnectionInitiated(final String endpointId, ConnectionInfo connectionInfo) {
            debugLog("Connection initiated:id=" + endpointId + " name=" + connectionInfo.getEndpointName());

            devices.put(endpointId, connectionInfo.getEndpointName());

            Nearby.Connections.acceptConnection(googleApiClient, endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            debugLog("Connection result:status=" + result.getStatus());

            switch (result.getStatus().getStatusCode()) {
                case STATUS_OK: {
                    debugLog("Connection established:id=" + endpointId);

                    endpointIds.add(endpointId);

                    showConnectedDevices();

                    textViewStatus.setText(R.string.text_status_connected);
                    buttonPayload.setEnabled(true);
                    break;
                }
                case STATUS_CONNECTION_REJECTED:
                    textViewStatus.setText(R.string.text_status_rejected);
                    buttonPayload.setEnabled(false);
                    break;
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            debugLog("Disconnected:id=" + endpointId);

            endpointIds.remove(endpointId);

            if (endpointIds.isEmpty()) {
                buttonPayload.setEnabled(false);
                textViewStatus.setText(R.string.text_status_disconnected);
                textViewConnectedDevices.setText(null);
            } else {
                MainActivity.this.showConnectedDevices();
            }
        }
    };

    private void showConnectedDevices() {
        // Show connected devices.
        String devicesText = "Connected devices:" + "\n";
        for (String id : endpointIds) {
            String deviceName = devices.get(id);
            devicesText = devicesText + deviceName + "\n";
        }
        textViewConnectedDevices.setText(devicesText);
    }

    private final PayloadCallback payloadCallback = new PayloadCallback() {

        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            debugLog("Payload received from " + endpointId);

            byte[] data = payload.asBytes();
            if (data != null) {
                Toast.makeText(getApplicationContext(), new String(data), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
        }
    };

    private void debugLog(String message) {
        Log.d(TAG, message);

        String text = textViewLog.getText().toString();
        textViewLog.setText(text + "\n" + message);
    }
}
