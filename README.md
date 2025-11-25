# JavaFX Interactive Water Simulation

A real-time fluid physics simulation built in Java. This project demonstrates 2D wave propagation algorithms combined with autonomous agents (fish) that interact with the environment.

## 🌊 Overview

This application simulates the behavior of water using the "Hugo Elias" neighbor-averaging algorithm. It creates a grid of "height" values that ripple outwards when disturbed. The visual effect is achieved by manipulating pixel brightness on a background image to simulate light refraction.

## 🚀 Features

*   **Fluid Physics:** Real-time ripple propagation with adjustable viscosity (damping).
*   **Interactive:** Click or drag the mouse to create splashes and trails.
*   **Autonomous Agents:** Includes a "Fish" class that:
    *   Wanders organically using vector-based steering behaviors.
    *   Automatically triggers ripples in the water as it swims.
    *   Rotates dynamically to face its movement direction.
*   **Dynamic Controls:**
    *   **Viscosity:** Control how thick the water feels.
    *   **Strength:** Control the impact of splashes.
    *   **Fish Count:** Dynamically add or remove fish (1 to 5).
    *   **Background Switcher:** Cycle through different underwater scenes.

## 🛠️ Technical Details

*   **Language:** Java (JDK 11+)
*   **Framework:** JavaFX
*   **Performance:** Uses a resolution scaling factor (`SCALE = 2`) to separate the physics grid resolution from the rendering resolution, ensuring high FPS even on standard hardware.
*   **Buffers:** Uses a double-buffering technique (`current[][]` and `previous[][]`) to calculate wave states.

## 📦 Installation & Setup

1.  **Clone the repository.**
2.  **Add Assets:** Place the following images in the root directory of your project (where the `src` folder is located):
    *   `fish.png` (Ensure the fish faces **right** in the image).
    *   `background1.jpg`
    *   `background2.jpg`
    *   `background3.jpg`
    *(Note: If images are missing, the app will generate fallback shapes/colors so it won't crash).*
3.  **Run:** Execute `WaterRippleApp.java` using your preferred IDE (IntelliJ, Eclipse) or via command line if JavaFX modules are configured.

## 🎮 How to Use

*   **Left Click:** Create a large splash.
*   **Mouse Drag:** Draw waves on the water.
*   **Sliders:** Use the UI at the bottom to adjust simulation parameters in real-time.

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
