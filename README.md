# Controller CE - Better Than Wolves Community Edition

[![Platform](https://img.shields.io/badge/Minecraft-1.6.4-green.svg)](https://fabricmc.net/)
[![Mod Version](https://img.shields.io/badge/BTW%20CE-3.0.0+-blue.svg)](https://github.com/btw-community)
[![License](https://img.shields.io/badge/License-CC0--1.0-lightgrey.svg)](LICENSE)

**Controller CE** brings comprehensive, console-authentic gamepad support to **Better Than Wolves CE 3.0.0+** (Minecraft 1.6.4). 

Engineered with dual-backend architecture combining **Jamepad (SDL 2.30)** for desktop PC plug-and-play compatibility and **LWJGL 2** fallback for PojavLauncher (Android).

---

## ✨ Key Features

- **True Analog Input**: Full 360° analog player movement with deadzone filtering, smooth speed scaling, and stick click sprint.
- **Console-Grade Camera Response**: Analog camera look with configurable exponential response curve and customizable sensitivity.
- **Console Edition Virtual Cursor**:
  - Pixel-perfect Xbox Console Edition crosshair reticle with rounded edges in container screens.
  - Option to switch between authentic **Xbox Crosshair** and clean **Circular Reticle**.
  - **Magnetic Slot Snapping**: Sticks roam freely across menus, buttons, and EMI recipe panels, snapping cleanly to the nearest inventory slot when released.
  - **D-Pad Grid Navigation**: D-pad steps cleanly slot-by-slot through inventories and containers.
  - **Snappy Sensitivity**: Fast, agile pointer navigation calibrated for modern displays.
- **Escape / Back Navigation**: Pressing **B / Circle (O)** seamlessly acts like Escape, navigating back to previous menus, closing dialogs, or exiting inventories.
- **Hold-Repeat Hotbar Cycling**: Holding Left or Right Bumper (**LB / RB**) smoothly scrolls through hotbar slots without needing to mash buttons.
- **Full EMI Recipe Viewer Compatibility**: Click search bars, favorite items, and browse recipes directly using your controller.
- **In-Game Settings Menu**: Seamlessly accessible via `Options -> Controller...` with sliders for Sensitivity, Deadzone, Aim Curve, Cursor Speed, and toggles for Invert Look Y, Sneak Mode, and Hardware Cursor Suppression.
- **Virtual Controller Test Mode**: Press **F12** at any time to simulate a controller using keyboard inputs.

---

## 🎮 Default Controller Layout

| Gamepad Button | In-Game Action | Menu / GUI Action |
|---|---|---|
| **Left Stick** | Analog Movement (Walk / Run) | Free Virtual Cursor Movement |
| **Right Stick** | Analog Camera Look | — |
| **Right Trigger (RT / R2)** | Mine / Attack | — |
| **Left Trigger (LT / L2)** | Place Block / Use Item | — |
| **Left Bumper (LB / L1)** | Cycle Hotbar Left (Hold to scroll) | Prev Tab / Drop Stack Modifier |
| **Right Bumper (RB / R1)** | Cycle Hotbar Right (Hold to scroll) | Next Tab |
| **A / Cross (✕)** | Jump (Ascend while flying) | Left Click / Select / Pick up Stack |
| **B / Circle (○)** | Drop Held Item | **Back / Escape** (Previous Menu / Close) |
| **X / Square (□)** | Open Inventory | Right Click / Split Stack / Place Single |
| **Y / Triangle (△)** | BTW Special Action / Sprint | Quick Move (Shift-Click / Take 64) |
| **Left Stick Click (L3)** | Sprint Toggle | — |
| **Right Stick Click (R3)** | Crouch / Sneak & **Descend while flying** | — |
| **Start / Options** | Pause Menu | Close Menu / Back (Escape) |
| **Back / Select** | Cycle Camera Perspective (F5) | — |
| **D-Pad (Up/Down/Left/Right)** | Hotbar Shortcuts / Drop | Slot-to-Slot Navigation / Nudge |

---

## ⚙️ In-Game Configuration

Access the controller menu via **Esc ➔ Options ➔ Controller...**:

- **Sensitivity Slider**: 20% to 300% (Default: 100%).
- **Stick Deadzone Slider**: 5% to 40% (Default: 15%).
- **Aim Curve Slider**: 1.0 (Linear) to 2.5 (Exponential, Default: 1.6).
- **Cursor Speed Slider**: 6.0 to 30.0 (Default: 14.0).
- **Invert Look Y**: Toggle `ON` / `OFF`.
- **Drop with Circle (O)**: Toggle `YES` / `D-Pad Only`.
- **Crouch (R3)**: Toggle `HOLD` / `TOGGLE`.
- **Hide Windows Cursor**: Toggle `YES` / `NO` (Suppresses Windows desktop mouse cursor while using gamepad).
- **Cursor Style**: Toggle between `Xbox` crosshair and `Circle` reticle.

Configuration is automatically saved to `config/controller.properties`.

---

## 🕹️ Supported Gamepads Out of the Box

- **Xbox:** Xbox 360, Xbox One, Xbox Series X/S, Xbox Elite.
- **PlayStation:** DualShock 3, DualShock 4, DualSense (PS5).
- **Nintendo:** Switch Pro Controller, Joy-Cons.
- **PC & Third-Party:** 8BitDo, Logitech (F310/F710), Steam Deck, generic XInput and DirectInput controllers.
- **Android / PojavLauncher:** Automatic fallback to LWJGL 2 controller backend.

---

## 🧪 Virtual Controller Mode (Test Without Gamepad)

Press **F12** in-game at any time to toggle testing mode:
- **Arrow Keys:** Left Stick (Movement / Menu Cursor)
- **I / J / K / L:** Right Stick (Camera Look)
- **Numpad 1 / Enter:** A (Jump / Select)
- **Numpad 2 / Backspace:** B (Drop / Back)
- **Numpad 3 / R:** X (Inventory / Split)
- **Numpad 4 / F:** Y (Special / Shift-Click)
- **Q / E:** Bumpers LB / RB (Cycle Hotbar)
- **U / O:** Triggers LT / RT (Use / Attack)
- **Z:** R3 (Crouch / Descend)

---

## 📦 Installation

1. Ensure **Better Than Wolves CE 3.0.0+** with Fabric Loader 0.14.19+ is installed.
2. Download `controller-ce-<version>.jar` from GitHub Releases or Modrinth.
3. Place the `.jar` into your Minecraft `.minecraft/mods` directory.
4. Plug in your controller and launch the game!

---

## 🛠️ Building from Source

Prerequisites: Java 17 (Adoptium recommended).

Clone the repository and build:
```bash
git clone https://github.com/chronicboy/controller-ce.git
cd controller-ce
./gradlew build
```
The compiled mod JAR will be located in `build/libs/controller-ce-1.0.0.jar`.
