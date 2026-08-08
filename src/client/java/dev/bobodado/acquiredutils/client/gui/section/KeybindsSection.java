package dev.bobodado.acquiredutils.client.gui.section;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.client.AcquiredUtilsClient;
import dev.bobodado.acquiredutils.client.gui.widget.CustomKeybindRowWidget;
import dev.bobodado.acquiredutils.client.gui.widget.KeyListenerSlot;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Player-defined "press this key -> send this chat message" bindings.
 * Slot Lock no longer lives here — it's a LockedKeybindWidget in
 * GeneralSection now.
 * <p>
 * Rebuilt to use CustomKeybindRowWidget (a real AbstractWidget with its own
 * onClick) instead of manually hit-testing a RowHitbox array inside a
 * section-wide mouseClicked() override. The old approach was the root cause
 * of the "pressing a key doesn't bind until you click elsewhere" bug: the
 * new one uses the exact same click-dispatch path the checkbox/slider/
 * dropdown widgets already rely on successfully.
 * <p>
 * Scrolling: rows outside the visible content area simply aren't created as
 * widgets during buildContent() (which reruns on every scroll via
 * screen.rebuild()), rather than using a scissor+manual-click-gating
 * approach — simpler and avoids a whole class of "widget exists but is
 * invisible yet still clickable" bugs.
 */
public class KeybindsSection extends ModSection {

	private final KeyListenerSlot keybindSlot = new KeyListenerSlot();

	private boolean addingNew = false;
	private EditBox messageField;
	private int scrollOffset = 0;

	private int lastContentY;
	private int lastContentHeight;

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
		lastContentY = contentY;
		lastContentHeight = contentHeight;

		int btnW = s(100), btnH = s(16);
		addWidget(Button.builder(Component.translatable("acquiredutils.gui.add_keybind"), b -> {
			addingNew = true;
			screen.rebuild();
		}).bounds(contentX, contentY, btnW, btnH).build());

		int listTop = contentY + btnH + s(6);

		if (addingNew) {
			int fieldW = s(140), fieldH = s(16);
			messageField = new EditBox(screen.getFont(), contentX, listTop, fieldW, fieldH,
					Component.translatable("acquiredutils.gui.keybind_message_hint"));
			messageField.setMaxLength(256);
			messageField.setFocused(true);
			addWidget(messageField);

			addWidget(Button.builder(Component.translatable("acquiredutils.gui.confirm"), b -> confirmAdd())
					.bounds(contentX + fieldW + s(4), listTop, s(60), fieldH).build());

			listTop += s(22);
		}

		int rowH = s(24);
		int visibleBottom = contentY + contentHeight;
		List<AcquiredUtilsConfig.CustomKeybindEntry> entries = AcquiredUtilsConfig.get().customKeybinds;

		int maxScroll = Math.max(0, entries.size() * rowH - (visibleBottom - listTop));
		if (scrollOffset > maxScroll) scrollOffset = maxScroll;
		if (scrollOffset < 0) scrollOffset = 0;

		for (int i = 0; i < entries.size(); i++) {
			int rowY = listTop + i * rowH - scrollOffset;
			// Skip creating a widget entirely for rows scrolled out of view —
			// no scissor test needed, and it can never be clicked while hidden.
			if (rowY + rowH <= listTop || rowY >= visibleBottom) {
				continue;
			}

			AcquiredUtilsConfig.CustomKeybindEntry entry = entries.get(i);
			final int index = i;
			addWidget(new CustomKeybindRowWidget(
					contentX, rowY, contentWidth, rowH,
					Component.literal(entry.message),
					keybindSlot,
					() -> entry.keyCode,
					keyCode -> {
						entry.keyCode = keyCode;
						AcquiredUtilsClient.syncCustomKeybinds();
					},
					() -> {
						AcquiredUtilsConfig.get().customKeybinds.remove(index);
						AcquiredUtilsClient.syncCustomKeybinds();
						screen.rebuild();
					}
			));
		}
	}

	private void confirmAdd() {
		if (messageField == null) return;
		String message = messageField.getValue().trim();
		if (!message.isEmpty()) {
			AcquiredUtilsConfig.get().customKeybinds.add(
					new AcquiredUtilsConfig.CustomKeybindEntry(message, -1));
			addingNew = false;
			AcquiredUtilsClient.syncCustomKeybinds();
			screen.rebuild();
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
	                    int contentX, int contentY, int contentWidth, int contentHeight) {
		if (AcquiredUtilsConfig.get().customKeybinds.isEmpty() && !addingNew) {
			int btnH = s(16);
			graphics.drawString(screen.getFont(),
					Component.translatable("acquiredutils.gui.no_custom_keybinds"),
					contentX, contentY + btnH + s(6), 0xFF999999, false);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scrollOffset -= (int) (scrollY * 20 * scale());
		screen.rebuild();
		return true;
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

		if (addingNew && messageField != null && messageField.isFocused()) {
			int keyCode = InputConstants.getKey(event).getValue();
			if (keyCode == 257 || keyCode == 335) { // Enter / numpad Enter
				confirmAdd();
				return true;
			}
		}

		return false;
	}

	@Override
	public void onClose() {
		keybindSlot.clear();
		AcquiredUtilsClient.syncCustomKeybinds();
	}
}