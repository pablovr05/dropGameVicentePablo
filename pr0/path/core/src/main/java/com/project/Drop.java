package com.project;

public class Drop {
    private float x, y, speed, width, height;

    public Drop(float x, float y, float speed, float width, float height) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.width = width;
        this.height = height;
    }

    public void update() {
        y -= speed; // Mueve la gota hacia abajo
    }

    public boolean collidesWith(Collector collector) {
        return x < collector.getX() + collector.getWidth() &&
               x + width > collector.getX() &&
               y < collector.getY() + collector.getHeight() &&
               y + height > collector.getY();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
