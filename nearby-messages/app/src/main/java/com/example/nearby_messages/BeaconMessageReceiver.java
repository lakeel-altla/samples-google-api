package com.example.nearby_messages;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.UUID;

/**
 * This BeaconMessageReceiver class is processed when detect beacons in background.
 */
public class BeaconMessageReceiver extends BroadcastReceiver {

    private static final String TAG = BeaconMessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Nearby.Messages.handleIntent(intent, new MessageListener() {

            @Override
            public void onFound(Message message) {
                // This callback will be called when detect beacons.
                // Once called, unless it can call the MessageListener#onLost() method, it will not be called again.

                String messageAsString = new String(message.getContent());
                Log.i(TAG, "Found message via PendingIntent: " + messageAsString);

                // Show notification.
                String notificationMessage = "Found beacon:Message=" + messageAsString;
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                notifyNotification(context, pendingIntent, notificationMessage);
            }

            @Override
            public void onLost(Message message) {
                String messageAsString = new String(message.getContent());
                Log.i(TAG, "Lost message via PendingIntent: " + messageAsString);
            }
        });
    }

    private void notifyNotification(Context context, PendingIntent pendingIntent, String message) {
        int uuid = UUID.randomUUID().hashCode();

        Notification notification = createNotification(context, pendingIntent, message);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(uuid, notification);
    }

    private Notification createNotification(Context context, PendingIntent pendingIntent, String message) {
        String title = context.getResources().getString(R.string.app_name);

        return new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setTicker(message)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_nearby_white)
                .setVibrate(new long[]{0, 200, 100, 200, 100, 200})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .build();
    }
}
