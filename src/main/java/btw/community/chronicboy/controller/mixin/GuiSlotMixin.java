package btw.community.chronicboy.controller.mixin;

import btw.community.chronicboy.controller.gui.VirtualCursor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.GuiSlot;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(GuiSlot.class)
public abstract class GuiSlotMixin {
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z", remap = false))
    private boolean redirectIsButtonDown(int button) {
        if (button == 0 && VirtualCursor.isLeftDown()) {
            return true;
        }
        return Mouse.isButtonDown(button);
    }
}
