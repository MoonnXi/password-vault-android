package com.passwordvault.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.passwordvault.R;
import com.passwordvault.util.StorageUtil;
import com.passwordvault.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CountdownActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private TextView tvUnlockTime;
    private Button btnGeneratePassword;
    private Button btnViewHistory;
    private Button btnBack;
    private LinearLayout llCountdown;
    private LinearLayout llGenerate;
    private CountDownTimer countDownTimer;
    private long timeRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        initViews();
        setupListeners();
        checkAppStatus();
    }

    private void initViews() {
        tvCountdown = findViewById(R.id.tv_countdown);
        tvUnlockTime = findViewById(R.id.tv_unlock_time);
        btnGeneratePassword = findViewById(R.id.btn_generate_password);
        btnViewHistory = findViewById(R.id.btn_view_history);
        btnBack = findViewById(R.id.btn_back);
        llCountdown = findViewById(R.id.ll_countdown);
        llGenerate = findViewById(R.id.ll_generate);
    }

    private void setupListeners() {
        btnGeneratePassword.setOnClickListener(v -> generatePassword());
        btnViewHistory.setOnClickListener(v -> viewHistory());
        btnBack.setOnClickListener(v -> finish());
    }

    private void generatePassword() {
        // Get configuration
        int passwordLength = StorageUtil.getPasswordLength(this, 16);
        boolean includeLowercase = StorageUtil.getIncludeLowercase(this, true);
        boolean includeUppercase = StorageUtil.getIncludeUppercase(this, true);
        boolean includeDigits = StorageUtil.getIncludeDigits(this, true);
        boolean includeSpecial = StorageUtil.getIncludeSpecial(this, true);

        // Generate password (try online first, fallback to offline if needed)
        String password = ApiClient.generatePassword(this, passwordLength, includeLowercase, includeUppercase, includeDigits, includeSpecial);
        if (password == null) {
            // Offline mode: use simple offline password generation
            password = generateOfflinePassword(passwordLength, includeLowercase, includeUppercase, includeDigits, includeSpecial);
        }

        // Navigate to PasswordActivity to show generated password and operation buttons
        Intent intent = new Intent(this, PasswordActivity.class);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private void viewHistory() {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    private void checkAppStatus() {
        boolean isSealed = StorageUtil.getIsSealed(this, false);
        boolean isUnlocked = StorageUtil.getIsUnlocked(this, false);
        boolean isExtracted = StorageUtil.getIsExtracted(this, false);

        if (isSealed && !isUnlocked) {
            // Password is sealed, show countdown
            showCountdownUI();
            checkUnlockStatus();
        } else if (isUnlocked && !isExtracted) {
            // Password is unlocked, show extract page
            startActivity(new Intent(this, ExtractActivity.class));
            finish();
        } else {
            // Show initial UI with generate button
            showInitialUI();
        }
    }

    private void showInitialUI() {
        llCountdown.setVisibility(View.GONE);
        llGenerate.setVisibility(View.VISIBLE);
        btnViewHistory.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
    }

    private void showCountdownUI() {
        llCountdown.setVisibility(View.VISIBLE);
        llGenerate.setVisibility(View.GONE);
        btnViewHistory.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
    }

    private void checkUnlockStatus() {
        long unlockTime = StorageUtil.getUnlockTime(this, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime >= unlockTime) {
            // Password is unlocked, navigate to extract page
            StorageUtil.saveIsUnlocked(this, true);
            startActivity(new Intent(this, ExtractActivity.class));
            finish();
        } else {
            // Password still sealed, show countdown
            updateUnlockTimeDisplay(unlockTime);
            startCountdown(unlockTime);
        }
    }

    private void startCountdown(long unlockTime) {
        long currentTime = System.currentTimeMillis();
        timeRemaining = unlockTime - currentTime;

        if (timeRemaining <= 0) {
            // Already unlocked
            StorageUtil.saveIsUnlocked(this, true);
            startActivity(new Intent(this, ExtractActivity.class));
            finish();
            return;
        }

        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                updateCountdownDisplay();
            }

            @Override
            public void onFinish() {
                // Countdown finished, password unlocked
                StorageUtil.saveIsUnlocked(CountdownActivity.this, true);
                startActivity(new Intent(CountdownActivity.this, ExtractActivity.class));
                finish();
            }
        };

        countDownTimer.start();
        updateCountdownDisplay();
    }

    private void updateCountdownDisplay() {
        long days = timeRemaining / (1000 * 60 * 60 * 24);
        long hours = (timeRemaining / (1000 * 60 * 60)) % 24;
        long minutes = (timeRemaining / (1000 * 60)) % 60;
        long seconds = (timeRemaining / 1000) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("时");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("分");
        }
        sb.append(seconds).append("秒");

        tvCountdown.setText(sb.toString());
    }

    private void updateUnlockTimeDisplay(long unlockTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(new Date(unlockTime));
        tvUnlockTime.setText(getString(R.string.unlock_time) + ": " + formattedTime);
    }

    private String generateOfflinePassword(int length, boolean includeLowercase, boolean includeUppercase, boolean includeDigits, boolean includeSpecial) {
        String characters = "";
        if (includeLowercase) characters += "abcdefghijkmnpqrstuvwxyz";
        if (includeUppercase) characters += "ABCDEFGHJKLMNPQRSTUVWXYZ";
        if (includeDigits) characters += "23456789";
        if (includeSpecial) characters += "!@#$%^&*()_+-=[]{}|;:,.<>?";

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
