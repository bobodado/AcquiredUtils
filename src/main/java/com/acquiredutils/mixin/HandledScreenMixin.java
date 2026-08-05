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
    private static final int S = 3;
    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void onSlot(GuiGraphics g, Slot slot, int x, int y, CallbackInfo ci) {
        if (slot.hasItem()) {
            RarityType r = RarityDetector.detectRarity(slot.getItem());
            if (r != RarityType.COMMON) renderIndicator(g, slot, r);
        }
    }
    private void renderIndicator(GuiGraphics g, Slot slot, RarityType r) {
        int x = slot.x, y = slot.y + 16 - S * 3;
        g.pose().pushMatrix();
        int c = 0xFF000000 | r.getColor();
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                g.fill(x + col * S, y + row * S, x + col * S + S, y + row * S + S, c);
        g.pose().popMatrix();
    }
}