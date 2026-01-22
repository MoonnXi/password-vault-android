package com.passwordvault.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.passwordvault.R;
import com.passwordvault.util.StorageUtil;

public class PasswordActivity extends AppCompatActivity {

    private TextView tvGeneratedPassword;
    private Button btnCopyPassword;
    private Button btnSealPassword;
    private Button btnBack;
    private String currentGeneratedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        initViews();
        setupListeners();
        getPasswordFromIntent();
    }

    private void initViews() {
        tvGeneratedPassword = findViewById(R.id.tv_generated_password);
        btnCopyPassword = findViewById(R.id.btn_copy_password);
        btnSealPassword = findViewById(R.id.btn_seal_password);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        btnCopyPassword.setOnClickListener(v -> copyPassword());
        btnSealPassword.setOnClickListener(v -> sealPassword());
        btnBack.setOnClickListener(v -> finish());
    }

    private void getPasswordFromIntent() {
        currentGeneratedPassword = getIntent().getStringExtra("password");
        if (currentGeneratedPassword != null) {
            tvGeneratedPassword.setText(currentGeneratedPassword);
        } else {
            Toast.makeText(this, "未获取到密码", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void copyPassword() {
        if (currentGeneratedPassword != null) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Password", currentGeneratedPassword);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "密码已复制到剪贴板", Toast.LENGTH_SHORT).show();
        }
    }

    private void sealPassword() {
        if (currentGeneratedPassword == null) {
            Toast.makeText(this, "请先生成密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get configuration
        int unlockDuration = StorageUtil.getUnlockDuration(this, 1);

        // Encrypt password (simplified for demo, use proper encryption in production)
        String encryptedPassword = encryptPassword(currentGeneratedPassword);
        String checksum = generateChecksum(currentGeneratedPassword);

        // Save password data
        StorageUtil.saveEncryptedPassword(this, encryptedPassword);
        StorageUtil.savePasswordChecksum(this, checksum);

        // Calculate unlock time
        long sealedTime = System.currentTimeMillis();
        long unlockTime = sealedTime + (unlockDuration * 60 * 1000L);

        // Save time data
        StorageUtil.saveSealedTime(this, sealedTime);
        StorageUtil.saveUnlockTime(this, unlockTime);
        StorageUtil.saveIsSealed(this, true);
        StorageUtil.saveIsUnlocked(this, false);
        StorageUtil.saveIsExtracted(this, false);

        // Add password to history with the same timestamp as sealed time
        StorageUtil.addPasswordToHistory(this, currentGeneratedPassword, sealedTime);

        // Navigate to countdown activity and clear the back stack
        Intent intent = new Intent(this, CountdownActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    

    private String encryptPassword(String password) {
        // Simplified encryption for demo, use proper AES-256 in production
        return password + "_encrypted";
    }

    private String generateChecksum(String password) {
        // Simplified checksum for demo, use proper hashing in production
        return String.valueOf(password.hashCode());
    }
}