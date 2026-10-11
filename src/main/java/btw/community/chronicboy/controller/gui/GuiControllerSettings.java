package btw.community.chronicboy.controller.gui;

import btw.community.chronicboy.controller.config.ControllerConfig;
import btw.community.chronicboy.controller.input.ControllerHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;

@Environment(EnvType.CLIENT)
public class GuiControllerSettings extends GuiScreen {
    private final GuiScreen parentScreen;

    private GuiControllerSlider sensSlider;
    private GuiControllerSlider deadzoneSlider;
    private GuiControllerSlider curveSlider;
    private GuiControllerSlider cursorSpeedSlider;

    private GuiSmallButton invertYButton;
    private GuiSmallButton dropCircleButton;
    private GuiSmallButton sneakModeButton;
    private GuiSmallButton hideCursorButton;

    public GuiControllerSettings(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int leftX = this.width / 2 - 155;
        int rightX = this.width / 2 + 5;
        int startY = this.height / 6 - 6;

        // Row 1: Sensitivity (20% to 300%) & Deadzone (5% to 40%)
        sensSlider = new GuiControllerSlider(1, leftX, startY, 150, 20,
                (ControllerConfig.cameraSensitivity - 0.2f) / 2.8f,
                new GuiControllerSlider.SliderCallback() {
                    @Override
                    public void onChange(float val) {
                        ControllerConfig.cameraSensitivity = 0.2f + val * 2.8f;
                    }

                    @Override
                    public String getDisplayString(float val) {
                        return String.format("Sensitivity: %d%%", Math.round((0.2f + val * 2.8f) * 100));
                    }
                });
        this.buttonList.add(sensSlider);

        deadzoneSlider = new GuiControllerSlider(2, rightX, startY, 150, 20,
                (ControllerConfig.deadzone - 0.05f) / 0.35f,
                new GuiControllerSlider.SliderCallback() {
                    @Override
                    public void onChange(float val) {
                        ControllerConfig.deadzone = 0.05f + val * 0.35f;
                    }

                    @Override
                    public String getDisplayString(float val) {
                        return String.format("Stick Deadzone: %d%%", Math.round((0.05f + val * 0.35f) * 100));
                    }
                });
        this.buttonList.add(deadzoneSlider);

        // Row 2: Aim Curve (1.0 to 2.5) & Virtual Cursor Speed (4.0 to 20.0)
        curveSlider = new GuiControllerSlider(3, leftX, startY + 24, 150, 20,
                (ControllerConfig.cameraCurve - 1.0f) / 1.5f,
                new GuiControllerSlider.SliderCallback() {
                    @Override
                    public void onChange(float val) {
                        ControllerConfig.cameraCurve = 1.0f + val * 1.5f;
                    }

                    @Override
                    public String getDisplayString(float val) {
                        float curve = 1.0f + val * 1.5f;
                        return String.format("Aim Curve: %.1f%s", curve, curve <= 1.05f ? " (Linear)" : " (Smooth)");
                    }
                });
        this.buttonList.add(curveSlider);

        cursorSpeedSlider = new GuiControllerSlider(4, rightX, startY + 24, 150, 20,
                (ControllerConfig.virtualCursorSpeed - 6.0f) / 24.0f,
                new GuiControllerSlider.SliderCallback() {
                    @Override
                    public void onChange(float val) {
                        ControllerConfig.virtualCursorSpeed = 6.0f + val * 24.0f;
                    }

                    @Override
                    public String getDisplayString(float val) {
                        return String.format("Cursor Speed: %.1f", 6.0f + val * 24.0f);
                    }
                });
        this.buttonList.add(cursorSpeedSlider);

        // Row 3: Invert Y & Drop with Circle (O)
        invertYButton = new GuiSmallButton(5, leftX, startY + 48, 150, 20, getInvertYText());
        this.buttonList.add(invertYButton);

        dropCircleButton = new GuiSmallButton(6, rightX, startY + 48, 150, 20, getDropCircleText());
        this.buttonList.add(dropCircleButton);

        // Row 4: Sneak Mode (Hold / Toggle) & Hide Windows Cursor
        sneakModeButton = new GuiSmallButton(7, leftX, startY + 72, 150, 20, getSneakModeText());
        this.buttonList.add(sneakModeButton);

        hideCursorButton = new GuiSmallButton(8, rightX, startY + 72, 150, 20, getHideCursorText());
        this.buttonList.add(hideCursorButton);

        // Row 5: Cursor Style (Centered)
        this.buttonList.add(new GuiSmallButton(9, this.width / 2 - 75, startY + 96, 150, 20, getCursorStyleText()));

        // Bottom Row: Reset Defaults & Done
        this.buttonList.add(new GuiButton(201, this.width / 2 - 155, this.height / 6 + 144, 150, 20, "Reset Defaults"));
        this.buttonList.add(new GuiButton(200, this.width / 2 + 5, this.height / 6 + 144, 150, 20, "Done"));
    }

    private String getCursorStyleText() {
        return "Cursor Style: " + ControllerConfig.cursorStyle;
    }

    private String getInvertYText() {
        return "Invert Look Y: " + (ControllerConfig.invertY ? "ON" : "OFF");
    }

    private String getDropCircleText() {
        return "Drop with (O): " + (ControllerConfig.dropWithCircle ? "YES" : "D-Pad Only");
    }

    private String getSneakModeText() {
        return "Crouch (R3): " + (ControllerConfig.toggleSneak ? "TOGGLE" : "HOLD");
    }

    private String getHideCursorText() {
        return "Hide Windows Cursor: " + (ControllerConfig.hideHardwareCursor ? "YES" : "NO");
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // Escape / Back
            ControllerConfig.save();
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id == 5) {
            ControllerConfig.invertY = !ControllerConfig.invertY;
            button.displayString = getInvertYText();
        } else if (button.id == 6) {
            ControllerConfig.dropWithCircle = !ControllerConfig.dropWithCircle;
            button.displayString = getDropCircleText();
        } else if (button.id == 7) {
            ControllerConfig.toggleSneak = !ControllerConfig.toggleSneak;
            button.displayString = getSneakModeText();
        } else if (button.id == 8) {
            ControllerConfig.hideHardwareCursor = !ControllerConfig.hideHardwareCursor;
            if (!ControllerConfig.hideHardwareCursor) {
                VirtualCursor.showSystemCursor();
            }
            button.displayString = getHideCursorText();
        } else if (button.id == 9) {
            ControllerConfig.cursorStyle = "Xbox".equalsIgnoreCase(ControllerConfig.cursorStyle) ? "Circle" : "Xbox";
            button.displayString = getCursorStyleText();
        } else if (button.id == 201) {
            ControllerConfig.resetDefaults();
            initGui();
        } else if (button.id == 200) {
            ControllerConfig.save();
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Draw title
        this.drawCenteredString(this.fontRenderer, "Controller Settings", this.width / 2, 14, 0xFFFFFF);

        // Subtitle with device status
        String deviceName = ControllerHandler.getInstance().getCurrentSnapshot().isConnected
                ? "\u00a7aConnected: \u00a7f" + ControllerHandler.getInstance().getCurrentSnapshot().controllerName
                : "\u00a7cNo Controller Connected (Press F12 for Virtual Pad)";
        this.drawCenteredString(this.fontRenderer, deviceName, this.width / 2, 28, 0xAAAAAA);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
