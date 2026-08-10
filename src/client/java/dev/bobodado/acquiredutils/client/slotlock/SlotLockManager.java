package dev.bobodado.acquiredutils.client.slotlock;

import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import dev.bobodado.acquiredutils.mixin.HandledScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.Arrays;

public final class SlotLockManager {

    public static final int PLAYER_INVENTORY_SIZE = 41;
    public static final int HOTBAR_SIZE = 9;

    private static final Identifier LOCK_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    AcquiredUtils.MOD_ID,
                    "textures/gui/lock.png"
            );

    private SlotLockManager() {
    }

    public static void normalizeConfig(AcquiredUtilsConfig config) {
        if (config.lockedSlots == null) {
            config.lockedSlots =
                    new boolean[PLAYER_INVENTORY_SIZE];

            return;
        }

        if (config.lockedSlots.length != PLAYER_INVENTORY_SIZE) {
            boolean[] normalized =
                    new boolean[PLAYER_INVENTORY_SIZE];

            System.arraycopy(
                    config.lockedSlots,
                    0,
                    normalized,
                    0,
                    Math.min(
                            config.lockedSlots.length,
                            normalized.length
                    )
            );

            config.lockedSlots = normalized;
        }
    }

    public static void clearAll() {
        normalizeConfig(AcquiredUtilsConfig.get());

        Arrays.fill(
                AcquiredUtilsConfig.get().lockedSlots,
                false
        );

        AcquiredUtilsConfig.save();
    }

    public static boolean isPlayerInventorySlot(Slot slot) {
        LocalPlayer player =
                Minecraft.getInstance().player;

        return player != null
                && slot != null
                && slot.container == player.getInventory()
                && slot.getContainerSlot() >= 0
                && slot.getContainerSlot()
                < PLAYER_INVENTORY_SIZE;
    }

    public static boolean isLocked(Slot slot) {
        if (!isPlayerInventorySlot(slot)) {
            return false;
        }

        return isLockedIndex(
                slot.getContainerSlot()
        );
    }

    public static boolean isLockedIndex(int index) {
        AcquiredUtilsConfig config =
                AcquiredUtilsConfig.get();

        normalizeConfig(config);

        return index >= 0
                && index < PLAYER_INVENTORY_SIZE
                && config.lockedSlots[index];
    }

    public static void toggleHoveredSlot() {
        Minecraft client =
                Minecraft.getInstance();

        if (!(client.screen
                instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        double mouseX =
                client.mouse.getX()
                        * screen.width
                        / client.getWindow().getWidth();

        double mouseY =
                screen.height
                        - client.mouse.getY()
                        * screen.height
                        / client.getWindow().getHeight()
                        - 1.0;

        Slot slot =
                getSlotAt(
                        screen,
                        mouseX,
                        mouseY
                );

        if (!isPlayerInventorySlot(slot)) {
            return;
        }

        int index =
                slot.getContainerSlot();

        if (index < 0
                || index >= PLAYER_INVENTORY_SIZE) {
            return;
        }

        AcquiredUtilsConfig config =
                AcquiredUtilsConfig.get();

        normalizeConfig(config);

        config.lockedSlots[index] =
                !config.lockedSlots[index];

        AcquiredUtilsConfig.save();

        AcquiredUtils.LOGGER.info(
                "[AcquiredUtils] Slot {} {}",
                index,
                config.lockedSlots[index]
                        ? "locked"
                        : "unlocked"
        );
    }

    private static Slot getSlotAt(
            AbstractContainerScreen<?> screen,
            double mouseX,
            double mouseY
    ) {
        HandledScreenAccessor accessor =
                (HandledScreenAccessor) screen;

        double localX =
                mouseX
                        - accessor.acquiredutils$getLeftPos();

        double localY =
                mouseY
                        - accessor.acquiredutils$getTopPos();

        for (Slot slot : screen.getMenu().slots) {

            if (slot.isActive()
                    && localX >= slot.x
                    && localX < slot.x + 16
                    && localY >= slot.y
                    && localY < slot.y + 16) {

                return slot;
            }
        }

        return null;
    }

    public static boolean shouldBlockClick(
            Slot slot,
            int button,
            ClickType actionType
    ) {
        if (isLocked(slot)) {
            return true;
        }

        /*
         * SWAP uses buttons 0-8 to select a hotbar slot.
         *
         * Protect the destination as well, even when the
         * currently hovered source slot is not locked.
         */
        return actionType == ClickType.SWAP
                && button >= 0
                && button < HOTBAR_SIZE
                && isLockedIndex(button);
    }

    public static void renderLock(
            GuiGraphics graphics,
            Slot slot
    ) {
        if (!isLocked(slot)) {
            return;
        }

        /*
         * lock.png remains 16x16.
         *
         * No scaling is performed on the source image.
         * It is rendered at native 16x16 size.
         */
        graphics.drawTexture(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                LOCK_TEXTURE,
                slot.x,
                slot.y,
                0.0f,
                0.0f,
                16,
                16,
                16,
                16
        );
    }
}