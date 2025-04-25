package roukaya.chelly.findfriends;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MySmsReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String messageBody, phoneNumber;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length > -1) {
                    messageBody = messages[0].getMessageBody();
                    phoneNumber = messages[0].getDisplayOriginatingAddress();

                    Toast.makeText(context, "Message : " + messageBody + "Reçu de la part de;" + phoneNumber, Toast.LENGTH_LONG).show();

                    if (messageBody.contains(Constants.MSG_SendMePosition)) {
                        // Recuperer la position GPS => Envoyer vers phonenumber
                        Intent i = new Intent(context, roukaya.chelly.findfriends.MyLocationService.class);
                        i.putExtra("PHONE", phoneNumber);
                        context.startService(i);


                        if (messageBody.contains(Constants.MSG_MAPOSITION)) {
                            String[] t = messageBody.split("#");
                            String logitude = t[1];
                            String latitude = t[2];

                            // Creer une notification
                            NotificationCompat.Builder mynotif = new NotificationCompat.Builder(context, "FindFriendsID");
                            mynotif.setContentTitle("Position reçu");
                            mynotif.setContentText("Appuyer pour voir la position sur google map");
                            mynotif.setAutoCancel(true);
                            mynotif.setSmallIcon(android.R.drawable.ic_dialog_map);

                            // Action sur la notif
                            Intent mapIntent = new Intent();
                            mapIntent.setAction(Intent.ACTION_VIEW);
                            mapIntent.setData(Uri.parse("geo:"+latitude+","+logitude));

                            PendingIntent pi = PendingIntent.getActivity(context, 0, mapIntent, PendingIntent.FLAG_IMMUTABLE);
                            mynotif.setContentIntent(pi);

                            // Lancer une notification
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            NotificationChannel canal = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                canal = new NotificationChannel("FindFriendsID", "Le canal de notre app findfriends", NotificationManager.IMPORTANCE_DEFAULT);
                            }
                            notificationManager.createNotificationChannel(canal);
                            notificationManager.notify(1, mynotif.build());
                        }

                    }
                }
            }
        }
    }
}