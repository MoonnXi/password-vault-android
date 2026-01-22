package com.passwordvault.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.passwordvault.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAppStatus();
    }

    private void checkAppStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasConfig = prefs.contains("password_length");
        boolean isSealed = prefs.getBoolean("is_sealed", false);
        boolean isUnlocked = prefs.getBoolean("is_unlocked", false);
        boolean isExtracted = prefs.getBoolean("is_extracted", false);

        if (!hasConfig) {
            // First time launch, go to config page
            startActivity(new Intent(this, ConfigActivity.class));
            finish();
        } else if (isSealed && !isUnlocked) {
            // Password is sealed, show countdown
            startActivity(new Intent(this, CountdownActivity.class));
            finish();
        } else if (isUnlocked && !isExtracted) {
            // Password is unlocked, show extract page
            startActivity(new Intent(this, ExtractActivity.class));
            finish();
        } else {
            // Password is extracted or not sealed, go to config page
            startActivity(new Intent(this, ConfigActivity.class));
            finish();
        }
    }
}
