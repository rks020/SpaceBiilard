package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.graphics.RectF;

public class NeonGameOverPanel extends FrameLayout {

    private Paint bgPaint;
    private Paint borderPaint;
    private Paint textPaint;
    private Path framePath;

    // Buttons
    public NeonButton btnRevive;
    public NeonButton btnReboot;
    // public NeonButton btnHallOfFame; // Removed
    public NeonButton btnMainMenu;

    public NeonGameOverPanel(Context context) {
        super(context);
        init(context);
    }

    public NeonGameOverPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private java.util.List<Star> stars = new java.util.ArrayList<>();
    private java.util.Random random = new java.util.Random();
    private Paint starPaint;

    private static class Star {
        float x, y, size;
        int alpha;

        Star(float x, float y, float size, int alpha) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
        }
    }

    private void init(Context context) {
        setWillNotDraw(false); // Enable onDraw
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.argb(220, 20, 20, 40));

        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.WHITE);
        starPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f);
        borderPaint.setColor(Color.RED);
        borderPaint.setShadowLayer(25, 0, 0, Color.RED);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.RED);
        textPaint.setShadowLayer(30, 0, 0, Color.RED);

        // Add Buttons
        int buttonWidth = (int) (180 * getResources().getDisplayMetrics().density);
        int buttonHeight = (int) (45 * getResources().getDisplayMetrics().density);
        int margin = (int) (15 * getResources().getDisplayMetrics().density);

        // Buttons will be positioned in onLayout or simple gravity with top margins
        // But since we have a custom background shape, standard gravity might not align
        // perfectly with the "visual" center.
        // We can use a vertical LinearLayout inside, centered.

        // Actually, let's use LayoutParams on components directly for now.

        // Buttons - Return to HQ Style
        // Buttons - Return to HQ Style
        btnRevive = new NeonButton(context, "REVIVE", Color.GREEN);
        btnRevive.setShowAdBadge(true); // Enable AD badge
        btnRevive.setHasStartIcon(true); // Enable Play icon
        addView(btnRevive, createParams(buttonWidth, buttonHeight));

        btnReboot = new NeonButton(context, "REBOOT LEVEL", Color.CYAN);
        addView(btnReboot, createParams(buttonWidth, buttonHeight));

        // Hall of Fame removed as requested

        btnMainMenu = new NeonButton(context, "ABORT MISSION", Color.rgb(255, 60, 60)); // Red for Abort
        addView(btnMainMenu, createParams(buttonWidth, buttonHeight));
    }

    private LayoutParams createParams(int w, int h) {
        LayoutParams params = new LayoutParams(w, h);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        return params;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int h = getHeight();

        // Panel dimensions matches onDraw
        float menuHeight = h * 0.55f;
        float menuTop = (h - menuHeight) / 2;

        // Header occupies top 25% of panel
        float contentStartY = menuTop + menuHeight * 0.35f;
        float space = h * 0.10f; // Spacing between buttons

        // Position buttons safely relative to panel
        btnRevive.setY(contentStartY);
        btnReboot.setY(contentStartY + space);
        btnMainMenu.setY(contentStartY + space * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        // 1. Draw Deep Space Background
        canvas.drawColor(Color.rgb(15, 15, 25)); // Deep dark blue/black

        // 2. Initialize and Draw Stars
        if (stars.isEmpty() && w > 0 && h > 0) {
            for (int i = 0; i < 100; i++) {
                float sx = random.nextFloat() * w;
                float sy = random.nextFloat() * h;
                float sSize = 2f + random.nextFloat() * 4f;
                int sAlpha = 100 + random.nextInt(155);
                stars.add(new Star(sx, sy, sSize, sAlpha));
            }
        }

        for (Star s : stars) {
            starPaint.setAlpha(s.alpha);
            canvas.drawCircle(s.x, s.y, s.size, starPaint);
        }

        // 3. Dark Overlay (Lighter now to see stars)
        canvas.drawColor(Color.argb(100, 0, 0, 0));

        float centerX = w / 2f;

        // Panel Geometry
        float menuWidth = w * 0.85f;
        float menuHeight = h * 0.55f;
        float menuTop = (h - menuHeight) / 2;
        float menuBottom = menuTop + menuHeight;

        // Modern Rounded Panel
        RectF panelRect = new RectF(centerX - menuWidth / 2, menuTop, centerX + menuWidth / 2, menuBottom);

        // 1. Background (Dark Glass)
        bgPaint.setColor(Color.argb(240, 15, 15, 25));
        canvas.drawRoundRect(panelRect, 60, 60, bgPaint);

        // 2. Border (Glowing Red)
        borderPaint.setColor(Color.RED);
        borderPaint.setStrokeWidth(6f);
        borderPaint.setShadowLayer(30, 0, 0, Color.RED);
        canvas.drawRoundRect(panelRect, 60, 60, borderPaint);

        // 3. Header Text "GAME OVER"
        textPaint.setTextSize(w * 0.12f);
        textPaint.setColor(Color.RED);
        textPaint.setShadowLayer(40, 0, 0, Color.RED);

        // Draw Text
        float headerY = menuTop + menuHeight * 0.20f;
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        canvas.drawText("GAME OVER", centerX, headerY - fm.ascent / 2, textPaint);

        // 4. Decor Line
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(4);
        linePaint.setAlpha(120);
        float lineY = headerY + 60;
        canvas.drawLine(centerX - menuWidth * 0.25f, lineY, centerX + menuWidth * 0.25f, lineY, linePaint);
    }
}
