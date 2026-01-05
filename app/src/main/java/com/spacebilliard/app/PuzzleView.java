package com.spacebilliard.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private float BASE_MAX_SPEED = 40f;
    private float TRAIL_SPEED_THRESHOLD = 20f;

    // Game State
    private long score = 0;
    private float speedMeter = 100f;
    private float maxSpeedMeter = 100f;
    private int speedLevel = 0;
    private int maxSpeedLevel = 10;
    private boolean gameOver = false;

    // Combo System
    private int comboCount = 0;
    private int comboMultiplier = 1;
    private long lastPortalTime = 0;
    private long comboResetDelay = 2000;

    // Time Attack Mode
    private boolean isTimeAttackMode = true;
    private float timeRemaining = 60f;
    private float maxTime = 60f;

    // Score Animation
    private float scoreTextScale = 1f;
    private float targetScoreScale = 1f;

    // Background stars
    private List<Star> stars = new ArrayList<>();

    private static class Star {
        float x, y;
        float brightness;
        float size;
    }

    // Frame timing
    private static final long TARGET_FPS = 60;
    private static final long FRAME_TIME = 1000000000 / TARGET_FPS;
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
        private final int MAX_POOL_SIZE = 100;

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
        paint.setAntiAlias(true);
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
        if (!isPlaying || gameOver)
            return;

        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastFrameTime) / 1000000000.0f;
        lastFrameTime = currentTime;

        if (deltaTime > 0.05f)
            deltaTime = 0.05f;

        // Check combo timeout
        if (comboCount > 0 && System.currentTimeMillis() - lastPortalTime > comboResetDelay) {
            comboCount = 0;
            comboMultiplier = 1;
        }

        // Score increases based on speed level AND combo
        int baseIncrease = speedLevel == maxSpeedLevel ? 10 : 1;
        score += baseIncrease * comboMultiplier;

        // Time Attack: decrease time
        if (isTimeAttackMode) {
            timeRemaining -= deltaTime;
            if (timeRemaining <= 0) {
                timeRemaining = 0;
                gameOver = true;
            }
        }

        // Animate score text scale
        scoreTextScale += (targetScoreScale - scoreTextScale) * 0.1f;

        // Update Particles
        if (activeParticles.size() < 50) {
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

        // Collisions
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
        float dt = Math.min(deltaTime * 60f, 2f);

        b.x += b.vx * dt;
        b.y += b.vy * dt;

        // Friction decreases with speed level
        float frictionFactor = 0.999f - (speedLevel * 0.0001f);
        b.vx *= Math.pow(frictionFactor, dt);
        b.vy *= Math.pow(frictionFactor, dt);

        // Trail
        float speed = (float) Math.sqrt(b.vx * b.vx + b.vy * b.vy);
        if (speed > TRAIL_SPEED_THRESHOLD && Math.random() > 0.7) {
            int color = (Math.random() > 0.5) ? Color.RED : Color.parseColor("#FFA500");
            if (activeParticles.size() < 50) {
                activeParticles.add(particlePool.obtain(b.x, b.y, color));
            }
        }

        // Cap Speed
        float maxSpeed = BASE_MAX_SPEED + (speedLevel * 8f);
        if (speed > maxSpeed) {
            float ratio = maxSpeed / speed;
            b.vx *= ratio;
            b.vy *= ratio;
        }

        // Wall Collision & Portal Check
        float dx = b.x - centerX;
        float dy = b.y - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist + b.radius > arenaRadius) {
            float angleRad = (float) Math.atan2(dy, dx);
            float angleDeg = (float) Math.toDegrees(angleRad);
            if (angleDeg < 0)
                angleDeg += 360;

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

                // Slow down
                b.vx *= 0.7f;
                b.vy *= 0.7f;

                if (speedLevel > 0) {
                    speedLevel--;
                }

                // Decrease speed meter
                speedMeter -= 15f;
                if (speedMeter <= 0) {
                    speedMeter = 0;
                    gameOver = true;
                }

                // Reset combo
                comboCount = 0;
                comboMultiplier = 1;
                targetScoreScale = 1f;

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
        // Spawn deeper inside for smoother transition
        float spawnRadius = arenaRadius - b.radius - 80;

        b.x = centerX + (float) Math.cos(outRad) * spawnRadius;
        b.y = centerY + (float) Math.sin(outRad) * spawnRadius;

        float rotDeg = outAngle - inAngle + 180;
        float rotRad = (float) Math.toRadians(rotDeg);

        float cos = (float) Math.cos(rotRad);
        float sin = (float) Math.sin(rotRad);

        float newVx = b.vx * cos - b.vy * sin;
        float newVy = b.vx * sin + b.vy * cos;

        // Speed boost
        newVx *= 1.8f;
        newVy *= 1.8f;

        if (speedLevel < maxSpeedLevel) {
            speedLevel++;
        }

        speedMeter += 10f;
        if (speedMeter > maxSpeedMeter) {
            speedMeter = maxSpeedMeter;
        }

        // Combo System
        comboCount++;
        comboMultiplier = Math.min(comboCount, 10);
        lastPortalTime = System.currentTimeMillis();

        targetScoreScale = 1f + (comboMultiplier * 0.1f);

        // Time Attack bonus
        if (isTimeAttackMode) {
            timeRemaining += 2f;
            if (timeRemaining > maxTime) {
                timeRemaining = maxTime;
            }
        }

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

        // Dark blue gradient background
        paint.setStyle(Paint.Style.FILL);
        int topColor = Color.rgb(5, 5, 20);
        int bottomColor = Color.rgb(15, 15, 40);
        android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                0, 0, 0, screenHeight,
                topColor, bottomColor,
                android.graphics.Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        paint.setShader(null);

        // Draw stars
        paint.setStyle(Paint.Style.FILL);
        for (Star star : stars) {
            int alpha = (int) (star.brightness * 255);
            paint.setColor(Color.argb(alpha, 255, 255, 255));
            canvas.drawCircle(star.x, star.y, star.size, paint);
        }

        // Arena with GLOW - Purple to avoid blue portal conflict
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(20);
        paint.setColor(Color.argb(50, 200, 50, 255)); // Purple glow
        canvas.drawCircle(centerX, centerY, arenaRadius + 10, paint);

        paint.setStrokeWidth(12);
        paint.setColor(Color.argb(255, 200, 50, 255)); // Purple main
        canvas.drawCircle(centerX, centerY, arenaRadius, paint);

        // Particles
        paint.setStyle(Paint.Style.FILL);
        for (Particle p : activeParticles)
            p.draw(canvas, paint);

        // Portals
        drawPortal(canvas, orangePortalAngle, Color.parseColor("#FFA500"));
        drawPortal(canvas, bluePortalAngle, Color.parseColor("#00BFFF"));

        // Controls
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 255, 165, 0));
        canvas.drawRect(0, screenHeight - CONTROL_Height, screenWidth / 2, screenHeight, paint);

        paint.setColor(Color.argb(50, 0, 191, 255));
        canvas.drawRect(screenWidth / 2, screenHeight - CONTROL_Height, screenWidth, screenHeight, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("ORANGE", 50, screenHeight - CONTROL_Height + 100, paint);
        canvas.drawText("BLUE", screenWidth / 2 + 50, screenHeight - CONTROL_Height + 100, paint);

        // Score - Animated
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        float animatedSize = 80 * scoreTextScale;
        paint.setTextSize(animatedSize);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(score), centerX, 120, paint);

        // Speed Meter - Neon style
        drawSpeedMeter(canvas);

        // Combo Counter with neon star
        if (comboCount > 1) {
            drawComboStar(canvas, screenWidth - 180, 80, 30);
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(50);
            paint.setColor(Color.YELLOW);
            canvas.drawText("x" + comboMultiplier, screenWidth - 30, 100, paint);
        }

        // Time - with neon clock
        if (isTimeAttackMode) {
            float iconY = comboCount > 1 ? 130 : 30;
            drawClockIcon(canvas, screenWidth - 160, iconY + 20, timeRemaining > 10);

            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(40);
            int timeColor = timeRemaining > 10 ? Color.rgb(0, 255, 255) : Color.RED;
            paint.setColor(timeColor);
            canvas.drawText(String.format("%.1fs", timeRemaining), screenWidth - 30, comboCount > 1 ? 160 : 60, paint);
        }

        // Speed Level
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        canvas.drawText("Speed: " + speedLevel + "/" + maxSpeedLevel, 30, 90, paint);

        // Game Over
        if (gameOver) {
            paint.setTextSize(100);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.RED);
            canvas.drawText("GAME OVER", centerX, centerY - 100, paint);
            paint.setTextSize(50);
            canvas.drawText("Score: " + score, centerX, centerY - 20, paint);

            // RESTART button (red style)
            drawRedButton(canvas, centerX - 150, centerY + 40, 300, 80, "RESTART");

            // BACK button below restart
            drawRedButton(canvas, centerX - 150, centerY + 140, 300, 80, "BACK");
        }

        // Balls with glow
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);

        if (whiteBall != null && whiteBall.active) {
            paint.setColor(Color.argb(80, 255, 255, 255));
            canvas.drawCircle(whiteBall.x, whiteBall.y, whiteBall.radius + 15, paint);
            paint.setColor(Color.argb(150, 255, 255, 255));
            canvas.drawCircle(whiteBall.x, whiteBall.y, whiteBall.radius + 8, paint);

            whiteBall.draw(canvas, paint);
        }

        for (Ball b : targetBalls)
            b.draw(canvas, paint);

        holder.unlockCanvasAndPost(canvas);
    }

    private void drawSpeedMeter(Canvas canvas) {
        float barWidth = screenWidth * 0.6f;
        float barX = centerX - barWidth / 2;
        float barY = 160;
        float barHeight = 30;

        // Outer glow
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.argb(80, 0, 229, 255));
        canvas.drawRoundRect(barX - 5, barY - 5, barX + barWidth + 5, barY + barHeight + 5, 8, 8, paint);

        // Border
        paint.setStrokeWidth(2);
        paint.setColor(Color.argb(200, 0, 229, 255));
        canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 5, 5, paint);

        // Fill
        paint.setStyle(Paint.Style.FILL);
        float fillWidth = barWidth * (speedMeter / maxSpeedMeter);
        int barColor = speedMeter > 50 ? Color.rgb(0, 255, 150)
                : (speedMeter > 25 ? Color.rgb(255, 200, 0) : Color.RED);
        paint.setColor(barColor);
        if (fillWidth > 0) {
            canvas.drawRoundRect(barX, barY, barX + fillWidth, barY + barHeight, 5, 5, paint);
        }

        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        canvas.drawText(String.format("%.0f%%", speedMeter), centerX, barY + 22, paint);
    }

    private void drawComboStar(Canvas canvas, float x, float y, float size) {
        Path star = new Path();
        float angle = (float) Math.PI / 5;
        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? size : size / 2;
            float currX = x + r * (float) Math.cos(i * angle - Math.PI / 2);
            float currY = y + r * (float) Math.sin(i * angle - Math.PI / 2);
            if (i == 0)
                star.moveTo(currX, currY);
            else
                star.lineTo(currX, currY);
        }
        star.close();

        // Glow
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(60, 255, 215, 0));
        canvas.drawPath(star, paint);

        // Main
        paint.setColor(Color.argb(255, 255, 215, 0));
        canvas.save();
        canvas.scale(0.8f, 0.8f, x, y);
        canvas.drawPath(star, paint);
        canvas.restore();
    }

    private void drawClockIcon(Canvas canvas, float x, float y, boolean good) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.argb(100, 0, 255, 255));
        canvas.drawCircle(x, y, 22, paint);

        paint.setStrokeWidth(2);
        int color = good ? Color.rgb(0, 255, 255) : Color.RED;
        paint.setColor(color);
        canvas.drawCircle(x, y, 18, paint);

        paint.setStrokeWidth(2);
        canvas.drawLine(x, y, x, y - 10, paint);
        canvas.drawLine(x, y, x + 6, y, paint);
    }

    private void drawRedButton(Canvas canvas, float x, float y, float w, float h, String text) {
        // Outer glow (red)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(40, 255, 50, 50));
        canvas.drawRoundRect(x - 6, y - 6, x + w + 6, y + h + 6, 12, 12, paint);

        // Background (dark red)
        paint.setColor(Color.argb(180, 80, 20, 20));
        canvas.drawRoundRect(x, y, x + w, y + h, 8, 8, paint);

        // Border (bright red)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(255, 80, 80));
        canvas.drawRoundRect(x, y, x + w, y + h, 8, 8, paint);

        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 220, 220));
        paint.setTextSize(38);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        canvas.drawText(text, x + w / 2, y + h / 2 + 12, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
    }

    private void drawPortal(Canvas canvas, float angle, int color) {
        int oldColor = paint.getColor();
        float oldWidth = paint.getStrokeWidth();
        Paint.Style oldStyle = paint.getStyle();

        // Outer glow
        paint.setColor(Color.argb(60, Color.red(color), Color.green(color), Color.blue(color)));
        paint.setStrokeWidth(35);
        paint.setStyle(Paint.Style.STROKE);
        if (arenaBounds == null) {
            arenaBounds = new RectF(centerX - arenaRadius, centerY - arenaRadius,
                    centerX + arenaRadius, centerY + arenaRadius);
        }
        canvas.drawArc(arenaBounds, angle - portalWidth / 2, portalWidth, false, paint);

        // Main portal
        paint.setColor(color);
        paint.setStrokeWidth(25);
        canvas.drawArc(arenaBounds, angle - portalWidth / 2, portalWidth, false, paint);

        paint.setColor(oldColor);
        paint.setStrokeWidth(oldWidth);
        paint.setStyle(oldStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            float tapX = event.getX();
            float tapY = event.getY();

            float buttonWidth = 300;
            float buttonHeight = 80;
            float buttonX = centerX - buttonWidth / 2;

            // Restart button
            float restartY = centerY + 40;
            if (tapX >= buttonX && tapX <= buttonX + buttonWidth &&
                    tapY >= restartY && tapY <= restartY + buttonHeight) {
                loadLevel();
                return true;
            }

            // Back button
            float backY = centerY + 140;
            if (tapX >= buttonX && tapX <= buttonX + buttonWidth &&
                    tapY >= backY && tapY <= backY + buttonHeight) {
                ((android.app.Activity) getContext()).finish();
                return true;
            }
        }

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

        // Create stars
        stars.clear();
        for (int i = 0; i < 80; i++) {
            Star star = new Star();
            star.x = (float) (Math.random() * screenWidth);
            star.y = (float) (Math.random() * (screenHeight - CONTROL_Height));
            star.brightness = 0.3f + (float) (Math.random() * 0.7f);
            star.size = 1f + (float) (Math.random() * 2f);
            stars.add(star);
        }

        // Reset
        score = 0;
        speedMeter = 100f;
        speedLevel = 0;
        gameOver = false;
        comboCount = 0;
        comboMultiplier = 1;
        lastPortalTime = 0;
        scoreTextScale = 1f;
        targetScoreScale = 1f;

        if (isTimeAttackMode) {
            timeRemaining = maxTime;
        }

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
        lastFrameTime = System.nanoTime();
        gameThread = new Thread(this);
        gameThread.start();
    }
}