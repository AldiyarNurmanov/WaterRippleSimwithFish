/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.aldiscompany.javafxproject;
 


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Interactive Water Simulation with Autonomous Agents.
 * 
 * Implements the Hugo Elias algorithm for fluid propagation.
 * Renders the effect by manipulating pixel brightness on a background image.
 */
public class JavaFXproject extends Application {

    // --- CONFIGURATION ---
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    
    // Performance Optimization: Calculate physics at half-resolution (SCALE=2)
    // This reduces CPU load by 75% while maintaining visual quality.
    private static final int SCALE = 2;

    private static final int GRID_WIDTH = WIDTH / SCALE;
    private static final int GRID_HEIGHT = HEIGHT / SCALE;

    // Double buffering for the ripple physics (Previous Frame -> Current Frame)
    private double[][] current = new double[GRID_WIDTH][GRID_HEIGHT];
    private double[][] previous = new double[GRID_WIDTH][GRID_HEIGHT];

    // Physics parameters
    private double damping = 0.96;       // Viscosity (how fast waves die out)
    private double rippleStrength = 40;  // Height of the splash

    // JavaFX Components
    private Canvas canvas;
    private GraphicsContext gc;
    private WritableImage frame;         // The buffer we modify manually
    private PixelWriter frameWriter;

    // Assets & Logic
    private List<Image> backgrounds = new ArrayList<>();
    private int bgIndex = 0;
    private List<Fish> fishSchool = new ArrayList<>();
    private Image sharedFishImage; 
    private Random rng = new Random();

    @Override
    public void start(Stage stage) throws Exception {

        loadAssets();

        // Init Canvas
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        frame = new WritableImage(WIDTH, HEIGHT);
        frameWriter = frame.getPixelWriter();

        // Start with 1 fish
        updateFishCount(1);

        // --- INPUT HANDLERS ---
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                e -> disturb(e.getX(), e.getY(), rippleStrength));
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                e -> disturb(e.getX(), e.getY(), rippleStrength * 0.5));

        // --- GUI CONTROLS ---
        Slider dampingSlider = new Slider(0.90, 1.0, damping);
        dampingSlider.valueProperty().addListener((obs, oldV, newV) -> damping = newV.doubleValue());
        VBox dampingBox = new VBox(new Label("Viscosity"), dampingSlider);

        Slider strengthSlider = new Slider(10, 100, rippleStrength);
        strengthSlider.valueProperty().addListener((obs, oldV, newV) -> rippleStrength = newV.doubleValue());
        VBox strengthBox = new VBox(new Label("Strength"), strengthSlider);

        Slider fishCountSlider = new Slider(1, 5, 1);
        fishCountSlider.setShowTickLabels(true);
        fishCountSlider.setShowTickMarks(true);
        fishCountSlider.setMajorTickUnit(1);
        fishCountSlider.setMinorTickCount(0);
        fishCountSlider.setSnapToTicks(true);
        fishCountSlider.valueProperty().addListener((obs, oldV, newV) -> updateFishCount(newV.intValue()));
        VBox fishBox = new VBox(new Label("Fish Count"), fishCountSlider);

        Button switchBg = new Button("Switch Background");
        switchBg.setOnAction(e -> bgIndex = (bgIndex + 1) % backgrounds.size());
        VBox btnBox = new VBox(new Label(" "), switchBg); 

        HBox controls = new HBox(20, dampingBox, strengthBox, fishBox, btnBox);
        controls.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(controls);

        stage.setScene(new Scene(root));
        stage.setTitle("Water Ripple Simulation");
        stage.show();

        // --- MAIN GAME LOOP ---
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 1. Update Fish & Trigger Trails
                for (Fish f : fishSchool) {
                    f.update();
                    // Trigger a small ripple at fish location
                    disturb(f.getX(), f.getY(), rippleStrength * 0.3);
                }

                // 2. Physics Calculation
                updateRipples();
                
                // 3. Render Water
                drawFrame();
                
                // 4. Render Fish on top
                for (Fish f : fishSchool) {
                    f.draw(gc);
                }
            }
        }.start();
    }

    /**
     * Dynamically adjusts the number of fish in the tank.
     */
    private void updateFishCount(int count) {
        while (fishSchool.size() < count) {
            double startX = WIDTH/2.0 + (rng.nextDouble() - 0.5) * 100;
            double startY = HEIGHT/2.0 + (rng.nextDouble() - 0.5) * 100;
            fishSchool.add(new Fish(startX, startY, sharedFishImage, WIDTH, HEIGHT));
        }
        while (fishSchool.size() > count) {
            fishSchool.remove(fishSchool.size() - 1);
        }
    }

    private void loadAssets() {
        try {
            sharedFishImage = new Image(new FileInputStream("fish.png"));
        } catch (Exception e) {
            System.out.println("fish.png not found. Using fallback shape.");
            sharedFishImage = null;
        }

        try {
            backgrounds.add(new Image(new FileInputStream("background.png")));
            backgrounds.add(new Image(new FileInputStream("background1.png")));
            backgrounds.add(new Image(new FileInputStream("background2.png")));
        } catch (Exception e) {
            System.out.println("Background images not found, using fallback color.");
            WritableImage defaultBg = new WritableImage(WIDTH, HEIGHT);
            PixelWriter pw = defaultBg.getPixelWriter();
            for(int x=0; x<WIDTH; x++) 
                for(int y=0; y<HEIGHT; y++) 
                    pw.setColor(x, y, Color.DARKBLUE);
            backgrounds.add(defaultBg);
        }
    }

    // Creates a ripple by modifying the 'previous' buffer at the given coordinate
    private void disturb(double mx, double my, double strength) {
        int x = (int) (mx / SCALE);
        int y = (int) (my / SCALE);

        if (x > 1 && x < GRID_WIDTH - 1 && y > 1 && y < GRID_HEIGHT - 1) {
            previous[x][y] += strength;
        }
    }

    // Core Ripple Algorithm: Average neighbors, subtract current, apply damping
    private void updateRipples() {
        for (int i = 1; i < GRID_WIDTH - 1; i++) {
            for (int j = 1; j < GRID_HEIGHT - 1; j++) {
                current[i][j] = (
                        previous[i - 1][j] + previous[i + 1][j] +
                        previous[i][j - 1] + previous[i][j + 1]
                ) / 2 - current[i][j];

                current[i][j] *= damping;
            }
        }
        // Swap buffers
        double[][] temp = previous;
        previous = current;
        current = temp;
    }

    // Renders pixels based on physics state + background image
    private void drawFrame() {
        Image bg = backgrounds.get(bgIndex);
        PixelReader pr = bg.getPixelReader();
        int bgW = (int)bg.getWidth();
        int bgH = (int)bg.getHeight();

        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                double v = previous[i][j];
                double brightnessShift = v * 0.03;

                int x = i * SCALE;
                int y = j * SCALE;

                // Clamp to avoid crashes if background is smaller than window
                if (x >= bgW) x = bgW - 1;
                if (y >= bgH) y = bgH - 1;

                Color c = pr.getColor(x, y);

                double r = clamp(c.getRed() + brightnessShift);
                double g = clamp(c.getGreen() + brightnessShift);
                double b = clamp(c.getBlue() + brightnessShift);

                Color out = new Color(r, g, b, 1);

                // Draw Scale x Scale block
                for (int dx = 0; dx < SCALE; dx++) {
                    for (int dy = 0; dy < SCALE; dy++) {
                        frameWriter.setColor(i * SCALE + dx, j * SCALE + dy, out);
                    }
                }
            }
        }
        gc.drawImage(frame, 0, 0);
    }

    private double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    public static void main(String[] args) {
        launch();
    }
}











