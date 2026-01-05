package com.spacebilliard.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

public class MainMenuActivity extends Activity {

    private android.animation.ObjectAnimator jupiterRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main_menu);

        // Listeners for buttons
        /*
         * Button btnStart = findViewById(R.id.btnStart);
         * 
         * Button btnHowTo = findViewById(R.id.btnHowTo);
         * Button btnHallOfFame = findViewById(R.id.btnHallOfFame);
         * Button btnShop = findViewById(R.id.btnShop);
         */

        /*
         * btnStart.setOnClickListener(v -> {
         * Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
         * startActivity(intent);
         * });
         * 
         * btnShop.setOnClickListener(v -> {
         * Intent intent = new Intent(MainMenuActivity.this, ShopActivity.class);
         * startActivity(intent);
         * });
         * 
         * btnHowTo.setOnClickListener(v -> {
         * Intent intent = new Intent(MainMenuActivity.this, HowToPlayActivity.class);
         * startActivity(intent);
         * });
         * 
         * btnHallOfFame.setOnClickListener(v -> {
         * Intent intent = new Intent(MainMenuActivity.this, HallOfFameActivity.class);
         * startActivity(intent);
         * });
         */

        // --- NEW BUTTONS (RANKING & GUIDE) ---
        findViewById(R.id.btnRanking).setOnClickListener(v -> {
            startActivity(new Intent(MainMenuActivity.this, HallOfFameActivity.class));
        });

        View btnGuide = findViewById(R.id.btnGuide);

        // Check if first launch
        SharedPreferences prefs = getSharedPreferences("SpaceBilliard", MODE_PRIVATE);
        boolean hasSeenGuide = prefs.getBoolean("hasSeenGuide", false);

        // Start blinking animation if first time
        if (!hasSeenGuide) {
            android.animation.ObjectAnimator blinkAnim = android.animation.ObjectAnimator.ofFloat(
                    btnGuide, "alpha", 1f, 0.3f, 1f);
            blinkAnim.setDuration(1500);
            blinkAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            blinkAnim.start();

            // Store reference to stop later
            btnGuide.setTag(blinkAnim);
        }

        btnGuide.setOnClickListener(v -> {
            // Stop blinking animation if running
            Object animTag = v.getTag();
            if (animTag instanceof android.animation.ObjectAnimator) {
                ((android.animation.ObjectAnimator) animTag).cancel();
                v.setAlpha(1f);
            }

            // Mark as seen
            prefs.edit().putBoolean("hasSeenGuide", true).apply();

            startActivity(new Intent(MainMenuActivity.this, HowToPlayActivity.class));
        });

        // --- PLANET ANIMATION ---
        // We use our custom PlanetView which handles internal rotation.
        // We only add a floating animation here.
        View planetView = findViewById(R.id.imgJupiter);

        if (planetView != null) {
            // Floating (Up/Down)
            android.animation.ObjectAnimator floatAnim = android.animation.ObjectAnimator.ofFloat(
                    planetView, "translationY", -20f, 20f);
            floatAnim.setDuration(4000);
            floatAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            floatAnim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            floatAnim.start();
        }

        // Bottom Navigation Bar Buttons (5 buttons)
        findViewById(R.id.btnBottomPlay).setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, LevelSelectActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnBottomUpgrades).setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, UpgradesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnBottomQuests).setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, QuestsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnBottomSpecial).setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ModesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnBottomShop).setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ShopActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (jupiterRotation != null && !jupiterRotation.isStarted()) {
            jupiterRotation.resume();
        }
        updateQuestBadge();
    }

    private void updateQuestBadge() {
        TextView questBadge = findViewById(R.id.questBadge);
        if (questBadge == null)
            return;

        QuestManager questManager = QuestManager.getInstance(this);
        java.util.List<Quest> quests = questManager.getAllQuests();
        int claimableCount = 0;

        for (Quest q : quests) {
            if (q.isCompleted() && !q.isClaimed()) {
                claimableCount++;
            }
        }

        if (claimableCount > 0) {
            questBadge.setVisibility(View.VISIBLE);
            questBadge.setText(String.valueOf(claimableCount));
        } else {
            questBadge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (jupiterRotation != null) {
            jupiterRotation.pause();
        }
    }
}
