package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class NeonPowerPanel extends View {

    private Paint bgPaint;
    private Paint borderPaint;
    private Paint glowPaint;
    private Paint textPaint;
    private Paint powerBarBgPaint;
    private Paint powerBarFillPaint;
    private Paint stageTextPaint;
    private Paint livesTextPaint;
    private Paint heartPaint;
    private Path heartPath;

    private int power = 0; // 0-100
    private String stage = "1/10";
    private int lives = 3;

    private int powerColor = Color.rgb(255, 100, 100); // Red-ish
    private int stageColor = Color.GREEN;
    private int livesColor = Color.CYAN;
    private int heartColor = Color.rgb(255, 60, 120); // Pink/Red

    public NeonPowerPanel(Context context) {
        super(context);
        init();
    }

    public NeonPowerPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.argb(180, 10, 10, 30));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setColor(Color.CYAN);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(Color.CYAN);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(powerColor);
        textPaint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        powerBarBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        powerBarBgPaint.setStyle(Paint.Style.FILL);
        powerBarBgPaint.setColor(Color.argb(100, 50, 50, 50));

        powerBarFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        powerBarFillPaint.setStyle(Paint.Style.FILL);
        powerBarFillPaint.setColor(powerColor);

        stageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stageTextPaint.setColor(stageColor);
        stageTextPaint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
        stageTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        livesTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        livesTextPaint.setColor(Color.WHITE); // White text for lives number
        livesTextPaint.setTextSize(14 * getResources().getDisplayMetrics().scaledDensity);
        livesTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        heartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        heartPaint.setStyle(Paint.Style.FILL);
        heartPaint.setColor(heartColor);
        heartPaint.setShadowLayer(5, 0, 0, heartColor);

        heartPath = new Path();
    }

    public void setPower(int power) {
        this.power = Math.max(0, Math.min(100, power));
        invalidate();
    }

    public void setStage(String stage) {
        this.stage = stage;
        invalidate();
    }

    public void setLives(int lives) {
        this.lives = lives;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Match height with InfoPanel (85dp)
        int desiredWidth = (int) (200 * getResources().getDisplayMetrics().density);
        int desiredHeight = (int) (85 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float cornerRadius = 20f;

        RectF rect = new RectF(5, 5, w - 5, h - 5);

        // Glow
        for (int i = 1; i <= 3; i++) {
            glowPaint.setAlpha(60 / i);
            glowPaint.setStrokeWidth(i * 3);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint);
        }

        // Background
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);

        // Border
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);

        // Content Layout (3 Lines)
        float leftMargin = 15f;
        float lineSpacing = h / 4;
        float y1 = lineSpacing * 1.1f; // Power
        float y2 = lineSpacing * 2.1f; // Stage
        float y3 = lineSpacing * 3.1f; // Lives (Heart)

        // Line 1: POWER label and bar
        canvas.drawText("POWER:", leftMargin, y1, textPaint);

        String maxPercentText = "100%";
        float percentWidth = textPaint.measureText(maxPercentText);
        float rightMargin = 15f;

        float barX = leftMargin + textPaint.measureText("POWER:") + 10;
        float barWidth = w - barX - percentWidth - rightMargin - 15;
        float barHeight = 6f; // Small bar
        float barY = y1 - barHeight + 2; // Vertically center with text approx

        RectF barBg = new RectF(barX, barY, barX + barWidth, barY + barHeight);
        canvas.drawRoundRect(barBg, 3, 3, powerBarBgPaint);

        float fillWidth = (barWidth * power) / 100f;
        RectF barFill = new RectF(barX, barY, barX + fillWidth, barY + barHeight);
        canvas.drawRoundRect(barFill, 3, 3, powerBarFillPaint);

        // Percentage (Aligned Right)
        String percentText = power + "%";
        canvas.drawText(percentText, w - rightMargin - percentWidth, y1, textPaint);

        // Line 2: STAGE
        canvas.drawText("STAGE: ", leftMargin, y2, textPaint);
        float stageX = leftMargin + textPaint.measureText("STAGE: ");
        canvas.drawText(stage, stageX, y2, stageTextPaint);

        // Line 3: LIVES (Heart Icon + Number)
        float heartSize = 14 * getResources().getDisplayMetrics().density;
        // Center heart vertically on y3 line
        float heartX = leftMargin + heartSize / 2;
        float heartY = y3 - heartSize / 3; // Adjusted to align better with text baseline

        drawHeart(canvas, heartX, heartY, heartSize);

        // Lives Count (Aligned next to heart)
        canvas.drawText(String.valueOf(lives), heartX + heartSize + 10, y3, livesTextPaint);
    }

    private void drawHeart(Canvas canvas, float x, float y, float size) {
        heartPath.reset();
        float halfSize = size / 2;
        // Simple heart shape
        heartPath.moveTo(x, y + halfSize * 0.6f);
        heartPath.cubicTo(x - halfSize * 2, y - halfSize, x - halfSize * 0.5f, y - halfSize * 1.5f, x,
                y - halfSize * 0.5f);
        heartPath.cubicTo(x + halfSize * 0.5f, y - halfSize * 1.5f, x + halfSize * 2, y - halfSize, x,
                y + halfSize * 0.6f);

        canvas.drawPath(heartPath, heartPaint);
    }
}
