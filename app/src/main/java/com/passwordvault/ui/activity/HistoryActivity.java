package com.passwordvault.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.passwordvault.R;
import com.passwordvault.util.StorageUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private LinearLayout llHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        showPasswordHistory();
    }

    private void initViews() {
        llHistory = findViewById(R.id.ll_history);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void showPasswordHistory() {
        // Clear existing history views
        llHistory.removeAllViews();
        
        // Get password history
        String history = StorageUtil.getPasswordHistory(this);
        
        if (history.isEmpty()) {
            // No history yet
            TextView emptyHistoryView = new TextView(this);
            emptyHistoryView.setText("暂无历史密码记录");
            emptyHistoryView.setTextSize(14);
            emptyHistoryView.setTextColor(getResources().getColor(R.color.gray));
            emptyHistoryView.setPadding(16, 16, 16, 16);
            llHistory.addView(emptyHistoryView);
            return;
        }
        
        // Check if there's a currently sealed password
        boolean isSealed = StorageUtil.getIsSealed(this, false);
        boolean isUnlocked = StorageUtil.getIsUnlocked(this, false);
        long currentSealedTime = 0;
        
        if (isSealed && !isUnlocked) {
            // There's a currently sealed password, get its timestamp
            currentSealedTime = StorageUtil.getSealedTime(this, 0);
        }
        
        // Parse history entries
        String[] entries = history.split(";\\s*");
        
        // Add history entries to layout (newest first)
        for (String entry : entries) {
            if (!entry.isEmpty()) {
                String[] parts = entry.split("\\|", 2);
                if (parts.length == 2) {
                    try {
                        long timestamp = Long.parseLong(parts[0]);
                        String password = parts[1];
                        
                        // Skip the currently sealed password if it's still in countdown
                        if (currentSealedTime > 0) {
                            // Allow a small time difference to account for clock skew
                            long timeDiff = Math.abs(timestamp - currentSealedTime);
                            if (timeDiff < 10000) { // 10 seconds tolerance
                                continue;
                            }
                        }
                        
                        // Create history item view
                        LinearLayout historyItem = new LinearLayout(this);
                        historyItem.setOrientation(LinearLayout.VERTICAL);
                        historyItem.setPadding(16, 16, 16, 16);
                        historyItem.setBackgroundResource(R.drawable.password_background);
                        // Set LayoutParams before calling setMargin
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        historyItem.setLayoutParams(layoutParams);
                        setMargin(historyItem, 0, 10, 0, 10);
                        
                        // Format timestamp
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        String formattedTime = sdf.format(new Date(timestamp));
                        
                        // Create time text view
                        TextView timeView = new TextView(this);
                        timeView.setText(formattedTime);
                        timeView.setTextSize(12);
                        timeView.setTextColor(getResources().getColor(R.color.gray));
                        
                        // Create password text view
                        TextView passwordView = new TextView(this);
                        passwordView.setText(password);
                        passwordView.setTextSize(14);
                        passwordView.setTextIsSelectable(true);
                        passwordView.setPadding(0, 4, 0, 0);
                        
                        // Add views to history item
                        historyItem.addView(timeView);
                        historyItem.addView(passwordView);
                        
                        // Add history item to layout
                        llHistory.addView(historyItem);
                        
                    } catch (NumberFormatException e) {
                        // Ignore invalid entries
                    }
                }
            }
        }
        
        // Check if no history items were added (all were skipped)
        if (llHistory.getChildCount() == 0) {
            TextView emptyHistoryView = new TextView(this);
            emptyHistoryView.setText("暂无历史密码记录");
            emptyHistoryView.setTextSize(14);
            emptyHistoryView.setTextColor(getResources().getColor(R.color.gray));
            emptyHistoryView.setPadding(16, 16, 16, 16);
            llHistory.addView(emptyHistoryView);
        }
    }

    // Helper method to set margins on views
    private void setMargin(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
