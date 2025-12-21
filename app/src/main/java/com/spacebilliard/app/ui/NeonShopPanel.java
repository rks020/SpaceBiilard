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
    private Paint awningPaint;
    private Paint headerBgPaint;
    private Paint textPaint;
    private Paint shadowPaint;
    private Paint accentPaint;
    private Paint decorPaint;

    public NeonShopPanel(Context context) {
        super(context);
        init();
    }

    public NeonShopPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(Color.rgb(255, 100, 50));

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setColor(Color.rgb(255, 150, 0));

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        awningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        awningPaint.setStyle(Paint.Style.FILL);

        headerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerBgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.WHITE);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.BLACK);

        accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setStyle(Paint.Style.FILL);

        decorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        decorPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float density = getResources().getDisplayMetrics().density;

        float headerHeight = 100 * density;
        float awningHeight = 70 * density;
        float margin = 15 * density;

        // Main body area
        RectF bodyRect = new RectF(margin, headerHeight + awningHeight - 20 * density,
                w - margin, h - margin);

        // 1. Deep Shadow
        shadowPaint.setAlpha(100);
        RectF shadowRect = new RectF(bodyRect.left + 5 * density, bodyRect.top + 5 * density,
                bodyRect.right + 5 * density, bodyRect.bottom + 5 * density);
        canvas.drawRoundRect(shadowRect, 25 * density, 25 * density, shadowPaint);

        // 2. Outer Glow
        glowPaint.setAlpha(80);
        glowPaint.setStrokeWidth(12f);
        canvas.drawRoundRect(bodyRect, 25 * density, 25 * density, glowPaint);

        // 3. Main Body with Gradient (Warm tones)
        LinearGradient bodyGradient = new LinearGradient(
                0, bodyRect.top, 0, bodyRect.bottom,
                new int[] { Color.rgb(200, 80, 60), Color.rgb(150, 50, 40) },
                null, Shader.TileMode.CLAMP);
        bgPaint.setShader(bodyGradient);
        canvas.drawRoundRect(bodyRect, 25 * density, 25 * density, bgPaint);
        bgPaint.setShader(null);

        // 4. Inner Content Area (Darker)
        RectF contentRect = new RectF(bodyRect.left + 12 * density, bodyRect.top + 12 * density,
                bodyRect.right - 12 * density, bodyRect.bottom - 12 * density);
        bgPaint.setColor(Color.argb(230, 20, 10, 15));
        canvas.drawRoundRect(contentRect, 20 * density, 20 * density, bgPaint);

        // 5. Decorative Stripes on sides
        decorPaint.setColor(Color.rgb(180, 60, 40));
        float stripeW = 8 * density;
        canvas.drawRoundRect(new RectF(bodyRect.left + 5 * density, bodyRect.top + 20 * density,
                bodyRect.left + 5 * density + stripeW, bodyRect.bottom - 20 * density),
                4 * density, 4 * density, decorPaint);
        canvas.drawRoundRect(new RectF(bodyRect.right - 5 * density - stripeW, bodyRect.top + 20 * density,
                bodyRect.right - 5 * density, bodyRect.bottom - 20 * density),
                4 * density, 4 * density, decorPaint);

        // 6. Border
        borderPaint.setColor(Color.rgb(255, 120, 60));
        canvas.drawRoundRect(bodyRect, 25 * density, 25 * density, borderPaint);

        // 7. Decorative Awning (Striped canopy)
        drawAwning(canvas, margin + 10 * density, headerHeight - 10 * density,
                w - margin - 10 * density, awningHeight, density);

        // 8. Header Sign Board
        drawHeaderSign(canvas, w / 2, headerHeight / 2, density);

        super.onDraw(canvas);
    }

    private void drawAwning(Canvas canvas, float left, float top, float right, float height, float density) {
        int stripes = 8;
        float stripeWidth = (right - left) / stripes;

        // Awning stripes
        for (int i = 0; i < stripes; i++) {
            boolean isRed = i % 2 == 0;
            awningPaint.setColor(isRed ? Color.rgb(220, 60, 40) : Color.rgb(255, 200, 100));

            RectF stripe = new RectF(left + i * stripeWidth, top,
                    left + (i + 1) * stripeWidth, top + height);
            canvas.drawRect(stripe, awningPaint);

            // Wavy bottom edge
            float cx = stripe.centerX();
            float cy = stripe.bottom;
            float radius = stripeWidth / 2.2f;
            canvas.drawCircle(cx, cy, radius, awningPaint);
        }

        // Top shadow
        shadowPaint.setAlpha(60);
        canvas.drawRect(left, top, right, top + 4 * density, shadowPaint);

        // Support poles
        decorPaint.setColor(Color.rgb(100, 40, 30));
        float poleW = 6 * density;
        canvas.drawRect(left - poleW / 2, top, left + poleW / 2, top + height + 15 * density, decorPaint);
        canvas.drawRect(right - poleW / 2, top, right + poleW / 2, top + height + 15 * density, decorPaint);
    }

    private void drawHeaderSign(Canvas canvas, float cx, float cy, float density) {
        float signW = 250 * density;
        float signH = 70 * density;
        RectF signRect = new RectF(cx - signW / 2, cy - signH / 2, cx + signW / 2, cy + signH / 2);

        // Shadow
        shadowPaint.setAlpha(120);
        RectF shadowRect = new RectF(signRect.left + 3 * density, signRect.top + 3 * density,
                signRect.right + 3 * density, signRect.bottom + 3 * density);
        canvas.drawRoundRect(shadowRect, 15 * density, 15 * density, shadowPaint);

        // Sign background gradient
        LinearGradient signGradient = new LinearGradient(
                0, signRect.top, 0, signRect.bottom,
                new int[] { Color.rgb(255, 140, 60), Color.rgb(200, 80, 40) },
                null, Shader.TileMode.CLAMP);
        headerBgPaint.setShader(signGradient);
        canvas.drawRoundRect(signRect, 15 * density, 15 * density, headerBgPaint);
        headerBgPaint.setShader(null);

        // Decorative top bar
        accentPaint.setColor(Color.rgb(255, 200, 100));
        RectF topBar = new RectF(signRect.left + 10 * density, signRect.top + 5 * density,
                signRect.right - 10 * density, signRect.top + 15 * density);
        canvas.drawRoundRect(topBar, 5 * density, 5 * density, accentPaint);

        // Border
        borderPaint.setColor(Color.rgb(255, 180, 80));
        borderPaint.setStrokeWidth(3f);
        canvas.drawRoundRect(signRect, 15 * density, 15 * density, borderPaint);

        // Inner border
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setAlpha(150);
        RectF innerBorder = new RectF(signRect.left + 5 * density, signRect.top + 5 * density,
                signRect.right - 5 * density, signRect.bottom - 5 * density);
        canvas.drawRoundRect(innerBorder, 12 * density, 12 * density, borderPaint);
        borderPaint.setAlpha(255);

        // "SHOP" text
        textPaint.setTextSize(40 * density);
        textPaint.setShadowLayer(6, 0, 2 * density, Color.argb(180, 0, 0, 0));
        canvas.drawText("SHOP", cx, cy + 12 * density, textPaint);
        textPaint.clearShadowLayer();

        // Decorative screws
        decorPaint.setColor(Color.rgb(120, 60, 40));
        float screwR = 3 * density;
        canvas.drawCircle(signRect.left + 10 * density, signRect.top + 10 * density, screwR, decorPaint);
        canvas.drawCircle(signRect.right - 10 * density, signRect.top + 10 * density, screwR, decorPaint);
        canvas.drawCircle(signRect.left + 10 * density, signRect.bottom - 10 * density, screwR, decorPaint);
        canvas.drawCircle(signRect.right - 10 * density, signRect.bottom - 10 * density, screwR, decorPaint);
    }
}
