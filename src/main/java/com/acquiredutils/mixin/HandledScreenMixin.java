package com.acquiredutils.mixin;

import com.acquiredutils.rarity.RarityDetector;
import com.acquiredutils.rarity.RarityType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin {

    private static final int INDICATOR_SIZE = 3;

    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void onRenderSlot(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (slot.hasItem()) {
            RarityType rarity = RarityDetector.detectRarity(slot.getItem());
            if (rarity != RarityType.COMMON) {
                renderRarityIndicator(graphics, slot, rarity);
            }
        }
    }

    private void renderRarityIndicator(GuiGraphics graphics, Slot slot, RarityType rarity) {
        int x = slot.x;
        int y = slot.y + 16 - INDICATOR_SIZE * 3;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 250);

        int color = 0xFF000000 | rarity.getColor();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                graphics.fill(
                        x + col * INDICATOR_SIZE,
                        y + row * INDICATOR_SIZE,
                        x + col * INDICATOR_SIZE + INDICATOR_SIZE,
                        y + row * INDICATOR_SIZE + INDICATOR_SIZE,
                        color
                );
            }
        }

        graphics.pose().popPose();
    }
}
