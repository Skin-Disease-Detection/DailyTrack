package com.example.dailytrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiv";

    public static int track = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving Geofence event... ");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "", MapsActivity.class);
                track = 1;
                sendMessageToAdmin(context, "User has Entered the geofence!");

                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", MapsActivity.class);
                track = 1;
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "", LoginActivity.class);
                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "", HomeActivity.class);

                // Send FCM message when user exits the geofence
                sendMessageToAdmin(context, "User has exited the geofence!");

                track = -1;
                break;
        }
    }

    private void sendMessageToAdmin(Context context, String message) {
        // Subscribe to the admin topic
        FirebaseMessaging.getInstance().subscribeToTopic("admin_topic")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to admin_topic");
                        // Create a data message
                        RemoteMessage.Builder remoteMessageBuilder = new RemoteMessage.Builder("admin_topic");
                        remoteMessageBuilder.addData("message", message);

                        // Send the message
                        FirebaseMessaging.getInstance().send(remoteMessageBuilder.build());
                    } else {
                        Log.e(TAG, "Subscription to admin_topic failed", task.getException());
                    }
                });
    }
}
