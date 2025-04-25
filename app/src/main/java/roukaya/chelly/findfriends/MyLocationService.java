package roukaya.chelly.findfriends;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MyLocationService extends Service {
    // Le service est une activité sans interface graphique
    public MyLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String number = intent.getStringExtra("PHONE");
        // Recuperation de la position
        FusedLocationProviderClient mClient = LocationServices.getFusedLocationProviderClient(this);

        if(Constants.GPS_SMS_PERMISSION_STATS) {
            mClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null) {
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();

                        SmsManager manager = SmsManager.getDefault();
                        manager.sendTextMessage(number, null,   Constants.MSG_MAPOSITION+"#"+longitude+"#"+latitude, null, null);
                    } else {
                        Toast.makeText(MyLocationService.this, "Pas de position GPS!!", Toast.LENGTH_SHORT);
                    }
                }
            });
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}