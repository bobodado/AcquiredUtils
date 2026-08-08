package dev.bobodado.acquiredutils.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;
import java.util.function.Consumer;

/**
 * A single row for a player-added "press key -> send chat message" entry:
 * message text  ......  [key box]  [x delete].
 * <p>
 * Same reliable pattern as LockedKeybindWidget — real AbstractWidget with
 * its own onClick(), arming a shared KeyListenerSlot instead of the old
 * hand-rolled RowHitbox/mouseClicked hit-testing that KeybindsSection used
 * to do manually. That old approach is what caused the "key press doesn't
 * register until you click somewhere else" bug — this widget-based version
 * uses the exact same click dispatch path the checkbox/slider/dropdown
 * already rely on successfully.
 */
public class CustomKeybindRowWidget extends AbstractWidget implements KeyListenerSlot.Listener {

	private static final int COLOR_LISTENING = 0xFFE38A2D;
	private static final int COLOR_NONE = 0xFF666666;
	private static final int COLOR_TEXT = 0xFFF2F2F2;
	private static final int KEY_BOX_WIDTH = 80;
	private static final int DELETE_SIZE = 14;

	private final KeyListenerSlot slot;
	private final IntSupplier keyGetter;
	private final Consumer<Integer> keySetter;
	private final Runnable onDelete;

	public CustomKeybindRowWidget(int x, int y, int width, int height, Component message, KeyListenerSlot slot,
	                               IntSupplier keyGetter, Consumer<Integer> keySetter, Runnable onDelete) {
		super(x, y, width, height, message);
		this.slot = slot;
		this.keyGetter = keyGetter;
		this.keySetter = keySetter;
		this.onDelete = onDelete;
	}

	@Override
	public void applyKeyCode(int keyCode) {
		keySetter.accept(keyCode);
		if (slot.isListening(this)) {
			slot.clear();
		}
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		var font = Minecraft.getInstance().font;
		boolean listening = slot.isListening(this);

		graphics.drawString(font, getMessage(), getX(), getY() + (height - 8) / 2, COLOR_TEXT, false);

		int keyBoxX = getX() + width - KEY_BOX_WIDTH - DELETE_SIZE - 6;
		int keyCode = keyGetter.getAsInt();
		String keyText = listening ? "..." : (keyCode < 0 ? "[NONE]"
				: InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString());
		int keyColor = listening ? COLOR_LISTENING : (keyCode < 0 ? COLOR_NONE : COLOR_TEXT);

		graphics.fill(keyBoxX, getY(), keyBoxX + KEY_BOX_WIDTH, getY() + height, 0xFF1A1A1A);
		graphics.renderOutline(keyBoxX, getY(), KEY_BOX_WIDTH, height, listening ? COLOR_LISTENING : 0xFF5A5A5A);
		int tw = font.width(keyText);
		graphics.drawString(font, keyText, keyBoxX + (KEY_BOX_WIDTH - tw) / 2, getY() + (height - 8) / 2, keyColor, false);

		int dx = getX() + width - DELETE_SIZE;
		int dy = getY() + (height - DELETE_SIZE) / 2;
		boolean dHover = mouseX >= dx && mouseX < dx + DELETE_SIZE && mouseY >= dy && mouseY < dy + DELETE_SIZE;
		int dc = dHover ? 0xFFFF5555 : 0xFFAA4444;
		graphics.fill(dx, dy, dx + DELETE_SIZE, dy + DELETE_SIZE, dc);
		graphics.renderOutline(dx, dy, DELETE_SIZE, DELETE_SIZE, 0xFFCC3333);
		int xw = font.width("x");
		graphics.drawString(font, "x", dx + (DELETE_SIZE - xw) / 2, dy + (DELETE_SIZE - 8) / 2, COLOR_TEXT, false);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();

		int dx = getX() + width - DELETE_SIZE;
		int dy = getY() + (height - DELETE_SIZE) / 2;
		boolean onDeleteBtn = mouseX >= dx && mouseX < dx + DELETE_SIZE && mouseY >= dy && mouseY < dy + DELETE_SIZE;
		if (onDeleteBtn) {
			onDelete.run();
			return;
		}

		int keyBoxX = getX() + width - KEY_BOX_WIDTH - DELETE_SIZE - 6;
		boolean onKeyBox = mouseX >= keyBoxX && mouseX < keyBoxX + KEY_BOX_WIDTH
				&& mouseY >= getY() && mouseY < getY() + height;
		if (onKeyBox) {
			slot.current = this;
		}
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, getMessage());
	}
}