package com.example.google_awareness;

import com.google.android.gms.awareness.fence.FenceState;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;


public class AwarenessReceiver extends BroadcastReceiver {

    private static final String TAG = AwarenessReceiver.class.getSimpleName();

    private static final String FENCE_KEY = "walkingWithHeadphoneFenceKey";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        FenceState fenceState = FenceState.extract(intent);

        if (TextUtils.equals(fenceState.getFenceKey(), FENCE_KEY)) {
            switch (fenceState.getCurrentState()) {
                case FenceState.TRUE:
                    Log.i(TAG, "FenceState is true");
                    sendLocalNotification(context);
                    break;
                case FenceState.FALSE:
                    Log.i(TAG, "FenceState is false");
                    break;
                case FenceState.UNKNOWN:
                    Log.i(TAG, "FenceState is unknown");
                    break;
            }
        }
    }

    private void sendLocalNotification(Context context) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Show local notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText("You are running while wearing a headphone.");
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();

        mManager.notify(0, notification);
    }
}
