package dev.bobodado.acquiredutils.mixin;

import dev.bobodado.acquiredutils.client.slotlock.SlotLockManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    @Inject(
            method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void acquiredutils$blockLockedSlotClick(
            Slot slot,
            int slotId,
            int button,
            ClickType clickType,
            CallbackInfo callbackInfo
    ) {
        if (SlotLockManager.shouldBlockClick(
                slot,
                button,
                clickType
        )) {
            callbackInfo.cancel();
        }
    }

    @Inject(
            method = "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
            at = @At("TAIL")
    )
    private void acquiredutils$drawLockedSlotOverlay(
            GuiGraphics graphics,
            Slot slot,
            CallbackInfo callbackInfo
    ) {
        SlotLockManager.renderLock(
                graphics,
                slot
        );
    }
}