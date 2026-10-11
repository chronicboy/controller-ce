package btw.community.chronicboy.controller.mixin;

import btw.community.chronicboy.controller.input.ControllerHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow private Minecraft mc;

    @Inject(method = "updateCameraAndRender(F)V", at = @At("TAIL"))
    private void injectAnalogCameraLook(float partialTicks, CallbackInfo ci) {
        if (this.mc != null && this.mc.thePlayer != null) {
            ControllerHandler.getInstance().applyAnalogCamera(this.mc.thePlayer);
        }
    }
}
