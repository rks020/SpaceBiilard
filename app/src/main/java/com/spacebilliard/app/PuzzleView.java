package com.spacebilliard.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;

public class PuzzleView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private Thread gameThread;
    private volatile boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;
    private float screenWidth, screenHeight;

    // Arena
    private float centerX, centerY;
    private float arenaRadius;
    private RectF arenaBounds;

    // Portals (Angles in Degrees)
    private float orangePortalAngle = 180f;
    private float bluePortalAngle = 0f;
    private float portalWidth = 45f;

    // Balls
    private Ball whiteBall;
    private List<Ball> targetBalls = new ArrayList<>();

    // Controls
    private boolean isTouchingOrange = false;
    private boolean isTouchingBlue = false;
    private float lastTouchOrangeX, lastTouchBlueX;
    private float CONTROL_Height = 400f;

    // Particles & Physics
    private ParticlePool particlePool = new ParticlePool();
    private List<Particle> activeParticles = new ArrayList<>();
    private float MAX_SPEED = 40f;
    private float TRAIL_SPEED_THRESHOLD = 20f;

    // Frame timing - EN ÖNEMLİ DEĞİŞİKLİK
    private static final long TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000000000 / TARGET_FPS; // nanoseconds
    private long lastFrameTime = System.nanoTime();

    private class Particle {
        float x, y;
        float radius;
        int alpha;
        int color;
        boolean active;

        public void reset(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.radius = 15f + (float) Math.random() * 10f;
            this.alpha = 255;
            this.active = true;
        }

        public boolean update() {
            if (!active)
                return false;
            alpha -= 15;
            radius *= 0.9f;
            if (alpha <= 0) {
                active = false;
                return false;
            }
            return true;
        }

        public void draw(Canvas canvas, Paint paint) {
            if (!active)
                return;
            int oldAlpha = paint.getAlpha();
            int oldColor = paint.getColor();
            paint.setColor(color);
            paint.setAlpha(alpha);
            canvas.drawCircle(x, y, radius, paint);
            paint.setColor(oldColor);
            paint.setAlpha(oldAlpha);
        }
    }

    private class ParticlePool {
        private final List<Particle> pool = new ArrayList<>();
        private final int MAX_POOL_SIZE = 100; // Azaltıldı

        public Particle obtain(float x, float y, int color) {
            Particle p;
            if (pool.isEmpty()) {
                p = new Particle();
            } else {
                p = pool.remove(pool.size() - 1);
            }
            p.reset(x, y, color);
            return p;
        }

        public void recycle(Particle p) {
            if (pool.size() < MAX_POOL_SIZE) {
                pool.add(p);
            }
        }
    }

    public PuzzleView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true); // Smooth edges
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    @Override
    public void run() {
        while (isPlaying) {
            long startTime = System.nanoTime();

            update();
            draw();

            // FPS kontrolü - kasma çözümü
            long frameTime = System.nanoTime() - startTime;
            if (frameTime < FRAME_TIME) {
                try {
                    Thread.sleep((FRAME_TIME - frameTime) / 1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void update() {
        if (!isPlaying)
            return;

        // Delta time hesaplama - smooth hareket için
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1000000000.0f;
        lastFrameTime = currentTime;

        // Cap delta time - büyük sıçramaları önler
        if (deltaTime > 0.05f)
            deltaTime = 0.05f;

        // Update Particles - daha az sıklıkta
        if (activeParticles.size() < 50) { // Partikül limiti
            for (int i = 0; i < activeParticles.size(); i++) {
                Particle p = activeParticles.get(i);
                if (!p.update()) {
                    particlePool.recycle(p);
                    activeParticles.remove(i);
                    i--;
                }
            }
        }

        // Update Balls
        if (whiteBall != null && whiteBall.active)
            updateBall(whiteBall, deltaTime);
        for (Ball b : targetBalls)
            updateBall(b, deltaTime);

        // Collisions between balls
        if (whiteBall != null && whiteBall.active) {
            for (Ball b : targetBalls)
                checkCollision(whiteBall, b);
        }
        for (int i = 0; i < targetBalls.size(); i++) {
            for (int j = i + 1; j < targetBalls.size(); j++) {
                checkCollision(targetBalls.get(i), targetBalls.get(j));
            }
        }
    }

    private void updateBall(Ball b, float deltaTime) {
        // Delta time ile smooth hareket
        float dt = Math.min(deltaTime * 60f, 2f); // 60 FPS normalize

        b.x += b.vx * dt;
        b.y += b.vy * dt;
        b.vx *= Math.pow(0.999f, dt);
        b.vy *= Math.pow(0.999f, dt);

        // Speed Check & Trail - daha az partikül
        float speed = (float) Math.sqrt(b.vx * b.vx + b.vy * b.vy);
        if (speed > TRAIL_SPEED_THRESHOLD && Math.random() > 0.7) { // %30 şans
            int color = (Math.random() > 0.5) ? Color.RED : Color.parseColor("#FFA500");
            if (activeParticles.size() < 50) {
                activeParticles.add(particlePool.obtain(b.x, b.y, color));
            }
        }

        // Cap Speed
        if (speed > MAX_SPEED) {
            float ratio = MAX_SPEED / speed;
            b.vx *= ratio;
            b.vy *= ratio;
        }

        // Circular Wall Collision & Portal Check
        float dx = b.x - centerX;
        float dy = b.y - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist + b.radius > arenaRadius) {
            float angleRad = (float) Math.atan2(dy, dx);
            float angleDeg = (float) Math.toDegrees(angleRad);
            if (angleDeg < 0)
                angleDeg += 360;

            // Check Portals
            if (isInsidePortal(angleDeg, orangePortalAngle)) {
                teleport(b, orangePortalAngle, bluePortalAngle);
            } else if (isInsidePortal(angleDeg, bluePortalAngle)) {
                teleport(b, bluePortalAngle, orangePortalAngle);
            } else {
                // Bounce off Wall
                float nx = dx / dist;
                float ny = dy / dist;

                float dot = b.vx * nx + b.vy * ny;
                b.vx = b.vx - 2 * dot * nx;
                b.vy = b.vy - 2 * dot * ny;

                float overlap = (dist + b.radius) - arenaRadius;
                b.x -= nx * overlap;
                b.y -= ny * overlap;
            }
        }
    }

    private boolean isInsidePortal(float ballAngle, float portalAngle) {
        float normPortalAngle = portalAngle % 360;
        if (normPortalAngle < 0)
            normPortalAngle += 360;

        float halfWidth = portalWidth / 2;
        float diff = Math.abs(angleDifference(ballAngle, normPortalAngle));
        return diff <= halfWidth;
    }

    private float angleDifference(float a1, float a2) {
        float diff = a1 - a2;
        while (diff < -180)
            diff += 360;
        while (diff > 180)
            diff -= 360;
        return diff;
    }

    private void teleport(Ball b, float inAngle, float outAngle) {
        float outRad = (float) Math.toRadians(outAngle);
        float spawnRadius = arenaRadius - b.radius - 20;

        b.x = centerX + (float) Math.cos(outRad) * spawnRadius;
        b.y = centerY + (float) Math.sin(outRad) * spawnRadius;

        float rotDeg = outAngle - inAngle + 180;
        float rotRad = (float) Math.toRadians(rotDeg);

        float cos = (float) Math.cos(rotRad);
        float sin = (float) Math.sin(rotRad);

        float newVx = b.vx * cos - b.vy * sin;
        float newVy = b.vx * sin + b.vy * cos;

        newVx *= 1.3f;
        newVy *= 1.3f;

        b.vx = newVx;
        b.vy = newVy;
    }

    private void checkCollision(Ball b1, Ball b2) {
        float dx = b1.x - b2.x;
        float dy = b1.y - b2.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < b1.radius + b2.radius && distance > 0) {
            float nx = dx / distance;
            float ny = dy / distance;
            float tx = -ny;
            float ty = nx;

            float dpTan1 = b1.vx * tx + b1.vy * ty;
            float dpTan2 = b2.vx * tx + b2.vy * ty;
            float dpNorm1 = b1.vx * nx + b1.vy * ny;
            float dpNorm2 = b2.vx * nx + b2.vy * ny;

            float finalNorm1 = dpNorm2;
            float finalNorm2 = dpNorm1;

            b1.vx = tx * dpTan1 + nx * finalNorm1;
            b1.vy = ty * dpTan1 + ny * finalNorm1;
            b2.vx = tx * dpTan2 + nx * finalNorm2;
            b2.vy = ty * dpTan2 + ny * finalNorm2;

            float overlap = (b1.radius + b2.radius - distance) / 2.0f;
            b1.x += nx * overlap;
            b1.y += ny * overlap;
            b2.x -= nx * overlap;
            b2.y -= ny * overlap;
        }
    }

    private void draw() {
        if (!holder.getSurface().isValid())
            return;
        Canvas canvas = holder.lockCanvas();
        if (canvas == null)
            return;

        canvas.drawColor(Color.BLACK);

        // Draw Arena
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, arenaRadius, paint);

        // Draw Particles
        paint.setStyle(Paint.Style.FILL);
        for (Particle p : activeParticles)
            p.draw(canvas, paint);

        // Draw Portals
        drawPortal(canvas, orangePortalAngle, Color.parseColor("#FFA500"));
        drawPortal(canvas, bluePortalAngle, Color.parseColor("#00BFFF"));

        // Draw Controls Zones
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 255, 165, 0));
        canvas.drawRect(0, screenHeight - CONTROL_Height, screenWidth / 2, screenHeight, paint);

        paint.setColor(Color.argb(50, 0, 191, 255));
        canvas.drawRect(screenWidth / 2, screenHeight - CONTROL_Height, screenWidth, screenHeight, paint);

        // Labels
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("ORANGE", 50, screenHeight - CONTROL_Height + 100, paint);
        canvas.drawText("BLUE", screenWidth / 2 + 50, screenHeight - CONTROL_Height + 100, paint);

        // Draw Balls
        paint.setStyle(Paint.Style.FILL);
        if (whiteBall != null && whiteBall.active)
            whiteBall.draw(canvas, paint);
        for (Ball b : targetBalls)
            b.draw(canvas, paint);

        holder.unlockCanvasAndPost(canvas);
    }

    private void drawPortal(Canvas canvas, float angle, int color) {
        int oldColor = paint.getColor();
        float oldWidth = paint.getStrokeWidth();
        Paint.Style oldStyle = paint.getStyle();

        paint.setColor(color);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);

        if (arenaBounds == null) {
            arenaBounds = new RectF(centerX - arenaRadius, centerY - arenaRadius,
                    centerX + arenaRadius, centerY + arenaRadius);
        }
        canvas.drawArc(arenaBounds, angle - portalWidth / 2, portalWidth, false, paint);

        paint.setColor(oldColor);
        paint.setStrokeWidth(oldWidth);
        paint.setStyle(oldStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);

                if (y > screenHeight - CONTROL_Height) {
                    if (x < screenWidth / 2) {
                        isTouchingOrange = true;
                        lastTouchOrangeX = x;
                    } else {
                        isTouchingBlue = true;
                        lastTouchBlueX = x;
                    }
                } else {
                    if (whiteBall != null) {
                        if (Math.abs(whiteBall.vx) < 1 && Math.abs(whiteBall.vy) < 1) {
                            float ddx = x - whiteBall.x;
                            float ddy = y - whiteBall.y;
                            float mag = (float) Math.sqrt(ddx * ddx + ddy * ddy);
                            if (mag > 0) {
                                whiteBall.vx = (ddx / mag) * 20;
                                whiteBall.vy = (ddy / mag) * 20;
                            }
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);

                    if (y > screenHeight - CONTROL_Height) {
                        if (x < screenWidth / 2 && isTouchingOrange) {
                            float delta = x - lastTouchOrangeX;
                            orangePortalAngle += delta * 0.5f;
                            lastTouchOrangeX = x;
                        } else if (x >= screenWidth / 2 && isTouchingBlue) {
                            float delta = x - lastTouchBlueX;
                            bluePortalAngle += delta * 0.5f;
                            lastTouchBlueX = x;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                float x = event.getX(pointerIndex);
                if (x < screenWidth / 2)
                    isTouchingOrange = false;
                else
                    isTouchingBlue = false;
                break;
            }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        centerX = w / 2f;
        centerY = (h - CONTROL_Height) / 2f;
        arenaRadius = (Math.min(screenWidth, (h - CONTROL_Height))) / 2f - 40;

        loadLevel();
    }

    private void loadLevel() {
        whiteBall = new Ball(centerX, centerY, 30, Color.WHITE);
        targetBalls.clear();
        activeParticles.clear();

        giveRandomVelocity(whiteBall);
    }

    private void giveRandomVelocity(Ball b) {
        double angle = Math.random() * Math.PI * 2;
        float speed = 15f;
        b.vx = (float) (Math.cos(angle) * speed);
        b.vy = (float) (Math.sin(angle) * speed);
    }

    public void pause() {
        isPlaying = false;
        try {
            if (gameThread != null)
                gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        lastFrameTime = System.nanoTime(); // Önemli: resume'da reset
        gameThread = new Thread(this);
        gameThread.start();
    }
}