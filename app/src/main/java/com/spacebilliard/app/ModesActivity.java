package com.spacebilliard.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;

import com.spacebilliard.app.ui.NeonInfoPanel;

public class ModesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setBackgroundColor(Color.BLACK);

        // Back Button
        Button backBtn = new Button(this);
        backBtn.setText("BACK");
        backBtn.setTextColor(Color.CYAN);
        backBtn.setBackgroundColor(Color.TRANSPARENT); // Simple style for now
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(250, 120);
        backParams.gravity = Gravity.TOP | Gravity.LEFT;
        backParams.setMargins(20, 20, 0, 0);
        backBtn.setOnClickListener(v -> finish());
        mainLayout.addView(backBtn, backParams);

        // Info Panel
        NeonInfoPanel infoPanel = new NeonInfoPanel(this);
        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                (int) (300 * getResources().getDisplayMetrics().density),
                (int) (200 * getResources().getDisplayMetrics().density));
        panelParams.gravity = Gravity.CENTER;
        infoPanel.setLayoutParams(panelParams);

        // Use multiline text logic in NeonInfoPanel
        infoPanel.setData("WELCOME TO\nONE SHOT MODE", "", "", "");

        mainLayout.addView(infoPanel);

        // START BUTTON
        Button startBtn = new Button(this);
        startBtn.setText("START");
        startBtn.setTextColor(Color.WHITE);
        startBtn.setBackgroundColor(Color.GRAY); // Placeholder style
        FrameLayout.LayoutParams startParams = new FrameLayout.LayoutParams(300, 150);
        startParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        startParams.setMargins(0, 0, 0, 200);
        startBtn.setOnClickListener(v -> {
            // Switch to Puzzle View
            PuzzleView puzzleView = new PuzzleView(this);
            mainLayout.removeAllViews();
            mainLayout.addView(puzzleView);
            // Re-add back button
            mainLayout.addView(backBtn);
        });
        mainLayout.addView(startBtn, startParams);

        setContentView(mainLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If showing puzzle view, pause it?
        // Ideally we need a reference if we want to pause it correctly.
    }
}
