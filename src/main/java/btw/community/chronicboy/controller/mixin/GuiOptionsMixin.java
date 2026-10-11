package btw.community.chronicboy.controller.mixin;

import btw.community.chronicboy.controller.gui.GuiControllerSettings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiOptions;
import net.minecraft.src.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GuiOptions.class)
public abstract class GuiOptionsMixin extends GuiScreen {
    @Inject(method = "initGui", at = @At("TAIL"))
    private void addControllerOptionsButton(CallbackInfo ci) {
        for (Object obj : this.buttonList) {
            if (obj instanceof GuiButton btn && btn.id == 200) {
                btn.width = 98;
                btn.xPosition = this.width / 2 + 2;

                this.buttonList.add(new GuiButton(450, this.width / 2 - 100, btn.yPosition, 98, 20, "Controller..."));
                break;
            }
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button != null && button.enabled && button.id == 450) {
            this.mc.displayGuiScreen(new GuiControllerSettings(this));
        }
    }
}
