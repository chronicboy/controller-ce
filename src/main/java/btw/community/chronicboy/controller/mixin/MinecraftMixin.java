package btw.community.chronicboy.controller.mixin;

import btw.community.chronicboy.controller.input.ControllerHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "startGame", at = @At("RETURN"))
    private void onStartGame(CallbackInfo ci) {
        ControllerHandler.getInstance().init();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTick(CallbackInfo ci) {
        ControllerHandler.getInstance().update();
    }
}
