package btw.community.chronicboy.controller.input;

import org.lwjgl.input.Keyboard;

import java.util.EnumSet;
import java.util.Set;

public class VirtualKeyboardBackend implements IControllerBackend {
    private boolean active = false;

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        this.active = !this.active;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void update(ControllerSnapshot snapshot, ControllerSnapshot prev) {
        if (!active) {
            snapshot.updateFrom(prev, 0, 0, 0, 0, 0, 0, EnumSet.noneOf(ControllerButton.class), false, "None");
            return;
        }

        float lx = 0;
        float ly = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) lx -= 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) lx += 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) ly += 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) ly -= 1.0f;

        float rx = 0;
        float ry = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_J)) rx -= 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_L)) rx += 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) ry += 1.0f;
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) ry -= 1.0f;

        float lt = Keyboard.isKeyDown(Keyboard.KEY_U) ? 1.0f : 0.0f;
        float rt = Keyboard.isKeyDown(Keyboard.KEY_O) ? 1.0f : 0.0f;

        Set<ControllerButton> btns = EnumSet.noneOf(ControllerButton.class);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1) || Keyboard.isKeyDown(Keyboard.KEY_RETURN)) btns.add(ControllerButton.A);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2) || Keyboard.isKeyDown(Keyboard.KEY_BACK)) btns.add(ControllerButton.B);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD3) || Keyboard.isKeyDown(Keyboard.KEY_R)) btns.add(ControllerButton.X);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4) || Keyboard.isKeyDown(Keyboard.KEY_F)) btns.add(ControllerButton.Y);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD7) || Keyboard.isKeyDown(Keyboard.KEY_Q)) btns.add(ControllerButton.LB);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD9) || Keyboard.isKeyDown(Keyboard.KEY_E)) btns.add(ControllerButton.RB);
        if (Keyboard.isKeyDown(Keyboard.KEY_P)) btns.add(ControllerButton.START);
        if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) btns.add(ControllerButton.BACK);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) btns.add(ControllerButton.L3);
        if (Keyboard.isKeyDown(Keyboard.KEY_Z)) btns.add(ControllerButton.R3);

        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD8)) btns.add(ControllerButton.DPAD_UP);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD5)) btns.add(ControllerButton.DPAD_DOWN);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4)) btns.add(ControllerButton.DPAD_LEFT);
        if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD6)) btns.add(ControllerButton.DPAD_RIGHT);

        snapshot.updateFrom(prev, lx, ly, rx, ry, lt, rt, btns, true, "Virtual Keyboard Pad");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return "Virtual Pad (Debug F12)";
    }

    @Override
    public void cleanup() {
        active = false;
    }
}
