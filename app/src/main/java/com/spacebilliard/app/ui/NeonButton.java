package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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

    public NeonButton(Context context, String text, int color) {
        super(context);
        this.text = text;
        this.themeColor = color;
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

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
        int desiredWidth = (int) (120 * getResources().getDisplayMetrics().density);
        int desiredHeight = (int) (45 * getResources().getDisplayMetrics().density);

        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float pad = 10;
        float w = getWidth();
        float h = getHeight();
        float radius = h / 2f;

        if (bounds == null) {
            bounds = new RectF();
        }
        bounds.set(pad, pad, w - pad, h - pad);

        // 1. Draw Background (Glass effect)
        bgPaint.setColor(themeColor);
        bgPaint.setAlpha(isPressed ? 80 : 30);
        canvas.drawRoundRect(bounds, radius, radius, bgPaint);

        // 2. Draw Glow
        for (int i = 1; i <= 3; i++) {
            glowPaint.setColor(themeColor);
            glowPaint.setAlpha(100 / i);
            glowPaint.setStrokeWidth(i * 4);
            canvas.drawRoundRect(bounds, radius, radius, glowPaint);
        }

        // 3. Draw Border
        borderPaint.setColor(themeColor);
        borderPaint.setShadowLayer(10, 0, 0, themeColor);
        canvas.drawRoundRect(bounds, radius, radius, borderPaint);
        borderPaint.clearShadowLayer();

        // 4. Draw Text
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(h * 0.45f);
        textPaint.setShadowLayer(15, 0, 0, themeColor);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float y = h / 2 - (fm.descent + fm.ascent) / 2;

        canvas.drawText(text, w / 2, y, textPaint);
        textPaint.clearShadowLayer();
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
