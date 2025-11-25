/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aldiscompany.javafxproject;

/**
 *
 * @author 3007
 */

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * Represents an autonomous agent that wanders the canvas organically.
 * Handles its own physics updates and rendering logic.
 */
public class Fish {
    private double x, y;
    private double vx, vy;
    private double speed = 3.0;
    
    // Determines how "twitchy" the movement is. Lower = smoother arcs.
    private double maxTurn = 0.2; 
    
    private double worldWidth, worldHeight;
    private Image fishImage;
    private Random random = new Random();

    /**
     * @param image Shared image resource to prevent reloading from disk for every fish.
     */
    public Fish(double startX, double startY, Image image, double worldWidth, double worldHeight) {
        this.x = startX;
        this.y = startY;
        this.fishImage = image;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        // Initialize with a random direction vector
        double angle = random.nextDouble() * Math.PI * 2;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    /**
     * Updates position using a "wander" steering behavior.
     */
    public void update() {
        // 1. Add a small random force to the current velocity vector
        vx += (random.nextDouble() - 0.5) * maxTurn;
        vy += (random.nextDouble() - 0.5) * maxTurn;

        // 2. Normalize velocity back to constant speed
        // This ensures the fish changes direction without speeding up or slowing down
        double currentSpeed = Math.sqrt(vx*vx + vy*vy);
        if (currentSpeed != 0) {
            vx = (vx / currentSpeed) * speed;
            vy = (vy / currentSpeed) * speed;
        }

        // 3. Update position
        x += vx;
        y += vy;

        // 4. Soft Boundary Check (push back if hitting edges)
        if (x < 50) vx += maxTurn;
        if (x > worldWidth - 50) vx -= maxTurn;
        if (y < 50) vy += maxTurn;
        if (y > worldHeight - 50) vy -= maxTurn;
    }

    /**
     * Renders the fish rotated in the direction of travel.
     */
    public void draw(GraphicsContext gc) {
        gc.save(); // Save current transform state
        
        // Move canvas origin to fish position
        gc.translate(x, y);

        // Calculate rotation angle based on velocity vector
        double angleRad = Math.atan2(vy, vx);
        double angleDeg = Math.toDegrees(angleRad);
        
        gc.rotate(angleDeg);

        if (fishImage != null) {
            // Draw image centered (Assumes source image faces RIGHT)
                double scale = 0.08; // Try 0.15 (15% of original size)
        double w = fishImage.getWidth() * scale;
        double h = fishImage.getHeight() * scale;

        // OR: Force a specific pixel size (e.g., 60 pixels wide)
        // double scale = 60 / fishImage.getWidth();
        // double w = 60;
        // double h = fishImage.getHeight() * scale;
            gc.drawImage(fishImage, w/2, h/2, -w, -h);
        } else {
            // Fallback geometric shape if image fails to load
            gc.setFill(Color.ORANGE);
            gc.fillOval(-15, -10, 30, 20);
            gc.setFill(Color.BLACK);
            gc.fillOval(5, -5, 5, 5); 
        }

        gc.restore(); // Restore state so other objects don't rotate
    }

    public double getX() { return x; }
    public double getY() { return y; }
}

