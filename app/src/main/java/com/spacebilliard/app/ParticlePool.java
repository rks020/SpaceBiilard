package com.spacebilliard.app;

import java.util.Stack;

/**
 * Object pool for Particle to reduce GC pressure and improve FPS
 * Reuses Particle objects instead of creating new ones every frame
 */
public class ParticlePool {
    private final Stack<GameView.Particle> pool = new Stack<>();
    private final GameView gameView;

    public ParticlePool(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * Get a particle from pool or create new if needed
     */
    public GameView.Particle obtain(float x, float y, float angle, float speed, int color) {
        if (pool.isEmpty()) {
            return gameView.new Particle(x, y, angle, speed, color);
        }
        GameView.Particle p = pool.pop();
        p.reset(x, y, angle, speed, color);
        return p;
    }

    /**
     * Get a particle with type
     */
    public GameView.Particle obtain(float x, float y, float angle, float speed, int color, GameView.ParticleType type) {
        if (pool.isEmpty()) {
            return gameView.new Particle(x, y, angle, speed, color, type);
        }
        GameView.Particle p = pool.pop();
        p.reset(x, y, angle, speed, color, type);
        return p;
    }

    /**
     * Return particle to pool for reuse
     */
    public void free(GameView.Particle particle) {
        if (particle != null && pool.size() < 200) { // Max pool size to prevent memory leak
            pool.push(particle);
        }
    }

    /**
     * Clear the pool (e.g., on level restart)
     */
    public void clear() {
        pool.clear();
    }
}
