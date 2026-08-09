package dev.bobodado.acquiredutils.client.gui.section;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.client.AcquiredUtilsClient;
import dev.bobodado.acquiredutils.client.gui.widget.KeyListenerSlot;
import dev.bobodado.acquiredutils.client.gui.widget.LockedKeybindWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Every keybind here is a fixed mod feature: the player can toggle it and
 * rebind its key, but the action itself is not player-authored. Slot Lock is
 * currently the only one; add more rows the same way (buildContent() +
 * matching render() description line) if/when a second fixed feature needs
 * its own keybind.
 * <p>
 * Row height bumped from 16 -> 22 to give LockedKeybindWidget's now-larger
 * label room to breathe, and the description offset moved down to match —
 * both values only affect this section's own layout.
 */
public class KeybindsSection extends ModSection {

	private final KeyListenerSlot keybindSlot = new KeyListenerSlot();

	public KeybindsSection(dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen screen) {
		super(screen);
	}

	@Override
	public String getId() {
		return "keybinds";
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("acquiredutils.gui.tab.keybinds");
	}

	@Override
	public void buildContent(int contentX, int contentY, int contentWidth, int contentHeight) {
		AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();
		int rowY = contentY;

		addWidget(new LockedKeybindWidget(
				contentX, rowY, contentWidth, s(22),
				Component.translatable("acquiredutils.gui.keybind.slot_lock"),
				keybindSlot,
				() -> cfg.slotLockEnabled,
				enabled -> cfg.slotLockEnabled = enabled,
				() -> cfg.slotLockKey,
				keyCode -> {
					cfg.slotLockKey = keyCode;
					AcquiredUtilsClient.syncSlotLockKeybind();
				}
		));

		// Add more LockedKeybindWidget rows here for future fixed features,
		// e.g.: rowY += s(40); addWidget(new LockedKeybindWidget(contentX, rowY, ...));
		// — and a matching description line in render() below.
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
	                    int contentX, int contentY, int contentWidth, int contentHeight) {
		// Sits just below the row (row height s(22)), matching GeneralSection's
		// label-then-description spacing pattern. If this still shows the raw
		// key "acquiredutils.gui.desc.slot_lock" instead of real text in-game,
		// that key is missing from en_us.json — add it there, not here.
		screen.drawDescription(graphics, "acquiredutils.gui.desc.slot_lock", contentX, contentY + s(26));
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (keybindSlot.current != null) {
			int keyCode = InputConstants.getKey(event).getValue();
			if (keyCode == 256) { // Escape: unbind
				keyCode = -1;
			}
			keybindSlot.current.applyKeyCode(keyCode);
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		keybindSlot.clear();
	}
}