package com.passwordvault.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.passwordvault.R;

public class ConfigActivity extends AppCompatActivity {

    private EditText etPasswordLength;
    private CheckBox cbLowercase;
    private CheckBox cbUppercase;
    private CheckBox cbDigits;
    private CheckBox cbSpecial;
    private Spinner spUnlockDuration;
    private Button btnSaveConfig;
    private Button btnViewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initViews();
        loadSavedConfig();
        setupListeners();
    }

    private void initViews() {
        etPasswordLength = findViewById(R.id.et_password_length);
        cbLowercase = findViewById(R.id.cb_lowercase);
        cbUppercase = findViewById(R.id.cb_uppercase);
        cbDigits = findViewById(R.id.cb_digits);
        cbSpecial = findViewById(R.id.cb_special);
        spUnlockDuration = findViewById(R.id.sp_unlock_duration);
        btnSaveConfig = findViewById(R.id.btn_save_config);
        btnViewHistory = findViewById(R.id.btn_view_history);

        // Setup unlock duration spinner
        String[] durations = {"1 Minute", "15 Days", "25 Days", "30 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnlockDuration.setAdapter(adapter);
    }

    private void loadSavedConfig() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int passwordLength = prefs.getInt("password_length", 16);
        boolean includeLowercase = prefs.getBoolean("include_lowercase", true);
        boolean includeUppercase = prefs.getBoolean("include_uppercase", true);
        boolean includeDigits = prefs.getBoolean("include_digits", true);
        boolean includeSpecial = prefs.getBoolean("include_special", true);
        int unlockDuration = prefs.getInt("unlock_duration", 1); // Default 1 day

        etPasswordLength.setText(String.valueOf(passwordLength));
        cbLowercase.setChecked(includeLowercase);
        cbUppercase.setChecked(includeUppercase);
        cbDigits.setChecked(includeDigits);
        cbSpecial.setChecked(includeSpecial);

        // Set spinner selection based on saved duration
        switch (unlockDuration) {
            case 1:
                spUnlockDuration.setSelection(0);
                break;
            case 21600:
                spUnlockDuration.setSelection(1);
                break;
            case 36000:
                spUnlockDuration.setSelection(2);
                break;
            case 43200:
                spUnlockDuration.setSelection(3);
                break;
        }
    }

    private void setupListeners() {
        btnSaveConfig.setOnClickListener(v -> saveConfig());
        btnViewHistory.setOnClickListener(v -> viewHistory());
    }

    private void viewHistory() {
        startActivity(new android.content.Intent(this, HistoryActivity.class));
    }

    private void saveConfig() {
        try {
            int passwordLength = Integer.parseInt(etPasswordLength.getText().toString());
            boolean includeLowercase = cbLowercase.isChecked();
            boolean includeUppercase = cbUppercase.isChecked();
            boolean includeDigits = cbDigits.isChecked();
            boolean includeSpecial = cbSpecial.isChecked();
            int unlockDuration = getUnlockDurationFromSpinner();

            // Validate configuration
            if (passwordLength <= 0 || passwordLength > 128) {
                Toast.makeText(this, "Password length must be between 1 and 128", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!includeLowercase && !includeUppercase && !includeDigits && !includeSpecial) {
                Toast.makeText(this, "At least one character set must be selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save configuration to SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("password_length", passwordLength);
            editor.putBoolean("include_lowercase", includeLowercase);
            editor.putBoolean("include_uppercase", includeUppercase);
            editor.putBoolean("include_digits", includeDigits);
            editor.putBoolean("include_special", includeSpecial);
            editor.putInt("unlock_duration", unlockDuration);
            editor.apply();

            Toast.makeText(this, "配置成功", Toast.LENGTH_SHORT).show();

            // Navigate to countdown activity to seal password
            startActivity(new android.content.Intent(this, CountdownActivity.class));
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid password length", Toast.LENGTH_SHORT).show();
        }
    }

    private int getUnlockDurationFromSpinner() {
        int position = spUnlockDuration.getSelectedItemPosition();
        switch (position) {
            case 0:
                return 1; // 1 minute
            case 1:
                return 21600; // 15 days (15 * 24 * 60 minutes)
            case 2:
                return 36000; // 25 days (25 * 24 * 60 minutes)
            case 3:
                return 43200; // 30 days (30 * 24 * 60 minutes)
            default:
                return 1;
        }
    }
}
