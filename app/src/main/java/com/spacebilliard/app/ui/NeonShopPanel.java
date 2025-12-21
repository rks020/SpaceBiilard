package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class NeonShopPanel extends FrameLayout {

    private Paint borderPaint;
    private Paint glowPaint;
    private Paint bgPaint;
    private Paint awningPaintCyan;
    private Paint awningPaintWhite;
    private Paint headerBgPaint;
    private Paint textPaint;

    public NeonShopPanel(Context context) {
        super(context);
        init();
    }

    public NeonShopPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false); // Enable onDraw for ViewGroup
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Border
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5f);
        borderPaint.setColor(Color.CYAN);

        // Glow
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(15f);
        glowPaint.setColor(Color.CYAN);
        glowPaint.setAlpha(100);

        // Background (Dark)
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#CC050515")); // Semi-transparent dark

        // Awning
        awningPaintCyan = new Paint(Paint.ANTI_ALIAS_FLAG);
        awningPaintCyan.setColor(Color.CYAN);
        awningPaintCyan.setStyle(Paint.Style.FILL);

        awningPaintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        awningPaintWhite.setColor(Color.WHITE);
        awningPaintWhite.setStyle(Paint.Style.FILL);

        // Header Background (Purple)
        headerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerBgPaint.setStyle(Paint.Style.FILL);
        headerBgPaint.setColor(Color.parseColor("#AA8800FF")); // Purple semi-transparent

        // Header Text is drawn by a TextView child usually, but we can draw decorative
        // parts
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        // Define main body rect (below header)
        // Let's assume the panel covers the whole view but with padding
        float pad = 20f;
        RectF mainRect = new RectF(pad, pad + 150, w - pad, h - pad); // 150px top space for header/awning

        // Draw Glow
        glowPaint.setStrokeWidth(20f);
        glowPaint.setAlpha(50);
        canvas.drawRoundRect(mainRect, 40, 40, glowPaint);

        glowPaint.setStrokeWidth(10f);
        glowPaint.setAlpha(100);
        canvas.drawRoundRect(mainRect, 40, 40, glowPaint);

        // Draw Background
        canvas.drawRoundRect(mainRect, 40, 40, bgPaint);

        // Draw Border
        // Gradient Border (Cyan to Purple)
        LinearGradient shader = new LinearGradient(0, 0, 0, h,
                new int[] { Color.CYAN, Color.MAGENTA, Color.CYAN },
                null, Shader.TileMode.CLAMP);
        borderPaint.setShader(shader);
        canvas.drawRoundRect(mainRect, 40, 40, borderPaint);

        // Draw Awning (Tente)
        drawAwning(canvas, pad + 5, pad + 100, w - pad - 5); // Just below top edge

        // Draw Header Board (Purple) behind 'NEON SHOP' title
        // Position: Top of the panel, overlapping the awning slightly
        RectF headerRect = new RectF(w / 2 - 200, pad, w / 2 + 200, pad + 80);
        canvas.drawRoundRect(headerRect, 20, 20, headerBgPaint);

        // Header Border
        Paint headerBorder = new Paint(borderPaint);
        headerBorder.setShader(null);
        headerBorder.setColor(Color.CYAN);
        headerBorder.setStrokeWidth(3f);
        canvas.drawRoundRect(headerRect, 20, 20, headerBorder);

        super.onDraw(canvas);
    }

    private void drawAwning(Canvas canvas, float left, float top, float right) {
        float height = 60f;
        float width = right - left;
        int stripes = 7;
        float stripeWidth = width / stripes;

        // Draw Stripes
        for (int i = 0; i < stripes; i++) {
            RectF stripe = new RectF(left + i * stripeWidth, top, left + (i + 1) * stripeWidth, top + height);
            canvas.drawRect(stripe, (i % 2 == 0) ? awningPaintCyan : awningPaintWhite);

            // Wavy bottom
            float circleRadius = stripeWidth / 2;
            canvas.drawCircle(stripe.centerX(), stripe.bottom, circleRadius,
                    (i % 2 == 0) ? awningPaintCyan : awningPaintWhite);
        }

        // Top shadow line
        Paint shadow = new Paint();
        shadow.setColor(Color.BLACK);
        shadow.setAlpha(50);
        canvas.drawRect(left, top, right, top + 5, shadow);
    }
}
