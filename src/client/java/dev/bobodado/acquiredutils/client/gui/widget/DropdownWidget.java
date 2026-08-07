package dev.bobodado.acquiredutils.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Custom select/dropdown widget — matches the layout map's DropdownWidget spec
 * (§4.7): a closed field with a ▼ arrow that expands downward to show all
 * options, with the currently-selected option highlighted.
 * <p>
 * No vanilla Minecraft widget provides this, so this is a hand-built
 * {@link AbstractWidget}. Colors below reference the placeholder palette from
 * the layout map (§6) — replace the literal ARGB ints with your real theme
 * once finalized; consider centralizing them in a small Theme/Colors class if
 * more widgets need them.
 * <p>
 * NOTE: if this still fails to compile with "does not override or implement
 * a method from a supertype" on updateWidgetNarration, double-check your
 * IDE didn't auto-import a same-named NarrationElementOutput/AbstractWidget
 * from Cloth Config or YACL (both are on this project's classpath) instead
 * of the vanilla net.minecraft.client.gui.narration.NarrationElementOutput
 * imported explicitly below.
 */
public class DropdownWidget extends AbstractWidget {

	private static final int COLOR_FIELD_BG = 0xFF141414;
	private static final int COLOR_BORDER = 0xFF5A5A5A;
	private static final int COLOR_OPEN_BG = 0xFF181818;
	private static final int COLOR_TEXT = 0xFFF2F2F2;
	private static final int COLOR_SELECTION_BG = 0xFF4B2C6B; // "dark purple" from reference
	private static final int ROW_HEIGHT = 12;

	private final List<Component> options;
	private final Consumer<Integer> onSelect;
	private int selectedIndex;
	private boolean open = false;

	public DropdownWidget(int x, int y, int width, int height, List<Component> options,
	                       int initialSelectedIndex, Consumer<Integer> onSelect) {
		super(x, y, width, height, options.get(initialSelectedIndex));
		this.options = options;
		this.selectedIndex = initialSelectedIndex;
		this.onSelect = onSelect;
	}

	public boolean isOpen() {
		return open;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Closed field
		graphics.fill(getX(), getY(), getX() + width, getY() + height, COLOR_FIELD_BG);
		graphics.renderOutline(getX(), getY(), width, height, COLOR_BORDER);
		graphics.drawString(Minecraft.getInstance().font,
				options.get(selectedIndex), getX() + 4, getY() + (height - 8) / 2, COLOR_TEXT, false);
		// ▼ arrow, right-aligned
		graphics.drawString(Minecraft.getInstance().font,
				"\u25BE", getX() + width - 10, getY() + (height - 8) / 2, COLOR_TEXT, false);

		if (open) {
			int listY = getY() + height;
			int listHeight = options.size() * ROW_HEIGHT;
			graphics.fill(getX(), listY, getX() + width, listY + listHeight, COLOR_OPEN_BG);
			graphics.renderOutline(getX(), listY, width, listHeight, COLOR_BORDER);

			for (int i = 0; i < options.size(); i++) {
				int rowY = listY + i * ROW_HEIGHT;
				if (i == selectedIndex) {
					graphics.fill(getX() + 1, rowY, getX() + width - 1, rowY + ROW_HEIGHT, COLOR_SELECTION_BG);
				}
				graphics.drawString(Minecraft.getInstance().font,
						options.get(i), getX() + 4, rowY + 2, COLOR_TEXT, false);
			}
		}
	}

@Override
public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
	double mouseX = event.x();
	double mouseY = event.y();

	int listY = getY() + height;

	if (!open) {
		open = true;
		return;
	}

	int relativeY = (int) mouseY - listY;
	if (relativeY < 0) {
		// Click landed back on the closed field — just toggle shut.
		open = false;
		return;
	}
	int index = relativeY / ROW_HEIGHT;
	if (index >= 0 && index < options.size()) {
		selectedIndex = index;
		setMessage(options.get(index));
		onSelect.accept(index);
	}
	open = false;
}
	/** Total height including the open list, for layout code that needs to reserve space. */
	public int getExpandedHeight() {
		return open ? height + options.size() * ROW_HEIGHT : height;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, options.get(selectedIndex));
	}
}