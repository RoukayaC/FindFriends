package roukaya.chelly.findfriends;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for 2 seconds then check login status
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("FindFriendsPrefs", MODE_PRIVATE);
            boolean stayLoggedIn = prefs.getBoolean("stay_logged_in", false);

            Intent intent;
            if (stayLoggedIn) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2000); // 2 seconds delay
    }
}