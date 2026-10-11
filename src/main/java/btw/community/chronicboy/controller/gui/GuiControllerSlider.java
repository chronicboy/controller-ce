package btw.community.chronicboy.controller.gui;

import net.minecraft.src.GuiButton;
import net.minecraft.src.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiControllerSlider extends GuiButton {
    public interface SliderCallback {
        void onChange(float normalizedValue);
        String getDisplayString(float normalizedValue);
    }

    public float sliderValue;
    public boolean dragging = false;
    private final SliderCallback callback;

    public GuiControllerSlider(int id, int x, int y, int width, int height, float initialNormalized, SliderCallback callback) {
        super(id, x, y, width, height, "");
        this.sliderValue = Math.max(0.0f, Math.min(1.0f, initialNormalized));
        this.callback = callback;
        this.displayString = callback.getDisplayString(this.sliderValue);
    }

    @Override
    protected int getHoverState(boolean hovered) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.drawButton) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                if (this.sliderValue < 0.0f) this.sliderValue = 0.0f;
                if (this.sliderValue > 1.0f) this.sliderValue = 1.0f;
                this.callback.onChange(this.sliderValue);
                this.displayString = this.callback.getDisplayString(this.sliderValue);
            }

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            if (this.sliderValue < 0.0f) this.sliderValue = 0.0f;
            if (this.sliderValue > 1.0f) this.sliderValue = 1.0f;
            this.callback.onChange(this.sliderValue);
            this.displayString = this.callback.getDisplayString(this.sliderValue);
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    public void updateValue(float normalized) {
        this.sliderValue = Math.max(0.0f, Math.min(1.0f, normalized));
        this.callback.onChange(this.sliderValue);
        this.displayString = this.callback.getDisplayString(this.sliderValue);
    }
}
