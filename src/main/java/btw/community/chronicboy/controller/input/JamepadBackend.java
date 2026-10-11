package btw.community.chronicboy.controller.input;

import api.AddonHandler;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;

import java.util.EnumSet;
import java.util.Set;

public class JamepadBackend implements IControllerBackend {
    private ControllerManager manager;
    private boolean initialized = false;
    private int activeControllerIndex = 0;

    @Override
    public boolean init() {
        try {
            com.studiohartman.jamepad.Configuration config = new com.studiohartman.jamepad.Configuration();
            config.useRawInput = false; // Disable RawInput window hooks on Windows so SDL2 never steals focus from LWJGL2
            config.loadDatabaseInMemory = true;
            manager = new ControllerManager(config);
            manager.initSDLGamepad();
            initialized = true;
            AddonHandler.logMessage("Controller CE: Jamepad SDL2 backend initialized successfully.");
            return true;
        } catch (Throwable t) {
            AddonHandler.logMessage("Controller CE: Failed to initialize Jamepad backend (" + t.getMessage() + "). Falling back to LWJGL.");
            initialized = false;
            return false;
        }
    }

    @Override
    public void update(ControllerSnapshot snapshot, ControllerSnapshot prev) {
        if (!initialized || manager == null) {
            snapshot.updateFrom(prev, 0, 0, 0, 0, 0, 0, EnumSet.noneOf(ControllerButton.class), false, "None");
            return;
        }

        try {
            manager.update();
            int count = manager.getNumControllers();
            ControllerState state = null;

            // Find first connected controller
            for (int i = 0; i < Math.max(1, count); i++) {
                ControllerState s = manager.getState(i);
                if (s.isConnected) {
                    activeControllerIndex = i;
                    state = s;
                    break;
                }
            }

            if (state == null || !state.isConnected) {
                snapshot.updateFrom(prev, 0, 0, 0, 0, 0, 0, EnumSet.noneOf(ControllerButton.class), false, "None");
                return;
            }

            Set<ControllerButton> btns = EnumSet.noneOf(ControllerButton.class);
            if (state.a) btns.add(ControllerButton.A);
            if (state.b) btns.add(ControllerButton.B);
            if (state.x) btns.add(ControllerButton.X);
            if (state.y) btns.add(ControllerButton.Y);
            if (state.lb) btns.add(ControllerButton.LB);
            if (state.rb) btns.add(ControllerButton.RB);
            if (state.start) btns.add(ControllerButton.START);
            if (state.back) btns.add(ControllerButton.BACK);
            if (state.guide) btns.add(ControllerButton.GUIDE);
            if (state.leftStickClick) btns.add(ControllerButton.L3);
            if (state.rightStickClick) btns.add(ControllerButton.R3);
            if (state.dpadUp) btns.add(ControllerButton.DPAD_UP);
            if (state.dpadDown) btns.add(ControllerButton.DPAD_DOWN);
            if (state.dpadLeft) btns.add(ControllerButton.DPAD_LEFT);
            if (state.dpadRight) btns.add(ControllerButton.DPAD_RIGHT);

            String name = state.controllerType != null ? state.controllerType : "Gamepad " + activeControllerIndex;

            // SDL2 axis convention: UP is negative (-1.0), DOWN is positive (+1.0).
            // Invert Y so that UP is +1.0 (Forward/Up) and DOWN is -1.0 (Backward/Down).
            float rawLX = state.leftStickX;
            float rawLY = -state.leftStickY;
            float rawRX = state.rightStickX;
            float rawRY = -state.rightStickY;

            snapshot.updateFrom(prev, rawLX, rawLY,
                    rawRX, rawRY,
                    state.leftTrigger, state.rightTrigger,
                    btns, true, name);

        } catch (Throwable t) {
            snapshot.updateFrom(prev, 0, 0, 0, 0, 0, 0, EnumSet.noneOf(ControllerButton.class), false, "Error");
        }
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

    @Override
    public String getName() {
        return "Jamepad (SDL2)";
    }

    @Override
    public void cleanup() {
        if (manager != null) {
            try {
                manager.quitSDLGamepad();
            } catch (Throwable ignored) {}
            manager = null;
            initialized = false;
        }
    }
}
