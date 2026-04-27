package com.example.submarine;

import com.badlogic.gdx.math.Rectangle;

public class Treasure {
    public float x;
    public float y;
    public float width;
    public float height;
    public boolean collected;

    public Treasure(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.collected = false;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
