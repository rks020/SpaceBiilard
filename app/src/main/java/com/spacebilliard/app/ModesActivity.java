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

                // Back Button (removed from top-left, will be placed below START)

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

                // START BUTTON (Return to HQ style)
                Button startBtn = new Button(this);
                startBtn.setText("START");
                startBtn.setTextColor(Color.parseColor("#EAFBFF"));
                startBtn.setBackground(getDrawable(R.drawable.btn_return_hq));
                startBtn.setTextSize(16);
                startBtn.setTypeface(null, android.graphics.Typeface.BOLD);
                startBtn.setLetterSpacing(0.08f);
                startBtn.setElevation(6 * getResources().getDisplayMetrics().density);

                FrameLayout.LayoutParams startParams = new FrameLayout.LayoutParams(
                                (int) (280 * getResources().getDisplayMetrics().density),
                                (int) (56 * getResources().getDisplayMetrics().density));
                startParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                startParams.setMargins(0, 0, 0, (int) (140 * getResources().getDisplayMetrics().density));

                startBtn.setOnClickListener(v -> {
                        // Switch to Puzzle View
                        PuzzleView puzzleView = new PuzzleView(this);
                        mainLayout.removeAllViews();
                        mainLayout.addView(puzzleView);

                        // Re-add back button in top-left for puzzle view (smaller)
                        Button backBtnPuzzle = new Button(this);
                        backBtnPuzzle.setText("BACK");
                        backBtnPuzzle.setTextColor(Color.parseColor("#EAFBFF"));
                        backBtnPuzzle.setBackground(getDrawable(R.drawable.btn_close_red));
                        backBtnPuzzle.setTextSize(12);
                        backBtnPuzzle.setTypeface(null, android.graphics.Typeface.BOLD);
                        FrameLayout.LayoutParams backPuzzleParams = new FrameLayout.LayoutParams(
                                        (int) (90 * getResources().getDisplayMetrics().density),
                                        (int) (40 * getResources().getDisplayMetrics().density));
                        backPuzzleParams.gravity = Gravity.TOP | Gravity.LEFT;
                        backPuzzleParams.setMargins(15, 15, 0, 0);
                        backBtnPuzzle.setOnClickListener(v2 -> finish());
                        mainLayout.addView(backBtnPuzzle, backPuzzleParams);
                });
                mainLayout.addView(startBtn, startParams);

                // BACK BUTTON (Red style, below START)
                Button backBtn = new Button(this);
                backBtn.setText("BACK");
                backBtn.setTextColor(Color.parseColor("#EAFBFF"));
                backBtn.setBackground(getDrawable(R.drawable.btn_close_red));
                backBtn.setTextSize(16);
                backBtn.setTypeface(null, android.graphics.Typeface.BOLD);
                backBtn.setLetterSpacing(0.08f);
                backBtn.setElevation(6 * getResources().getDisplayMetrics().density);

                FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                                (int) (280 * getResources().getDisplayMetrics().density),
                                (int) (56 * getResources().getDisplayMetrics().density));
                backParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                backParams.setMargins(0, 0, 0, (int) (60 * getResources().getDisplayMetrics().density));

                backBtn.setOnClickListener(v -> finish());
                mainLayout.addView(backBtn, backParams);

                setContentView(mainLayout);
        }

        @Override
        protected void onPause() {
                super.onPause();
                // If showing puzzle view, pause it?
                // Ideally we need a reference if we want to pause it correctly.
        }
}
