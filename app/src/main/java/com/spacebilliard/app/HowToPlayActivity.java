package com.spacebilliard.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager;
import android.widget.Button;
import android.graphics.Color;

import com.spacebilliard.app.ui.NeonButton;

public class HowToPlayActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_how_to_play);

        // Prevent closing by touching outside (modal behavior)
        setFinishOnTouchOutside(false);

        NeonButton btnGotIt = findViewById(R.id.btnGotIt);
        btnGotIt.setText("GOT IT!");
        btnGotIt.setThemeColor(Color.parseColor("#00E5FF")); // Cyan

        btnGotIt.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0); // No animation
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
