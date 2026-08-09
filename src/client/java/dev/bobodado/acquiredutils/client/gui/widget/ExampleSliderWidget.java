package dev.bobodado.acquiredutils.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Custom-rendered slider matching the "Ancient Forge" theme, replacing the
 * inherited vanilla AbstractSliderButton look (plain gray bar + handle).
 * Overriding renderWidget() here is valid Java even though
 * AbstractSliderButton already provides a concrete implementation of it —
 * this subclass's override simply takes precedence. Nothing about the
 * value/min/max/onChange logic below changed from before.
 */
public class ExampleSliderWidget extends AbstractSliderButton {

	private static final int COLOR_TRACK_BG = 0xFF1F1611;
	private static final int COLOR_TRACK_BORDER = 0xFF8B5A2B;
	private static final int COLOR_FILL = 0xFF6B4A2E;
	private static final int COLOR_HANDLE = 0xFFD98F3E;
	private static final int COLOR_HANDLE_BORDER = 0xFF1F1611;
	private static final int COLOR_TEXT = 0xFFF2F2F2;
	private static final int HANDLE_WIDTH = 4;

	private final double min;
	private final double max;
	private final Consumer<Float> onChange;

	public ExampleSliderWidget(int x, int y, int width, int height, float initialValue,
	                           float min, float max, Consumer<Float> onChange) {
		super(x, y, width, height, Component.literal(format(initialValue, min, max)), toNormalized(initialValue, min, max));
		this.min = min;
		this.max = max;
		this.onChange = onChange;
	}

	/** Backward-compatible constructor for the original 0.1–5.0 slider. */
	public ExampleSliderWidget(int x, int y, int width, int height, float initialValue, Consumer<Float> onChange) {
		this(x, y, width, height, initialValue, 0.1f, 5.0f, onChange);
	}

	private static double toNormalized(float value, double min, double max) {
		double clamped = Math.max(min, Math.min(max, value));
		return (clamped - min) / (max - min);
	}

	private float fromNormalized() {
		return (float) (min + this.value * (max - min));
	}

	private static String format(float value, double min, double max) {
		if (min == 0.5 && max == 2.0) {
			return String.format("%.2f", value);
		}
		return String.format("%.1f", value);
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(getX(), getY(), getX() + width, getY() + height, COLOR_TRACK_BG);
		graphics.renderOutline(getX(), getY(), width, height, COLOR_TRACK_BORDER);

		int handleX = getX() + (int) (this.value * (width - HANDLE_WIDTH));

		// Filled portion behind the handle, showing progress along the track.
		if (handleX > getX()) {
			graphics.fill(getX() + 1, getY() + 1, handleX, getY() + height - 1, COLOR_FILL);
		}

		graphics.fill(handleX, getY(), handleX + HANDLE_WIDTH, getY() + height, COLOR_HANDLE);
		graphics.renderOutline(handleX, getY(), HANDLE_WIDTH, height, COLOR_HANDLE_BORDER);

		var font = net.minecraft.client.Minecraft.getInstance().font;
		int tw = font.width(getMessage());
		graphics.drawString(font, getMessage(), getX() + (width - tw) / 2, getY() + (height - 8) / 2, COLOR_TEXT, false);
	}

	@Override
	protected void updateMessage() {
		setMessage(Component.literal(format(fromNormalized(), min, max)));
	}

	@Override
	protected void applyValue() {
		onChange.accept(fromNormalized());
	}
}