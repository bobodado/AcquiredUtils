package dev.bobodado.acquiredutils.client.gui.section;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.AcquiredUtilsClient;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * "Press this key -> send this chat message" custom bindings, plus the
 * built-in Slot Lock binding.
 * <p>
 * Two real bugs were fixed here from the previous version:
 * 1. Rows now use CustomKeybindEntry.id (not list index) so deleting one
 *    entry can never desync a different entry's live key. See
 *    AcquiredUtilsClient for the matching id-keyed map.
 * 2. mouseClicked() used to swallow EVERY click on screen while a key was
 *    being listened for (including clicks on the config screen's own X
 *    close button), which is why closing the menu right after binding a
 *    key stopped working. It now only swallows the click if it actually
 *    landed on the row being edited.
 */
public class KeybindsSection extends ModSection {

	private static final int COLOR_LISTENING = 0xFFE38A2D;
	private static final int COLOR_NONE = 0xFF666666;
	private static final int COLOR_ROW_HOVER = 0x20FFFFFF;

	private enum ListenTarget { NONE, SLOT_LOCK, CUSTOM }
	private ListenTarget listening = ListenTarget.NONE;
	private int listeningCustomIndex = -1;
	private boolean addingNew = false;
	private EditBox messageField;
	private float scrollOffset = 0;

	private final List<RowHitbox> hitboxes = new ArrayList<>();

	/**
	 * type: 0 = slot-lock key box, 1 = custom-entry key box, 2 = custom-entry delete button.
	 * index: irrelevant for type 0; the custom entry's CURRENT list index for types 1/2
	 * (safe to use here because hitboxes are rebuilt fresh from the live list every
	 * render — this is different from the long-lived map in AcquiredUtilsClient,
	 * which is why that one needed a stable id instead).
	 */
	private record RowHitbox(int x, int y, int w, int h, int type, int index) {}

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
		int btnW = s(100), btnH = s(16);
		addWidget(Button.builder(Component.translatable("acquiredutils.gui.add_keybind"), b -> {
			addingNew = true;
			screen.rebuild();
		}).bounds(contentX, contentY, btnW, btnH).build());

		if (addingNew) {
			int fieldW = s(140), fieldH = s(16);
			int fieldY = contentY + btnH + s(6);
			messageField = new EditBox(screen.getFont(), contentX, fieldY, fieldW, fieldH,
					Component.translatable("acquiredutils.gui.keybind_message_hint"));
			messageField.setMaxLength(256);
			messageField.setFocused(true);
			addWidget(messageField);

			addWidget(Button.builder(Component.translatable("acquiredutils.gui.confirm"), b -> confirmAdd())
					.bounds(contentX + fieldW + s(4), fieldY, s(60), fieldH).build());
		}
	}

	private void confirmAdd() {
		if (messageField == null) {
			return;
		}
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
		hitboxes.clear();

		int btnH = s(16);
		int listTop = contentY + btnH + s(6);
		if (addingNew) listTop += s(22);

		int clipY1 = listTop;
		int clipY2 = contentY + contentHeight;
		int rowH = s(24);
		AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

		int totalRows = 1 + cfg.customKeybinds.size();
		int totalH = totalRows * rowH;
		int visibleH = clipY2 - clipY1;

		if (scrollOffset < 0) scrollOffset = 0;
		if (totalH > visibleH) {
			if (scrollOffset > totalH - visibleH) scrollOffset = totalH - visibleH;
		} else {
			scrollOffset = 0;
		}

		graphics.enableScissor(contentX, clipY1, contentX + contentWidth, clipY2);

		int drawY = listTop - (int) scrollOffset;

		drawY = renderRow(graphics, mouseX, mouseY,
				Component.translatable("acquiredutils.gui.keybind.slot_lock"),
				cfg.slotLockKey, contentX, drawY, contentWidth, rowH, true, -1);

		for (int i = 0; i < cfg.customKeybinds.size(); i++) {
			var entry = cfg.customKeybinds.get(i);
			drawY = renderRow(graphics, mouseX, mouseY,
					Component.literal(entry.message), entry.keyCode,
					contentX, drawY, contentWidth, rowH, false, i);
		}

		graphics.disableScissor();

		if (totalH > visibleH) {
			int sbW = s(4);
			int sbX = contentX + contentWidth - sbW;
			int sbH = (int) (visibleH * ((float) visibleH / totalH));
			int sbY = clipY1 + (int) ((visibleH - sbH) * (scrollOffset / (float) (totalH - visibleH)));
			graphics.fill(sbX, clipY1, sbX + sbW, clipY2, 0x30FFFFFF);
			graphics.fill(sbX, sbY, sbX + sbW, sbY + sbH, 0x80FFFFFF);
		}
	}

	private int renderRow(GuiGraphics g, int mx, int my, Component label, int keyCode,
	                       int x, int y, int w, int h, boolean builtIn, int customIdx) {
		boolean isListening = (listening == ListenTarget.SLOT_LOCK && builtIn)
				|| (listening == ListenTarget.CUSTOM && !builtIn && customIdx == listeningCustomIndex);
		boolean hovered = my >= y && my < y + h && mx >= x && mx < x + w;

		if (hovered && listening == ListenTarget.NONE) {
			g.fill(x, y, x + w, y + h, COLOR_ROW_HOVER);
		}

		g.drawString(screen.getFont(), label, x, y + (h - 8) / 2, 0xFFF2F2F2, false);

		int keyBtnW = s(80), keyBtnH = s(16);
		int keyBtnX = x + w - keyBtnW - (builtIn ? 0 : s(22));
		int keyBtnY = y + (h - keyBtnH) / 2;

		int keyColor = isListening ? COLOR_LISTENING : (keyCode < 0 ? COLOR_NONE : 0xFFF2F2F2);
		String keyText = isListening ? "..." : getKeyName(keyCode);

		g.fill(keyBtnX, keyBtnY, keyBtnX + keyBtnW, keyBtnY + keyBtnH, 0xFF1A1A1A);
		g.renderOutline(keyBtnX, keyBtnY, keyBtnW, keyBtnH, isListening ? COLOR_LISTENING : 0xFF5A5A5A);

		int tw = screen.getFont().width(keyText);
		g.drawString(screen.getFont(), keyText, keyBtnX + (keyBtnW - tw) / 2,
				keyBtnY + (keyBtnH - 8) / 2, keyColor, false);

		if (!builtIn) {
			int ds = s(14);
			int dx = x + w - ds;
			int dy = y + (h - ds) / 2;
			boolean dHover = mx >= dx && mx < dx + ds && my >= dy && my < dy + ds;
			int dc = dHover ? 0xFFFF5555 : 0xFFAA4444;
			g.fill(dx, dy, dx + ds, dy + ds, dc);
			g.renderOutline(dx, dy, ds, ds, 0xFFCC3333);
			int xw = screen.getFont().width("x");
			g.drawString(screen.getFont(), "x", dx + (ds - xw) / 2, dy + (ds - 8) / 2, 0xFFF2F2F2, false);
		}

		hitboxes.add(new RowHitbox(keyBtnX, keyBtnY, keyBtnW, keyBtnH, builtIn ? 0 : 1, customIdx));
		if (!builtIn) {
			int ds = s(14);
			hitboxes.add(new RowHitbox(x + w - ds, y + (h - ds) / 2, ds, ds, 2, customIdx));
		}

		return y + h;
	}

	private String getKeyName(int keyCode) {
		if (keyCode < 0) return "[NONE]";
		return InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (listening != ListenTarget.NONE) {
			// Only swallow this click if it's actually on the row being edited.
			// Previously this swallowed EVERY click anywhere on screen while
			// listening, which silently blocked the X close button too.
			boolean withinListeningRow = isWithinListeningRow(mouseX, mouseY);
			stopListening();
			return withinListeningRow;
		}

		for (RowHitbox box : hitboxes) {
			if (mouseX >= box.x() && mouseX < box.x() + box.w()
					&& mouseY >= box.y() && mouseY < box.y() + box.h()) {

				if (box.type() == 0) {
					startListening(ListenTarget.SLOT_LOCK, -1);
					return true;
				} else if (box.type() == 1) {
					startListening(ListenTarget.CUSTOM, box.index());
					return true;
				} else if (box.type() == 2) {
					AcquiredUtilsConfig.get().customKeybinds.remove(box.index());
					AcquiredUtilsClient.syncCustomKeybinds();
					screen.rebuild();
					return true;
				}
			}
		}
		return false;
	}

	private boolean isWithinListeningRow(double mouseX, double mouseY) {
		for (RowHitbox box : hitboxes) {
			boolean isKeyBox = box.type() == 0 || box.type() == 1;
			boolean matchesTarget = (listening == ListenTarget.SLOT_LOCK && box.type() == 0)
					|| (listening == ListenTarget.CUSTOM && box.type() == 1 && box.index() == listeningCustomIndex);
			if (isKeyBox && matchesTarget
					&& mouseX >= box.x() && mouseX < box.x() + box.w()
					&& mouseY >= box.y() && mouseY < box.y() + box.h()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scrollOffset -= (float) (scrollY * 20 * scale());
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (listening != ListenTarget.NONE) {
			AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();
			int keyCode = InputConstants.getKey(event).getValue();

			if (keyCode == 256) { // Escape: unbind instead of assigning Escape itself
				keyCode = -1;
			}

			if (listening == ListenTarget.SLOT_LOCK) {
				cfg.slotLockKey = keyCode;
				AcquiredUtilsClient.syncSlotLockKeybind();
			} else if (listeningCustomIndex >= 0 && listeningCustomIndex < cfg.customKeybinds.size()) {
				cfg.customKeybinds.get(listeningCustomIndex).keyCode = keyCode;
				AcquiredUtilsClient.syncCustomKeybinds();
			}

			stopListening();
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

	private void startListening(ListenTarget target, int idx) {
		this.listening = target;
		this.listeningCustomIndex = idx;
	}

	private void stopListening() {
		this.listening = ListenTarget.NONE;
		this.listeningCustomIndex = -1;
	}

	@Override
	public void onClose() {
		stopListening();
		AcquiredUtilsClient.syncSlotLockKeybind();
		AcquiredUtilsClient.syncCustomKeybinds();
	}
}