package com.spacebilliard.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class GameOverActivity extends Activity {

    public static final int RESULT_REVIVE = 101;
    public static final int RESULT_REBOOT = 102;
    public static final int RESULT_HOF = 103;
    public static final int RESULT_MAIN_MENU = 104;

    // Sound
    private SoundPool soundPool;
    private int soundHomeButton;
    private int soundMenuSelect;

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

        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load Home Button Sound
        soundHomeButton = soundPool.load(this, R.raw.homebutton, 1);
        soundMenuSelect = soundPool.load(this, R.raw.menuselect, 1);

        // Attach listeners to panel buttons
        panel.btnRevive.setOnClickListener(v -> {
            setResult(RESULT_REVIVE);
            finish();
            overridePendingTransition(0, 0); // No animation
        });

        panel.btnReboot.setOnClickListener(v -> {
            // Play Reboot/Select Sound
            soundPool.play(soundMenuSelect, 1f, 1f, 0, 0, 1f);

            v.postDelayed(() -> {
                setResult(RESULT_REBOOT);
                finish();
                overridePendingTransition(0, 0);
            }, 200);
        });

        // Hall of Fame button is removed from panel

        panel.btnMainMenu.setOnClickListener(v -> {
            // Play Home Sound
            soundPool.play(soundHomeButton, 1f, 1f, 0, 0, 1f);

            // Small delay to let sound play before killing activity context
            v.postDelayed(() -> {
                setResult(RESULT_MAIN_MENU);
                finish();
                overridePendingTransition(0, 0);
            }, 100);
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from dismissing without choice, or default to Main Menu
        setResult(RESULT_MAIN_MENU);
        finish();
    }
}
