package btw.community.chronicboy.controller.input;

import api.AddonHandler;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import java.util.EnumSet;
import java.util.Set;

public class LwjglBackend implements IControllerBackend {
    private boolean initialized = false;

    @Override
    public boolean init() {
        try {
            if (!Controllers.isCreated()) {
                Controllers.create();
            }
            initialized = true;
            AddonHandler.logMessage("Controller CE: LWJGL controller fallback initialized successfully.");
            return true;
        } catch (Throwable t) {
            AddonHandler.logMessage("Controller CE: LWJGL controller backend unavailable (" + t.getMessage() + ")");
            initialized = false;
            return false;
        }
    }

    @Override
    public void update(ControllerSnapshot snapshot, ControllerSnapshot prev) {
        if (!initialized || !Controllers.isCreated() || Controllers.getControllerCount() == 0) {
            snapshot.updateFrom(prev, 0, 0, 0, 0, 0, 0, EnumSet.noneOf(ControllerButton.class), false, "None");
            return;
        }

        try {
            Controllers.poll();
            Controller target = null;
            for (int i = 0; i < Controllers.getControllerCount(); i++) {
                Controller c = Controllers.getController(i);
                if (c != null && c.getAxisCount() >= 2) {
                    target = c;
                    break;
                }
            }

            if (target == null) {
                snapshot.updateFrom(prev, 0, 0, 0, 0, 0, 0, EnumSet.noneOf(ControllerButton.class), false, "None");
                return;
            }

            float lx = target.getAxisCount() > 0 ? target.getAxisValue(0) : 0;
            float ly = target.getAxisCount() > 1 ? -target.getAxisValue(1) : 0; // LWJGL Y is often inverted
            float rx = target.getAxisCount() > 2 ? target.getAxisValue(2) : 0;
            float ry = target.getAxisCount() > 3 ? -target.getAxisValue(3) : 0;
            float lt = target.getAxisCount() > 4 ? Math.max(0, target.getAxisValue(4)) : 0;
            float rt = target.getAxisCount() > 5 ? Math.max(0, target.getAxisValue(5)) : 0;

            Set<ControllerButton> btns = EnumSet.noneOf(ControllerButton.class);
            if (target.getButtonCount() > 0 && target.isButtonPressed(0)) btns.add(ControllerButton.A);
            if (target.getButtonCount() > 1 && target.isButtonPressed(1)) btns.add(ControllerButton.B);
            if (target.getButtonCount() > 2 && target.isButtonPressed(2)) btns.add(ControllerButton.X);
            if (target.getButtonCount() > 3 && target.isButtonPressed(3)) btns.add(ControllerButton.Y);
            if (target.getButtonCount() > 4 && target.isButtonPressed(4)) btns.add(ControllerButton.LB);
            if (target.getButtonCount() > 5 && target.isButtonPressed(5)) btns.add(ControllerButton.RB);
            if (target.getButtonCount() > 6 && target.isButtonPressed(6)) btns.add(ControllerButton.BACK);
            if (target.getButtonCount() > 7 && target.isButtonPressed(7)) btns.add(ControllerButton.START);
            if (target.getButtonCount() > 8 && target.isButtonPressed(8)) btns.add(ControllerButton.L3);
            if (target.getButtonCount() > 9 && target.isButtonPressed(9)) btns.add(ControllerButton.R3);

            float povX = target.getPovX();
            float povY = target.getPovY();
            if (povY > 0.5f) btns.add(ControllerButton.DPAD_UP);
            if (povY < -0.5f) btns.add(ControllerButton.DPAD_DOWN);
            if (povX < -0.5f) btns.add(ControllerButton.DPAD_LEFT);
            if (povX > 0.5f) btns.add(ControllerButton.DPAD_RIGHT);

            snapshot.updateFrom(prev, lx, ly, rx, ry, lt, rt, btns, true, target.getName());
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
        return "LWJGL Fallback";
    }

    @Override
    public void cleanup() {
        try {
            if (Controllers.isCreated()) {
                Controllers.destroy();
            }
        } catch (Throwable ignored) {}
        initialized = false;
    }
}
