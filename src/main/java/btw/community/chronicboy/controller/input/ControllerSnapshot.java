package btw.community.chronicboy.controller.input;

import btw.community.chronicboy.controller.config.ControllerConfig;
import java.util.EnumSet;
import java.util.Set;

public class ControllerSnapshot {
    public float leftStickX;
    public float leftStickY;
    public float rightStickX;
    public float rightStickY;

    public float leftTrigger;
    public float rightTrigger;

    public final Set<ControllerButton> pressedButtons = EnumSet.noneOf(ControllerButton.class);
    public final Set<ControllerButton> justPressedButtons = EnumSet.noneOf(ControllerButton.class);
    public final Set<ControllerButton> justReleasedButtons = EnumSet.noneOf(ControllerButton.class);

    public boolean isConnected;
    public String controllerName = "None";

    public void copyFrom(ControllerSnapshot other) {
        if (other == null) return;
        this.leftStickX = other.leftStickX;
        this.leftStickY = other.leftStickY;
        this.rightStickX = other.rightStickX;
        this.rightStickY = other.rightStickY;
        this.leftTrigger = other.leftTrigger;
        this.rightTrigger = other.rightTrigger;
        this.isConnected = other.isConnected;
        this.controllerName = other.controllerName;

        this.pressedButtons.clear();
        this.pressedButtons.addAll(other.pressedButtons);

        this.justPressedButtons.clear();
        this.justPressedButtons.addAll(other.justPressedButtons);

        this.justReleasedButtons.clear();
        this.justReleasedButtons.addAll(other.justReleasedButtons);
    }

    public void updateFrom(ControllerSnapshot prev, float rawLX, float rawLY, float rawRX, float rawRY,
                           float rawLT, float rawRT, Set<ControllerButton> currentButtons,
                           boolean connected, String name) {
        this.isConnected = connected;
        this.controllerName = name;

        // Apply deadzone filtering
        float dz = ControllerConfig.deadzone;
        this.leftStickX = applyDeadzone(rawLX, dz);
        this.leftStickY = applyDeadzone(rawLY, dz);

        this.rightStickX = applyDeadzone(rawRX, dz);
        this.rightStickY = applyDeadzone(rawRY, dz);

        this.leftTrigger = Math.max(0.0f, Math.min(1.0f, rawLT));
        this.rightTrigger = Math.max(0.0f, Math.min(1.0f, rawRT));

        // Triggers as buttons if pulled past threshold
        if (this.leftTrigger >= 0.35f) {
            currentButtons.add(ControllerButton.LT);
        }
        if (this.rightTrigger >= 0.35f) {
            currentButtons.add(ControllerButton.RT);
        }

        this.pressedButtons.clear();
        this.pressedButtons.addAll(currentButtons);

        this.justPressedButtons.clear();
        this.justReleasedButtons.clear();

        for (ControllerButton btn : ControllerButton.values()) {
            boolean wasDown = prev != null && prev.pressedButtons.contains(btn);
            boolean isDown = this.pressedButtons.contains(btn);

            if (isDown && !wasDown) {
                this.justPressedButtons.add(btn);
            } else if (!isDown && wasDown) {
                this.justReleasedButtons.add(btn);
            }
        }
    }

    private static float applyDeadzone(float value, float deadzone) {
        float abs = Math.abs(value);
        if (abs <= deadzone) {
            return 0.0f;
        }
        // Scaled radial / axial deadzone curve
        float scaled = (abs - deadzone) / (1.0f - deadzone);
        return Math.signum(value) * Math.min(1.0f, scaled);
    }

    public boolean isPressed(ControllerButton button) {
        return pressedButtons.contains(button);
    }

    public boolean isJustPressed(ControllerButton button) {
        return justPressedButtons.contains(button);
    }

    public boolean isJustReleased(ControllerButton button) {
        return justReleasedButtons.contains(button);
    }
}
