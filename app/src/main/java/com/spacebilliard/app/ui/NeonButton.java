package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class NeonButton extends View {

    private String text = "BUTTON";
    private int themeColor = Color.CYAN;
    private Paint borderPaint;
    private Paint textPaint;
    private Paint glowPaint;
    private Paint bgPaint;
    private Paint shadowPaint;
    private Paint accentPaint;
    private RectF bounds;
    private boolean isPressed = false;

    public NeonButton(Context context) {
        super(context);
        init();
    }

    public NeonButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private boolean showAdBadge = false;
    private boolean hasStartIcon = false;

    public NeonButton(Context context, String text, int color) {
        super(context);
        this.text = text;
        this.themeColor = color;
        init();
    }

    public void setShowAdBadge(boolean show) {
        this.showAdBadge = show;
        invalidate();
    }

    public void setHasStartIcon(boolean has) {
        this.hasStartIcon = has;
        invalidate();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);

        accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setStyle(Paint.Style.FILL);

        setClickable(true);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public void setThemeColor(int color) {
        this.themeColor = color;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) (140 * getResources().getDisplayMetrics().density);
        int desiredHeight = (int) (50 * getResources().getDisplayMetrics().density);

        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float w = getWidth();
        float h = getHeight();
        float margin = 4 * density;
        float radius = h / 2; // Pill shape

        if (bounds == null) {
            bounds = new RectF();
        }
        bounds.set(margin, margin, w - margin, h - margin);

        // 1. Shadow Layer (Bottom depth)
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(isPressed ? 40 : 80);
        RectF shadowRect = new RectF(bounds.left + 2 * density, bounds.top + 3 * density,
                bounds.right + 2 * density, bounds.bottom + 3 * density);
        canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint);

        // 2. Main Body with Gradient
        int topColor = isPressed ? darkenColor(themeColor, 0.4f) : lightenColor(themeColor, 0.2f);
        int bottomColor = isPressed ? darkenColor(themeColor, 0.6f) : darkenColor(themeColor, 0.3f);

        LinearGradient gradient = new LinearGradient(
                0, bounds.top, 0, bounds.bottom,
                topColor, bottomColor, Shader.TileMode.CLAMP);
        bgPaint.setShader(gradient);
        canvas.drawRoundRect(bounds, radius, radius, bgPaint);
        bgPaint.setShader(null);

        // 3. Top Accent Bar (Decorative highlight)
        float accentH = h * 0.25f;
        RectF accentRect = new RectF(bounds.left + 8 * density, bounds.top + 4 * density,
                bounds.right - 8 * density, bounds.top + accentH);
        accentPaint.setColor(Color.WHITE);
        accentPaint.setAlpha(isPressed ? 30 : 60);
        canvas.drawRoundRect(accentRect, radius * 0.6f, radius * 0.6f, accentPaint);

        // 4. Outer Glow
        glowPaint.setColor(themeColor);
        glowPaint.setAlpha(isPressed ? 60 : 100);
        glowPaint.setStrokeWidth(6f);
        canvas.drawRoundRect(bounds, radius, radius, glowPaint);

        // 5. Border
        borderPaint.setColor(lightenColor(themeColor, 0.3f));
        borderPaint.setAlpha(isPressed ? 180 : 255);
        canvas.drawRoundRect(bounds, radius, radius, borderPaint);

        // 6. Inner Border (Detail)
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setAlpha(100);
        RectF innerBorder = new RectF(bounds.left + 3 * density, bounds.top + 3 * density,
                bounds.right - 3 * density, bounds.bottom - 3 * density);
        canvas.drawRoundRect(innerBorder, radius * 0.8f, radius * 0.8f, borderPaint);

        // 7. Text & Icons
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(h * 0.4f);
        textPaint.setShadowLayer(8, 0, isPressed ? 1 : 2, Color.argb(150, 0, 0, 0));

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = h / 2 - (fm.descent + fm.ascent) / 2;
        if (isPressed)
            textY += 2 * density;

        float textWidth = textPaint.measureText(text);
        float centerX = w / 2;

        // Adjust center if icons present
        // If start icon is present, shift text slightly right
        if (hasStartIcon) {
            centerX += 10 * density;
        }
        // If ad badge is present, shift text slightly left
        if (showAdBadge) {
            centerX -= 15 * density;
        }

        canvas.drawText(text, centerX, textY, textPaint);
        textPaint.clearShadowLayer();

        // Draw Start Icon (Triangle)
        if (hasStartIcon) {
            Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            iconPaint.setColor(Color.WHITE);
            iconPaint.setStyle(Paint.Style.FILL);
            iconPaint.setShadowLayer(5, 0, 0, Color.BLACK);

            android.graphics.Path path = new android.graphics.Path();
            float iconH = h * 0.35f;
            float iconW = iconH * 0.8f;
            float iconX = centerX - textWidth / 2 - iconW - 10 * density;
            float iconY = h / 2;

            if (isPressed)
                iconY += 2 * density;

            path.moveTo(iconX, iconY - iconH / 2);
            path.lineTo(iconX + iconW, iconY);
            path.lineTo(iconX, iconY + iconH / 2);
            path.close();

            canvas.drawPath(path, iconPaint);
        }

        // Draw AD Badge
        if (showAdBadge) {
            float badgeH = h * 0.5f;
            float badgeW = badgeH * 2.2f;
            float badgeRight = w - 12 * density;
            float badgeTop = h / 2 - badgeH / 2;

            if (isPressed)
                badgeTop += 2 * density;

            RectF badgeRect = new RectF(badgeRight - badgeW, badgeTop, badgeRight, badgeTop + badgeH);

            // Badge Background
            Paint badgeBg = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeBg.setColor(Color.argb(80, 0, 0, 0)); // Dark semi-transparent
            canvas.drawRoundRect(badgeRect, badgeH / 2, badgeH / 2, badgeBg);

            // Badge Border
            Paint badgeBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            badgeBorder.setStyle(Paint.Style.STROKE);
            badgeBorder.setColor(Color.argb(150, 255, 255, 255));
            badgeBorder.setStrokeWidth(2f);
            canvas.drawRoundRect(badgeRect, badgeH / 2, badgeH / 2, badgeBorder);

            // "AD" Text
            textPaint.setTextSize(badgeH * 0.6f);
            textPaint.setTextAlign(Paint.Align.LEFT);
            float badgeTextY = badgeTop + badgeH / 2 - (textPaint.descent() + textPaint.ascent()) / 2;
            canvas.drawText("AD", badgeRect.left + 8 * density, badgeTextY, textPaint);

            // Small Play Icon in Badge
            Paint smallPlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            smallPlayPaint.setColor(Color.YELLOW); // Gold/Yellow play button
            smallPlayPaint.setStyle(Paint.Style.FILL);

            android.graphics.Path p = new android.graphics.Path();
            float pSize = badgeH * 0.4f;
            float pX = badgeRect.right - pSize - 6 * density;
            float pY = badgeTop + badgeH / 2;

            p.moveTo(pX, pY - pSize / 2);
            p.lineTo(pX + pSize * 0.8f, pY);
            p.lineTo(pX, pY + pSize / 2);
            p.close();

            canvas.drawPath(p, smallPlayPaint);

            // Reset text alignment
            textPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    private int lightenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = Math.min(255, (int) (r + (255 - r) * factor));
        g = Math.min(255, (int) (g + (255 - g) * factor));
        b = Math.min(255, (int) (b + (255 - b) * factor));

        return Color.rgb(r, g, b);
    }

    private int darkenColor(int color, float factor) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = (int) (r * (1 - factor));
        g = (int) (g * (1 - factor));
        b = (int) (b * (1 - factor));

        return Color.rgb(r, g, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                invalidate();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    performClick();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
