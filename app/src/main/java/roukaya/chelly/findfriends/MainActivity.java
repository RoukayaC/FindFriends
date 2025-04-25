package roukaya.chelly.findfriends;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import roukaya.chelly.findfriends.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Modern permission request using ActivityResultLauncher
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::handlePermissionResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        checkAndRequestPermissions();
    }

    private void setupNavigation() {
        // Configure top level destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_friends, R.id.navigation_dashboard)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void checkAndRequestPermissions() {
        // Permissions needed for the app to function properly
        List<String> permissionsNeeded = new ArrayList<>(Arrays.asList(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ));

        // Add notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Check which permissions we need to request
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // If SMS and location permissions are already granted, enable the app functionality
        boolean smsSendPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        Constants.GPS_SMS_PERMISSION_STATS = smsSendPermissionGranted && locationPermissionGranted;

        // Request permissions if needed
        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    private void handlePermissionResult(Map<String, Boolean> results) {
        // Check SMS permission (with null safety)
        Boolean smsResult = results.get(Manifest.permission.SEND_SMS);
        boolean smsSendPermissionGranted = smsResult != null && smsResult;

        // Check location permission (with null safety)
        Boolean locationResult = results.get(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean locationPermissionGranted = locationResult != null && locationResult;

        // Update permission status
        Constants.GPS_SMS_PERMISSION_STATS = smsSendPermissionGranted && locationPermissionGranted;

        // Show appropriate message
        if (Constants.GPS_SMS_PERMISSION_STATS) {
            Toast.makeText(this, "All required permissions granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Some permissions were denied. App functionality will be limited.", Toast.LENGTH_LONG).show();
        }
    }
}