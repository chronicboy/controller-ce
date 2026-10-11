## 1.0.0

### Features
* Complete native gamepad controller support for Better Than Wolves CE 3.0.0+ (Minecraft 1.6.4).
* Full dual-backend architecture: Primary Jamepad (SDL 2.30) backend for automatic support of over 1,000 gamepads (PlayStation 3/4/5, Xbox 360/One/Series, Switch Pro, 8BitDo, generic USB) with graceful fallback to LWJGL 2 for PojavLauncher/mobile compatibility.
* True analog player movement with deadzone filtering, smooth speed scaling, and sprint activation.
* Controlify-level analog camera look curve with 360 deg/sec response and fine aiming precision near stick center.
* In-Game Controller Settings GUI (`GuiControllerSettings`) accessible from the Options menu with sliders for Sensitivity (20% to 300%), Stick Deadzone, Aim Curve, and Cursor Speed, plus toggles for Invert Y-Axis, Drop Button, Crouch Mode, and Hide Cursor.
* **Authentic Xbox Console Edition Crosshair Cursor**: Bundled pixel-perfect transparent crosshair sprite (`textures/gui/cursor.png`) accurately reproducing the iconic Xbox 360 / Console Edition Minecraft inventory reticle.
* **Cursor Style Toggle**: Choose between authentic Xbox Crosshair and Modern Circular Reticle directly from Controller Settings.
* **Console-Authentic B / Circle (O) Back Navigation**: Pressing B / Circle in menus and containers triggers `keyTyped((char)27, Keyboard.KEY_ESCAPE)` via `GuiScreenAccessor`, returning to previous screens or closing dialogs instead of hard-closing to game.
* **Accelerated & Responsive Virtual Cursor**: Increased default cursor speed from 9.0 to 14.0 (slider range 6.0 to 30.0) with reduced interpolation lag (`smoothFactor = 0.20`), delivering snappy, agile pointer control across all screens.
* **Continuous Hotbar Scrolling**: Holding Left or Right Bumper (LB / RB / L1 / R1) smoothly and continuously scrolls through the hotbar with an initial hold delay and steady repeat rate (Controllify hold-repeat model).
* **Xbox-style inventory slot navigation**: D-pad moves exactly one slot in the pressed direction with smart wrapping/clamping.
* **Magnetic Slot Snapping**: Left stick roams freely anywhere on screen (allowing clicking buttons, search bars, EMI recipe viewers, or bookmarks outside the inventory grid) and smoothly snaps to the nearest slot when the stick is released near one.
* **Triangle (Y) Quick-Move**: Fast-moves items between inventory sections or grabs a full stack of 64 from Creative inventory (Shift-Click equivalent).
* **Universal GUI Input Forwarding**: Click events (A/X) are routed through `GuiScreen.mouseClicked` and `EmiScreenManager`, ensuring full functionality with external buttons, text fields (EMI search bar), crafting buttons, and modded sidebars.
* Dropping items with Circle ($B$ / O): In-game drops held item; in inventory/containers drops item stack from hovered slot (or full stack with modifier).
* Crouch / Sneak and flying descent with Right Stick Click (R3) with support for both Hold and Toggle modes.
* Pure circular console cursor with automatic Windows hardware cursor suppression (`Mouse.setNativeCursor`) while using the controller.
* Full support for selecting and launching worlds and servers in `GuiSlot` lists via controller Cross button.
* In-game Virtual Controller Test Mode toggleable with F12 (Arrow keys, Numpad, and IJKL) allowing full testing without physical hardware plugged in.
* Configuration file (`config/controller.properties`) for persisting all controller settings.

### Bug Fixes
* Fixed B / Circle button in sub-menus completely dumping player back into the world: now cleanly navigates back to previous screen (Options, Pause, Title, etc.) using `invokeKeyTyped`.
* Streamlined Controller Settings menu by removing redundant button layout screen and centering cursor style controls.
* Fixed interaction with external UI elements (EMI recipe browser, search inputs, favorites, craftables) by integrating direct `EmiScreenManager` click dispatch.
* Refined Xbox crosshair reticle: reduced size to 10x10 GUI pixels with rounded arm tips, rendering in front of all GUI items and depth layers (`z = 300`).
* Added smooth interpolation (lerp) for analog stick cursor movement.
* Configured B / Circle button in container screens to cleanly close the screen without discarding hovered items.
* Fixed hotbar scrolling requiring repeated button taps: bumpers now smoothly repeat when held.
* Fixed D-pad jumping multiple slots or drifting: D-pad in container screens now strictly navigates one slot per press.
* Bundled official `gamecontrollerdb.txt` in the mod jar root, resolving Jamepad's classpath resource warning and supplying hardware mappings for thousands of controllers.
* Fixed camera pitch direction: pushing right stick up now correctly looks up, and pushing down looks down.
* Fixed camera sensitivity scaling by compensating for `Entity.setAngles` internal 0.15D dampening multiplier.
* Fixed cursor warping outside Minecraft: Virtual Cursor now strictly respects `Display.isActive()` and only updates coordinates when controller stick is deflected, allowing physical mouse to be used freely without snapping or fighting.
* Fixed jumping in inventories: `MovementInputMixin` now ignores controller jump/movement while any `GuiScreen` is open.
* Fixed world and server list selection: added `GuiSlotMixin` redirecting `Mouse.isButtonDown(0)` to `VirtualCursor.isLeftDown()`.
* Fixed rapid repeat-click issue on controller buttons by implementing frame-to-frame previous snapshot state preservation and strict edge triggering.
* Fixed window disappearing on startup: disabled SDL2 RawInput window sink (`useRawInput = false`) in Jamepad to prevent focus loss collisions with LWJGL2's window message pump, and called `manager.update()` on every tick.
* Fixed window-not-appearing hang on startup by adding `Display.isCreated()` guards to all LWJGL cursor, mouse, and keyboard operations in `VirtualCursor` and `ControllerHandler`, preventing native calls before the OpenGL display is fully initialized.

### Technical
* Integrated and shaded `com.badlogicgames.jamepad:jamepad:2.30.0.0` with bundled native libraries for Windows, Linux, and macOS.
* Created modular input pipeline: `ControllerHandler`, `IControllerBackend`, `JamepadBackend`, `LwjglBackend`, and `VirtualKeyboardBackend`.
* Implemented `ControllerAction` enum and dynamic binding map serialized to `config/controller.properties`.
* Injected analog movement and dynamic jump/sneak bindings into `MovementInputFromOptions` via `MovementInputMixin`.
* Injected sub-tick analog camera look into `EntityRenderer` via `EntityRendererMixin`.
* Injected tick loop and lifecycle hooks into `Minecraft` via `MinecraftMixin`.
* Configured `shadowJar` and `remapJar` pipeline to produce a self-contained ~3.5MB release jar ready for distribution.
