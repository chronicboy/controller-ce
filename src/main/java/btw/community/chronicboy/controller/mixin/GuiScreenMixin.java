package btw.community.chronicboy.controller.mixin;

import btw.community.chronicboy.controller.config.ControllerConfig;
import btw.community.chronicboy.controller.gui.VirtualCursor;
import btw.community.chronicboy.controller.input.ControllerHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin {
    @Inject(method = "drawScreen(IIF)V", at = @At("TAIL"))
    private void renderVirtualCursor(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (ControllerConfig.enableVirtualCursor && ControllerHandler.getInstance().getCurrentSnapshot().isConnected) {
            VirtualCursor.render((GuiScreen) (Object) this);
        }
    }
}
