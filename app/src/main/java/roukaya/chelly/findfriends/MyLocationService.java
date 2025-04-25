package roukaya.chelly.findfriends;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;

public class MyLocationService extends Service {
    private static final String TAG = "MyLocationService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String phoneNumber = intent.getStringExtra("PHONE");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "No phone number provided");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Check if we have location and SMS permissions
        if (Constants.GPS_SMS_PERMISSION_STATS) {
            sendLocationToPhone(phoneNumber);
        } else {
            Log.e(TAG, "Missing required permissions");
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void sendLocationToPhone(String phoneNumber) {
        // Get location provider client
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permission again to satisfy lint
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        // Get the current location with high accuracy
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @Override
            public CancellationToken onCanceledRequested(OnTokenCanceledListener onTokenCanceledListener) {
                return this;
            }
        }).addOnSuccessListener(location -> {
            if (location != null) {
                // We have a location, send it via SMS
                sendSmsWithLocation(phoneNumber, location);
            } else {
                // Try to get the last known location as a fallback
                locationClient.getLastLocation().addOnSuccessListener(lastLocation -> {
                    if (lastLocation != null) {
                        sendSmsWithLocation(phoneNumber, lastLocation);
                    } else {
                        Toast.makeText(MyLocationService.this, "Could not determine location", Toast.LENGTH_SHORT).show();
                    }
                    stopSelf();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            stopSelf();
        });
    }

    private void sendSmsWithLocation(String phoneNumber, Location location) {
        try {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            // Format the message with location coordinates
            String message = Constants.MSG_MAPOSITION + "#" + longitude + "#" + latitude;

            // Send the SMS
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Log.d(TAG, "Location SMS sent to: " + phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send location SMS", e);
        } finally {
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}