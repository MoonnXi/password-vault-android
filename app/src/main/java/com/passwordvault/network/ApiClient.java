package com.passwordvault.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final String TAG = "ApiClient";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String generatePassword(Context context, int length, boolean includeLowercase, boolean includeUppercase, boolean includeDigits, boolean includeSpecial) {
        // Always use offline mode since this is a standalone app
        return null; // Offline mode
    }

    public static String regeneratePassword(Context context, int length, boolean includeLowercase, boolean includeUppercase, boolean includeDigits, boolean includeSpecial) {
        // Always use offline mode since this is a standalone app
        return null; // Offline mode
    }

    public static boolean validateConfig(Context context, int length, boolean includeLowercase, boolean includeUppercase, boolean includeDigits, boolean includeSpecial) {
        if (!isNetworkAvailable(context)) {
            // Offline mode, validate locally
            return length > 0 && length <= 128 && (includeLowercase || includeUppercase || includeDigits || includeSpecial);
        }

        try {
            URL url = new URL(BASE_URL + "/password/validate/config");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            StringBuilder postData = new StringBuilder();
            postData.append("length=").append(length);
            postData.append("&includeLowercase=").append(includeLowercase);
            postData.append("&includeUppercase=").append(includeUppercase);
            postData.append("&includeDigits=").append(includeDigits);
            postData.append("&includeSpecial=").append(includeSpecial);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString().contains("true");
            } else {
                Log.e(TAG, "POST request failed with response code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating config: " + e.getMessage());
            return false;
        }
    }

    public static String getStatus(Context context) {
        if (!isNetworkAvailable(context)) {
            return "Offline";
        }

        try {
            URL url = new URL(BASE_URL + "/password/status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                Log.e(TAG, "GET request failed with response code: " + responseCode);
                return "Error";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting status: " + e.getMessage());
            return "Error";
        }
    }
}
