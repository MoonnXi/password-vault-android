package com.passwordvault.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StorageUtil {

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Configuration related methods
    public static void savePasswordLength(Context context, int length) {
        getSharedPreferences(context).edit().putInt("password_length", length).apply();
    }

    public static int getPasswordLength(Context context, int defaultValue) {
        return getSharedPreferences(context).getInt("password_length", defaultValue);
    }

    public static void saveIncludeLowercase(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("include_lowercase", value).apply();
    }

    public static boolean getIncludeLowercase(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("include_lowercase", defaultValue);
    }

    public static void saveIncludeUppercase(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("include_uppercase", value).apply();
    }

    public static boolean getIncludeUppercase(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("include_uppercase", defaultValue);
    }

    public static void saveIncludeDigits(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("include_digits", value).apply();
    }

    public static boolean getIncludeDigits(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("include_digits", defaultValue);
    }

    public static void saveIncludeSpecial(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("include_special", value).apply();
    }

    public static boolean getIncludeSpecial(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("include_special", defaultValue);
    }

    public static void saveUnlockDuration(Context context, int days) {
        getSharedPreferences(context).edit().putInt("unlock_duration", days).apply();
    }

    public static int getUnlockDuration(Context context, int defaultValue) {
        return getSharedPreferences(context).getInt("unlock_duration", defaultValue);
    }

    // Password related methods
    public static void saveEncryptedPassword(Context context, String password) {
        getSharedPreferences(context).edit().putString("encrypted_password", password).apply();
    }

    public static String getEncryptedPassword(Context context, String defaultValue) {
        return getSharedPreferences(context).getString("encrypted_password", defaultValue);
    }

    public static void savePasswordChecksum(Context context, String checksum) {
        getSharedPreferences(context).edit().putString("password_checksum", checksum).apply();
    }

    public static String getPasswordChecksum(Context context, String defaultValue) {
        return getSharedPreferences(context).getString("password_checksum", defaultValue);
    }

    // Status related methods
    public static void saveSealedTime(Context context, long time) {
        getSharedPreferences(context).edit().putLong("sealed_time", time).apply();
    }

    public static long getSealedTime(Context context, long defaultValue) {
        return getSharedPreferences(context).getLong("sealed_time", defaultValue);
    }

    public static void saveUnlockTime(Context context, long time) {
        getSharedPreferences(context).edit().putLong("unlock_time", time).apply();
    }

    public static long getUnlockTime(Context context, long defaultValue) {
        return getSharedPreferences(context).getLong("unlock_time", defaultValue);
    }

    public static void saveIsSealed(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("is_sealed", value).apply();
    }

    public static boolean getIsSealed(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("is_sealed", defaultValue);
    }

    public static void saveIsUnlocked(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("is_unlocked", value).apply();
    }

    public static boolean getIsUnlocked(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("is_unlocked", defaultValue);
    }

    public static void saveIsExtracted(Context context, boolean value) {
        getSharedPreferences(context).edit().putBoolean("is_extracted", value).apply();
    }

    public static boolean getIsExtracted(Context context, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean("is_extracted", defaultValue);
    }

    // Reset all password related data
    public static void resetPasswordData(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove("encrypted_password");
        editor.remove("password_checksum");
        editor.remove("sealed_time");
        editor.remove("unlock_time");
        editor.remove("is_sealed");
        editor.remove("is_unlocked");
        editor.remove("is_extracted");
        editor.apply();
    }

    // Check if configuration exists
    public static boolean hasConfiguration(Context context) {
        return getSharedPreferences(context).contains("password_length");
    }

    // History password related methods
    public static void addPasswordToHistory(Context context, String password) {
        addPasswordToHistory(context, password, System.currentTimeMillis());
    }

    public static void addPasswordToHistory(Context context, String password, long timestamp) {
        SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Get current history
        String history = prefs.getString("password_history", "");
        
        // Create new history entry with timestamp
        String newEntry = timestamp + "|" + password;
        
        // Add to beginning of history
        String newHistory = newEntry;
        if (!history.isEmpty()) {
            newHistory += ";" + history;
        }
        

        
        editor.putString("password_history", newHistory);
        editor.apply();
    }

    public static String getPasswordHistory(Context context) {
        return getSharedPreferences(context).getString("password_history", "");
    }

    public static void clearPasswordHistory(Context context) {
        getSharedPreferences(context).edit().remove("password_history").apply();
    }
}
