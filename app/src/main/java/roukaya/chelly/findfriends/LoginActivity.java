package roukaya.chelly.findfriends;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FindFriendsPrefs";
    private static final String KEY_STAY_LOGGED_IN = "stay_logged_in";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user chose to stay logged in
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean stayLoggedIn = prefs.getBoolean(KEY_STAY_LOGGED_IN, false);

        if (stayLoggedIn) {
            // Skip login screen, go directly to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        CheckBox checkBoxStayLoggedIn = findViewById(R.id.checkbox_stay_logged_in);
        Button buttonLogin = findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(v -> {
            // Your login validation logic

            // Save login state
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_STAY_LOGGED_IN, checkBoxStayLoggedIn.isChecked());
            editor.putString(KEY_USERNAME, "user"); // Replace with actual username
            editor.apply();

            // Navigate to main activity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }
}