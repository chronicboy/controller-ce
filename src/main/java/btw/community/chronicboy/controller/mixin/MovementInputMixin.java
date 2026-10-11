package btw.community.chronicboy.controller.mixin;

import btw.community.chronicboy.controller.config.ControllerConfig;
import btw.community.chronicboy.controller.input.ControllerAction;
import btw.community.chronicboy.controller.input.ControllerButton;
import btw.community.chronicboy.controller.input.ControllerHandler;
import btw.community.chronicboy.controller.input.ControllerSnapshot;
import net.minecraft.src.Minecraft;
import net.minecraft.src.MovementInput;
import net.minecraft.src.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public abstract class MovementInputMixin extends MovementInput {
    private static boolean isSneakToggled = false;

    @Inject(method = "updatePlayerMoveState", at = @At("TAIL"))
    private void injectAnalogMovement(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        // NEVER apply controller movement, jump, or sneak when a GUI/menu/inventory is open!
        if (mc == null || mc.currentScreen != null) {
            return;
        }

        ControllerSnapshot snap = ControllerHandler.getInstance().getCurrentSnapshot();
        if (snap != null && snap.isConnected) {
            // Crouch / Sneak & Descend while flying — must be set BEFORE movement scaling
            if (ControllerConfig.toggleSneak) {
                if (ControllerHandler.getInstance().isActionJustPressed(ControllerAction.SNEAK)) {
                    isSneakToggled = !isSneakToggled;
                }
                if (isSneakToggled) {
                    this.sneak = true;
                }
            } else {
                if (ControllerHandler.getInstance().isActionPressed(ControllerAction.SNEAK)) {
                    this.sneak = true;
                }
            }

            float lx = snap.leftStickX;
            float ly = snap.leftStickY;

            if (Math.abs(lx) > 0.001f || Math.abs(ly) > 0.001f) {
                // In Minecraft MovementInput:
                // moveForward: +1.0 for Forward, -1.0 for Backward
                // moveStrafe: +1.0 for Strafe Left, -1.0 for Strafe Right
                this.moveForward = ly;
                this.moveStrafe = -lx;

                if (this.sneak) {
                    this.moveStrafe = (float) ((double) this.moveStrafe * 0.3D);
                    this.moveForward = (float) ((double) this.moveForward * 0.3D);
                }
            }

            // Jump (and ascend while flying)
            if (ControllerHandler.getInstance().isActionPressed(ControllerAction.JUMP)) {
                this.jump = true;
            }

            // Special key (BTW sprint / special)
            if (ControllerHandler.getInstance().isActionPressed(ControllerAction.SPECIAL)) {
                this.special = true;
            }
        }
    }
}
