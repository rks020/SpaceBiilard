package com.spacebilliard.app;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.spacebilliard.app.ui.NeonButton;
import com.spacebilliard.app.ui.NeonShopItem;

public class ShopActivity extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().setFlags(
                                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // 1. Root Container with Space Background
                FrameLayout root = new FrameLayout(this);
                root.setBackgroundColor(Color.rgb(5, 5, 20)); // Deep space blue

                // 2. Main Shop Panel (The one with the neon border)
                LinearLayout mainPanel = new LinearLayout(this);
                mainPanel.setOrientation(LinearLayout.VERTICAL);

                FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                                (int) (getResources().getDisplayMetrics().widthPixels * 0.9f),
                                (int) (getResources().getDisplayMetrics().heightPixels * 0.85f));
                panelParams.gravity = Gravity.CENTER;
                mainPanel.setLayoutParams(panelParams);

                // Custom Background for the main panel (Rounded rect with neon border)
                GradientDrawable panelBg = new GradientDrawable();
                panelBg.setColor(Color.argb(230, 10, 20, 40));
                panelBg.setCornerRadius(80f);
                panelBg.setStroke(6, Color.rgb(0, 255, 255)); // Neon Cyan border
                mainPanel.setBackground(panelBg);

                // 3. Header View (Top Bar)
                setupHeader(mainPanel);

                // 4. Awning View (The stripes)
                View awning = new View(this) {
                        @Override
                        protected void onDraw(Canvas canvas) {
                                float w = getWidth();
                                float h = getHeight();
                                int stripes = 8;
                                float stripeW = w / stripes;
                                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

                                for (int i = 0; i < stripes; i++) {
                                        p.setColor(i % 2 == 0 ? Color.WHITE : Color.rgb(0, 180, 200));
                                        canvas.drawRect(i * stripeW, 0, (i + 1) * stripeW, h - 20, p);

                                        // Semi-circle bottom effect
                                        canvas.drawCircle(i * stripeW + stripeW / 2, h - 20, stripeW / 2, p);
                                }
                        }
                };
                LinearLayout.LayoutParams awningParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) (60 * getResources().getDisplayMetrics().density));
                mainPanel.addView(awning, awningParams);

                // 5. Scrollable Grid Area
                ScrollView scrollView = new ScrollView(this);
                scrollView.setPadding(30, 20, 30, 20);
                scrollView.setVerticalScrollBarEnabled(false);
                LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
                mainPanel.addView(scrollView, scrollParams);

                GridLayout grid = new GridLayout(this);
                grid.setColumnCount(3);
                grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
                scrollView.addView(grid);

                // Populate items like in the image
                addItemsToGrid(grid);

                // 6. Footer Layout for Buttons
                setupFooter(mainPanel);

                root.addView(mainPanel);
                setContentView(root);
        }

        private void setupHeader(LinearLayout parent) {
                FrameLayout header = new FrameLayout(this);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) (70 * getResources().getDisplayMetrics().density));
                header.setLayoutParams(headerParams);

                // Header Background
                GradientDrawable headerBg = new GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                new int[] { Color.rgb(100, 50, 255), Color.rgb(50, 200, 255) });
                headerBg.setCornerRadii(new float[] { 80f, 80f, 80f, 80f, 0, 0, 0, 0 });
                header.setBackground(headerBg);

                TextView title = new TextView(this);
                title.setText("NEON SHOP");
                title.setTextColor(Color.WHITE);
                title.setTextSize(28);
                title.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                titleParams.gravity = Gravity.CENTER;
                header.addView(title, titleParams);

                // Close Button (X)
                TextView closeBtn = new TextView(this);
                closeBtn.setText("X");
                closeBtn.setTextColor(Color.argb(150, 0, 0, 0));
                closeBtn.setTextSize(24);
                closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
                FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                closeParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                closeParams.rightMargin = 40;
                closeBtn.setOnClickListener(v -> finish());
                header.addView(closeBtn, closeParams);

                parent.addView(header);
        }

        private void addItemsToGrid(GridLayout grid) {
                android.content.SharedPreferences prefs = getSharedPreferences("SpaceBilliard",
                                android.content.Context.MODE_PRIVATE);
                String currentSkin = prefs.getString("selectedSkin", "default");

                // Skins
                addItem(grid, "DEFAULT BALL", currentSkin.equals("default") ? "EQUIPPED" : "SELECT", Color.WHITE,
                                "SKIN", "NONE", "default");
                addItem(grid, "TR FLAG BALL", currentSkin.equals("tr_flag") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN",
                                "NONE", "tr_flag");
                addItem(grid, "SOCCER BALL", currentSkin.equals("soccer") ? "EQUIPPED" : "SELECT",
                                Color.rgb(200, 200, 200), "SKIN", "NONE", "soccer");
                addItem(grid, "NEON PULSE", currentSkin.equals("neon_pulse") ? "EQUIPPED" : "SELECT", Color.CYAN,
                                "SKIN", "NONE", "neon_pulse");

                // Additional Countries
                addItem(grid, "USA", currentSkin.equals("usa") ? "EQUIPPED" : "SELECT", Color.rgb(0, 40, 104), "SKIN",
                                "NONE", "usa");
                addItem(grid, "GERMANY", currentSkin.equals("germany") ? "EQUIPPED" : "SELECT", Color.BLACK, "SKIN",
                                "NONE", "germany");
                addItem(grid, "FRANCE", currentSkin.equals("france") ? "EQUIPPED" : "SELECT", Color.BLUE, "SKIN",
                                "NONE", "france");
                addItem(grid, "ITALY", currentSkin.equals("italy") ? "EQUIPPED" : "SELECT", Color.GREEN, "SKIN", "NONE",
                                "italy");
                addItem(grid, "UK", currentSkin.equals("uk") ? "EQUIPPED" : "SELECT", Color.rgb(1, 33, 105), "SKIN",
                                "NONE", "uk");
                addItem(grid, "SPAIN", currentSkin.equals("spain") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN", "NONE",
                                "spain");
                addItem(grid, "PORTUGAL", currentSkin.equals("portugal") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN",
                                "NONE", "portugal");
                addItem(grid, "NETHERLANDS", currentSkin.equals("netherlands") ? "EQUIPPED" : "SELECT",
                                Color.rgb(174, 28, 40), "SKIN", "NONE", "netherlands");
                addItem(grid, "BELGIUM", currentSkin.equals("belgium") ? "EQUIPPED" : "SELECT", Color.BLACK, "SKIN",
                                "NONE", "belgium");
                addItem(grid, "SWITZERLAND", currentSkin.equals("switzerland") ? "EQUIPPED" : "SELECT", Color.RED,
                                "SKIN", "NONE", "switzerland");
                addItem(grid, "AUSTRIA", currentSkin.equals("austria") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN",
                                "NONE", "austria");
                addItem(grid, "SWEDEN", currentSkin.equals("sweden") ? "EQUIPPED" : "SELECT", Color.BLUE, "SKIN",
                                "NONE", "sweden");
                addItem(grid, "NORWAY", currentSkin.equals("norway") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN", "NONE",
                                "norway");
                addItem(grid, "DENMARK", currentSkin.equals("denmark") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN",
                                "NONE", "denmark");
                addItem(grid, "FINLAND", currentSkin.equals("finland") ? "EQUIPPED" : "SELECT", Color.WHITE, "SKIN",
                                "NONE", "finland");
                addItem(grid, "POLAND", currentSkin.equals("poland") ? "EQUIPPED" : "SELECT", Color.WHITE, "SKIN",
                                "NONE", "poland");
                addItem(grid, "GREECE", currentSkin.equals("greece") ? "EQUIPPED" : "SELECT", Color.BLUE, "SKIN",
                                "NONE", "greece");
                addItem(grid, "IRELAND", currentSkin.equals("ireland") ? "EQUIPPED" : "SELECT", Color.GREEN, "SKIN",
                                "NONE", "ireland");
                addItem(grid, "CANADA", currentSkin.equals("canada") ? "EQUIPPED" : "SELECT", Color.RED, "SKIN", "NONE",
                                "canada");
                addItem(grid, "BRAZIL", currentSkin.equals("brazil") ? "EQUIPPED" : "SELECT", Color.GREEN, "SKIN",
                                "NONE", "brazil");

                // Premium Skins
                addItem(grid, "CYBER CORE", currentSkin.equals("cyber_core") ? "EQUIPPED" : "SELECT", Color.CYAN,
                                "SKIN",
                                "GEM", "cyber_core");
                addItem(grid, "SOLAR FLARE", currentSkin.equals("solar_flare") ? "EQUIPPED" : "SELECT", Color.YELLOW,
                                "SKIN",
                                "GEM", "solar_flare");
                addItem(grid, "FROST BITE", currentSkin.equals("frost_bite") ? "EQUIPPED" : "SELECT",
                                Color.rgb(200, 240, 255), "SKIN",
                                "GEM", "frost_bite");

                // Items
                // Removed items as requested by user

                // Trails (6 new colors)
                String currentTrail = prefs.getString("selectedTrail", "none");

                addItem(grid, "RED TRAIL", currentTrail.equals("red") ? "EQUIPPED" : "SELECT", Color.RED, "TRAIL",
                                "GEM", "trail_red");
                addItem(grid, "BLUE TRAIL", currentTrail.equals("blue") ? "EQUIPPED" : "SELECT", Color.BLUE, "TRAIL",
                                "GEM", "trail_blue");
                addItem(grid, "GREEN TRAIL", currentTrail.equals("green") ? "EQUIPPED" : "SELECT", Color.GREEN, "TRAIL",
                                "GEM", "trail_green");
                addItem(grid, "GOLD TRAIL", currentTrail.equals("gold") ? "EQUIPPED" : "SELECT", Color.rgb(255, 215, 0),
                                "TRAIL", "GEM", "trail_gold");
                addItem(grid, "PURPLE TRAIL", currentTrail.equals("purple") ? "EQUIPPED" : "SELECT",
                                Color.rgb(160, 32, 240), "TRAIL", "GEM", "trail_purple");
                addItem(grid, "PINK TRAIL", currentTrail.equals("pink") ? "EQUIPPED" : "SELECT",
                                Color.rgb(255, 105, 180), "TRAIL", "GEM", "trail_pink");
                addItem(grid, "NEON TRAIL", currentTrail.equals("neon") ? "EQUIPPED" : "SELECT", Color.CYAN, "TRAIL",
                                "GEM", "trail_neon");

                // Premium Trails
                addItem(grid, "COSMIC TRAIL", currentTrail.equals("cosmic") ? "EQUIPPED" : "SELECT",
                                Color.rgb(100, 100, 255), "TRAIL",
                                "GEM", "trail_cosmic");
                addItem(grid, "LAVA TRAIL", currentTrail.equals("lava") ? "EQUIPPED" : "SELECT", Color.rgb(255, 69, 0),
                                "TRAIL",
                                "GEM", "trail_lava");
                addItem(grid, "ELECTRIC TRAIL", currentTrail.equals("electric") ? "EQUIPPED" : "SELECT", Color.CYAN,
                                "TRAIL",
                                "GEM", "trail_electric");
                addItem(grid, "RAINBOW TRAIL", currentTrail.equals("rainbow") ? "EQUIPPED" : "SELECT", Color.MAGENTA,
                                "TRAIL",
                                "GEM", "trail_rainbow");

                // Auras
                String currentAura = prefs.getString("selectedAura", "none");
                addItem(grid, "NO AURA", currentAura.equals("none") ? "EQUIPPED" : "SELECT", Color.GRAY, "AURA", "NONE",
                                "aura_none");
                addItem(grid, "NEON AURA", currentAura.equals("neon") ? "EQUIPPED" : "SELECT", Color.rgb(0, 255, 100),
                                "AURA", "GEM", "aura_neon");

                // Trajectory
                String currentTraj = prefs.getString("selectedTrajectory", "dashed");
                addItem(grid, "LASER SIGHT", currentTraj.equals("laser") ? "EQUIPPED" : "SELECT", Color.RED, "SIGHT",
                                "GEM", "traj_laser");
                addItem(grid, "ELECTRIC SIGHT", currentTraj.equals("electric") ? "EQUIPPED" : "SELECT", Color.CYAN,
                                "SIGHT", "GEM", "traj_electric");
                addItem(grid, "PEARL SIGHT", currentTraj.equals("dots") ? "EQUIPPED" : "SELECT", Color.rgb(255, 215, 0),
                                "SIGHT", "GEM", "traj_dots");
                addItem(grid, "PLASMA SIGHT", currentTraj.equals("plasma") ? "EQUIPPED" : "SELECT", Color.MAGENTA,
                                "SIGHT", "GEM", "traj_plasma");

                // Impact Effects (BOOM)
                String currentBoom = prefs.getString("selectedImpact", "classic");
                addItem(grid, "STAR BURST", currentBoom.equals("stars") ? "EQUIPPED" : "SELECT", Color.YELLOW,
                                "BOOM", "GEM", "impact_stars");
                addItem(grid, "ELECTRIC BOOM", currentBoom.equals("electric") ? "EQUIPPED" : "SELECT", Color.CYAN,
                                "BOOM", "GEM", "impact_electric");
        }

        private void addItem(GridLayout grid, String name, String price, int color, String qty, String pIcon,
                        String skinId) {
                NeonShopItem item = new NeonShopItem(this, name, price, color, qty, pIcon);
                item.setSkinId(skinId);

                item.setOnClickListener(v -> {
                        if (skinId != null) {
                                android.content.SharedPreferences prefs = getSharedPreferences("SpaceBilliard",
                                                android.content.Context.MODE_PRIVATE);
                                if (skinId.startsWith("trail_")) {
                                        String type = skinId.substring(6);
                                        if (prefs.getString("selectedTrail", "none").equals(type)) {
                                                prefs.edit().putString("selectedTrail", "none").apply();
                                        } else {
                                                prefs.edit().putString("selectedTrail", type).apply();
                                        }
                                } else if (skinId.startsWith("aura_")) {
                                        String type = skinId.substring(5);
                                        if (prefs.getString("selectedAura", "none").equals(type)) {
                                                prefs.edit().putString("selectedAura", "none").apply();
                                        } else {
                                                prefs.edit().putString("selectedAura", type).apply();
                                        }
                                } else if (skinId.startsWith("traj_")) {
                                        String type = skinId.substring(5);
                                        if (prefs.getString("selectedTrajectory", "dashed").equals(type)) {
                                                prefs.edit().putString("selectedTrajectory", "dashed").apply();
                                        } else {
                                                prefs.edit().putString("selectedTrajectory", type).apply();
                                        }
                                } else if (skinId.startsWith("impact_")) {
                                        String type = skinId.substring(7);
                                        if (prefs.getString("selectedImpact", "classic").equals(type)) {
                                                prefs.edit().putString("selectedImpact", "classic").apply();
                                        } else {
                                                prefs.edit().putString("selectedImpact", type).apply();
                                        }
                                } else {
                                        if (prefs.getString("selectedSkin", "default").equals(skinId)) {
                                                prefs.edit().putString("selectedSkin", "default").apply();
                                        } else {
                                                prefs.edit().putString("selectedSkin", skinId).apply();
                                        }
                                }
                                // Refresh grid to show equipped status
                                grid.removeAllViews();
                                addItemsToGrid(grid);
                        }
                });

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(10, 10, 10, 10);
                params.width = (int) (95 * getResources().getDisplayMetrics().density);
                params.height = (int) (130 * getResources().getDisplayMetrics().density);
                item.setLayoutParams(params);
                grid.addView(item);
        }

        private void setupFooter(LinearLayout parent) {
                LinearLayout footer = new LinearLayout(this);
                footer.setOrientation(LinearLayout.HORIZONTAL);
                footer.setGravity(Gravity.CENTER);
                footer.setPadding(0, 20, 0, 40);

                NeonButton back = new NeonButton(this, "BACK", Color.rgb(255, 50, 255));
                back.setOnClickListener(v -> finish());

                NeonButton select = new NeonButton(this, "SELECT", Color.rgb(255, 200, 50));

                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                                (int) (120 * getResources().getDisplayMetrics().density),
                                (int) (45 * getResources().getDisplayMetrics().density));
                btnParams.setMargins(20, 0, 20, 0);

                footer.addView(back, btnParams);
                footer.addView(select, btnParams);

                parent.addView(footer);
        }
}
