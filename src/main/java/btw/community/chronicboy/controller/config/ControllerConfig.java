package btw.community.chronicboy.controller.config;

import btw.community.chronicboy.controller.input.ControllerAction;
import btw.community.chronicboy.controller.input.ControllerButton;

import java.io.*;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class ControllerConfig {
    private static final File CONFIG_FILE = new File("config/controller.properties");

    public static float deadzone = 0.15f;
    public static float cameraSensitivity = 1.0f;
    public static float cameraCurve = 1.6f;
    public static boolean invertY = false;
    public static float virtualCursorSpeed = 14.0f;
    public static boolean enableVirtualCursor = true;
    public static boolean dropWithCircle = true;
    public static boolean toggleSneak = false;
    public static boolean hideHardwareCursor = true;
    public static boolean debugVirtualController = false;

    // Visual styles
    public static String cursorStyle = "Xbox"; // "Xbox", "Circle"
    public static String controllerStyle = "Xbox"; // "Xbox", "PlayStation", "Switch"

    // Custom button bindings
    public static final Map<ControllerButton, ControllerAction> buttonBindings = new EnumMap<>(ControllerButton.class);

    static {
        resetBindings();
    }

    public static void resetBindings() {
        buttonBindings.clear();
        buttonBindings.put(ControllerButton.A, ControllerAction.JUMP);
        buttonBindings.put(ControllerButton.B, ControllerAction.DROP);
        buttonBindings.put(ControllerButton.X, ControllerAction.INVENTORY);
        buttonBindings.put(ControllerButton.Y, ControllerAction.SPECIAL);
        buttonBindings.put(ControllerButton.LB, ControllerAction.HOTBAR_PREV);
        buttonBindings.put(ControllerButton.RB, ControllerAction.HOTBAR_NEXT);
        buttonBindings.put(ControllerButton.LT, ControllerAction.USE);
        buttonBindings.put(ControllerButton.RT, ControllerAction.ATTACK);
        buttonBindings.put(ControllerButton.L3, ControllerAction.SPRINT);
        buttonBindings.put(ControllerButton.R3, ControllerAction.SNEAK);
        buttonBindings.put(ControllerButton.START, ControllerAction.PAUSE);
        buttonBindings.put(ControllerButton.BACK, ControllerAction.PERSPECTIVE);
        buttonBindings.put(ControllerButton.DPAD_UP, ControllerAction.PICK_BLOCK);
        buttonBindings.put(ControllerButton.DPAD_DOWN, ControllerAction.DROP);
        buttonBindings.put(ControllerButton.DPAD_LEFT, ControllerAction.PLAYER_LIST);
        buttonBindings.put(ControllerButton.DPAD_RIGHT, ControllerAction.CHAT);
        buttonBindings.put(ControllerButton.GUIDE, ControllerAction.NONE);
    }

    public static ControllerAction getBinding(ControllerButton button) {
        return buttonBindings.getOrDefault(button, ControllerAction.NONE);
    }

    public static void setBinding(ControllerButton button, ControllerAction action) {
        buttonBindings.put(button, action);
    }

    public static void load() {
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }

        if (CONFIG_FILE.exists()) {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG_FILE.toPath())) {
                props.load(in);
                deadzone = Float.parseFloat(props.getProperty("deadzone", "0.15"));
                cameraSensitivity = Float.parseFloat(props.getProperty("cameraSensitivity", "1.0"));
                cameraCurve = Float.parseFloat(props.getProperty("cameraCurve", "1.6"));
                invertY = Boolean.parseBoolean(props.getProperty("invertY", "false"));
                virtualCursorSpeed = Float.parseFloat(props.getProperty("virtualCursorSpeed", "14.0"));
                enableVirtualCursor = Boolean.parseBoolean(props.getProperty("enableVirtualCursor", "true"));
                dropWithCircle = Boolean.parseBoolean(props.getProperty("dropWithCircle", "true"));
                toggleSneak = Boolean.parseBoolean(props.getProperty("toggleSneak", "false"));
                hideHardwareCursor = Boolean.parseBoolean(props.getProperty("hideHardwareCursor", "true"));
                debugVirtualController = Boolean.parseBoolean(props.getProperty("debugVirtualController", "false"));
                cursorStyle = props.getProperty("cursorStyle", "Xbox");
                controllerStyle = props.getProperty("controllerStyle", "Xbox");

                // Load custom button bindings
                for (ControllerButton btn : ControllerButton.values()) {
                    String propKey = "bind." + btn.name();
                    if (props.containsKey(propKey)) {
                        try {
                            ControllerAction action = ControllerAction.valueOf(props.getProperty(propKey));
                            buttonBindings.put(btn, action);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("deadzone", String.valueOf(deadzone));
        props.setProperty("cameraSensitivity", String.valueOf(cameraSensitivity));
        props.setProperty("cameraCurve", String.valueOf(cameraCurve));
        props.setProperty("invertY", String.valueOf(invertY));
        props.setProperty("virtualCursorSpeed", String.valueOf(virtualCursorSpeed));
        props.setProperty("enableVirtualCursor", String.valueOf(enableVirtualCursor));
        props.setProperty("dropWithCircle", String.valueOf(dropWithCircle));
        props.setProperty("toggleSneak", String.valueOf(toggleSneak));
        props.setProperty("hideHardwareCursor", String.valueOf(hideHardwareCursor));
        props.setProperty("debugVirtualController", String.valueOf(debugVirtualController));
        props.setProperty("cursorStyle", cursorStyle);
        props.setProperty("controllerStyle", controllerStyle);

        // Save bindings
        for (Map.Entry<ControllerButton, ControllerAction> entry : buttonBindings.entrySet()) {
            props.setProperty("bind." + entry.getKey().name(), entry.getValue().name());
        }

        try (OutputStream out = Files.newOutputStream(CONFIG_FILE.toPath())) {
            props.store(out, "Controller CE Configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetDefaults() {
        deadzone = 0.15f;
        cameraSensitivity = 1.0f;
        cameraCurve = 1.6f;
        invertY = false;
        virtualCursorSpeed = 14.0f;
        enableVirtualCursor = true;
        dropWithCircle = true;
        toggleSneak = false;
        hideHardwareCursor = true;
        cursorStyle = "Xbox";
        controllerStyle = "Xbox";
        resetBindings();
        save();
    }
}
