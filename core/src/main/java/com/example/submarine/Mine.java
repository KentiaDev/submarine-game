package com.example.submarine;

import com.badlogic.gdx.math.Circle;

public class Mine {
    public float x, y, width, height;
    public float startY, endY, speed;
    public boolean movingUp;
    public boolean active;
    public boolean exploded;

    public float proximityRadius;
    public float explosionRadius;

    public float activationTime = 0f;
    public float explosionTimer = 0f;

    public Mine(float x, float y, float width, float height,
                float endY, float speed, float proximityRadius, float explosionRadius) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.startY = y;
        this.endY = endY;
        this.speed = speed;
        this.movingUp = true;

        this.active = false;
        this.exploded = false;

        this.proximityRadius = proximityRadius;
        this.explosionRadius = explosionRadius;
    }

    public Circle getProximityCircle() {
        return new Circle(x + width / 2f, y + height / 2f, proximityRadius);
    }

    public Circle getExplosionCircle() {
        return new Circle(x + width / 2f, y + height / 2f, explosionRadius);
    }
}
