package com.passwordvault.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.passwordvault.R;
import com.passwordvault.util.StorageUtil;
import com.passwordvault.network.ApiClient;

public class ExtractActivity extends AppCompatActivity {

    private TextView tvPassword;
    private Button btnCopyPassword;
    private boolean isExtracted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extract);

        initViews();
        checkExtractStatus();
        setupListeners();
    }

    private void initViews() {
        tvPassword = findViewById(R.id.tv_password);
        btnCopyPassword = findViewById(R.id.btn_copy_password);
    }

    private void checkExtractStatus() {
        isExtracted = StorageUtil.getIsExtracted(this, false);
        if (isExtracted) {
            // Password already extracted, show extracted status
            showExtractedStatus();
        } else {
            // Password not extracted, show password
            showPassword();
        }
    }

    private void showPassword() {
        // Get encrypted password and checksum
        String encryptedPassword = StorageUtil.getEncryptedPassword(this, "");
        String checksum = StorageUtil.getPasswordChecksum(this, "");

        if (encryptedPassword.isEmpty() || checksum.isEmpty()) {
            Toast.makeText(this, "No password found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Decrypt password (simplified for demo)
        String password = decryptPassword(encryptedPassword);

        // Verify checksum
        if (!verifyChecksum(password, checksum)) {
            Toast.makeText(this, "Password verification failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvPassword.setText(password);
        btnCopyPassword.setEnabled(true);
    }

    private void showExtractedStatus() {
        tvPassword.setText(R.string.password_extracted);
        tvPassword.setTextColor(getResources().getColor(R.color.gray));
        btnCopyPassword.setEnabled(false);
        btnCopyPassword.setText(R.string.password_extracted);
    }

    private void setupListeners() {
        btnCopyPassword.setOnClickListener(v -> {
            if (!isExtracted) {
                copyPassword();
            }
        });
    }

    private void copyPassword() {
        // Get password
        String encryptedPassword = StorageUtil.getEncryptedPassword(this, "");
        String password = decryptPassword(encryptedPassword);

        // Copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Password", password);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, R.string.password_copied, Toast.LENGTH_SHORT).show();

        // Add password to history only when extracted
        StorageUtil.addPasswordToHistory(this, password);

        // Mark as extracted
        StorageUtil.saveIsExtracted(this, true);
        isExtracted = true;

        // Hide password
        showExtractedStatus();

        // Regenerate password and reset seal
        regenerateAndReseal();
    }

    private void regenerateAndReseal() {
        // Get configuration
        int passwordLength = StorageUtil.getPasswordLength(this, 16);
        boolean includeLowercase = StorageUtil.getIncludeLowercase(this, true);
        boolean includeUppercase = StorageUtil.getIncludeUppercase(this, true);
        boolean includeDigits = StorageUtil.getIncludeDigits(this, true);
        boolean includeSpecial = StorageUtil.getIncludeSpecial(this, true);
        int unlockDuration = StorageUtil.getUnlockDuration(this, 1);

        // Regenerate password (try online first, fallback to offline if needed)
        String newPassword = ApiClient.regeneratePassword(this, passwordLength, includeLowercase, includeUppercase, includeDigits, includeSpecial);
        if (newPassword == null) {
            // Offline mode: use simple offline password generation
            newPassword = generateOfflinePassword(passwordLength, includeLowercase, includeUppercase, includeDigits, includeSpecial);
        }

        // Encrypt new password
        String encryptedNewPassword = encryptPassword(newPassword);
        String newChecksum = generateChecksum(newPassword);

        // Calculate new unlock time
        long newSealedTime = System.currentTimeMillis();
        long newUnlockTime = newSealedTime + (unlockDuration * 60 * 1000L);

        // Save new password data
        StorageUtil.saveEncryptedPassword(this, encryptedNewPassword);
        StorageUtil.savePasswordChecksum(this, newChecksum);
        StorageUtil.saveSealedTime(this, newSealedTime);
        StorageUtil.saveUnlockTime(this, newUnlockTime);
        StorageUtil.saveIsSealed(this, false);
        StorageUtil.saveIsUnlocked(this, false);
        StorageUtil.saveIsExtracted(this, false);
        
        // Add new password to history only after extraction
        // StorageUtil.addPasswordToHistory(this, newPassword);

        // Show regeneration status
        Toast.makeText(this, R.string.regenerating_password, Toast.LENGTH_SHORT).show();
    }

    private String decryptPassword(String encryptedPassword) {
        // Simplified decryption for demo, use proper decryption in production
        return encryptedPassword.replace("_encrypted", "");
    }

    private boolean verifyChecksum(String password, String checksum) {
        // Simplified checksum verification for demo
        return String.valueOf(password.hashCode()).equals(checksum);
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

    private String encryptPassword(String password) {
        // Simplified encryption for demo, use proper AES-256 in production
        return password + "_encrypted";
    }

    private String generateChecksum(String password) {
        // Simplified checksum for demo, use proper hashing in production
        return String.valueOf(password.hashCode());
    }
}
