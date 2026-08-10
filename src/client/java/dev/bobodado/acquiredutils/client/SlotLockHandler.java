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
 * Built entirely on Fabric API's official, documented, non-Mixin hooks —
 * ScreenEvents.AFTER_INIT / afterRender / ScreenMouseEvents.allowMouseClick —
 * so no Mixin setup needed in this project.
 * <p>
 * VERIFY (this project has not touched container/inventory internals before
 * now, unlike the GUI code, which is already confirmed against the real
 * jar). Run this and check for mismatches before trusting this file:
 * <pre>
 *   javap -p -classpath &lt;minecraft-merged-loom-mappings-jar&gt; net.minecraft.world.inventory.Slot | grep -E "container|getContainerSlot|int x|int y"
 *   javap -p -classpath &lt;minecraft-merged-loom-mappings-jar&gt; net.minecraft.client.gui.screens.inventory.AbstractContainerScreen | grep -E "getSlotUnderMouse|getGuiLeft|getGuiTop|getMenu"
 *   javap -p -classpath &lt;minecraft-merged-loom-mappings-jar&gt; net.minecraft.world.inventory.AbstractContainerMenu | grep -i slots
 *   javap -p -classpath &lt;minecraft-merged-loom-mappings-jar&gt; net.minecraft.client.gui.GuiGraphics | grep -i blit
 * </pre>
 * (Same jar path we used earlier for the onClick/MouseButtonEvent checks.)
 * If anything doesn't match, fix the corresponding line the same way we
 * fixed onClick — swap in whatever the real signature turns out to be.
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

			ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> {
				if (!AcquiredUtilsConfig.get().slotLockEnabled) {
					return true;
				}
				Slot hovered = containerScreen.getSlotUnderMouse();
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
		Slot hovered = containerScreen.getSlotUnderMouse();
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

		for (Slot slot : screen.getMenu().slots) {
			if (slot.container == mc.player.getInventory() && isLocked(slot.getContainerSlot())) {
				int x = screen.getGuiLeft() + slot.x;
				int y = screen.getGuiTop() + slot.y;
				graphics.blit(LOCK_ICON, x, y, 0, 0, 16, 16, 16, 16);
			}
		}
	}
}