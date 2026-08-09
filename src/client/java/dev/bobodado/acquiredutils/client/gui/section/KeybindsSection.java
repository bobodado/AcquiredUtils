package dev.bobodado.acquiredutils.client.gui.section;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.client.AcquiredUtilsClient;
import dev.bobodado.acquiredutils.client.gui.widget.KeyListenerSlot;
import dev.bobodado.acquiredutils.client.gui.widget.LockedKeybindWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Every keybind here is a fixed mod feature: the player can toggle it and
 * rebind its key, but the action itself is not player-authored — that's the
 * "locked keybind" pattern (see LockedKeybindWidget). Slot Lock is currently
 * the only one; add more rows the same way if/when a second fixed feature
 * needs its own keybind.
 * <p>
 * (The previous version of this section also let players add arbitrary
 * "press key -> send chat message" entries. That's been removed — that use
 * case is intentionally left to a dedicated macro/auto-text mod instead.)
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
				contentX, rowY, contentWidth, s(16),
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
		// e.g.: rowY += s(24); addWidget(new LockedKeybindWidget(contentX, rowY, ...));
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