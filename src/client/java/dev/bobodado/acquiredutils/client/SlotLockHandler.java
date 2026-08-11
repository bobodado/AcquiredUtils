package dev.bobodado.acquiredutils.client;

import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;

/**
 * Slot Lock: press the Slot Lock key while hovering a player-inventory slot
 * in any open inventory-like screen to lock/unlock it. Locked slots show the
 * lock icon overlay and reject clicks entirely.
 * <p>
 * AbstractContainerScreen's hoveredSlot/leftPos/topPos are `protected` with
 * no public accessor. This used reflection-by-name at first, which turned
 * out to be a real mistake: Fabric Loom's remapping pipeline only rewrites
 * actual compiled bytecode references between the dev environment's clean
 * names and the real game's obfuscated runtime names — a hardcoded string
 * like "hoveredSlot" passed to getDeclaredField() never gets translated, so
 * it silently failed the moment it ran against a real (non-dev) game.
 * <p>
 * Fixed with an access widener (acquiredutils.accesswidener, registered in
 * fabric.mod.json), which widens these three fields to accessible and,
 * unlike reflection, participates in the same remapping pipeline as normal
 * code — so the plain field accesses below (screen.hoveredSlot, .leftPos,
 * .topPos) correctly resolve in both the dev environment and the real game.
 */
public final class SlotLockHandler {

	private static final Identifier LOCK_ICON =
			Identifier.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "textures/gui/lock.png");

	private SlotLockHandler() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
				return;
			}

			ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, partialTick) -> {
				if (AcquiredUtilsConfig.get().slotLockEnabled) {
					renderLockedSlots(graphics, containerScreen);
				}
			});

			ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> {
				if (!AcquiredUtilsConfig.get().slotLockEnabled) {
					return true;
				}
				Slot hovered = containerScreen.hoveredSlot;
				if (hovered != null && isPlayerInventorySlot(hovered) && isLocked(hovered.getContainerSlot())) {
					return false; // block the click entirely
				}
				return true;
			});
		});
	}

	/** Called from the Slot Lock keybind's tick handler in AcquiredUtilsClient. */
	public static void toggleSlotUnderMouse() {
		if (!AcquiredUtilsConfig.get().slotLockEnabled) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
			return;
		}
		Slot hovered = containerScreen.hoveredSlot;
		if (hovered == null || !isPlayerInventorySlot(hovered)) {
			return;
		}

		int idx = hovered.getContainerSlot();
		var locked = AcquiredUtilsConfig.get().lockedSlots;
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
		if (mc.player == null) {
			return;
		}

		int left = screen.leftPos;
		int top = screen.topPos;

		for (Slot slot : screen.getMenu().slots) {
			if (slot.container == mc.player.getInventory() && isLocked(slot.getContainerSlot())) {
				int x = left + slot.x;
				int y = top + slot.y;
				graphics.blit(LOCK_ICON, x, y, 16, 16, 0f, 0f, 1f, 1f);
			}
		}
	}
}