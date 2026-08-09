package dev.bobodado.acquiredutils.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * A themed replacement for vanilla Button, matching the "Ancient Forge"
 * palette used by AcquiredUtilsConfigScreen. Same proven pattern as
 * DropdownWidget/LockedKeybindWidget — a plain AbstractWidget with its own
 * renderWidget()/onClick(), no unverified GuiGraphics API. Used for the
 * header close button and the sidebar section tabs.
 */
public class ThemedButtonWidget extends AbstractWidget {

	private static final int COLOR_BG_TOP = 0xFF3A2A1E;
	private static final int COLOR_BG_BOTTOM = 0xFF241A14;
	private static final int COLOR_BG_HOVER_TOP = 0xFF4A3626;
	private static final int COLOR_BG_HOVER_BOTTOM = 0xFF2E2119;
	private static final int COLOR_BORDER = 0xFF8B5A2B;
	private static final int COLOR_BORDER_HOVER = 0xFFD98F3E;
	private static final int COLOR_TEXT = 0xFFF2F2F2;

	private final Runnable clickHandler;
	private final boolean bold;

	public ThemedButtonWidget(int x, int y, int width, int height, Component label, Runnable clickHandler) {
		this(x, y, width, height, label, clickHandler, false);
	}

	public ThemedButtonWidget(int x, int y, int width, int height, Component label, Runnable clickHandler, boolean bold) {
		super(x, y, width, height, label);
		this.clickHandler = clickHandler;
		this.bold = bold;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isHovered();

		int topColor = hovered ? COLOR_BG_HOVER_TOP : COLOR_BG_TOP;
		int bottomColor = hovered ? COLOR_BG_HOVER_BOTTOM : COLOR_BG_BOTTOM;
		int rows = Math.max(1, height);
		for (int row = 0; row < rows; row++) {
			float t = rows <= 1 ? 0f : (float) row / (rows - 1);
			int color = lerp(topColor, bottomColor, t);
			graphics.fill(getX(), getY() + row, getX() + width, getY() + row + 1, color);
		}

		graphics.renderOutline(getX(), getY(), width, height, hovered ? COLOR_BORDER_HOVER : COLOR_BORDER);

		var font = Minecraft.getInstance().font;
		Component text = bold ? getMessage().copy().withStyle(Style.EMPTY.withBold(true)) : getMessage();
		int tw = font.width(text);
		int tx = getX() + (width - tw) / 2;
		int ty = getY() + (height - 8) / 2;
		graphics.drawString(font, text, tx, ty, COLOR_TEXT, false);
	}

	private static int lerp(int colorA, int colorB, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int a1 = (colorA >> 24) & 0xFF, r1 = (colorA >> 16) & 0xFF, g1 = (colorA >> 8) & 0xFF, b1 = colorA & 0xFF;
		int a2 = (colorB >> 24) & 0xFF, r2 = (colorB >> 16) & 0xFF, g2 = (colorB >> 8) & 0xFF, b2 = colorB & 0xFF;
		int a = (int) (a1 + (a2 - a1) * t);
		int r = (int) (r1 + (r2 - r1) * t);
		int g = (int) (g1 + (g2 - g1) * t);
		int b = (int) (b1 + (b2 - b1) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		clickHandler.run();
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, getMessage());
	}
}