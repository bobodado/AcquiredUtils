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

import java.lang.reflect.Field;

/**
 * Slot Lock: press the Slot Lock key while hovering a player-inventory slot
 * in any open inventory-like screen to lock/unlock it. Locked slots show the
 * lock icon overlay and reject clicks entirely.
 * <p>
 * AbstractContainerScreen's hoveredSlot/leftPos/topPos are all `protected`
 * with no public accessor (confirmed via javap against the real 1.21.11
 * jar) — this project has no Mixin setup, so this uses reflection to read
 * those fields instead. hoveredSlot in particular is already computed and
 * kept up to date by vanilla's own per-frame logic, so reading it directly
 * is simpler and more robust than recomputing hover-detection ourselves,
 * and it sidesteps a real problem with the original design: the keybind
 * toggle fires on the client tick, where no mouse-event coordinates are
 * available at all.
 * <p>
 * Everything below has been confirmed via javap against the real jars:
 * ScreenEvents.AfterRender and ScreenMouseEvents.AllowMouseClick signatures,
 * Slot.container/x/y/getContainerSlot(), AbstractContainerMenu.slots, and
 * GuiGraphics#blit(Identifier, int, int, int, int, float, float, float, float)
 * — the one blit() overload in this version that doesn't require an explicit
 * RenderPipeline argument.
 */
public final class SlotLockHandler {

	private static final Identifier LOCK_ICON =
			Identifier.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "textures/gui/lock.png");

	private static final Field HOVERED_SLOT_FIELD;
	private static final Field LEFT_POS_FIELD;
	private static final Field TOP_POS_FIELD;

	static {
		Field hovered = null, left = null, top = null;
		try {
			hovered = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
			hovered.setAccessible(true);
			left = AbstractContainerScreen.class.getDeclaredField("leftPos");
			left.setAccessible(true);
			top = AbstractContainerScreen.class.getDeclaredField("topPos");
			top.setAccessible(true);
		} catch (NoSuchFieldException e) {
			AcquiredUtils.LOGGER.error("[AcquiredUtils] Slot Lock: reflection setup failed, feature disabled", e);
		}
		HOVERED_SLOT_FIELD = hovered;
		LEFT_POS_FIELD = left;
		TOP_POS_FIELD = top;
	}

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
				Slot hovered = getHoveredSlot(containerScreen);
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
		Slot hovered = getHoveredSlot(containerScreen);
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

	private static Slot getHoveredSlot(AbstractContainerScreen<?> screen) {
		if (HOVERED_SLOT_FIELD == null) {
			return null;
		}
		try {
			return (Slot) HOVERED_SLOT_FIELD.get(screen);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	private static int getLeftPos(AbstractContainerScreen<?> screen) {
		if (LEFT_POS_FIELD == null) return 0;
		try {
			return LEFT_POS_FIELD.getInt(screen);
		} catch (IllegalAccessException e) {
			return 0;
		}
	}

	private static int getTopPos(AbstractContainerScreen<?> screen) {
		if (TOP_POS_FIELD == null) return 0;
		try {
			return TOP_POS_FIELD.getInt(screen);
		} catch (IllegalAccessException e) {
			return 0;
		}
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

		int left = getLeftPos(screen);
		int top = getTopPos(screen);

		for (Slot slot : screen.getMenu().slots) {
			if (slot.container == mc.player.getInventory() && isLocked(slot.getContainerSlot())) {
				int x = left + slot.x;
				int y = top + slot.y;
				// This is the one blit() overload that doesn't require a RenderPipeline
				// argument (confirmed via javap — most other overloads in this version
				// do). Last four params are UV fractions (0..1), not pixel offsets —
				// 0,0,1,1 draws the whole 16x16 icon with no cropping.
				graphics.blit(LOCK_ICON, x, y, 16, 16, 0f, 0f, 1f, 1f);
			}
		}
	}
}