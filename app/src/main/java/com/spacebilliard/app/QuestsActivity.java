package com.spacebilliard.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class QuestsActivity extends AppCompatActivity {

    private RecyclerView rvQuests;
    private QuestAdapter adapter;
    private QuestManager questManager;
    private TextView tvQuestCount;

    // Sound
    private SoundPool soundPool;
    private int soundHomeButton;
    private int soundCoinCollect;

    // AdMob
    private com.google.android.gms.ads.rewarded.RewardedAd rewardedAd;
    private com.spacebilliard.app.ui.NeonButton btnFreeCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quests);

        // Initialize Quest Manager
        questManager = QuestManager.getInstance(this);

        // Setup UI
        tvQuestCount = findViewById(R.id.tvQuestCount);
        rvQuests = findViewById(R.id.rvQuests);

        Button btnBack = findViewById(R.id.btnBack);
        btnFreeCoins = findViewById(R.id.btnFreeCoins);
        btnFreeCoins.setText("WATCH AD (+50 \uD83D\uDCB0)");
        btnFreeCoins.setThemeColor(android.graphics.Color.YELLOW);

        // Load Ad
        loadRewardedAd();

        btnFreeCoins.setOnClickListener(v -> showRewardedAd());
        rvQuests.setLayoutManager(new LinearLayoutManager(this));
        List<Quest> quests = questManager.getAllQuests();
        adapter = new QuestAdapter(quests);
        rvQuests.setAdapter(adapter);

        // Update quest count
        updateQuestCount();

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
        soundCoinCollect = soundPool.load(this, R.raw.money_collect, 1);

        // Set listener for reward sound
        adapter.setOnRewardClaimedListener(() -> {
            soundPool.play(soundCoinCollect, 1f, 1f, 0, 0, 1f);
        });

        // Back button
        btnBack.setOnClickListener(v -> {
            // Play Home Sound
            soundPool.play(soundHomeButton, 1f, 1f, 0, 0, 1f);

            v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(80)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                        finish();
                    })
                    .start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh quest progress when returning to this screen
        questManager.loadQuestProgress();
        adapter.updateQuests(questManager.getAllQuests());
        updateQuestCount();
    }

    private void updateQuestCount() {
        int completed = questManager.getCompletedQuestsCount();
        tvQuestCount.setText(completed + "/50");
    }

    private void loadRewardedAd() {
        com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
        com.google.android.gms.ads.rewarded.RewardedAd.load(this, "ca-app-pub-2131815746039092/7990766420", // Production
                                                                                                            // Rewarded
                                                                                                            // ID
                adRequest, new com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(
                            @androidx.annotation.NonNull com.google.android.gms.ads.LoadAdError loadAdError) {
                        rewardedAd = null;
                        btnFreeCoins.setText("AD LOADING...");
                        btnFreeCoins.setAlpha(0.5f);
                    }

                    @Override
                    public void onAdLoaded(
                            @androidx.annotation.NonNull com.google.android.gms.ads.rewarded.RewardedAd ad) {
                        rewardedAd = ad;
                        btnFreeCoins.setText("WATCH AD (+50 \uD83D\uDCB0)");
                        btnFreeCoins.setAlpha(1.0f);
                    }
                });
    }

    private void showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                // Grant Reward
                SharedPreferences prefs = getSharedPreferences("SpaceBilliard", MODE_PRIVATE);
                int currentCoins = prefs.getInt("coins", 0);
                prefs.edit().putInt("coins", currentCoins + 50).apply();

                // Sound & Toast
                soundPool.play(soundCoinCollect, 1f, 1f, 0, 0, 1f);
                android.widget.Toast.makeText(this, "RECEIVED 50 COINS!", android.widget.Toast.LENGTH_LONG).show();

                // Reload Ad
                loadRewardedAd();
            });
        } else {
            android.widget.Toast
                    .makeText(this, "Ad not ready yet, try again in a moment.", android.widget.Toast.LENGTH_SHORT)
                    .show();
            loadRewardedAd();
        }
    }
}
