package roukaya.chelly.findfriends;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MySmsReceiver extends BroadcastReceiver {

    private static final String TAG = "MySmsReceiver";
    private static final String CHANNEL_ID = "FindFriendsChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null ||
                !intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            return;
        }

        try {
            // Extract SMS messages from the intent
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null || pdus.length == 0) return;

            // Get the format of the message
            String format = bundle.getString("format");

            // Process all SMS messages
            for (Object pdu : pdus) {
                // Always use the newer method that requires a format parameter
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);

                String messageBody = smsMessage.getMessageBody();
                String phoneNumber = smsMessage.getDisplayOriginatingAddress();

                processMessage(context, messageBody, phoneNumber);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing SMS", e);
        }
    }

    private void processMessage(Context context, String messageBody, String phoneNumber) {
        // Process location request message
        if (messageBody.contains(Constants.MSG_SendMePosition)) {
            // Send back our location
            Intent locationService = new Intent(context, MyLocationService.class);
            locationService.putExtra("PHONE", phoneNumber);
            context.startService(locationService);
        }
        // Process received location message
        else if (messageBody.contains(Constants.MSG_MAPOSITION)) {
            try {
                // Parse coordinates from message
                String[] messageParts = messageBody.split("#");
                if (messageParts.length >= 3) {
                    String longitude = messageParts[1];
                    String latitude = messageParts[2];

                    // Store friend location in database
                    try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
                        dbHelper.addFriend(phoneNumber,
                                Double.parseDouble(latitude),
                                Double.parseDouble(longitude));
                    }

                    // Create notification for the received location
                    createLocationNotification(context, latitude, longitude);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to process location message", e);
            }
        }
    }

    private void createLocationNotification(Context context, String latitude, String longitude) {
        // Create notification channel
        createNotificationChannel(context);

        // Create intent for when notification is tapped
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(Uri.parse("geo:" + latitude + "," + longitude));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mapIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Location Received")
                .setContentText("Tap to view location on map")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "No permission to show notification", e);
        }
    }

    private void createNotificationChannel(Context context) {
        // Android 8.0 (API 26) and above require a notification channel
        CharSequence name = "FindFriends Notifications";
        String description = "Notifications for when friend locations are received";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}