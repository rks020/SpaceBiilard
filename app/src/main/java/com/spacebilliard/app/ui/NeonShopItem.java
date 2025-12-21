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
import android.view.View;

public class NeonShopItem extends View {

    private String itemName;
    private String price;
    private int themeColor;
    private String quantity = "x5";
    private String priceIcon = "GEM"; // GEM, HEART, COIN, NONE
    private boolean isSelected = false;
    private String skinId = null;

    // Paints
    private Paint glowPaint;
    private Paint borderPaint;
    private Paint bgPaint;
    private Paint textPaint;
    private Paint iconPaint;
    private Paint pillPaint;

    public NeonShopItem(Context context, String name, String price, int color) {
        super(context);
        this.itemName = name;
        this.price = price;
        this.themeColor = color;
        init();
    }

    public NeonShopItem(Context context, String name, String price, int color, String qty, String pIcon) {
        super(context);
        this.itemName = name;
        this.price = price;
        this.themeColor = color;
        this.quantity = qty;
        this.priceIcon = pIcon;
        init();
    }

    public NeonShopItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.itemName = "ITEM";
        this.price = "0";
        this.themeColor = Color.CYAN;
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeWidth(5f);

        pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pillPaint.setStrokeWidth(2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) (110 * getResources().getDisplayMetrics().density);
        int desiredHeight = (int) (140 * getResources().getDisplayMetrics().density);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float pad = 12;
        float cornerRadius = 35f;

        RectF rect = new RectF(pad, pad, w - pad, h - pad);

        // 1. Draw Card Background with subtle gradient
        int topColor = Color.argb(100, 10, 40, 60);
        int bottomColor = Color.argb(150, 5, 10, 20);
        bgPaint.setShader(new LinearGradient(0, 0, 0, h, topColor, bottomColor, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);
        bgPaint.setShader(null);

        // 2. Wavy/Stylized Neon Border (Inner)
        borderPaint.setColor(themeColor);
        borderPaint.setAlpha(255);

        // Draw 3 layers of glow for the border
        for (int i = 1; i <= 3; i++) {
            glowPaint.setColor(themeColor);
            glowPaint.setAlpha(100 / i);
            glowPaint.setStrokeWidth(i * 3);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint);
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint);

        // 3. Draw Icon (Placeholder shape based on itemName)
        drawItemIcon(canvas, w / 2, h * 0.4f, w * 0.25f);

        // 4. Quantity Label (x5)
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(h * 0.12f);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(quantity, w - pad - 15, h * 0.65f, textPaint);

        // 5. Price Pill at the bottom
        drawPricePill(canvas, w / 2, h * 0.82f);
    }

    private void drawItemIcon(Canvas canvas, float cx, float cy, float size) {
        iconPaint.setColor(themeColor);
        iconPaint.setShadowLayer(15, 0, 0, themeColor);

        String name = itemName.toUpperCase();
        if (name.contains("CUE")) {
            // Draw a diagonal cue
            canvas.drawLine(cx - size, cy + size, cx + size, cy - size, iconPaint);
            canvas.drawCircle(cx + size, cy - size, 5, iconPaint);
        } else if (name.contains("LIVES") || name.contains("OWNED")) {
            // Draw a rounded box
            RectF r = new RectF(cx - size, cy - size * 0.7f, cx + size, cy + size * 0.7f);
            canvas.drawRoundRect(r, 10, 10, iconPaint);
            canvas.drawCircle(cx, cy, size * 0.3f, iconPaint);
        } else if (name.contains("TR FLAG")) {
            // Draw a small TR flag on the ball icon
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.RED);
            canvas.drawCircle(cx, cy, size, p);

            // Crescent
            p.setColor(Color.WHITE);
            canvas.drawCircle(cx - size * 0.15f, cy, size * 0.55f, p);
            p.setColor(Color.RED);
            canvas.drawCircle(cx - size * 0.05f, cy, size * 0.45f, p);

            // Star
            p.setColor(Color.WHITE);
            drawStarIcon(canvas, cx + size * 0.35f, cy, size * 0.25f, p);
        } else if (name.contains("TRAIL")) {
            // Draw a trail icon with gradient/fading circles
            float trailSize = size * 0.8f; // Shrink trail icons slightly as requested
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.FILL);
            p.setColor(themeColor);
            p.setShadowLayer(15, 0, 0, themeColor);

            for (int i = 0; i < 5; i++) {
                float tr = trailSize * (1.0f - i * 0.18f);
                float tx = cx - i * (trailSize * 0.4f);
                p.setAlpha(200 - i * 40);
                canvas.drawCircle(tx, cy, tr, p);
            }
            p.setAlpha(255);
            canvas.drawCircle(cx, cy, trailSize, p);
            p.clearShadowLayer();
        } else if (name.contains("AURA")) {
            // Draw an Aura icon (ring)
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(themeColor);
            p.setShadowLayer(15, 0, 0, themeColor);
            canvas.drawCircle(cx, cy, size * 1.1f, p);

            p.setStyle(Paint.Style.FILL);
            p.setAlpha(100);
            canvas.drawCircle(cx, cy, size, p);
            p.clearShadowLayer();
        } else if (name.contains("SIGHT")) {
            // Draw a Trajectory Sight icon
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6);
            p.setColor(themeColor);
            p.setShadowLayer(15, 0, 0, themeColor);

            if (name.contains("ELECTRIC")) {
                // Zig-zag icon
                android.graphics.Path path = new android.graphics.Path();
                path.moveTo(cx - size, cy + size);
                path.lineTo(cx - size * 0.4f, cy + size * 0.2f);
                path.lineTo(cx + size * 0.2f, cy - size * 0.4f);
                path.lineTo(cx + size, cy - size);
                canvas.drawPath(path, p);
            } else if (name.contains("PEARL")) {
                // Dots icon
                p.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx - size * 0.6f, cy + size * 0.6f, 5, p);
                canvas.drawCircle(cx, cy, 7, p);
                canvas.drawCircle(cx + size * 0.6f, cy - size * 0.6f, 5, p);
            } else if (name.contains("PLASMA")) {
                // Glowing fading circles icon
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 3; i++) {
                    p.setAlpha(100 + i * 70);
                    canvas.drawCircle(cx - size * 0.7f + i * size * 0.7f, cy + size * 0.7f - i * size * 0.7f, 4 + i * 3,
                            p);
                }
            } else if (name.contains("COSMIC")) {
                // Stars/Sparkle icon
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                canvas.drawCircle(cx - size * 0.4f, cy + size * 0.4f, 4, p);
                canvas.drawCircle(cx + size * 0.5f, cy - size * 0.3f, 5, p);
                p.setColor(themeColor);
                canvas.drawCircle(cx, cy, size * 0.6f, p);
            } else if (name.contains("LAVA")) {
                // Flame/Bubble icon
                p.setStyle(Paint.Style.FILL);
                p.setColor(themeColor);
                canvas.drawCircle(cx, cy + size * 0.3f, size * 0.7f, p);
                canvas.drawCircle(cx - size * 0.4f, cy - size * 0.2f, size * 0.4f, p);
                canvas.drawCircle(cx + size * 0.3f, cy - size * 0.5f, size * 0.3f, p);
            } else if (name.contains("RAINBOW")) {
                // Multi-color dash icon
                int[] rb = { Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA };
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 5; i++) {
                    p.setColor(rb[i]);
                    canvas.drawCircle(cx - size + i * size * 0.5f, cy + size - i * size * 0.5f, 6, p);
                }
            } else {
                // Classic Laser icon
                canvas.drawLine(cx - size, cy + size, cx + size, cy - size, p);
            }
            p.clearShadowLayer();
        } else if (quantity.equals("BOOM")) {
            // Draw a Burst/Explosion icon
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(themeColor);
            p.setShadowLayer(15, 0, 0, themeColor);

            if (itemName.contains("STAR")) {
                // Draw 5-pointed stars for STAR BURST
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 5; i++) {
                    float angle = (float) (i * 2 * Math.PI / 5 - Math.PI / 2);
                    float rx = cx + (float) Math.cos(angle) * (size * 0.7f);
                    float ry = cy + (float) Math.sin(angle) * (size * 0.7f);
                    drawStarIcon(canvas, rx, ry, size * 0.25f, p);
                }
                // Center Star
                drawStarIcon(canvas, cx, cy, size * 0.4f, p);
            } else if (itemName.contains("ELECTRIC")) {
                // Draw lightning bolts for ELECTRIC BOOM
                for (int i = 0; i < 4; i++) {
                    float angle = (float) (i * Math.PI / 2);
                    float x1 = cx + (float) Math.cos(angle) * (size * 0.2f);
                    float y1 = cy + (float) Math.sin(angle) * (size * 0.2f);
                    float x2 = cx + (float) Math.cos(angle) * size;
                    float y2 = cy + (float) Math.sin(angle) * size;

                    Path path = new Path();
                    float midX = (x1 + x2) / 2 + (float) Math.cos(angle + Math.PI / 2) * (size * 0.3f);
                    float midY = (y1 + y2) / 2 + (float) Math.sin(angle + Math.PI / 2) * (size * 0.3f);
                    path.moveTo(x1, y1);
                    path.lineTo(midX, midY);
                    path.lineTo(x2, y2);
                    canvas.drawPath(path, p);
                }
            } else {
                // Default Explosion lines
                for (int i = 0; i < 8; i++) {
                    float angle = (float) (i * Math.PI / 4);
                    float x1 = cx + (float) Math.cos(angle) * (size * 0.3f);
                    float y1 = cy + (float) Math.sin(angle) * (size * 0.3f);
                    float x2 = cx + (float) Math.cos(angle) * size;
                    float y2 = cy + (float) Math.sin(angle) * size;
                    canvas.drawLine(x1, y1, x2, y2, p);
                }
            }
            p.clearShadowLayer();
        } else if (skinId != null && skinId.equals("cyber_core")) {
            // Metallic with circuits icon
            iconPaint.setColor(themeColor);
            iconPaint.setShadowLayer(15, 0, 0, themeColor);
            canvas.drawCircle(cx, cy, size, iconPaint);

            Paint p = new Paint(iconPaint);
            p.setStrokeWidth(3);
            canvas.drawArc(cx - size * 0.7f, cy - size * 0.7f, cx + size * 0.7f, cy + size * 0.7f, 45, 90, false, p);
            canvas.drawArc(cx - size * 0.7f, cy - size * 0.7f, cx + size * 0.7f, cy + size * 0.7f, 225, 90, false, p);
            canvas.drawCircle(cx, cy, size * 0.3f, p);
        } else if (skinId != null && skinId.equals("solar_flare")) {
            // Solar sun icon
            iconPaint.setColor(themeColor);
            iconPaint.setShadowLayer(20, 0, 0, themeColor);
            canvas.drawCircle(cx, cy, size * 0.8f, iconPaint);

            // Corona lines
            for (int i = 0; i < 12; i++) {
                float angle = (float) (i * Math.PI / 6);
                canvas.drawLine(cx + (float) Math.cos(angle) * size * 0.9f, cy + (float) Math.sin(angle) * size * 0.9f,
                        cx + (float) Math.cos(angle) * size * 1.2f, cy + (float) Math.sin(angle) * size * 1.2f,
                        iconPaint);
            }
        } else if (skinId != null && skinId.equals("frost_bite")) {
            // Ice crystal icon
            iconPaint.setColor(themeColor);
            iconPaint.setShadowLayer(15, 0, 0, Color.WHITE);
            canvas.drawCircle(cx, cy, size, iconPaint);

            // Ice cracks
            canvas.drawLine(cx - size * 0.5f, cy - size * 0.5f, cx, cy, iconPaint);
            canvas.drawLine(cx + size * 0.3f, cy - size * 0.6f, cx - size * 0.1f, cy + size * 0.2f, iconPaint);
        } else if (skinId != null && !skinId.equals("default") && !skinId.equals("neon_pulse")
                && !skinId.equals("soccer") && !skinId.startsWith("impact_")) {
            drawFlagIcon(canvas, cx, cy, size, skinId);
        } else if (itemName.contains("SOCCER")) {
            canvas.drawCircle(cx, cy, size, iconPaint);
            for (int i = 0; i < 5; i++) {
                float angle = (float) (i * 2 * Math.PI / 5);
                canvas.drawLine(cx, cy, cx + (float) Math.cos(angle) * size, cy + (float) Math.sin(angle) * size,
                        iconPaint);
            }
        } else {
            // Default: Double circle ball
            canvas.drawCircle(cx, cy, size, iconPaint);
            canvas.drawCircle(cx, cy, size * 0.4f, iconPaint);
            canvas.drawCircle(cx + size * 0.3f, cy - size * 0.3f, 4, iconPaint);
        }

        iconPaint.clearShadowLayer();
    }

    private void drawFlagIcon(Canvas canvas, float cx, float cy, float r, String id) {
        canvas.save();
        android.graphics.Path clip = new android.graphics.Path();
        clip.addCircle(cx, cy, r, android.graphics.Path.Direction.CW);
        canvas.clipPath(clip);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);

        switch (id) {
            case "tr_flag":
                p.setColor(Color.RED);
                canvas.drawCircle(cx, cy, r, p);

                // Crescent
                p.setColor(Color.WHITE);
                canvas.drawCircle(cx - r * 0.15f, cy, r * 0.55f, p);
                p.setColor(Color.RED);
                canvas.drawCircle(cx - r * 0.05f, cy, r * 0.45f, p);

                // Star
                p.setColor(Color.WHITE);
                drawStarIcon(canvas, cx + r * 0.35f, cy, r * 0.25f, p);
                break;
            case "usa":
                for (int i = 0; i < 7; i++) {
                    p.setColor(i % 2 == 0 ? Color.RED : Color.WHITE);
                    canvas.drawRect(cx - r, cy - r + (i * 2 * r / 7), cx + r, cy - r + ((i + 1) * 2 * r / 7), p);
                }
                p.setColor(Color.rgb(0, 40, 104));
                canvas.drawRect(cx - r, cy - r, cx, cy, p);
                break;
            case "germany":
                p.setColor(Color.BLACK);
                canvas.drawRect(cx - r, cy - r, cx + r, cy - r / 3, p);
                p.setColor(Color.RED);
                canvas.drawRect(cx - r, cy - r / 3, cx + r, cy + r / 3, p);
                p.setColor(Color.YELLOW);
                canvas.drawRect(cx - r, cy + r / 3, cx + r, cy + r, p);
                break;
            case "france":
                p.setColor(Color.rgb(0, 85, 164));
                canvas.drawRect(cx - r, cy - r, cx - r / 3, cy + r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r / 3, cy - r, cx + r / 3, cy + r, p);
                p.setColor(Color.rgb(239, 65, 53));
                canvas.drawRect(cx + r / 3, cy - r, cx + r, cy + r, p);
                break;
            case "italy":
                p.setColor(Color.rgb(0, 146, 70));
                canvas.drawRect(cx - r, cy - r, cx - r / 3, cy + r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r / 3, cy - r, cx + r / 3, cy + r, p);
                p.setColor(Color.rgb(206, 43, 55));
                canvas.drawRect(cx + r / 3, cy - r, cx + r, cy + r, p);
                break;
            case "uk":
                p.setColor(Color.rgb(1, 33, 105));
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r * 0.2f, cx + r, cy + r * 0.2f, p);
                canvas.drawRect(cx - r * 0.2f, cy - r, cx + r * 0.2f, cy + r, p);
                p.setColor(Color.RED);
                canvas.drawRect(cx - r, cy - r * 0.1f, cx + r, cy + r * 0.1f, p);
                canvas.drawRect(cx - r * 0.1f, cy - r, cx + r * 0.1f, cy + r, p);
                break;
            case "spain":
                p.setColor(Color.rgb(170, 21, 27));
                canvas.drawRect(cx - r, cy - r, cx + r, cy - r / 3, p);
                p.setColor(Color.rgb(255, 196, 0));
                canvas.drawRect(cx - r, cy - r / 3, cx + r, cy + r / 3, p);
                p.setColor(Color.rgb(170, 21, 27));
                canvas.drawRect(cx - r, cy + r / 3, cx + r, cy + r, p);
                break;
            case "portugal":
                p.setColor(Color.rgb(0, 102, 0));
                canvas.drawRect(cx - r, cy - r, cx - r * 0.2f, cy + r, p);
                p.setColor(Color.RED);
                canvas.drawRect(cx - r * 0.2f, cy - r, cx + r, cy + r, p);
                break;
            case "netherlands":
                p.setColor(Color.rgb(174, 28, 40));
                canvas.drawRect(cx - r, cy - r, cx + r, cy - r / 3, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r / 3, cx + r, cy + r / 3, p);
                p.setColor(Color.rgb(33, 70, 139));
                canvas.drawRect(cx - r, cy + r / 3, cx + r, cy + r, p);
                break;
            case "belgium":
                p.setColor(Color.BLACK);
                canvas.drawRect(cx - r, cy - r, cx - r / 3, cy + r, p);
                p.setColor(Color.rgb(253, 218, 36));
                canvas.drawRect(cx - r / 3, cy - r, cx + r / 3, cy + r, p);
                p.setColor(Color.rgb(239, 51, 64));
                canvas.drawRect(cx + r / 3, cy - r, cx + r, cy + r, p);
                break;
            case "switzerland":
                p.setColor(Color.RED);
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r * 0.6f, cy - r * 0.15f, cx + r * 0.6f, cy + r * 0.15f, p);
                canvas.drawRect(cx - r * 0.15f, cy - r * 0.6f, cx + r * 0.15f, cy + r * 0.6f, p);
                break;
            case "austria":
                p.setColor(Color.RED);
                canvas.drawRect(cx - r, cy - r, cx + r, cy - r / 3, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r / 3, cx + r, cy + r / 3, p);
                p.setColor(Color.RED);
                canvas.drawRect(cx - r, cy + r / 3, cx + r, cy + r, p);
                break;
            case "sweden":
                p.setColor(Color.rgb(0, 107, 168));
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.rgb(254, 204, 2));
                canvas.drawRect(cx - r, cy - r * 0.15f, cx + r, cy + r * 0.15f, p);
                canvas.drawRect(cx - r * 0.4f, cy - r, cx - r * 0.1f, cy + r, p);
                break;
            case "norway":
                p.setColor(Color.rgb(186, 12, 47));
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r * 0.2f, cx + r, cy + r * 0.2f, p);
                canvas.drawRect(cx - r * 0.5f, cy - r, cx - r * 0.1f, cy + r, p);
                p.setColor(Color.rgb(0, 32, 91));
                canvas.drawRect(cx - r, cy - r * 0.1f, cx + r, cy + r * 0.1f, p);
                canvas.drawRect(cx - r * 0.4f, cy - r, cx - r * 0.2f, cy + r, p);
                break;
            case "denmark":
                p.setColor(Color.rgb(198, 12, 48));
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r * 0.1f, cx + r, cy + r * 0.1f, p);
                canvas.drawRect(cx - r * 0.4f, cy - r, cx - r * 0.2f, cy + r, p);
                break;
            case "finland":
                p.setColor(Color.WHITE);
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.rgb(0, 53, 128));
                canvas.drawRect(cx - r, cy - r * 0.2f, cx + r, cy + r * 0.2f, p);
                canvas.drawRect(cx - r * 0.4f, cy - r, cx - r * 0.1f, cy + r, p);
                break;
            case "poland":
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r, cx + r, cy, p);
                p.setColor(Color.rgb(220, 20, 60));
                canvas.drawRect(cx - r, cy, cx + r, cy + r, p);
                break;
            case "greece":
                p.setColor(Color.rgb(13, 94, 175));
                for (int i = 0; i < 9; i++) {
                    p.setColor(i % 2 == 0 ? Color.rgb(13, 94, 175) : Color.WHITE);
                    canvas.drawRect(cx - r, cy - r + (i * 2 * r / 9), cx + r, cy - r + ((i + 1) * 2 * r / 9), p);
                }
                p.setColor(Color.rgb(13, 94, 175));
                canvas.drawRect(cx - r, cy - r, cx - r * 0.1f, cy - r * 0.1f, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r, cy - r * 0.6f, cx - r * 0.1f, cy - r * 0.5f, p);
                canvas.drawRect(cx - r * 0.6f, cy - r, cx - r * 0.5f, cy - r * 0.1f, p);
                break;
            case "ireland":
                p.setColor(Color.rgb(22, 155, 98));
                canvas.drawRect(cx - r, cy - r, cx - r / 3, cy + r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r / 3, cy - r, cx + r / 3, cy + r, p);
                p.setColor(Color.rgb(255, 136, 62));
                canvas.drawRect(cx + r / 3, cy - r, cx + r, cy + r, p);
                break;
            case "canada":
                p.setColor(Color.RED);
                canvas.drawRect(cx - r, cy - r, cx - r * 0.4f, cy + r, p);
                canvas.drawRect(cx + r * 0.4f, cy - r, cx + r, cy + r, p);
                p.setColor(Color.WHITE);
                canvas.drawRect(cx - r * 0.4f, cy - r, cx + r * 0.4f, cy + r, p);
                p.setColor(Color.RED);
                canvas.drawCircle(cx, cy, r * 0.3f, p);
                break;
            case "brazil":
                p.setColor(Color.rgb(0, 153, 51));
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.YELLOW);
                android.graphics.Path diamond = new android.graphics.Path();
                diamond.moveTo(cx, cy - r * 0.8f);
                diamond.lineTo(cx + r * 0.8f, cy);
                diamond.lineTo(cx, cy + r * 0.8f);
                diamond.lineTo(cx - r * 0.8f, cy);
                diamond.close();
                canvas.drawPath(diamond, p);
                p.setColor(Color.rgb(0, 39, 118));
                canvas.drawCircle(cx, cy, r * 0.35f, p);
                break;
            default:
                p.setColor(Color.GRAY);
                canvas.drawCircle(cx, cy, r, p);
                p.setColor(Color.WHITE);
                p.setTextSize(r);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("FLAG", cx, cy + r / 3, p);
                break;
        }
        canvas.restore();
    }

    private void drawPricePill(Canvas canvas, float cx, float cy) {
        float pillW = getWidth() * 0.75f;
        float pillH = getHeight() * 0.18f;
        RectF pillRect = new RectF(cx - pillW / 2, cy - pillH / 2, cx + pillW / 2, cy + pillH / 2);

        // Pill Background
        pillPaint.setStyle(Paint.Style.FILL);
        pillPaint.setColor(Color.argb(200, 0, 100, 150)); // Dark cyan
        canvas.drawRoundRect(pillRect, pillH / 2, pillH / 2, pillPaint);

        // Pill Border
        pillPaint.setStyle(Paint.Style.STROKE);
        pillPaint.setColor(Color.CYAN);
        canvas.drawRoundRect(pillRect, pillH / 2, pillH / 2, pillPaint);

        // Price Text and Icon
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(pillH * 0.55f); // Reduced from 0.7f to 0.55f
        textPaint.setColor(Color.WHITE);

        float textX = cx;
        // Removed Small Diamond/Square icon as requested by user
        /*
         * if (!priceIcon.equals("NONE")) {
         * // Draw small icon on the left
         * float iconSize = pillH * 0.4f;
         * float iconX = cx - pillW / 2 + pillH / 2 + 5;
         * drawSmallIcon(canvas, iconX, cy, iconSize, priceIcon);
         * textX += 10;
         * }
         */

        canvas.drawText(price, textX, cy + textPaint.getTextSize() / 3, textPaint);
    }

    private void drawSmallIcon(Canvas canvas, float x, float y, float size, String type) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        if (type.equals("GEM")) {
            p.setColor(Color.rgb(0, 200, 255));
            Path path = new Path();
            path.moveTo(x, y - size);
            path.lineTo(x + size, y);
            path.lineTo(x, y + size);
            path.lineTo(x - size, y);
            path.close();
            canvas.drawPath(path, p);
        } else if (type.equals("HEART")) {
            p.setColor(Color.rgb(255, 50, 100));
            canvas.drawCircle(x, y, size, p);
        } else if (type.equals("COIN")) {
            p.setColor(Color.YELLOW);
            canvas.drawCircle(x, y, size, p);
        }
    }

    private void drawStarIcon(Canvas canvas, float cx, float cy, float r, Paint p) {
        Path path = new Path();
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
        canvas.drawPath(path, p);
    }

    public void setItemName(String name) {
        this.itemName = name;
        invalidate();
    }

    public void setPrice(String price) {
        this.price = price;
        invalidate();
    }

    public void setThemeColor(int color) {
        this.themeColor = color;
        invalidate();
    }

    public void setSkinId(String skinId) {
        this.skinId = skinId;
    }

    public String getSkinId() {
        return skinId;
    }

    public String getItemName() {
        return itemName;
    }
}
