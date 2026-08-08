package dev.bobodado.acquiredutils.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * A single "built-in feature" row: [on/off checkbox]  Label  ......  [key box].
 * <p>
 * A plain AbstractWidget, dropped into any ModSection's buildContent() next
 * to its other normal widgets — participates in the screen's existing
 * widget dispatch exactly like a checkbox/slider/dropdown does. Represents a
 * fixed, built-in mod feature (e.g. "Slot Lock") the player can toggle and
 * rebind, but never delete.
 * <p>
 * Key capture uses KeyListenerSlot (see that class for why) — clicking the
 * key box arms this widget into the shared slot; the owning section's
 * keyPressed(KeyEvent) forwards the key to whichever widget is current.
 */
public class LockedKeybindWidget extends AbstractWidget implements KeyListenerSlot.Listener {

	private static final int COLOR_LISTENING = 0xFFE38A2D;
	private static final int COLOR_NONE = 0xFF666666;
	private static final int COLOR_TEXT = 0xFFF2F2F2;
	private static final int CHECKBOX_SIZE = 10;
	private static final int KEY_BOX_WIDTH = 70;

	private final KeyListenerSlot slot;
	private final BooleanSupplier enabledGetter;
	private final Consumer<Boolean> enabledSetter;
	private final IntSupplier keyGetter;
	private final Consumer<Integer> keySetter;

	public LockedKeybindWidget(int x, int y, int width, int height, Component label, KeyListenerSlot slot,
	                            BooleanSupplier enabledGetter, Consumer<Boolean> enabledSetter,
	                            IntSupplier keyGetter, Consumer<Integer> keySetter) {
		super(x, y, width, height, label);
		this.slot = slot;
		this.enabledGetter = enabledGetter;
		this.enabledSetter = enabledSetter;
		this.keyGetter = keyGetter;
		this.keySetter = keySetter;
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
		boolean enabled = enabledGetter.getAsBoolean();
		boolean listening = slot.isListening(this);

		int cbY = getY() + (height - CHECKBOX_SIZE) / 2;
		graphics.fill(getX(), cbY, getX() + CHECKBOX_SIZE, cbY + CHECKBOX_SIZE, 0xFF141414);
		graphics.renderOutline(getX(), cbY, CHECKBOX_SIZE, CHECKBOX_SIZE, 0xFF5A5A5A);
		if (enabled) {
			graphics.fill(getX() + 2, cbY + 2, getX() + CHECKBOX_SIZE - 2, cbY + CHECKBOX_SIZE - 2, 0xFF3FA34D);
		}

		int labelX = getX() + CHECKBOX_SIZE + 6;
		graphics.drawString(font, getMessage(), labelX, getY() + (height - 8) / 2, COLOR_TEXT, false);

		int keyBoxX = getX() + width - KEY_BOX_WIDTH;
		int keyBoxY = getY();
		int keyCode = keyGetter.getAsInt();
		String keyText = listening ? "..." : (keyCode < 0 ? "[NONE]"
				: InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString());
		int keyColor = listening ? COLOR_LISTENING : (keyCode < 0 ? COLOR_NONE : COLOR_TEXT);

		graphics.fill(keyBoxX, keyBoxY, keyBoxX + KEY_BOX_WIDTH, keyBoxY + height, 0xFF1A1A1A);
		graphics.renderOutline(keyBoxX, keyBoxY, KEY_BOX_WIDTH, height, listening ? COLOR_LISTENING : 0xFF5A5A5A);
		int tw = font.width(keyText);
		graphics.drawString(font, keyText, keyBoxX + (KEY_BOX_WIDTH - tw) / 2, getY() + (height - 8) / 2, keyColor, false);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();

		int cbY = getY() + (height - CHECKBOX_SIZE) / 2;
		boolean onCheckbox = mouseX >= getX() && mouseX < getX() + CHECKBOX_SIZE
				&& mouseY >= cbY && mouseY < cbY + CHECKBOX_SIZE;
		if (onCheckbox) {
			enabledSetter.accept(!enabledGetter.getAsBoolean());
			return;
		}

		int keyBoxX = getX() + width - KEY_BOX_WIDTH;
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