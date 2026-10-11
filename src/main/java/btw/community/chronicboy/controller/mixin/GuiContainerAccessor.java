package btw.community.chronicboy.controller.mixin;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {
    @Accessor("theSlot")
    Slot getTheSlot();

    @Accessor("guiLeft")
    int getGuiLeft();

    @Accessor("guiTop")
    int getGuiTop();

    @Accessor("inventorySlots")
    Container getInventorySlots();

    @Invoker("handleMouseClick")
    void invokeHandleMouseClick(Slot slot, int slotId, int clickedButton, int clickType);
}
