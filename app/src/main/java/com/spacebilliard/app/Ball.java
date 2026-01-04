package com.spacebilliard.app;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Ball {
    public float x, y;
    public float vx, vy;
    public float radius;
    public int color;
    public boolean active = true;

    public Ball(float x, float y, float radius, int color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        canvas.drawCircle(x, y, radius, paint);
    }
}
