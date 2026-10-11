package btw.community.chronicboy.controller.input;

import api.AddonHandler;
import btw.community.chronicboy.controller.config.ControllerConfig;
import btw.community.chronicboy.controller.gui.VirtualCursor;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.util.Map;

public class ControllerHandler {
    private static final ControllerHandler INSTANCE = new ControllerHandler();

    private IControllerBackend primaryBackend;
    private final VirtualKeyboardBackend virtualBackend = new VirtualKeyboardBackend();

    private final ControllerSnapshot currentSnapshot = new ControllerSnapshot();
    private final ControllerSnapshot previousSnapshot = new ControllerSnapshot();

    private boolean wasF12Down = false;
    private long lastFrameTime = System.nanoTime();

    private boolean configLoaded = false;
    private boolean backendInitialized = false;
    private int initDelayTicks = 0;
    private static final int INIT_DELAY = 20; // Wait 20 ticks (~1 second) for Display to fully stabilize

    // Bumper hold-repeat for hotbar scrolling
    private long bumperHeldSince = 0;
    private long bumperLastRepeat = 0;
    private boolean bumperRepeating = false;
    private static final long BUMPER_INITIAL_DELAY = 300; // ms before first repeat
    private static final long BUMPER_REPEAT_DELAY = 100;  // ms between repeats

    private ControllerHandler() {}

    public static ControllerHandler getInstance() {
        return INSTANCE;
    }

    public void init() {
        // Only load config during startGame() — defer heavy SDL2 backend init to update()
        if (!configLoaded) {
            ControllerConfig.load();
            configLoaded = true;
        }

        virtualBackend.init();
        if (ControllerConfig.debugVirtualController) {
            virtualBackend.setActive(true);
        }

        AddonHandler.logMessage("Controller CE initialized (backend will start after display is ready).");
    }

    private void initBackend() {
        if (backendInitialized) return;
        backendInitialized = true;

        // Try Jamepad SDL2 backend first
        JamepadBackend jamepad = new JamepadBackend();
        if (jamepad.init()) {
            primaryBackend = jamepad;
        } else {
            // Fallback to LWJGL backend
            LwjglBackend lwjgl = new LwjglBackend();
            if (lwjgl.init()) {
                primaryBackend = lwjgl;
            }
        }

        AddonHandler.logMessage("Controller CE backend started: " + (primaryBackend != null ? primaryBackend.getName() : "None"));
    }

    public void update() {
        // Skip controller processing if LWJGL Display isn't fully created yet
        if (!Display.isCreated()) return;

        // Defer backend initialization until after the Display has been active for a few ticks
        // This prevents SDL2's event subsystem from fighting with LWJGL2's window message pump
        if (!backendInitialized) {
            if (!Display.isActive()) {
                initDelayTicks = 0; // Reset counter if window isn't focused yet
                return;
            }
            initDelayTicks++;
            if (initDelayTicks < INIT_DELAY) return;
            initBackend();
        }

        // Toggle Virtual Controller Test Mode with F12
        boolean f12Down = Keyboard.isKeyDown(Keyboard.KEY_F12);
        if (f12Down && !wasF12Down) {
            virtualBackend.toggle();
            AddonHandler.logMessage("Controller CE: Virtual test mode is now " + (virtualBackend.isActive() ? "ACTIVE (Arrow Keys/Numpad)" : "OFF"));
        }
        wasF12Down = f12Down;

        // CRITICAL: Copy current state into previous snapshot before updating from backend!
        previousSnapshot.copyFrom(currentSnapshot);

        // Poll backend
        if (virtualBackend.isActive()) {
            virtualBackend.update(currentSnapshot, previousSnapshot);
        } else if (primaryBackend != null) {
            primaryBackend.update(currentSnapshot, previousSnapshot);
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        // Handle GUI interactions when a menu is open
        if (mc.currentScreen != null) {
            if (ControllerConfig.enableVirtualCursor && currentSnapshot.isConnected) {
                VirtualCursor.update(mc.currentScreen, currentSnapshot);
            }
            return;
        }

        // Gameplay actions when in-game
        if (mc.thePlayer != null && mc.inGameHasFocus && currentSnapshot.isConnected) {
            handleGameplayActions(mc);
        }
    }

    public boolean isActionPressed(ControllerAction action) {
        if (!currentSnapshot.isConnected) return false;
        for (Map.Entry<ControllerButton, ControllerAction> entry : ControllerConfig.buttonBindings.entrySet()) {
            if (entry.getValue() == action) {
                ControllerButton btn = entry.getKey();
                if (btn == ControllerButton.LT) {
                    if (currentSnapshot.leftTrigger >= 0.35f) return true;
                } else if (btn == ControllerButton.RT) {
                    if (currentSnapshot.rightTrigger >= 0.35f) return true;
                } else if (currentSnapshot.isPressed(btn)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isActionJustPressed(ControllerAction action) {
        if (!currentSnapshot.isConnected) return false;
        for (Map.Entry<ControllerButton, ControllerAction> entry : ControllerConfig.buttonBindings.entrySet()) {
            if (entry.getValue() == action) {
                ControllerButton btn = entry.getKey();
                if (btn == ControllerButton.LT) {
                    if (currentSnapshot.leftTrigger >= 0.35f && previousSnapshot.leftTrigger < 0.35f) return true;
                } else if (btn == ControllerButton.RT) {
                    if (currentSnapshot.rightTrigger >= 0.35f && previousSnapshot.rightTrigger < 0.35f) return true;
                } else if (currentSnapshot.isJustPressed(btn)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isActionJustReleased(ControllerAction action) {
        if (!currentSnapshot.isConnected) return false;
        for (Map.Entry<ControllerButton, ControllerAction> entry : ControllerConfig.buttonBindings.entrySet()) {
            if (entry.getValue() == action) {
                ControllerButton btn = entry.getKey();
                if (btn == ControllerButton.LT) {
                    if (currentSnapshot.leftTrigger < 0.35f && previousSnapshot.leftTrigger >= 0.35f) return true;
                } else if (btn == ControllerButton.RT) {
                    if (currentSnapshot.rightTrigger < 0.35f && previousSnapshot.rightTrigger >= 0.35f) return true;
                } else if (currentSnapshot.isJustReleased(btn)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleGameplayActions(Minecraft mc) {
        // Attack / Mine
        if (isActionJustPressed(ControllerAction.ATTACK)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.keyCode, true);
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode);
        } else if (isActionPressed(ControllerAction.ATTACK)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.keyCode, true);
        } else if (isActionJustReleased(ControllerAction.ATTACK)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.keyCode, false);
        }

        // Use Item / Place Block
        if (isActionJustPressed(ControllerAction.USE)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true);
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode);
        } else if (isActionPressed(ControllerAction.USE)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, true);
        } else if (isActionJustReleased(ControllerAction.USE)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.keyCode, false);
        }

        // Hotbar cycling with hold-repeat
        boolean lbDown = isActionPressed(ControllerAction.HOTBAR_PREV);
        boolean rbDown = isActionPressed(ControllerAction.HOTBAR_NEXT);

        if (lbDown) {
            if (isActionJustPressed(ControllerAction.HOTBAR_PREV)) {
                mc.thePlayer.inventory.changeCurrentItem(1);
                bumperHeldSince = System.currentTimeMillis();
                bumperRepeating = false;
            } else {
                long held = System.currentTimeMillis() - bumperHeldSince;
                if (!bumperRepeating && held >= BUMPER_INITIAL_DELAY) {
                    bumperRepeating = true;
                    bumperLastRepeat = System.currentTimeMillis();
                    mc.thePlayer.inventory.changeCurrentItem(1);
                } else if (bumperRepeating && System.currentTimeMillis() - bumperLastRepeat >= BUMPER_REPEAT_DELAY) {
                    bumperLastRepeat = System.currentTimeMillis();
                    mc.thePlayer.inventory.changeCurrentItem(1);
                }
            }
        }
        if (rbDown) {
            if (isActionJustPressed(ControllerAction.HOTBAR_NEXT)) {
                mc.thePlayer.inventory.changeCurrentItem(-1);
                bumperHeldSince = System.currentTimeMillis();
                bumperRepeating = false;
            } else {
                long held = System.currentTimeMillis() - bumperHeldSince;
                if (!bumperRepeating && held >= BUMPER_INITIAL_DELAY) {
                    bumperRepeating = true;
                    bumperLastRepeat = System.currentTimeMillis();
                    mc.thePlayer.inventory.changeCurrentItem(-1);
                } else if (bumperRepeating && System.currentTimeMillis() - bumperLastRepeat >= BUMPER_REPEAT_DELAY) {
                    bumperLastRepeat = System.currentTimeMillis();
                    mc.thePlayer.inventory.changeCurrentItem(-1);
                }
            }
        }

        // Inventory
        if (isActionJustPressed(ControllerAction.INVENTORY)) {
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
        }

        // Pause Menu
        if (isActionJustPressed(ControllerAction.PAUSE)) {
            mc.displayInGameMenu();
        }

        // Camera Perspective / F5
        if (isActionJustPressed(ControllerAction.PERSPECTIVE)) {
            mc.gameSettings.thirdPersonView = (mc.gameSettings.thirdPersonView + 1) % 3;
        }

        // Drop Item
        if (isActionJustPressed(ControllerAction.DROP)) {
            KeyBinding.onTick(mc.gameSettings.keyBindDrop.keyCode);
        }

        // Sprint Toggle
        if (isActionJustPressed(ControllerAction.SPRINT)) {
            if (mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(false);
            } else if (!mc.thePlayer.isSneaking() && currentSnapshot.leftStickY > 0.1f) {
                mc.thePlayer.setSprinting(true);
            }
        }

        // Pick Block
        if (isActionJustPressed(ControllerAction.PICK_BLOCK)) {
            KeyBinding.onTick(mc.gameSettings.keyBindPickBlock.keyCode);
        }

        // Chat
        if (isActionJustPressed(ControllerAction.CHAT)) {
            mc.displayGuiScreen(new GuiChat());
        }
    }

    public void applyAnalogCamera(EntityPlayer player) {
        if (player == null || !currentSnapshot.isConnected) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;

        long now = System.nanoTime();
        float delta = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        if (delta > 0.1f) delta = 0.1f;

        float rx = currentSnapshot.rightStickX;
        float ry = currentSnapshot.rightStickY; // +1.0 is Look UP, -1.0 is Look DOWN

        if (Math.abs(rx) > 0.001f || Math.abs(ry) > 0.001f) {
            // Controlify-style exponential curve for fine aiming and smooth turning
            float curve = ControllerConfig.cameraCurve;
            // Compensate for Entity.setAngles(yaw, pitch) 0.15D multiplier:
            // 360 degrees/second base speed at sensitivity 1.0
            float sens = (ControllerConfig.cameraSensitivity * 360.0f) / 0.15f;

            float yawVel = Math.signum(rx) * (float) Math.pow(Math.abs(rx), curve) * sens * delta;
            float pitchVel = Math.signum(ry) * (float) Math.pow(Math.abs(ry), curve) * sens * delta;

            if (ControllerConfig.invertY) {
                pitchVel = -pitchVel;
            }

            // Entity.setAngles(yaw, pitch) adds yaw*0.15 to yaw and SUBTRACTS pitch*0.15 from pitch!
            // Passing positive pitchVel subtracts pitch, looking UP as intended!
            player.setAngles(yawVel, pitchVel);
        }
    }

    public ControllerSnapshot getCurrentSnapshot() {
        return currentSnapshot;
    }

    public void cleanup() {
        if (primaryBackend != null) {
            primaryBackend.cleanup();
        }
        virtualBackend.cleanup();
    }
}
