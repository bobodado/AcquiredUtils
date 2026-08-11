package dev.bobodado.acquiredutils.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public final class SlotLockHandler {

    private SlotLockHandler() {
    }

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
                return;
            }

            ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> {
                if (!AcquiredUtilsConfig.get().slotLockEnabled) return true;
                Slot hovered = containerScreen.getHoveredSlot();
                if (hovered == null || !isPlayerInventorySlot(hovered) || !hovered.isActive()) {
                    return true;
                }
                return !isLocked(hovered.getContainerSlot());
            });

            ScreenKeyboardEvents.allowKeyPress(screen).register((s, event) -> {
                if (!AcquiredUtilsConfig.get().slotLockEnabled) return true;

                int keyCode = InputConstants.getKey(event).getValue();
                Options options = Minecraft.getInstance().options;

                if (keyCode == AcquiredUtilsConfig.get().slotLockKey && keyCode >= 0) {
                    toggleHoveredSlot(containerScreen);
                    return false;
                }

                Slot hovered = containerScreen.getHoveredSlot();
                if (hovered == null || !isPlayerInventorySlot(hovered) || !hovered.isActive()) {
                    return true;
                }
                int hoveredSlot = hovered.getContainerSlot();

                if (keyCode == options.keyDrop.getKey().getValue()) {
                    return !isLocked(hoveredSlot);
                }

                if (keyCode == options.keySwapOffhand.getKey().getValue()) {
                    if (isLocked(hoveredSlot) || isLocked(40)) return false;
                    return true;
                }

                for (int i = 0; i < 9; i++) {
                    if (keyCode == options.keyHotbarSlots[i].getKey().getValue()) {
                        if (isLocked(hoveredSlot) || isLocked(i)) return false;
                        return true;
                    }
                }

                return true;
            });

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, partialTick) -> {
                if (!AcquiredUtilsConfig.get().slotLockEnabled) return;
                renderLockedSlots(graphics, containerScreen);
            });
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!AcquiredUtilsConfig.get().slotLockEnabled) return;
            if (client.player == null) return;
            if (client.screen != null) return;

            int selectedSlot = client.player.getInventory().selected;
            if (!isLocked(selectedSlot)) return;

            Options options = client.options;
            options.keyDrop.consumeClick();
            options.keySwapOffhand.consumeClick();
        });
    }

    private static void toggleHoveredSlot(AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Slot hovered = screen.getHoveredSlot();
        if (hovered == null || !isPlayerInventorySlot(hovered) || !hovered.isActive()) return;

        int idx = hovered.getContainerSlot();
        java.util.Set<Integer> locked = AcquiredUtilsConfig.get().lockedSlots;
        if (locked.contains(idx)) {
            locked.remove(idx);
        } else {
            locked.add(idx);
        }
        AcquiredUtilsConfig.save();
        AcquiredUtils.LOGGER.info("[AcquiredUtils] Slot {} {}", idx, locked.contains(idx) ? "locked" : "unlocked");
    }

    private static boolean isPlayerInventorySlot(Slot slot) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && slot.container == mc.player.getInventory();
    }

    private static boolean isLocked(int containerSlotIndex) {
        return AcquiredUtilsConfig.get().lockedSlots.contains(containerSlotIndex);
    }

    private static void renderLockedSlots(GuiGraphics graphics, AbstractContainerScreen<?> screen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container == mc.player.getInventory() && slot.isActive() && isLocked(slot.getContainerSlot())) {
                int x = left + slot.x;
                int y = top + slot.y;
                drawPadlock(graphics, x, y);
            }
        }
    }

    private static void drawPadlock(GuiGraphics graphics, int x, int y) {
        int color = 0xFFD98F3E;
        graphics.fill(x + 4, y + 6, x + 12, y + 14, color);
        graphics.fill(x + 6, y + 2, x + 10, y + 6, color);
        graphics.fill(x + 5, y + 3, x + 6, y + 5, color);
        graphics.fill(x + 10, y + 3, x + 11, y + 5, color);
    }
}