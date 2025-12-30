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
        Button btnStart = findViewById(R.id.btnStart);

        Button btnHowTo = findViewById(R.id.btnHowTo);
        Button btnHallOfFame = findViewById(R.id.btnHallOfFame);
        Button btnShop = findViewById(R.id.btnShop);

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnShop.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        btnHowTo.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, HowToPlayActivity.class);
            startActivity(intent);
        });

        btnHallOfFame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, HallOfFameActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
