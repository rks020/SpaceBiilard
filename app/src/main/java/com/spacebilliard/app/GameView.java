package com.spacebilliard.app;

import com.spacebilliard.app.MainActivity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.util.Random;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import android.graphics.PointF;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private SurfaceHolder holder;
    private boolean isPlaying;
    private Canvas canvas;
    private Paint paint;
    private Random random;

    // Ekran boyutları
    private int screenWidth;
    private int screenHeight;
    private float centerX;
    private float centerY;
    private float circleRadius;

    // Oyun nesneleri
    private Ball whiteBall;
    private ArrayList<Ball> cloneBalls;
    private ArrayList<Ball> coloredBalls;
    private ArrayList<Ball> blackBalls;
    private ArrayList<SpecialBall> specialBalls;
    private ArrayList<Particle> particles;
    private ArrayList<ImpactArc> impactArcs;
    private ArrayList<GuidedMissile> missiles;
    private ArrayList<ElectricEffect> electricEffects;
    private ArrayList<Star> stars; // Static background stars

    // Oyun durumu
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int stage = 1; // Mevcut stage (1-10)
    private long timeLeft = 20000;
    private long lastTime;
    private int comboCounter = 0;

    // Özel yetenekler
    private boolean blackHoleActive = false;
    private long blackHoleEndTime = 0;
    private boolean barrierActive = false;
    private long barrierEndTime = 0;
    private boolean freezeActive = false;
    private long freezeEndTime = 0;
    private boolean ghostModeActive = false;
    private long ghostModeEndTime = 0;
    private float originalWhiteBallRadius = 0;
    private boolean powerBoostActive = false;
    private BlastWave blastWave = null;

    // UI durumu
    private boolean showInstructions = false;
    private boolean showHighScore = false;
    private int highScore = 0;
    private int highLevel = 1;

    // Combo sistemi
    private int comboHits = 0;
    private long lastHitTime = 0;
    private final long COMBO_TIMEOUT = 2000; // 2 saniye
    private String comboText = "";
    private long comboTextEndTime = 0;
    private int maxCombo = 0; // Combo rekoru

    // Stage Cleared animasyonu
    private boolean showStageCleared = false;
    private long stageClearedTime = 0;

    // Elektrik topu ikinci sıçrama için
    private boolean electricSecondBounce = false;
    private long electricSecondBounceTime = 0;
    private float electricFirstTargetX = 0;
    private float electricFirstTargetY = 0;

    // Level Seçici
    private boolean showLevelSelector = false;
    private int selectorPage = 1;
    private int maxUnlockedLevel = 1;

    // Kamera sallanma
    private float cameraShakeX = 0;
    private float cameraShakeY = 0;
    private long shakeEndTime = 0;
    private Random shakeRandom;
    private long immuneEndTime = 0; // Dokunulmazlık süresi
    private long lastShieldSoundTime = 0; // Kalkan sesi zamanlayıcısı

    // Son fırlatma gücü
    private float lastLaunchPower = 0;

    // Sürükleme
    private boolean isDragging = false;
    private Ball draggedBall = null;
    private float dragStartX, dragStartY;
    private long dragStartTime;
    private final float MAX_DRAG_DISTANCE = 200;
    private final long MAX_DRAG_TIME = 3000;

    private int coins = 0;

    // Level geçiş beklemesi
    private boolean levelCompleted = false;

    private long levelCompletionTime = 0;

    // Ses Efektleri
    private SoundPool soundPool;
    private int soundLaunch, soundCollision, soundCoin, soundBlackExplosion, soundElectric, soundFreeze, soundGameOver,
            soundMissile, soundPower, soundShield;
    private boolean soundLoaded = false;

    // MainActivity reference for updating UI panels
    private MainActivity mainActivity;
    private android.view.View startBtn, howToBtn, shopBtn;
    private android.graphics.Rect startBtnBounds, howToBtnBounds, shopBtnBounds;

    public void setMenuButtons(android.view.View start, android.view.View howTo, android.view.View shop) {
        this.startBtn = start;
        this.howToBtn = howTo;
        this.shopBtn = shop;
    }

    public void startGame() {
        showLevelSelector = true;
        updateMenuButtonsVisibility();
    }

    public void showInstructions() {
        showInstructions = true;
        updateMenuButtonsVisibility();
    }

    private String selectedSkin = "default";
    private String selectedTrail = "none";
    private String selectedAura = "none";
    private String selectedTrajectory = "dashed";
    private String selectedImpact = "classic";
    private final int MAX_TRAIL_POINTS = 15;

    private void updateMenuButtonsVisibility() {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> {
                boolean show = !gameStarted && !showLevelSelector && !showInstructions && !showHighScore;
                if (startBtn != null)
                    startBtn.setVisibility(show ? View.VISIBLE : View.GONE);
                if (howToBtn != null)
                    howToBtn.setVisibility(show ? View.VISIBLE : View.GONE);
                if (shopBtn != null)
                    shopBtn.setVisibility(show ? View.VISIBLE : View.GONE);
            });
        }
    }

    public GameView(Context context) {
        super(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
        holder = getHolder();
        paint = new Paint();
        paint.setAntiAlias(true);
        random = new Random();
        shakeRandom = new Random();

        cloneBalls = new ArrayList<>();
        coloredBalls = new ArrayList<>();
        blackBalls = new ArrayList<>();
        specialBalls = new ArrayList<>();
        particles = new ArrayList<>();
        impactArcs = new ArrayList<>();
        missiles = new ArrayList<>();
        electricEffects = new ArrayList<>();
        stars = new ArrayList<>();

        // Kayıtlı skoru yükle
        SharedPreferences prefs = context.getSharedPreferences("SpaceBilliard", Context.MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);
        highLevel = prefs.getInt("highLevel", 1);
        highScore = prefs.getInt("highScore", 0);
        highLevel = prefs.getInt("highLevel", 1);
        maxCombo = prefs.getInt("maxCombo", 0);
        // maxUnlockedLevel'ı yükle (ilerlemeyi kaydet)
        maxUnlockedLevel = prefs.getInt("maxUnlockedLevel", 1);

        // SoundPool Başlatma
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundLoaded = true);

        try {
            soundLaunch = soundPool.load(context, R.raw.drag_launch, 1);
            soundCollision = soundPool.load(context, R.raw.collision_sound, 1);
            soundCoin = soundPool.load(context, R.raw.coin_sound, 1);
            soundBlackExplosion = soundPool.load(context, R.raw.black_ball_explosion, 1);
            soundElectric = soundPool.load(context, R.raw.electric_ball, 1);
            soundFreeze = soundPool.load(context, R.raw.freeze, 1);
            soundGameOver = soundPool.load(context, R.raw.game_over, 1);
            soundMissile = soundPool.load(context, R.raw.guided_missile, 1);
            soundPower = soundPool.load(context, R.raw.power_boost, 1);
            soundShield = soundPool.load(context, R.raw.shield_block, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Yıldızları oluştur
        for (int i = 0; i < 100; i++) {
            stars.add(new Star());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        // Ekranın küçük tarafına göre kare alan oluştur
        int minSize = Math.min(w, h);
        centerX = w / 2f;
        centerY = h / 2f;
        circleRadius = minSize * 0.47f;

        // Beyaz topu başlat
        if (whiteBall == null) {
            whiteBall = new Ball(centerX, centerY, minSize * 0.02f, Color.WHITE);
            initLevel(1);
        } else {
            // Ekran döndürüldüğünde pozisyonları güncelle
            updatePositionsAfterResize();
        }
    }

    private void updatePositionsAfterResize() {
        // Tüm nesneleri yeni merkeze göre ölçekle
        float oldCenterX = centerX;
        float oldCenterY = centerY;

        whiteBall.x = centerX;
        whiteBall.y = centerY;

        for (Ball ball : coloredBalls) {
            ball.x = centerX + (ball.x - oldCenterX);
            ball.y = centerY + (ball.y - oldCenterY);
        }

        for (Ball ball : blackBalls) {
            ball.x = centerX + (ball.x - oldCenterX);
            ball.y = centerY + (ball.y - oldCenterY);
        }

        for (SpecialBall ball : specialBalls) {
            ball.x = centerX + (ball.x - oldCenterX);
            ball.y = centerY + (ball.y - oldCenterY);
        }
    }

    private void initLevel(int lv) {
        level = lv;
        // Zorluk Ayarı
        lives = 3;
        // Level 6'dan sonra süre artar, ama level arttıkça top sayısı da artar
        timeLeft = 20000 + (lv * 500);
        timeLeft = Math.min(timeLeft, 45000); // Max 45 sn

        lastTime = System.currentTimeMillis();
        comboCounter = 0;

        coloredBalls.clear();
        blackBalls.clear();
        specialBalls.clear();
        cloneBalls.clear();
        particles.clear();
        missiles.clear();
        electricEffects.clear();

        whiteBall.x = centerX;
        whiteBall.y = centerY;
        whiteBall.vx = 0;
        whiteBall.vy = 0;
        whiteBall.trail.clear();
        // Ghost modundan çıkarken top boyutunu sıfırla
        whiteBall.radius = (circleRadius / 0.47f) * 0.02f;

        blackHoleActive = false;
        barrierActive = false;
        freezeActive = false;
        ghostModeActive = false;
        powerBoostActive = false;
        blastWave = null;

        // Her level (5 stage) için top sayısını sıfırla ve yeniden başlat
        int stageInLevel = ((lv - 1) % 5) + 1;
        int ballCount = stageInLevel + 4; // Her level 5 topla başlar, her stage +1 artar

        int[] colors = {
                Color.rgb(255, 0, 85), // Pink-Red
                Color.rgb(0, 255, 153), // Cyan-Green
                Color.rgb(255, 255, 0), // Yellow
                Color.rgb(0, 204, 255), // Sky Blue
                Color.rgb(255, 102, 0) // Orange
        };

        for (int i = 0; i < ballCount; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float radius = circleRadius * 0.7f;
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;

            Ball ball = new Ball(x, y, whiteBall.radius, colors[random.nextInt(colors.length)]);
            ball.vx = (random.nextFloat() - 0.5f) * 8;
            ball.vy = (random.nextFloat() - 0.5f) * 8;
            coloredBalls.add(ball);
        }

        // Siyah toplar (Level içindeki stage'e göre artar)
        int blackCount = (stageInLevel >= 4) ? 2 : 1;
        for (int i = 0; i < blackCount; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float radius = circleRadius * 0.7f;
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;

            Ball ball = new Ball(x, y, whiteBall.radius * 1.2f, Color.BLACK);
            ball.vx = (random.nextFloat() - 0.5f) * 12;
            ball.vy = (random.nextFloat() - 0.5f) * 12;
            blackBalls.add(ball);
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            long startTime = System.currentTimeMillis();

            update();
            draw();

            // Özel top spawn
            if (gameStarted && !gameOver && random.nextFloat() < 0.001f && specialBalls.size() < 3) {
                spawnSpecialBall();
            }

            long frameTime = System.currentTimeMillis() - startTime;
            if (frameTime < 16) {
                try {
                    Thread.sleep(16 - frameTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void spawnSpecialBall() {
        String[] types = { "blackhole", "extraTime", "powerBoost", "barrier", "electric",
                "clone", "freeze", "missile", "teleport", "boom", "ghost" };
        String type = types[random.nextInt(types.length)];

        float angle = random.nextFloat() * (float) (2 * Math.PI);
        float radius = circleRadius * 0.7f;
        float x = centerX + (float) Math.cos(angle) * radius;
        float y = centerY + (float) Math.sin(angle) * radius;

        specialBalls.add(new SpecialBall(x, y, whiteBall.radius * 1.2f, type));
    }

    private void update() {
        if (!gameStarted || gameOver)
            return;

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastTime;
        lastTime = currentTime;

        // Zaman
        timeLeft -= deltaTime;
        if (timeLeft <= 0 && coloredBalls.size() > 0) {
            gameOver = true;
            saveProgress();
            playSound(soundGameOver);
            updateMenuButtonsVisibility();
            return;
        }

        // Tüm toplar toplandı mı?
        if (coloredBalls.size() == 0 && !levelCompleted) {
            levelCompleted = true;
            showStageCleared = true;
            stageClearedTime = System.currentTimeMillis();

            // Siyah topları yok et
            blackBalls.clear();

            // Partiküller oluştur
            for (int i = 0; i < 30; i++) {
                float angle = random.nextFloat() * (float) (2 * Math.PI);
                float speed = random.nextFloat() * 8 + 4;
                particles.add(new Particle(centerX, centerY, angle, speed, Color.rgb(255, 215, 0)));
            }
        }

        // Stage Cleared animasyonu bitince bir sonraki stage'e geç
        if (levelCompleted && System.currentTimeMillis() - stageClearedTime > 3000) {
            levelCompleted = false;
            showStageCleared = false;
            level++;

            // Her 5 stage tamamlandığında yeni level aç
            int completedStages = level - 1; // Tamamlanan stage sayısı
            int unlockedLevelCount = (completedStages / 5) + 1; // Açılması gereken level sayısı

            if (unlockedLevelCount > maxUnlockedLevel) {
                maxUnlockedLevel = unlockedLevelCount;
                saveProgress();
            }

            initLevel(level);
            return;
        }

        // Elektrik topu ikinci sıçrama kontrolü
        if (electricSecondBounce && System.currentTimeMillis() >= electricSecondBounceTime) {
            electricSecondBounce = false;
            if (coloredBalls.size() > 0) {
                Ball target2 = coloredBalls.get(random.nextInt(coloredBalls.size()));
                electricEffects
                        .add(new ElectricEffect(electricFirstTargetX, electricFirstTargetY, target2.x, target2.y));
                createImpactBurst(target2.x, target2.y, target2.color);
                score++;
                comboCounter++;
                playSound(soundElectric); // İkinci sıçrama sesi
                coloredBalls.remove(target2);
            }
        }

        // Özel yetenekler zaman kontrolü
        if (barrierActive && currentTime > barrierEndTime)
            barrierActive = false;
        if (freezeActive && currentTime > freezeEndTime)
            freezeActive = false;
        if (ghostModeActive && currentTime > ghostModeEndTime) {
            ghostModeActive = false;
            whiteBall.radius = originalWhiteBallRadius;
        }
        if (blackHoleActive && currentTime > blackHoleEndTime)
            blackHoleActive = false;

        // Beyaz top
        if (!isDragging || draggedBall != whiteBall) {
            whiteBall.x += whiteBall.vx;
            whiteBall.y += whiteBall.vy;
            reflectBall(whiteBall);
            whiteBall.vx *= 0.995f;
            whiteBall.vy *= 0.995f;

            // Trail update
            updateBallTrail(whiteBall);
        }

        // Clone toplar
        for (int i = cloneBalls.size() - 1; i >= 0; i--) {
            Ball ball = cloneBalls.get(i);

            // Clone topu için ömür kontrolü
            if (ball.isClone && ball.lifetime > 0) {
                long elapsed = System.currentTimeMillis() - ball.creationTime;
                if (elapsed > ball.lifetime) {
                    cloneBalls.remove(i);
                    continue;
                }
            } else if (System.currentTimeMillis() - ball.creationTime > 8000) {
                // Eski clone toplar için (lifetime olmayan)
                cloneBalls.remove(i);
                continue;
            }

            if (!isDragging || draggedBall != ball) {
                ball.x += ball.vx;
                ball.y += ball.vy;
                reflectBall(ball);
                ball.vx *= 0.995f;
                ball.vy *= 0.995f;
            }
        }

        // Renkli toplar (freeze kontrolü)
        if (!freezeActive) {
            for (Ball ball : coloredBalls) {
                ball.x += ball.vx;
                ball.y += ball.vy;
                reflectBall(ball);
            }

            for (Ball ball : blackBalls) {
                ball.x += ball.vx;
                ball.y += ball.vy;
                reflectBall(ball);
            }

            for (SpecialBall ball : specialBalls) {
                ball.x += ball.vx;
                ball.y += ball.vy;
                reflectBall(ball);
            }
        }

        // Black hole çekimi
        if (blackHoleActive) {
            attractBallsToWhite();
        }

        // Çarpışmalar
        checkCollisions();

        // Missiles
        updateMissiles();

        // Blast wave
        if (blastWave != null) {
            blastWave.update();
            if (blastWave.isDead())
                blastWave = null;
        }

        // Electric effects
        for (int i = electricEffects.size() - 1; i >= 0; i--) {
            ElectricEffect effect = electricEffects.get(i);
            effect.update();
            if (effect.isDead())
                electricEffects.remove(i);
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.isDead())
                particles.remove(i);
        }

        for (int i = impactArcs.size() - 1; i >= 0; i--) {
            ImpactArc arc = impactArcs.get(i);
            arc.update();
            if (arc.isDead())
                impactArcs.remove(i);
        }

        // Yıldızları güncelle
        if (stars != null) {
            for (Star star : stars) {
                star.update(screenWidth, screenHeight);
            }
        }

        // Kamera sallanma güncelle
        if (System.currentTimeMillis() < shakeEndTime) {
            cameraShakeX = (shakeRandom.nextFloat() - 0.5f) * 20;
            cameraShakeY = (shakeRandom.nextFloat() - 0.5f) * 20;
        } else {
            cameraShakeX = 0;
            cameraShakeY = 0;
        }

        // Combo Fire Effekt
        if (comboCounter >= 3 && (Math.abs(whiteBall.vx) > 1 || Math.abs(whiteBall.vy) > 1)) {
            createFlame(whiteBall.x, whiteBall.y);
        }

        // Combo timeout kontrolü (Eğer başka bir yerde yapılmıyorsa)
        if (comboCounter > 0 && System.currentTimeMillis() - lastHitTime > COMBO_TIMEOUT) {
            comboCounter = 0;
        }
    }

    private void updateBallTrail(Ball ball) {
        if (!selectedTrail.equals("none") && (Math.abs(ball.vx) > 0.1 || Math.abs(ball.vy) > 0.1)) {
            ball.trail.add(0, new TrailPoint(ball.x, ball.y, ball.radius));
            if (ball.trail.size() > MAX_TRAIL_POINTS) {
                ball.trail.remove(ball.trail.size() - 1);
            }
        } else {
            if (ball.trail.size() > 0)
                ball.trail.remove(ball.trail.size() - 1);
        }
    }

    private void attractBallsToWhite() {
        float attractionSpeed = 5;

        for (Ball ball : coloredBalls) {
            float dx = whiteBall.x - ball.x;
            float dy = whiteBall.y - ball.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > 5) {
                ball.x += (dx / distance) * attractionSpeed;
                ball.y += (dy / distance) * attractionSpeed;
            }
        }

        for (SpecialBall ball : specialBalls) {
            float dx = whiteBall.x - ball.x;
            float dy = whiteBall.y - ball.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > 5) {
                ball.x += (dx / distance) * attractionSpeed;
                ball.y += (dy / distance) * attractionSpeed;
            }
        }
    }

    private void checkCollisions() {
        // Drag sırasında çarpışma yok
        if (isDragging) {
            return;
        }

        ArrayList<Ball> allWhiteBalls = new ArrayList<>();
        allWhiteBalls.add(whiteBall);
        try {
            allWhiteBalls.addAll(cloneBalls);
        } catch (Exception e) {
            // ConcurrentModificationException önleme
        }

        for (Ball wBall : allWhiteBalls) {
            // Renkli toplar
            for (int i = coloredBalls.size() - 1; i >= 0; i--) {
                Ball ball = coloredBalls.get(i);
                if (checkBallCollision(wBall, ball)) {
                    score++;
                    timeLeft += 1000;
                    comboCounter++;

                    // Combo sistemi
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastHitTime < COMBO_TIMEOUT) {
                        comboHits++;
                        if (comboHits > maxCombo)
                            maxCombo = comboHits; // Rekor kontrolü
                        if (comboHits >= 2) {
                            comboText = "COMBO x" + (comboHits + 1);
                            comboTextEndTime = currentTime + 1500;
                        }
                    } else {
                        comboHits = 0;
                    }
                    lastHitTime = currentTime;

                    createImpactBurst(ball.x, ball.y, ball.color);
                    coloredBalls.remove(i);
                    playSound(soundCollision);

                    // Hız artır
                    float dx = wBall.x - ball.x;
                    float dy = wBall.y - ball.y;
                    float angle = (float) Math.atan2(dy, dx);
                    float speed = (float) Math.sqrt(wBall.vx * wBall.vx + wBall.vy * wBall.vy);
                    wBall.vx = (float) Math.cos(angle) * speed * 1.05f;
                    wBall.vy = (float) Math.sin(angle) * speed * 1.05f;
                }
            }

            // Siyah toplar (güvenli iterasyon)
            for (int i = blackBalls.size() - 1; i >= 0; i--) {
                if (i >= blackBalls.size())
                    continue; // Güvenlik kontrolü
                Ball ball = blackBalls.get(i);

                // Dokunulmazlık kontrolü
                if (System.currentTimeMillis() < immuneEndTime)
                    continue;

                if (checkBallCollision(wBall, ball)) {
                    if (barrierActive || ghostModeActive) {
                        // Korundu
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastShieldSoundTime > 500) {
                            playSound(soundShield);
                            lastShieldSoundTime = currentTime;
                        }
                    } else {
                        lives--;
                        comboCounter = 0;

                        // Siyah top patlama efekti (partikül)
                        createParticles(ball.x, ball.y, Color.BLACK);

                        // Kamera sallanma efekti
                        shakeEndTime = System.currentTimeMillis() + 500;

                        if (lives <= 0) {
                            gameOver = true;
                            saveProgress();
                            playSound(soundGameOver);
                            updateMenuButtonsVisibility();
                        } else {
                            wBall.x = centerX;
                            wBall.y = centerY;
                            wBall.vx = 0;
                            wBall.vy = 0;
                            immuneEndTime = System.currentTimeMillis() + 2000; // 2 saniye koruma
                        }
                    }
                }
            }

            // Özel toplar
            for (int i = specialBalls.size() - 1; i >= 0; i--) {
                SpecialBall ball = specialBalls.get(i);
                if (checkBallCollision(wBall, ball)) {
                    activateSpecialPower(ball.type, wBall);
                    createParticles(ball.x, ball.y, ball.getColor());
                    specialBalls.remove(i);
                }
            }
        }
    }

    private boolean checkBallCollision(Ball b1, Ball b2) {
        float dx = b1.x - b2.x;
        float dy = b1.y - b2.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < b1.radius + b2.radius;
    }

    private boolean checkBallCollision(Ball b1, SpecialBall b2) {
        float dx = b1.x - b2.x;
        float dy = b1.y - b2.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance < b1.radius + b2.radius;
    }

    private void activateSpecialPower(String type, Ball targetBall) {
        switch (type) {
            case "blackhole":
                blackHoleActive = true;
                blackHoleEndTime = System.currentTimeMillis() + 2000;
                break;
            case "extraTime":
                timeLeft += 5000;
                break;
            case "powerBoost":
                powerBoostActive = true;
                playSound(soundPower);
                break;
            case "barrier":
                barrierActive = true;
                barrierEndTime = System.currentTimeMillis() + 5000;
                playSound(soundShield);
                break;
            case "electric":
                triggerElectric();
                playSound(soundElectric);
                break;
            case "clone":
                // Ghost mode aktifse orijinal boyutu kullan
                float cloneRadius = ghostModeActive ? originalWhiteBallRadius : whiteBall.radius;
                Ball clone = new Ball(centerX, centerY, cloneRadius, Color.WHITE, 5000); // 5 saniye ömür
                cloneBalls.add(clone);
                break;
            case "freeze":
                freezeActive = true;
                freezeEndTime = System.currentTimeMillis() + 5000;
                playSound(soundFreeze);
                break;
            case "missile":
                if (blackBalls.size() > 0) {
                    missiles.add(new GuidedMissile(whiteBall.x, whiteBall.y, blackBalls.get(0)));
                    playSound(soundMissile);
                }
                break;
            case "teleport":
                float angle = random.nextFloat() * (float) (2 * Math.PI);
                float radius = circleRadius * 0.5f;
                targetBall.x = centerX + (float) Math.cos(angle) * radius;
                targetBall.y = centerY + (float) Math.sin(angle) * radius;
                createParticles(targetBall.x, targetBall.y, Color.GREEN);
                break;
            case "boom":
                blastWave = new BlastWave(targetBall.x, targetBall.y);
                break;
            case "ghost":
                if (!ghostModeActive) {
                    originalWhiteBallRadius = whiteBall.radius;
                    ghostModeActive = true;
                    ghostModeEndTime = System.currentTimeMillis() + 3000;
                    // Sadece beyaz topu büyüt, diğerleri etkilenmez
                    whiteBall.radius = originalWhiteBallRadius * 1.75f;
                }
                break;
        }
    }

    private void playSound(int soundID) {
        if (soundLoaded) {
            soundPool.play(soundID, 1, 1, 1, 0, 1f);
        }
    }

    private void triggerElectric() {
        if (coloredBalls.size() == 0)
            return;

        // İlk hedef
        Ball target1 = coloredBalls.get(random.nextInt(coloredBalls.size()));
        electricEffects.add(new ElectricEffect(whiteBall.x, whiteBall.y, target1.x, target1.y));
        createImpactBurst(target1.x, target1.y, target1.color);
        score++;
        comboCounter++;
        playSound(soundElectric); // İlk sıçrama sesi
        coloredBalls.remove(target1);

        // İkinci hedef için gecikme ayarla (0.4 saniye)
        if (coloredBalls.size() > 0) {
            electricFirstTargetX = target1.x;
            electricFirstTargetY = target1.y;
            electricSecondBounce = true;
            electricSecondBounceTime = System.currentTimeMillis() + 400;
        }
    }

    private void updateMissiles() {
        for (int i = missiles.size() - 1; i >= 0; i--) {
            GuidedMissile missile = missiles.get(i);
            missile.update();

            // Hedef kontrolü
            for (int j = blackBalls.size() - 1; j >= 0; j--) {
                Ball ball = blackBalls.get(j);
                float dx = missile.x - ball.x;
                float dy = missile.y - ball.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < missile.radius + ball.radius) {
                    createParticles(ball.x, ball.y, Color.BLACK);
                    blackBalls.remove(j);
                    missiles.remove(i);
                    playSound(soundBlackExplosion);
                    break;
                }
            }

            // Sınır kontrolü
            float dx = missile.x - centerX;
            float dy = missile.y - centerY;
            if (Math.sqrt(dx * dx + dy * dy) > circleRadius) {
                missiles.remove(i);
            }
        }
    }

    private void reflectBall(Ball ball) {
        float dx = ball.x - centerX;
        float dy = ball.y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance + ball.radius > circleRadius) {
            float nx = dx / distance;
            float ny = dy / distance;

            float dot = ball.vx * nx + ball.vy * ny;
            ball.vx -= 2 * dot * nx;
            ball.vy -= 2 * dot * ny;

            float angle = (float) Math.atan2(dy, dx);
            ball.x = centerX + (float) Math.cos(angle) * (circleRadius - ball.radius);
            ball.y = centerY + (float) Math.sin(angle) * (circleRadius - ball.radius);
        }
    }

    private void reflectBall(SpecialBall ball) {
        float dx = ball.x - centerX;
        float dy = ball.y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance + ball.radius > circleRadius) {
            float nx = dx / distance;
            float ny = dy / distance;

            float dot = ball.vx * nx + ball.vy * ny;
            ball.vx -= 2 * dot * nx;
            ball.vy -= 2 * dot * ny;

            float angle = (float) Math.atan2(dy, dx);
            ball.x = centerX + (float) Math.cos(angle) * (circleRadius - ball.radius);
            ball.y = centerY + (float) Math.sin(angle) * (circleRadius - ball.radius);
        }
    }

    private void createFlame(float x, float y) {
        // Spawn 1-2 flame particles each frame for continuous effect
        for (int i = 0; i < 2; i++) {
            particles.add(new Particle(x, y, 0, 0, Color.YELLOW, ParticleType.FLAME));
        }
    }

    private void createParticles(float x, float y, int color) {
        for (int i = 0; i < 15; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float speed = random.nextFloat() * 5 + 2;
            particles.add(new Particle(x, y, angle, speed, color));
        }
    }

    private void createImpactBurst(float x, float y, int color) {
        // Standard Circle Particles (Common base)
        int particleCount = 10;
        for (int i = 0; i < particleCount; i++) {
            float angle = random.nextFloat() * (float) (2 * Math.PI);
            float speed = random.nextFloat() * 6 + 2;
            particles.add(new Particle(x, y, angle, speed, color, ParticleType.CIRCLE));
        }

        switch (selectedImpact) {
            case "stars":
                // Star Burst: Circles + Stars
                for (int i = 0; i < 10; i++) {
                    float angle = random.nextFloat() * (float) (2 * Math.PI);
                    float speed = random.nextFloat() * 4 + 3;
                    particles.add(new Particle(x, y, angle, speed, color, ParticleType.STAR));
                }
                break;
            case "electric":
                // Electric Boom: Circles + Lightning Arcs
                for (int i = 0; i < 6; i++) {
                    float angle = random.nextFloat() * (float) (2 * Math.PI);
                    float length = random.nextFloat() * 40 + 30;
                    impactArcs.add(new ImpactArc(x, y, angle, length, color));
                }
                break;
            default:
                // Classic: Just circles (already added)
                break;
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();

            // Kamera Offset
            canvas.save();
            canvas.translate(cameraShakeX, cameraShakeY);

            // Arka plan
            int currentSpace = ((level - 1) / 50) + 1;
            if (currentSpace >= 2) {
                drawNebulaBackground(canvas);
            } else {
                canvas.drawColor(Color.rgb(5, 5, 16));
            }

            // Yıldızları çiz
            for (Star star : stars) {
                star.draw(canvas, paint);
            }

            // Çember
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6);
            paint.setColor(Color.rgb(0, 243, 255));
            paint.setShadowLayer(20, 0, 0, Color.rgb(0, 243, 255));
            canvas.drawCircle(centerX, centerY, circleRadius, paint);
            paint.clearShadowLayer();

            // Blast wave
            if (blastWave != null) {
                blastWave.draw(canvas, paint);
            }

            // Electric effects
            for (ElectricEffect effect : electricEffects) {
                effect.draw(canvas, paint);
            }

            // Parçacıklar
            for (Particle p : particles) {
                p.draw(canvas, paint);
            }

            for (ImpactArc arc : impactArcs) {
                arc.draw(canvas, paint);
            }

            // Toplar
            for (Ball ball : coloredBalls) {
                drawBall(canvas, ball);
            }

            for (Ball ball : blackBalls) {
                drawBall(canvas, ball);
            }

            for (SpecialBall ball : specialBalls) {
                drawSpecialBall(canvas, ball);
            }

            for (Ball ball : cloneBalls) {
                drawBall(canvas, ball);

                // Clone topu için geri sayım göster
                if (ball.isClone && ball.lifetime > 0) {
                    long elapsed = System.currentTimeMillis() - ball.creationTime;
                    long remaining = ball.lifetime - elapsed;
                    int seconds = (int) Math.ceil(remaining / 1000.0);

                    if (seconds > 0 && seconds <= 5) {
                        paint.setStyle(Paint.Style.FILL);
                        paint.setTextSize(ball.radius * 1.2f);
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setColor(Color.WHITE);
                        paint.setShadowLayer(8, 0, 0, Color.BLACK);
                        canvas.drawText(String.valueOf(seconds), ball.x + ball.radius * 0.8f,
                                ball.y - ball.radius * 0.8f, paint);
                        paint.clearShadowLayer();
                    }
                }
            }

            // Missiles
            for (GuidedMissile missile : missiles) {
                missile.draw(canvas, paint);
            }

            // Trail (Only for player ball)
            if (!selectedTrail.equals("none") && whiteBall.trail.size() > 0) {
                drawCometTrail(canvas, whiteBall);
            }

            // Beyaz top
            drawBall(canvas, whiteBall);

            // Barrier
            if (barrierActive) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                paint.setColor(Color.rgb(0, 150, 255));
                paint.setAlpha(128);
                canvas.drawCircle(whiteBall.x, whiteBall.y, whiteBall.radius * 2, paint);
                paint.setAlpha(255);
            }

            // Sürükleme
            Ball currentDraggedBall = draggedBall; // Race condition önleme
            if (isDragging && currentDraggedBall != null) {
                float dx = currentDraggedBall.x - dragStartX;
                float dy = currentDraggedBall.y - dragStartY;
                float distance = Math.min((float) Math.sqrt(dx * dx + dy * dy), MAX_DRAG_DISTANCE);
                float ratio = distance / MAX_DRAG_DISTANCE;

                // Eski çizgi (drag line) yerine nişan çizgisi (trajectory)
                if (distance > 10) {
                    float launchAngle = (float) Math.atan2(-dy, -dx);

                    // Çizgi ve Ok parametreleri
                    float startDist = currentDraggedBall.radius * 1.5f;
                    float lineLen = 400 * ratio; // Güç arttıkça uzayan çizgi

                    float startX = currentDraggedBall.x + (float) Math.cos(launchAngle) * startDist;
                    float startY = currentDraggedBall.y + (float) Math.sin(launchAngle) * startDist;
                    float endX = currentDraggedBall.x + (float) Math.cos(launchAngle) * (startDist + lineLen);
                    float endY = currentDraggedBall.y + (float) Math.sin(launchAngle) * (startDist + lineLen);

                    // Visual end point (slightly before the arrow tip to avoid clipping)
                    float vOffset = 15;
                    float vEndX = currentDraggedBall.x
                            + (float) Math.cos(launchAngle) * (startDist + Math.max(0, lineLen - vOffset));
                    float vEndY = currentDraggedBall.y
                            + (float) Math.sin(launchAngle) * (startDist + Math.max(0, lineLen - vOffset));

                    // Kesikli çizgi (Trajectory)
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.WHITE);
                    paint.setAlpha(180);

                    if (selectedTrajectory.equals("laser")) {
                        // Visionary Laser Style
                        paint.setAlpha(255);
                        paint.setShadowLayer(15, 0, 0, Color.RED);
                        paint.setStrokeWidth(8);
                        paint.setColor(Color.RED);
                        canvas.drawLine(startX, startY, vEndX, vEndY, paint);

                        paint.setColor(Color.WHITE);
                        paint.setStrokeWidth(3);
                        canvas.drawLine(startX, startY, vEndX, vEndY, paint);
                        paint.clearShadowLayer();
                    } else if (selectedTrajectory.equals("electric")) {
                        // Electric Zig-Zag
                        paint.setAlpha(255);
                        paint.setStrokeWidth(5);
                        paint.setColor(Color.CYAN);
                        paint.setShadowLayer(20, 0, 0, Color.CYAN);
                        paint.setStyle(Paint.Style.STROKE);

                        android.graphics.Path path = new android.graphics.Path();
                        path.moveTo(startX, startY);
                        int segs = 12;
                        for (int i = 1; i <= segs; i++) {
                            float px = startX + (vEndX - startX) * i / segs;
                            float py = startY + (vEndY - startY) * i / segs;
                            float offset = (float) (Math.sin(System.currentTimeMillis() * 0.05 + i) * 15);
                            float perpX = -(endY - startY) / lineLen;
                            float perpY = (endX - startX) / lineLen;
                            path.lineTo(px + perpX * offset, py + perpY * offset);
                        }
                        canvas.drawPath(path, paint);
                        paint.clearShadowLayer();
                    } else if (selectedTrajectory.equals("dots")) {
                        // Golden Pearls
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.rgb(255, 215, 0));
                        paint.setShadowLayer(15, 0, 0, Color.YELLOW);
                        int dots = 10;
                        for (int i = 0; i <= dots; i++) {
                            float px = startX + (vEndX - startX) * i / dots;
                            float py = startY + (vEndY - startY) * i / dots;
                            canvas.drawCircle(px, py, 6, paint);
                        }
                        paint.clearShadowLayer();
                    } else if (selectedTrajectory.equals("plasma")) {
                        // Fading Plasma
                        paint.setStyle(Paint.Style.FILL);
                        int dots = 15;
                        for (int i = 0; i <= dots; i++) {
                            float t = (float) i / dots;
                            float px = startX + (vEndX - startX) * t;
                            float py = startY + (vEndY - startY) * t;
                            paint.setColor(Color.MAGENTA);
                            paint.setAlpha((int) (255 * t));
                            paint.setShadowLayer(10 * t, 0, 0, Color.MAGENTA);
                            canvas.drawCircle(px, py, 8 * t, paint);
                        }
                        paint.setAlpha(255);
                        paint.clearShadowLayer();
                    } else {
                        // Default dashed
                        paint.setPathEffect(new android.graphics.DashPathEffect(new float[] { 20, 20 }, 0));
                        canvas.drawLine(startX, startY, vEndX, vEndY, paint);
                        paint.setPathEffect(null);
                    }
                    paint.setAlpha(255);

                    // Ok başı (V şeklinde) - Çizginin SONUNDA
                    int arrowColor = Color.WHITE;
                    int shadowColor = Color.CYAN;
                    if (selectedTrajectory.equals("laser")) {
                        arrowColor = Color.RED;
                        shadowColor = Color.RED;
                    } else if (selectedTrajectory.equals("electric")) {
                        arrowColor = Color.CYAN;
                        shadowColor = Color.CYAN;
                    } else if (selectedTrajectory.equals("dots")) {
                        arrowColor = Color.rgb(255, 215, 0);
                        shadowColor = Color.YELLOW;
                    } else if (selectedTrajectory.equals("plasma")) {
                        arrowColor = Color.MAGENTA;
                        shadowColor = Color.MAGENTA;
                    }

                    paint.setStrokeWidth(8);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    paint.setColor(arrowColor);
                    paint.setShadowLayer(10, 0, 0, shadowColor);

                    float arrowSize = (selectedTrajectory.equals("dashed")) ? 40 : 50;
                    float wingAngle = (float) Math.toRadians(150); // 150 derece kanat açısı

                    // startX/Y yerine endX/Y kullanıyoruz (Gerçek uç)
                    float wing1X = endX + (float) Math.cos(launchAngle + wingAngle) * arrowSize;
                    float wing1Y = endY + (float) Math.sin(launchAngle + wingAngle) * arrowSize;

                    float wing2X = endX + (float) Math.cos(launchAngle - wingAngle) * arrowSize;
                    float wing2Y = endY + (float) Math.sin(launchAngle - wingAngle) * arrowSize;

                    canvas.drawLine(endX, endY, wing1X, wing1Y, paint);
                    canvas.drawLine(endX, endY, wing2X, wing2Y, paint);
                    paint.clearShadowLayer();
                }

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                int powerColor = Color.rgb((int) (255 * ratio), (int) (255 * (1 - ratio)), 0);
                paint.setColor(powerColor);
                canvas.drawCircle(currentDraggedBall.x, currentDraggedBall.y, currentDraggedBall.radius * (1 + ratio),
                        paint);

                // Güç barı - topun etrafında dairesel
                float arcRadius = currentDraggedBall.radius * 2.5f;
                float sweepAngle = 360 * ratio;

                // Arka plan çember (gri)
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(8);
                paint.setColor(Color.rgb(60, 60, 60));
                paint.setAlpha(150);
                canvas.drawCircle(currentDraggedBall.x, currentDraggedBall.y, arcRadius, paint);
                paint.setAlpha(255);

                // Dolu kısım (mavi glow)
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(10);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setColor(Color.rgb(0, 180, 255));
                paint.setShadowLayer(20, 0, 0, Color.rgb(0, 180, 255));
                canvas.drawArc(
                        currentDraggedBall.x - arcRadius, currentDraggedBall.y - arcRadius,
                        currentDraggedBall.x + arcRadius, currentDraggedBall.y + arcRadius,
                        -90, sweepAngle, false, paint);
                paint.clearShadowLayer();
            }

            // Combo text göster
            if (System.currentTimeMillis() < comboTextEndTime && !comboText.isEmpty()) {
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(screenWidth * 0.12f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.rgb(255, 215, 0));
                paint.setShadowLayer(15, 0, 0, Color.rgb(255, 215, 0));
                canvas.drawText(comboText, centerX, centerY - screenHeight * 0.2f, paint);
                paint.clearShadowLayer();
            }

            // STAGE CLEARED animasyonu
            if (showStageCleared) {
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(screenWidth * 0.1f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.rgb(0, 255, 100));
                paint.setShadowLayer(25, 0, 0, Color.rgb(0, 255, 100));

                int currentStageNum = ((level - 1) % 5) + 1;
                if (currentStageNum == 5) {
                    canvas.drawText("LEVEL " + ((level / 5) + 2) + " UNLOCKED!", centerX, centerY, paint);
                } else {
                    canvas.drawText("STAGE CLEARED!", centerX, centerY, paint);
                }
                paint.clearShadowLayer();

                // Alt yazı
                paint.setTextSize(screenWidth * 0.045f);
                paint.setColor(Color.WHITE);
                canvas.drawText("Stage " + currentStageNum + " Complete", centerX, centerY + screenHeight * 0.08f,
                        paint);
            }

            // UI
            drawUI(canvas);

            canvas.restore();
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawNebulaBackground(Canvas canvas) {
        // Base dark space
        canvas.drawColor(Color.rgb(10, 5, 25));

        // Draw multiple large glowing gradients to simulate nebula clouds
        paint.setStyle(Paint.Style.FILL);

        // Purple Nebula Cloud 1
        RadialGradient nebula1 = new RadialGradient(screenWidth * 0.2f, screenHeight * 0.3f, screenWidth * 0.8f,
                new int[] { Color.argb(60, 128, 0, 128), Color.TRANSPARENT }, null, Shader.TileMode.CLAMP);
        paint.setShader(nebula1);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        // Magenta Nebula Cloud 2
        RadialGradient nebula2 = new RadialGradient(screenWidth * 0.8f, screenHeight * 0.7f, screenWidth * 0.7f,
                new int[] { Color.argb(50, 255, 0, 255), Color.TRANSPARENT }, null, Shader.TileMode.CLAMP);
        paint.setShader(nebula2);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        // Deep Blue Cloud 3
        RadialGradient nebula3 = new RadialGradient(screenWidth * 0.5f, screenHeight * 0.5f, screenWidth * 1.2f,
                new int[] { Color.argb(40, 0, 0, 150), Color.TRANSPARENT }, null, Shader.TileMode.CLAMP);
        paint.setShader(nebula3);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        paint.setShader(null);
    }

    private void drawBall(Canvas canvas, Ball ball) {
        if (ball == whiteBall || cloneBalls.contains(ball)) {
            // Draw Aura if active
            if (selectedAura.equals("neon")) {
                drawAuraEffect(canvas, ball);
            }

            switch (selectedSkin) {
                case "tr_flag":
                    drawTRFlagBall(canvas, ball);
                    return;
                case "soccer":
                    drawSoccerBall(canvas, ball);
                    return;
                case "neon_pulse":
                    drawNeonPulseBall(canvas, ball);
                    return;
                case "usa":
                case "germany":
                case "france":
                case "italy":
                case "uk":
                case "spain":
                case "portugal":
                case "netherlands":
                case "belgium":
                case "switzerland":
                case "austria":
                case "sweden":
                case "norway":
                case "denmark":
                case "finland":
                case "poland":
                case "greece":
                case "ireland":
                case "canada":
                case "brazil":
                    drawCountryFlagBall(canvas, ball, selectedSkin);
                    return;
            }
        }

        paint.setStyle(Paint.Style.FILL);

        RadialGradient gradient = new RadialGradient(
                ball.x - ball.radius / 3, ball.y - ball.radius / 3, ball.radius,
                Color.WHITE, ball.color, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        paint.setShadowLayer(15, 0, 0, ball.color);

        canvas.drawCircle(ball.x, ball.y, ball.radius, paint);

        paint.clearShadowLayer();
        paint.setShader(null);
    }

    private void drawTRFlagBall(Canvas canvas, Ball ball) {
        // Red Base
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setShadowLayer(20, 0, 0, Color.RED);
        canvas.drawCircle(ball.x, ball.y, ball.radius, paint);
        paint.clearShadowLayer();

        // White Crescent
        paint.setColor(Color.WHITE);
        canvas.drawCircle(ball.x - ball.radius * 0.15f, ball.y, ball.radius * 0.55f, paint);
        paint.setColor(Color.RED);
        canvas.drawCircle(ball.x - ball.radius * 0.05f, ball.y, ball.radius * 0.45f, paint);

        // White Star
        paint.setColor(Color.WHITE);
        float starX = ball.x + ball.radius * 0.35f;
        float starY = ball.y;
        float r = ball.radius * 0.25f;
        drawStarPath(canvas, starX, starY, r);
    }

    private void drawStarPath(Canvas canvas, float cx, float cy, float r) {
        android.graphics.Path path = new android.graphics.Path();
        for (int i = 0; i < 5; i++) {
            float angle = (float) (i * 2 * Math.PI / 5 - Math.PI / 2);
            float x = cx + (float) Math.cos(angle) * r;
            float y = cy + (float) Math.sin(angle) * r;
            if (i == 0)
                path.moveTo(x, y);
            else
                path.lineTo(x, y);

            angle += (float) (Math.PI / 5);
            x = cx + (float) Math.cos(angle) * (r * 0.4f);
            y = cy + (float) Math.sin(angle) * (r * 0.4f);
            path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawAuraEffect(Canvas canvas, Ball ball) {
        float speed = (float) Math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy);
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.01) * 0.15 + 0.85);
        float auraSize = ball.radius * (1.3f + (speed * 0.02f) * pulse);

        int auraColor = Color.CYAN; // Default
        if (selectedSkin.equals("tr_flag"))
            auraColor = Color.RED;
        else if (selectedSkin.equals("soccer"))
            auraColor = Color.WHITE;
        else if (selectedSkin.equals("neon_pulse"))
            auraColor = Color.rgb(0, 255, 255);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5 + (speed * 0.1f));
        paint.setColor(auraColor);
        paint.setAlpha((int) (150 * pulse));
        paint.setShadowLayer(25 * pulse + speed * 0.5f, 0, 0, auraColor);
        canvas.drawCircle(ball.x, ball.y, auraSize, paint);
        paint.clearShadowLayer();
        paint.setAlpha(255);
    }

    private void drawCometTrail(Canvas canvas, Ball ball) {
        paint.setStyle(Paint.Style.FILL);
        int trailColor = Color.CYAN; // Default

        switch (selectedTrail) {
            case "red":
                trailColor = Color.RED;
                break;
            case "blue":
                trailColor = Color.BLUE;
                break;
            case "green":
                trailColor = Color.GREEN;
                break;
            case "gold":
                trailColor = Color.rgb(255, 215, 0);
                break;
            case "purple":
                trailColor = Color.rgb(160, 32, 240);
                break;
            case "pink":
                trailColor = Color.rgb(255, 105, 180);
                break;
            case "neon":
                trailColor = Color.CYAN;
                break;
            case "comet":
                trailColor = Color.CYAN;
                break; // Fallback for old saves
        }

        for (int i = 0; i < ball.trail.size(); i++) {
            TrailPoint p = ball.trail.get(i);
            float ratio = 1.0f - (float) i / MAX_TRAIL_POINTS;
            int alpha = (int) (180 * ratio);
            float size = p.radius * ratio;

            paint.setColor(trailColor);
            paint.setAlpha(alpha);
            paint.setShadowLayer(10 * ratio, 0, 0, trailColor);
            canvas.drawCircle(p.x, p.y, size, paint);
        }
        paint.setAlpha(255);
        paint.clearShadowLayer();
    }

    private void drawSoccerBall(Canvas canvas, Ball ball) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(ball.x, ball.y, ball.radius, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        // Draw some simple pentagons/lines to resemble soccer ball
        for (int i = 0; i < 5; i++) {
            float angle = (float) (i * 2 * Math.PI / 5);
            float x1 = ball.x + (float) Math.cos(angle) * (ball.radius * 0.4f);
            float y1 = ball.y + (float) Math.sin(angle) * (ball.radius * 0.4f);
            float x2 = ball.x + (float) Math.cos(angle) * ball.radius;
            float y2 = ball.y + (float) Math.sin(angle) * ball.radius;
            canvas.drawLine(x1, y1, x2, y2, paint);

            float angleNext = (float) ((i + 1) * 2 * Math.PI / 5);
            float xNext = ball.x + (float) Math.cos(angleNext) * (ball.radius * 0.4f);
            float yNext = ball.y + (float) Math.sin(angleNext) * (ball.radius * 0.4f);
            canvas.drawLine(x1, y1, xNext, yNext, paint);
        }
    }

    private void drawNeonPulseBall(Canvas canvas, Ball ball) {
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.01) * 0.2 + 0.8);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(ball.x, ball.y, ball.radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.CYAN);
        paint.setShadowLayer(20 * pulse, 0, 0, Color.CYAN);
        canvas.drawCircle(ball.x, ball.y, ball.radius, paint);
        paint.clearShadowLayer();
    }

    private void drawCountryFlagBall(Canvas canvas, Ball ball, String country) {
        canvas.save();
        android.graphics.Path clipPath = new android.graphics.Path();
        clipPath.addCircle(ball.x, ball.y, ball.radius, android.graphics.Path.Direction.CW);
        canvas.clipPath(clipPath);

        float bx = ball.x;
        float by = ball.y;
        float r = ball.radius;

        switch (country) {
            case "usa":
                for (int i = 0; i < 7; i++) {
                    paint.setColor(i % 2 == 0 ? Color.RED : Color.WHITE);
                    canvas.drawRect(bx - r, by - r + (i * 2 * r / 7), bx + r, by - r + ((i + 1) * 2 * r / 7), paint);
                }
                paint.setColor(Color.rgb(0, 40, 104));
                canvas.drawRect(bx - r, by - r, bx, by, paint);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(bx - r * 0.5f, by - r * 0.5f, r * 0.15f, paint);
                break;
            case "germany":
                drawHorizStripes(canvas, bx, by, r, Color.BLACK, Color.RED, Color.rgb(255, 204, 0));
                break;
            case "france":
                drawVertStripes(canvas, bx, by, r, Color.rgb(0, 85, 164), Color.WHITE, Color.rgb(239, 65, 53));
                break;
            case "italy":
                drawVertStripes(canvas, bx, by, r, Color.rgb(0, 146, 70), Color.WHITE, Color.rgb(206, 43, 55));
                break;
            case "uk":
                paint.setColor(Color.rgb(1, 33, 105));
                canvas.drawCircle(bx, by, r, paint);
                paint.setColor(Color.WHITE);
                canvas.drawRect(bx - r, by - r * 0.3f, bx + r, by + r * 0.3f, paint);
                canvas.drawRect(bx - r * 0.3f, by - r, bx + r * 0.3f, by + r, paint);
                paint.setColor(Color.RED);
                canvas.drawRect(bx - r, by - r * 0.15f, bx + r, by + r * 0.15f, paint);
                canvas.drawRect(bx - r * 0.15f, by - r, bx + r * 0.15f, by + r, paint);
                break;
            case "spain":
                paint.setColor(Color.RED);
                canvas.drawRect(bx - r, by - r, bx + r, by - r * 0.5f, paint);
                canvas.drawRect(bx - r, by + r * 0.5f, bx + r, by + r, paint);
                paint.setColor(Color.rgb(255, 196, 0));
                canvas.drawRect(bx - r, by - r * 0.5f, bx + r, by + r * 0.5f, paint);
                break;
            case "portugal":
                paint.setColor(Color.rgb(0, 102, 0));
                canvas.drawRect(bx - r, by - r, bx - r * 0.2f, by + r, paint);
                paint.setColor(Color.RED);
                canvas.drawRect(bx - r * 0.2f, by - r, bx + r, by + r, paint);
                break;
            case "netherlands":
                drawHorizStripes(canvas, bx, by, r, Color.rgb(174, 28, 40), Color.WHITE, Color.rgb(33, 70, 139));
                break;
            case "belgium":
                drawVertStripes(canvas, bx, by, r, Color.BLACK, Color.rgb(253, 218, 36), Color.rgb(239, 51, 64));
                break;
            case "switzerland":
                paint.setColor(Color.RED);
                canvas.drawCircle(bx, by, r, paint);
                paint.setColor(Color.WHITE);
                canvas.drawRect(bx - r * 0.55f, by - r * 0.15f, bx + r * 0.55f, by + r * 0.15f, paint);
                canvas.drawRect(bx - r * 0.15f, by - r * 0.55f, bx + r * 0.15f, by + r * 0.55f, paint);
                break;
            case "austria":
                drawHorizStripes(canvas, bx, by, r, Color.RED, Color.WHITE, Color.RED);
                break;
            case "sweden":
                drawNordicCross(canvas, bx, by, r, Color.rgb(0, 107, 168), Color.rgb(254, 204, 2));
                break;
            case "norway":
                drawNordicCross(canvas, bx, by, r, Color.rgb(186, 12, 47), Color.rgb(0, 32, 91));
                break;
            case "denmark":
                drawNordicCross(canvas, bx, by, r, Color.rgb(198, 12, 48), Color.WHITE);
                break;
            case "finland":
                drawNordicCross(canvas, bx, by, r, Color.WHITE, Color.rgb(0, 53, 128));
                break;
            case "poland":
                drawHorizStripes(canvas, bx, by, r, Color.WHITE, Color.rgb(220, 20, 60));
                break;
            case "greece":
                paint.setColor(Color.rgb(13, 94, 175));
                drawHorizStripes(canvas, bx, by, r, Color.rgb(13, 94, 175), Color.WHITE, Color.rgb(13, 94, 175),
                        Color.WHITE, Color.rgb(13, 94, 175));
                canvas.drawRect(bx - r, by - r, bx, by + r * 0.2f, paint);
                paint.setColor(Color.WHITE);
                canvas.drawRect(bx - r, by - r * 0.5f, bx, by - r * 0.3f, paint);
                canvas.drawRect(bx - r * 0.6f, by - r, bx - r * 0.4f, by + r * 0.2f, paint);
                break;
            case "ireland":
                drawVertStripes(canvas, bx, by, r, Color.rgb(22, 155, 98), Color.WHITE, Color.rgb(255, 136, 62));
                break;
            case "canada":
                paint.setColor(Color.RED);
                canvas.drawRect(bx - r, by - r, bx - r * 0.4f, by + r, paint);
                canvas.drawRect(bx + r * 0.4f, by - r, bx + r, by + r, paint);
                paint.setColor(Color.WHITE);
                canvas.drawRect(bx - r * 0.4f, by - r, bx + r * 0.4f, by + r, paint);
                paint.setColor(Color.RED);
                canvas.drawCircle(bx, by, r * 0.3f, paint); // Leaf representation
                break;
            case "brazil":
                paint.setColor(Color.rgb(0, 153, 51));
                canvas.drawCircle(bx, by, r, paint);
                paint.setColor(Color.YELLOW);
                android.graphics.Path diamond = new android.graphics.Path();
                diamond.moveTo(bx, by - r * 0.8f);
                diamond.lineTo(bx + r * 0.8f, by);
                diamond.lineTo(bx, by + r * 0.8f);
                diamond.lineTo(bx - r * 0.8f, by);
                diamond.close();
                canvas.drawPath(diamond, paint);
                paint.setColor(Color.rgb(0, 39, 118));
                canvas.drawCircle(bx, by, r * 0.35f, paint);
                break;
        }
        canvas.restore();
    }

    private void drawHorizStripes(Canvas canvas, float x, float y, float r, int... colors) {
        float h = 2 * r / colors.length;
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawRect(x - r, y - r + i * h, x + r, y - r + (i + 1) * h, paint);
        }
    }

    private void drawVertStripes(Canvas canvas, float x, float y, float r, int... colors) {
        float w = 2 * r / colors.length;
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < colors.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawRect(x - r + i * w, y - r, x - r + (i + 1) * w, y + r, paint);
        }
    }

    private void drawNordicCross(Canvas canvas, float x, float y, float r, int bgColor, int crossColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        canvas.drawCircle(x, y, r, paint);
        paint.setColor(crossColor);
        canvas.drawRect(x - r, y - r * 0.2f, x + r, y + r * 0.2f, paint);
        canvas.drawRect(x - r * 0.5f, y - r, x - r * 0.1f, y + r, paint);
    }

    private void drawSpecialBall(Canvas canvas, SpecialBall ball) {
        paint.setStyle(Paint.Style.FILL);

        RadialGradient gradient = new RadialGradient(
                ball.x - ball.radius / 3, ball.y - ball.radius / 3, ball.radius,
                Color.WHITE, ball.getColor(), Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        paint.setShadowLayer(20, 0, 0, ball.getColor());

        canvas.drawCircle(ball.x, ball.y, ball.radius, paint);

        paint.clearShadowLayer();
        paint.setShader(null);

        // Harf çiz
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(ball.radius * 1.2f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(3, 0, 0, Color.BLACK);
        canvas.drawText(ball.getLetter(), ball.x, ball.y + ball.radius * 0.4f, paint);
        paint.clearShadowLayer();
    }

    private void drawUI(Canvas canvas) {
        // UI artık MainActivity'deki custom panellerde gösteriliyor
        // Eski text-based UI kaldırıldı

        // MainActivity'deki panelleri güncelle
        updateMainActivityPanels();

        // Level Seçim Ekranı
        if (showLevelSelector && !gameStarted) {
            drawLevelSelector(canvas);
            return;
        }

        if (!gameStarted) {
            // Arka plan paneli (Glassmorphism)
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(170, 25, 25, 50)); // Koyu mavi-mor
            float panelWidth = screenWidth * 0.85f;
            float panelHeight = screenHeight * 0.65f;
            canvas.drawRoundRect(
                    centerX - panelWidth / 2, centerY - panelHeight / 2,
                    centerX + panelWidth / 2, centerY + panelHeight / 2,
                    35, 35, paint);

            // Panel kenarlığı (Neon glow)
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.rgb(0, 243, 255));
            paint.setShadowLayer(18, 0, 0, Color.CYAN);
            canvas.drawRoundRect(
                    centerX - panelWidth / 2, centerY - panelHeight / 2,
                    centerX + panelWidth / 2, centerY + panelHeight / 2,
                    35, 35, paint);
            paint.clearShadowLayer();

            // Başlık
            paint.setTextAlign(Paint.Align.CENTER);

            // "NEON"
            paint.setTextSize(screenWidth * 0.15f);
            paint.setColor(Color.CYAN);
            paint.setShadowLayer(30, 0, 0, Color.CYAN);
            canvas.drawText("NEON", centerX, centerY - screenHeight * 0.25f, paint);

            // "BILLIARD"
            paint.setColor(Color.MAGENTA);
            paint.setShadowLayer(30, 0, 0, Color.MAGENTA);
            canvas.drawText("BILLIARD", centerX, centerY - screenHeight * 0.12f, paint);
            paint.clearShadowLayer();

            // Başlık "SPACE EDITION"
            paint.setTextSize(screenWidth * 0.05f);
            paint.setColor(Color.WHITE);
            paint.setAlpha(150);
            canvas.drawText("SPACE EDITION", centerX, centerY - screenHeight * 0.05f, paint);
            paint.setAlpha(255);

            // Removed canvas-drawn buttons as they are now NeonButton views in MainActivity
        }

        if (gameOver) {
            // Dark overlay
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setAlpha(200);
            canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
            paint.setAlpha(255);

            // Container Panel (Glassmorphism)
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(180, 20, 20, 40));
            float panelWidth = screenWidth * 0.85f;
            float panelHeight = screenHeight * 0.65f;
            canvas.drawRoundRect(
                    centerX - panelWidth / 2, centerY - panelHeight / 2,
                    centerX + panelWidth / 2, centerY + panelHeight / 2,
                    35, 35, paint);

            // Panel Border (Red Neon)
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            paint.setColor(Color.RED);
            paint.setShadowLayer(20, 0, 0, Color.RED);
            canvas.drawRoundRect(
                    centerX - panelWidth / 2, centerY - panelHeight / 2,
                    centerX + panelWidth / 2, centerY + panelHeight / 2,
                    35, 35, paint);
            paint.clearShadowLayer();

            // GAME OVER Header
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(screenWidth * 0.14f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            paint.setShadowLayer(30, 0, 0, Color.RED);
            canvas.drawText("GAME OVER", centerX, centerY - panelHeight * 0.3f, paint);
            paint.clearShadowLayer();

            // Final Score
            paint.setTextSize(screenWidth * 0.08f);
            paint.setColor(Color.WHITE);
            canvas.drawText("SCORE: " + score, centerX, centerY - panelHeight * 0.15f, paint);

            if (score > highScore)
                highScore = score;
            if (level > highLevel)
                highLevel = level;

            // Buttons
            float btnW = panelWidth * 0.7f;
            float btnH = screenHeight * 0.07f;
            float radius = btnH / 2f;

            // REBOOT SYSTEM
            float rebootY = centerY + panelHeight * 0.05f;
            drawNeonButton(canvas, "REBOOT SYSTEM", centerX, rebootY, btnW, btnH, Color.CYAN);

            // HALL OF FAME
            float hallY = centerY + panelHeight * 0.18f;
            drawNeonButton(canvas, "HALL OF FAME", centerX, hallY, btnW, btnH, Color.rgb(255, 215, 0));

            // MAIN MENU
            float menuY = centerY + panelHeight * 0.31f;
            drawNeonButton(canvas, "MAIN MENU", centerX, menuY, btnW, btnH, Color.LTGRAY);
        }
        // Başlık... (Mevcut kod aşağıda kalacak, sadece if bloğunu değiştiriyorum)

        // Instructions overlay
        if (showInstructions)

        {
            drawInstructionsOverlay(canvas);
        }

        // High Score overlay
        if (showHighScore) {
            drawHighScoreOverlay(canvas);
        }
    }

    private void drawInstructionsOverlay(Canvas canvas) {
        // Yarı saydam arka plan
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha(220);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        paint.setAlpha(255);

        // Ana panel (Glassmorphism)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(170, 25, 25, 50)); // Koyu mavi-mor
        float panelWidth = screenWidth * 0.9f;
        float panelHeight = screenHeight * 0.75f;
        canvas.drawRoundRect(
                centerX - panelWidth / 2, centerY - panelHeight / 2,
                centerX + panelWidth / 2, centerY + panelHeight / 2,
                35, 35, paint);

        // Panel kenarlığı (Neon glow)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(0, 243, 255));
        paint.setShadowLayer(18, 0, 0, Color.CYAN);
        canvas.drawRoundRect(
                centerX - panelWidth / 2, centerY - panelHeight / 2,
                centerX + panelWidth / 2, centerY + panelHeight / 2,
                35, 35, paint);
        paint.clearShadowLayer();

        // Başlık
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(screenWidth * 0.08f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(0, 243, 255));
        canvas.drawText("HOW TO PLAY", centerX, screenHeight * 0.2f, paint);

        // Talimatlar - Renkli toplar ve açıklamalar
        paint.setTextSize(screenWidth * 0.035f);
        paint.setTextAlign(Paint.Align.LEFT); // Sola hizalı
        paint.setColor(Color.WHITE);
        float y = screenHeight * 0.3f;
        float lineSpacing = screenHeight * 0.055f;
        float ballSize = screenWidth * 0.04f;
        float ballX = centerX - screenWidth * 0.35f; // Top pozisyonu
        float textX = ballX + ballSize * 1.5f; // Yazı topun sağında başlasın

        // Extra Time
        paint.setColor(Color.rgb(255, 165, 0));
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Extra Time: +5 Seconds.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Power Boost
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Power Boost: Kinetic surge.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Barrier
        paint.setColor(Color.BLUE);
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Barrier: Shield activation.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Electric
        paint.setColor(Color.CYAN);
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Electric: Chain lightning.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Clone
        paint.setColor(Color.rgb(255, 192, 203));
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Clone: Duplicate orb.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Freeze
        paint.setColor(Color.rgb(173, 216, 230));
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Freeze: Stasis field.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Missile
        paint.setColor(Color.RED);
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Missile: Homing projectile.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Teleport
        paint.setColor(Color.GREEN);
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Teleport: Quantum jump.", textX, y + ballSize * 0.4f, paint);
        y += lineSpacing;

        // Boom
        paint.setColor(Color.rgb(139, 0, 0));
        canvas.drawCircle(ballX, y, ballSize, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Boom: Shockwave.", textX, y + ballSize * 0.4f, paint);

        // Close butonu
        paint.setTextSize(screenWidth * 0.06f);
        paint.setColor(Color.rgb(255, 100, 100));
        canvas.drawText("TAP TO CLOSE", centerX, screenHeight * 0.85f, paint);
    }

    private void drawHighScoreOverlay(Canvas canvas) {
        // Yarı saydam arka plan
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha(220);
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        paint.setAlpha(255);

        // Ana panel (Glassmorphism)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(170, 25, 25, 50)); // Koyu mavi-mor
        float panelWidth = screenWidth * 0.85f;
        float panelHeight = screenHeight * 0.6f;
        canvas.drawRoundRect(
                centerX - panelWidth / 2, centerY - panelHeight / 2,
                centerX + panelWidth / 2, centerY + panelHeight / 2,
                35, 35, paint);

        // Panel kenarlığı (Neon glow)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(255, 215, 0));
        paint.setShadowLayer(18, 0, 0, Color.rgb(255, 215, 0));
        canvas.drawRoundRect(
                centerX - panelWidth / 2, centerY - panelHeight / 2,
                centerX + panelWidth / 2, centerY + panelHeight / 2,
                35, 35, paint);
        paint.clearShadowLayer();

        // Başlık
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(screenWidth * 0.1f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(255, 215, 0));
        canvas.drawText("HALL OF FAME", centerX, centerY - screenHeight * 0.2f, paint);

        // Skorlar
        paint.setTextSize(screenWidth * 0.07f);
        paint.setColor(Color.WHITE);
        canvas.drawText("Best Score: " + highScore, centerX, screenHeight * 0.45f, paint);
        canvas.drawText("Best Level: " + highLevel, centerX, screenHeight * 0.55f, paint);
        canvas.drawText("Max Combo: " + maxCombo, centerX, screenHeight * 0.65f, paint);

        // Close butonu
        paint.setTextSize(screenWidth * 0.06f);
        paint.setColor(Color.rgb(255, 100, 100));
        canvas.drawText("TAP TO CLOSE", centerX, screenHeight * 0.75f, paint);
    }

    private void drawNeonButton(Canvas canvas, String text, float cx, float cy, float w, float h, int color) {
        android.graphics.RectF rect = new android.graphics.RectF(cx - w / 2, cy - h / 2, cx + w / 2, cy + h / 2);
        float radius = h / 2;

        // Background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha(40);
        canvas.drawRoundRect(rect, radius, radius, paint);

        // Glow
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i <= 3; i++) {
            paint.setAlpha(100 / i);
            paint.setStrokeWidth(i * 3);
            canvas.drawRoundRect(rect, radius, radius, paint);
        }

        // Border
        paint.setAlpha(255);
        paint.setStrokeWidth(4);
        paint.setShadowLayer(15, 0, 0, color);
        canvas.drawRoundRect(rect, radius, radius, paint);
        paint.clearShadowLayer();

        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.4f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setShadowLayer(10, 0, 0, color);
        canvas.drawText(text, cx, cy + (h * 0.15f), paint);
        paint.clearShadowLayer();
    }

    private String getRank() {
        if (comboCounter >= 11)
            return "SS";
        if (comboCounter >= 9)
            return "S";
        if (comboCounter >= 7)
            return "A++";
        if (comboCounter >= 5)
            return "A";
        if (comboCounter >= 3)
            return "B";
        if (comboCounter >= 1)
            return "C";
        return "-";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Overlay kapatma
                if (showInstructions || showHighScore) {
                    showInstructions = false;
                    showHighScore = false;
                    updateMenuButtonsVisibility();
                    return true;
                }

                if (showLevelSelector) {
                    // Sayfa Değiştirme (Oklar)
                    float arrowY = centerY;
                    float arrowSize = screenWidth * 0.1f;

                    // Sol Ok
                    if (touchX < arrowSize * 1.5f && Math.abs(touchY - arrowY) < arrowSize) {
                        if (selectorPage > 1) {
                            selectorPage--;
                            playSound(soundLaunch); // Tuş sesi olarak kullan
                        }
                        return true;
                    }

                    // Sağ Ok
                    if (touchX > screenWidth - arrowSize * 1.5f && Math.abs(touchY - arrowY) < arrowSize) {
                        // Max sayfa sınırı yok, level arttıkça gider
                        selectorPage++;
                        playSound(soundLaunch);
                        return true;
                    }

                    // BACK Butonu (Yeni pozisyon)
                    float backY = screenHeight * 0.85f;
                    if (touchY > backY - screenWidth * 0.05f && touchY < backY + screenWidth * 0.05f) {
                        showLevelSelector = false;
                        updateMenuButtonsVisibility();
                        return true;
                    }

                    // Level Butonları
                    float gridStartX = screenWidth * 0.15f;
                    float gridStartY = screenHeight * 0.4f;
                    float cellWidth = screenWidth * 0.14f;
                    float cellHeight = screenWidth * 0.14f;
                    float gap = screenWidth * 0.02f;

                    for (int i = 0; i < 10; i++) {
                        int row = i / 5;
                        int col = i % 5;
                        float btnX = gridStartX + col * (cellWidth + gap) + cellWidth / 2;
                        float btnY = gridStartY + row * (cellHeight + gap) + cellHeight / 2;

                        if (Math.abs(touchX - btnX) < cellWidth / 2 && Math.abs(touchY - btnY) < cellHeight / 2) {
                            int selectedLv = (selectorPage - 1) * 10 + i + 1;
                            if (selectedLv <= maxUnlockedLevel) {
                                gameStarted = true;
                                updateMenuButtonsVisibility();
                                // Seçilen level'in ilk stage'ini başlat
                                level = (selectedLv - 1) * 5 + 1;
                                score = 0;
                                lives = 3;
                                initLevel(level);
                                showLevelSelector = false;
                                playSound(soundLaunch);
                            } else {
                                // Locked sound?
                            }
                            return true;
                        }
                    }
                    return true;
                }

                if (!gameStarted) {
                    // START GAME butonu kontrolü - removed as it's a NeonButton now

                    // HOW TO PLAY butonu kontrolü - removed as it's a NeonButton now
                } else if (gameOver) {
                    float panelHeight = screenHeight * 0.65f;
                    float btnW = screenWidth * 0.85f * 0.7f;
                    float btnH = screenHeight * 0.07f;

                    // REBOOT SYSTEM butonu
                    float rebootY = centerY + panelHeight * 0.05f;
                    if (Math.abs(touchX - centerX) < btnW / 2 && Math.abs(touchY - rebootY) < btnH / 2) {
                        gameOver = false;
                        score = 0;
                        initLevel(1);
                        updateMenuButtonsVisibility();
                        return true;
                    }

                    // HALL OF FAME butonu
                    float hallOfFameY = centerY + panelHeight * 0.18f;
                    if (Math.abs(touchX - centerX) < btnW / 2 && Math.abs(touchY - hallOfFameY) < btnH / 2) {
                        showHighScore = true;
                        updateMenuButtonsVisibility();
                        return true;
                    }

                    // MAIN MENU butonu
                    float mainMenuY = centerY + panelHeight * 0.31f;
                    if (Math.abs(touchX - centerX) < btnW / 2 && Math.abs(touchY - mainMenuY) < btnH / 2) {
                        gameOver = false;
                        gameStarted = false;
                        score = 0;
                        initLevel(1);
                        updateMenuButtonsVisibility();
                        return true;
                    }
                } else {
                    // Hangi topa dokunuldu?
                    Ball touchedBall = null;
                    float minDist = Float.MAX_VALUE;

                    for (Ball ball : cloneBalls) {
                        float dx = touchX - ball.x;
                        float dy = touchY - ball.y;
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);
                        if (distance < ball.radius * 3 && distance < minDist) {
                            minDist = distance;
                            touchedBall = ball;
                        }
                    }

                    float dx = touchX - whiteBall.x;
                    float dy = touchY - whiteBall.y;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance < whiteBall.radius * 3 && distance < minDist) {
                        touchedBall = whiteBall;
                    }

                    if (touchedBall != null) {
                        isDragging = true;
                        draggedBall = touchedBall;
                        dragStartX = touchedBall.x;
                        dragStartY = touchedBall.y;
                        dragStartTime = System.currentTimeMillis();
                        touchedBall.vx = 0;
                        touchedBall.vy = 0;
                        touchedBall.trail.clear(); // Drag başladığında eski izi temizle
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && draggedBall != null) {
                    float dx = touchX - dragStartX;
                    float dy = touchY - dragStartY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance > MAX_DRAG_DISTANCE) {
                        float angle = (float) Math.atan2(dy, dx);
                        draggedBall.x = dragStartX + (float) Math.cos(angle) * MAX_DRAG_DISTANCE;
                        draggedBall.y = dragStartY + (float) Math.sin(angle) * MAX_DRAG_DISTANCE;
                    } else {
                        draggedBall.x = touchX;
                        draggedBall.y = touchY;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (isDragging && draggedBall != null) {
                    float dx = draggedBall.x - dragStartX;
                    float dy = draggedBall.y - dragStartY;
                    float distance = Math.min((float) Math.sqrt(dx * dx + dy * dy), MAX_DRAG_DISTANCE);
                    float ratio = distance / MAX_DRAG_DISTANCE;
                    lastLaunchPower = ratio;
                    float maxSpeed = powerBoostActive ? 60 : 30;
                    float speed = ratio * maxSpeed;

                    if (distance > 5) {
                        draggedBall.vx = -dx / distance * speed;
                        draggedBall.vy = -dy / distance * speed;
                        playSound(soundLaunch);
                    }

                    isDragging = false;
                    draggedBall = null;
                    powerBoostActive = false;
                    comboCounter = 0;
                }
                break;
        }

        return true;
    }

    public void pause() {
        isPlaying = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        SharedPreferences prefs = getContext().getSharedPreferences("SpaceBilliard", Context.MODE_PRIVATE);
        selectedSkin = prefs.getString("selectedSkin", "default");
        selectedTrail = prefs.getString("selectedTrail", "none");
        selectedAura = prefs.getString("selectedAura", "none");
        selectedTrajectory = prefs.getString("selectedTrajectory", "dashed");
        selectedImpact = prefs.getString("selectedImpact", "classic");
        gameThread = new Thread(this);
        gameThread.start();
    }

    // İç sınıflar
    class Ball {
        float x, y, vx, vy, radius;
        int color;
        long creationTime;
        long lifetime; // Clone topları için yaşam süresi (ms)
        boolean isClone; // Clone topu mu?
        ArrayList<TrailPoint> trail = new ArrayList<>();

        Ball(float x, float y, float radius, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
            this.vx = 0;
            this.vy = 0;
            this.creationTime = System.currentTimeMillis();
            this.lifetime = 0;
            this.isClone = false;
        }

        Ball(float x, float y, float radius, int color, long lifetime) {
            this(x, y, radius, color);
            this.lifetime = lifetime;
            this.isClone = true;
        }
    }

    class SpecialBall {
        float x, y, vx, vy, radius;
        String type;

        SpecialBall(float x, float y, float radius, String type) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.type = type;
            this.vx = (random.nextFloat() - 0.5f) * 3;
            this.vy = (random.nextFloat() - 0.5f) * 3;
        }

        int getColor() {
            switch (type) {
                case "blackhole":
                    return Color.rgb(128, 0, 128);
                case "extraTime":
                    return Color.rgb(255, 165, 0);
                case "powerBoost":
                    return Color.rgb(255, 215, 0);
                case "barrier":
                    return Color.BLUE;
                case "electric":
                    return Color.CYAN;
                case "clone":
                    return Color.rgb(255, 192, 203);
                case "freeze":
                    return Color.rgb(173, 216, 230);
                case "missile":
                    return Color.RED;
                case "teleport":
                    return Color.GREEN;
                case "boom":
                    return Color.rgb(139, 0, 0);
                case "ghost":
                    return Color.rgb(211, 211, 211);
                default:
                    return Color.MAGENTA;
            }
        }

        String getLetter() {
            switch (type) {
                case "blackhole":
                    return "B";
                case "extraTime":
                    return "T";
                case "powerBoost":
                    return "P";
                case "barrier":
                    return "S";
                case "electric":
                    return "L";
                case "clone":
                    return "C";
                case "freeze":
                    return "F";
                case "missile":
                    return "M";
                case "teleport":
                    return "T";
                case "boom":
                    return "X";
                case "ghost":
                    return "G";
                default:
                    return "?";
            }
        }
    }

    private void saveProgress() {
        SharedPreferences prefs = getContext().getSharedPreferences("SpaceBilliard", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (score > highScore) {
            highScore = score;
            editor.putInt("highScore", highScore);
        }
        if (level > highLevel) {
            highLevel = level;
            editor.putInt("highLevel", highLevel);
        }

        // Persist maxUnlockedLevel
        editor.putInt("maxUnlockedLevel", maxUnlockedLevel);

        editor.putInt("maxCombo", maxCombo);
        editor.apply();
    }

    private void drawLevelSelector(Canvas canvas) {
        // Arka planı karart
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(200, 0, 0, 0));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        // Ana panel (Glassmorphism)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(160, 30, 30, 60)); // Koyu mavi-mor
        float panelWidth = screenWidth * 0.9f;
        float panelHeight = screenHeight * 0.75f;
        canvas.drawRoundRect(
                centerX - panelWidth / 2, screenHeight * 0.15f,
                centerX + panelWidth / 2, screenHeight * 0.15f + panelHeight,
                40, 40, paint);

        // Panel kenarlığı (Neon glow)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.rgb(0, 243, 255));
        paint.setShadowLayer(20, 0, 0, Color.CYAN);
        canvas.drawRoundRect(
                centerX - panelWidth / 2, screenHeight * 0.15f,
                centerX + panelWidth / 2, screenHeight * 0.15f + panelHeight,
                40, 40, paint);
        paint.clearShadowLayer();

        // Başlık
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(screenWidth * 0.1f);
        paint.setColor(Color.rgb(0, 243, 255));
        paint.setShadowLayer(20, 0, 0, Color.CYAN);
        canvas.drawText("SELECT LEVEL", centerX, screenHeight * 0.2f, paint);
        paint.clearShadowLayer();

        // Sayfa Göstergesi (Örn: SPACE 1)
        paint.setTextSize(screenWidth * 0.05f);
        paint.setColor(Color.WHITE);
        canvas.drawText("SPACE " + selectorPage, centerX, screenHeight * 0.28f, paint);

        // Sayfa bilgisi (Her sayfa bir level grubu temsil eder)
        paint.setTextSize(screenWidth * 0.04f);
        paint.setColor(Color.LTGRAY);
        canvas.drawText("Each level contains 5 stages", centerX, screenHeight * 0.32f, paint);

        // Grid (Panel içinde ortalanmış)
        float totalGridWidth = 5 * screenWidth * 0.14f + 4 * screenWidth * 0.02f; // 5 kutu + 4 boşluk
        float gridStartX = centerX - totalGridWidth / 2;
        float gridStartY = screenHeight * 0.42f;
        float cellWidth = screenWidth * 0.14f;
        float cellHeight = screenWidth * 0.14f;
        float gap = screenWidth * 0.02f;

        for (int i = 0; i < 10; i++) {
            int lvNum = (selectorPage - 1) * 10 + i + 1;
            int row = i / 5;
            int col = i % 5;

            float btnX = gridStartX + col * (cellWidth + gap) + cellWidth / 2;
            float btnY = gridStartY + row * (cellHeight + gap) + cellHeight / 2;

            // Kutu
            paint.setStyle(Paint.Style.FILL);
            if (lvNum <= maxUnlockedLevel) {
                paint.setColor(Color.rgb(200, 0, 50)); // Kırmızı buton (unlocked)
            } else {
                paint.setColor(Color.rgb(100, 100, 100)); // Gri (locked)
            }

            // Neon Stroke
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(btnX - cellWidth / 2, btnY - cellHeight / 2, btnX + cellWidth / 2,
                    btnY + cellHeight / 2, 20, 20, paint);

            // Stroke
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(btnX - cellWidth / 2, btnY - cellHeight / 2, btnX + cellWidth / 2,
                    btnY + cellHeight / 2, 20, 20, paint);

            // Metin veya Kilit
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            if (lvNum <= maxUnlockedLevel) {
                paint.setColor(Color.WHITE);
                paint.setTextSize(cellHeight * 0.5f);
                // Y offset biraz ayarlandı
                canvas.drawText(String.valueOf(lvNum), btnX, btnY + cellHeight * 0.2f, paint);

                // Yıldızlar (Basit mantık: 3 yıldız görsel)
                paint.setTextSize(cellHeight * 0.2f);
                paint.setColor(Color.YELLOW);
                canvas.drawText("★★★", btnX, btnY - cellHeight * 0.25f, paint);
            } else {
                // Kilit simgesi (Basitçe "L") veya renk
                paint.setColor(Color.LTGRAY);
                paint.setTextSize(cellHeight * 0.4f);
                canvas.drawText("🔒", btnX, btnY + cellHeight * 0.15f, paint);
            }
        }

        // Oklar
        paint.setTextSize(screenWidth * 0.1f);
        paint.setColor(Color.CYAN);
        if (selectorPage > 1)
            canvas.drawText("◄", screenWidth * 0.08f, centerY, paint);

        canvas.drawText("►", screenWidth * 0.92f, centerY, paint);

        // Back Butonu (Biraz yukarıda)
        paint.setTextSize(screenWidth * 0.05f);
        paint.setColor(Color.RED);
        canvas.drawText("BACK", centerX, screenHeight * 0.85f, paint);
    }

    enum ParticleType {
        CIRCLE, STAR, FLAME
    }

    class Particle {
        float x, y, vx, vy;
        int color, life = 30, maxLife = 30;
        ParticleType type = ParticleType.CIRCLE;
        float rotation = 0, vRotation = 0;

        Particle(float x, float y, float angle, float speed, int color) {
            this.x = x;
            this.y = y;
            this.vx = (float) Math.cos(angle) * speed;
            this.vy = (float) Math.sin(angle) * speed;
            this.color = color;
        }

        Particle(float x, float y, float angle, float speed, int color, ParticleType type) {
            this(x, y, angle, speed, color);
            this.type = type;
            if (type == ParticleType.STAR) {
                this.vRotation = (random.nextFloat() - 0.5f) * 0.2f;
                this.maxLife = 40;
                this.life = 40;
            } else if (type == ParticleType.FLAME) {
                this.maxLife = 20 + random.nextInt(15);
                this.life = this.maxLife;
                // Flames move mostly up
                this.vx = (random.nextFloat() - 0.5f) * 2f;
                this.vy = -random.nextFloat() * 4f - 2f;
            }
        }

        void update() {
            x += vx;
            y += vy;
            rotation += vRotation;
            life--;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha((int) (255 * (life / (float) maxLife)));

            if (type == ParticleType.STAR) {
                paint.setColor(color);
                drawStar(canvas, x, y, 8 * (life / (float) maxLife), rotation, paint);
            } else if (type == ParticleType.FLAME) {
                // Gradient fire color based on life
                float ratio = (float) life / maxLife;
                int r = 255;
                int g = (int) (255 * ratio);
                int b = (int) (100 * (ratio * ratio));
                paint.setColor(Color.rgb(r, g, b));
                paint.setShadowLayer(15 * ratio, 0, 0, Color.rgb(r, g, b));
                canvas.drawCircle(x, y, 12 * ratio, paint);
                paint.clearShadowLayer();
            } else {
                paint.setColor(color);
                canvas.drawCircle(x, y, 4, paint);
            }
            paint.setAlpha(255);
        }

        private void drawStar(Canvas canvas, float cx, float cy, float r, float rot, Paint p) {
            android.graphics.Path path = new android.graphics.Path();
            for (int i = 0; i < 5; i++) {
                float angle = (float) (i * 2 * Math.PI / 5 + rot - Math.PI / 2);
                float x = cx + (float) Math.cos(angle) * r;
                float y = cy + (float) Math.sin(angle) * r;
                if (i == 0)
                    path.moveTo(x, y);
                else
                    path.lineTo(x, y);

                angle += (float) (Math.PI / 5);
                x = cx + (float) Math.cos(angle) * (r * 0.4f);
                y = cy + (float) Math.sin(angle) * (r * 0.4f);
                path.lineTo(x, y);
            }
            path.close();
            canvas.drawPath(path, p);
        }
    }

    class ImpactArc {
        float x1, y1, x2, y2;
        int color, life = 15, maxLife = 15;
        java.util.ArrayList<PointF> points = new java.util.ArrayList<>();

        ImpactArc(float x, float y, float angle, float length, int color) {
            this.color = color;
            float endX = x + (float) Math.cos(angle) * length;
            float endY = y + (float) Math.sin(angle) * length;

            points.add(new PointF(x, y));
            int segments = 3;
            for (int i = 1; i < segments; i++) {
                float px = x + (endX - x) * i / segments + (random.nextFloat() - 0.5f) * 20;
                float py = y + (endY - y) * i / segments + (random.nextFloat() - 0.5f) * 20;
                points.add(new PointF(px, py));
            }
            points.add(new PointF(endX, endY));
        }

        void update() {
            life--;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(color);
            paint.setAlpha((int) (200 * (life / (float) maxLife)));
            paint.setShadowLayer(10, 0, 0, color);

            for (int i = 0; i < points.size() - 1; i++) {
                canvas.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y, paint);
            }
            paint.clearShadowLayer();
            paint.setAlpha(255);
        }
    }

    class GuidedMissile {
        float x, y, vx, vy, radius = 8, speed = 4;
        Ball target;

        GuidedMissile(float x, float y, Ball target) {
            this.x = x;
            this.y = y;
            this.target = target;
        }

        void update() {
            float dx = target.x - x;
            float dy = target.y - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > speed) {
                vx = (dx / distance) * speed;
                vy = (dy / distance) * speed;
                x += vx;
                y += vy;
            } else {
                x = target.x;
                y = target.y;
            }

            speed = Math.min(speed + 0.5f, 10);
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setShadowLayer(15, 0, 0, Color.RED);
            canvas.drawCircle(x, y, radius, paint);
            paint.clearShadowLayer();
        }
    }

    class ElectricEffect {
        float x1, y1, x2, y2;
        int life = 30;

        ElectricEffect(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        void update() {
            life--;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.CYAN);
            paint.setAlpha((int) (255 * (life / 30f)));

            // Zigzag çizgi
            float midX = (x1 + x2) / 2 + (random.nextFloat() - 0.5f) * 20;
            float midY = (y1 + y2) / 2 + (random.nextFloat() - 0.5f) * 20;

            canvas.drawLine(x1, y1, midX, midY, paint);
            canvas.drawLine(midX, midY, x2, y2, paint);

            paint.setAlpha(255);
        }
    }

    class BlastWave {
        float x, y, radius = 0, maxRadius;
        int life = 60;

        BlastWave(float x, float y) {
            this.x = x;
            this.y = y;
            this.maxRadius = whiteBall.radius * 30;
        }

        void update() {
            radius = maxRadius * (1 - life / 60f);
            life--;

            // Çevredeki topları dalga yayıldıkça yok et
            for (int i = coloredBalls.size() - 1; i >= 0; i--) {
                Ball ball = coloredBalls.get(i);
                float dx = x - ball.x;
                float dy = y - ball.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance < radius) {
                    score += 2;
                    createImpactBurst(ball.x, ball.y, ball.color);
                    coloredBalls.remove(i);
                    playSound(soundCollision);
                }
            }

            for (int i = blackBalls.size() - 1; i >= 0; i--) {
                Ball ball = blackBalls.get(i);
                float dx = x - ball.x;
                float dy = y - ball.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance < radius) {
                    score += 5;
                    createParticles(ball.x, ball.y, Color.BLACK);
                    blackBalls.remove(i);
                    playSound(soundCollision); // Siyah top patlasa da standart çarpışma sesi (kullanıcı isteği)
                }
            }
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8 * (life / 60f));
            paint.setColor(Color.rgb(255, 69, 0));
            paint.setAlpha((int) (255 * (life / 60f)));
            paint.setShadowLayer(20, 0, 0, Color.RED);
            canvas.drawCircle(x, y, radius, paint);
            paint.clearShadowLayer();
            paint.setAlpha(255);
        }
    }

    // Yıldız sınıfı
    private class Star {
        float x, y;
        float vx, vy;
        float size;
        int alpha;

        Star() {
            Random r = new Random();
            x = r.nextFloat() * 10000;
            y = r.nextFloat() * 10000;
            vx = (r.nextFloat() - 0.5f) * 2;
            vy = (r.nextFloat() - 0.5f) * 2;
            size = r.nextFloat() * 3 + 1;
            alpha = r.nextInt(155) + 100;
        }

        void update(int screenW, int screenH) {
            x += vx;
            y += vy;

            if (x < 0)
                x = screenW;
            if (x > screenW)
                x = 0;
            if (y < 0)
                y = screenH;
            if (y > screenH)
                y = 0;
        }

        void draw(Canvas canvas, Paint paint) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAlpha(alpha);
            canvas.drawCircle(x, y, size, paint);
            paint.setAlpha(255);
        }
    }

    private void updateMainActivityPanels() {
        if (mainActivity != null && gameStarted && !gameOver) {
            int currentStage = ((level - 1) % 5) + 1;
            int power = (int) (lastLaunchPower * (powerBoostActive ? 200 : 100));

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.updatePanels(
                            (int) (timeLeft / 1000),
                            score,
                            coins, // Add coins
                            power,
                            currentStage + "/5",
                            lives);
                }
            });
        }
    }

    class TrailPoint {
        float x, y, radius;

        TrailPoint(float x, float y, float radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }
}
