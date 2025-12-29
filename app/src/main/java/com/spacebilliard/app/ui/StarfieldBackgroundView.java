package com.spacebilliard.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class StarfieldBackgroundView extends View {

    private static class Star {
        float x, y;
        float radius;
        int alpha;
        boolean fadingOut;
        int speed;

        public Star(float width, float height) {
            reset(width, height);
            // Random initial alpha to prevent synchronized blinking
            alpha = (int) (Math.random() * 255);
        }

        public void reset(float width, float height) {
            Random r = new Random();
            x = r.nextFloat() * width;
            y = r.nextFloat() * height;
            radius = 2f + r.nextFloat() * 3f;
            alpha = r.nextInt(255);
            fadingOut = r.nextBoolean();
            speed = 2 + r.nextInt(5); // Blinking speed
        }

        public void update() {
            if (fadingOut) {
                alpha -= speed;
                if (alpha <= 50) {
                    alpha = 50;
                    fadingOut = false;
                }
            } else {
                alpha += speed;
                if (alpha >= 255) {
                    alpha = 255;
                    fadingOut = true;
                }
            }
        }
    }

    private final Paint paint = new Paint();
    private final ArrayList<Star> stars = new ArrayList<>();
    private final int STAR_COUNT = 100;
    private boolean initialized = false;

    public StarfieldBackgroundView(Context context) {
        super(context);
        init();
    }

    public StarfieldBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stars.clear();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(w, h));
        }
        initialized = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background color (Deep Space)
        canvas.drawColor(Color.parseColor("#141428"));

        if (!initialized)
            return;

        for (Star star : stars) {
            star.update();
            paint.setAlpha(star.alpha);
            canvas.drawCircle(star.x, star.y, star.radius, paint);
        }

        // Trigger next frame
        invalidate();
    }
}
