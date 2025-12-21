package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class NeonInfoPanel extends View {

    private Paint bgPaint;
    private Paint borderPaint;
    private Paint glowPaint;
    private Paint textPaint;
    private Paint valuePaint;
    private Paint coinIconPaint;

    private String line1Label = "TIME:";
    private String line1Value = "20";
    private String line2Label = "SCORE:";
    private String line2Value = "0";
    private String coinsValue = "0"; // Coin değeri

    private int themeColor = Color.CYAN;

    public NeonInfoPanel(Context context) {
        super(context);
        init();
    }

    public NeonInfoPanel(Context context, AttributeSet attrs) {
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
        borderPaint.setColor(themeColor);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(themeColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14 * getResources().getDisplayMetrics().scaledDensity);
        textPaint.setTypeface(Typeface.MONOSPACE);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(themeColor);
        valuePaint.setTextSize(14 * getResources().getDisplayMetrics().scaledDensity);
        valuePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        coinIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coinIconPaint.setStyle(Paint.Style.FILL);
        coinIconPaint.setColor(Color.rgb(180, 100, 255)); // Purple for Coin
    }

    public void setData(String l1Label, String l1Value, String l2Label, String l2Value) {
        this.line1Label = l1Label;
        this.line1Value = l1Value;
        this.line2Label = l2Label;
        this.line2Value = l2Value;
        invalidate();
    }

    public void setCoins(String coins) {
        this.coinsValue = coins;
        invalidate();
    }

    public void setThemeColor(int color) {
        this.themeColor = color;
        borderPaint.setColor(color);
        glowPaint.setColor(color);
        valuePaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Increased height for 3 lines
        int desiredWidth = (int) (250 * getResources().getDisplayMetrics().density);
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

        // Text Layout
        float leftMargin = 20f;
        float lineSpacing = h / 4; // Divide space for 3 items generally
        float y1 = lineSpacing * 1.1f;
        float y2 = lineSpacing * 2.1f;
        float y3 = lineSpacing * 3.1f;

        // Line 1: TIME
        canvas.drawText(line1Label, leftMargin, y1, textPaint);
        float labelWidth = textPaint.measureText(line1Label);
        canvas.drawText(line1Value, leftMargin + labelWidth + 10, y1, valuePaint);

        // Line 2: SCORE
        canvas.drawText(line2Label, leftMargin, y2, textPaint);
        labelWidth = textPaint.measureText(line2Label);
        canvas.drawText(line2Value, leftMargin + labelWidth + 10, y2, valuePaint);

        // Line 3: COINS (Icon + Text)
        // Icon (Circle)
        float iconRadius = 8f * getResources().getDisplayMetrics().density;
        float iconX = leftMargin + iconRadius;
        float iconY = y3 - iconRadius / 2;

        canvas.drawCircle(iconX, iconY, iconRadius, coinIconPaint);

        // "C" in Icon
        Paint iconTextP = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconTextP.setColor(Color.WHITE);
        iconTextP.setTextSize(10 * getResources().getDisplayMetrics().scaledDensity);
        iconTextP.setTextAlign(Paint.Align.CENTER);
        iconTextP.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("C", iconX, iconY + iconTextP.getTextSize() / 3, iconTextP);

        // Coin Value
        canvas.drawText("COINS: " + coinsValue, iconX + iconRadius + 15, y3, textPaint);
    }
}
