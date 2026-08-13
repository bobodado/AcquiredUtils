package dev.bobodado.acquiredutils.client.pickup;

import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public final class RarityHighlightHandler {

    private RarityHighlightHandler() {
    }

    public static void init() {
        // Rendering is coordinated by ContainerOverlayHandler.
    }

    public static void renderOverlay(
        GuiGraphics graphics,
        AbstractContainerScreen<?> screen,
        Player player,
        int leftPos,
        int topPos
    ) {
        AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

        if (player == null || !cfg.rarityCircleEnabled) {
            return;
        }

        ItemRarity minimum = ItemRarity.fromName(cfg.rarityCircleMinRarity);
        int radius = Math.max(3, Math.min(8, Math.round(cfg.rarityCircleSize)));
        int alpha = Math.max(0, Math.min(255, Math.round(cfg.rarityCircleOpacity * 255.0f)));

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.isActive() || slot.getItem().isEmpty()) {
                continue;
            }

            ItemRarity rarity = ItemRarityDetector.detect(slot.getItem(), player);
            if (rarity == null || rarity.ordinal() < minimum.ordinal()) {
                continue;
            }

            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;

            drawRarityCircle(
                graphics,
                slotX + 8,
                slotY + 8,
                rarity.color(),
                radius,
                alpha
            );
        }
    }

    private static void drawRarityCircle(
        GuiGraphics graphics,
        int centerX,
        int centerY,
        int color,
        int outerRadius,
        int alpha
    ) {
        int innerRadius = Math.max(1, outerRadius - 1);
        int argb = (alpha << 24) | (color & 0x00FFFFFF);

        for (int dy = -outerRadius; dy <= outerRadius; dy++) {
            int outerWidth = (int) Math.floor(
                Math.sqrt(outerRadius * outerRadius - dy * dy)
            );

            if (Math.abs(dy) >= innerRadius) {
                graphics.fill(
                    centerX - outerWidth,
                    centerY + dy,
                    centerX + outerWidth + 1,
                    centerY + dy + 1,
                    argb
                );
                continue;
            }

            int innerWidth = (int) Math.floor(
                Math.sqrt(innerRadius * innerRadius - dy * dy)
            );

            graphics.fill(
                centerX - outerWidth,
                centerY + dy,
                centerX - innerWidth,
                centerY + dy + 1,
                argb
            );
            graphics.fill(
                centerX + innerWidth + 1,
                centerY + dy,
                centerX + outerWidth + 1,
                centerY + dy + 1,
                argb
            );
        }
    }
}
