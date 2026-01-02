package com.spacebilliard.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class GameOverActivity extends Activity {

    public static final int RESULT_REVIVE = 101;
    public static final int RESULT_REBOOT = 102;
    public static final int RESULT_HOF = 103;
    public static final int RESULT_MAIN_MENU = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Use our custom modern panel
        com.spacebilliard.app.ui.NeonGameOverPanel panel = new com.spacebilliard.app.ui.NeonGameOverPanel(this);
        setContentView(panel);

        // Prevent closing by touching outside (modal behavior)
        setFinishOnTouchOutside(false);

        // Attach listeners to panel buttons
        panel.btnRevive.setOnClickListener(v -> {
            setResult(RESULT_REVIVE);
            finish();
            overridePendingTransition(0, 0); // No animation
        });

        panel.btnReboot.setOnClickListener(v -> {
            setResult(RESULT_REBOOT);
            finish();
            overridePendingTransition(0, 0);
        });

        // Hall of Fame button is removed from panel

        panel.btnMainMenu.setOnClickListener(v -> {
            setResult(RESULT_MAIN_MENU);
            finish();
            overridePendingTransition(0, 0);
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from dismissing without choice, or default to Main Menu
        setResult(RESULT_MAIN_MENU);
        finish();
    }
}
