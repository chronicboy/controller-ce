package btw.community.chronicboy.controller.mixin;

import net.minecraft.src.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {
    @Invoker("mouseClicked")
    void invokeMouseClicked(int mouseX, int mouseY, int button);

    @Invoker("mouseMovedOrUp")
    void invokeMouseMovedOrUp(int mouseX, int mouseY, int which);

    @Invoker("keyTyped")
    void invokeKeyTyped(char character, int keyCode);
}
