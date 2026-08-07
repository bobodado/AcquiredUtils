package dev.bobodado.acquiredutils.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Item 2 in the settings list: "Example" slider, range 0.1–5.0 (layout map
 * §4.6). Extends vanilla {@link AbstractSliderButton}, which internally
 * stores position as a normalized 0.0–1.0 double — this class maps that onto
 * the 0.1–5.0 range and shows the live value in the widget label.
 * <p>
 * VERIFY: AbstractSliderButton's constructor signature and abstract methods
 * (updateMessage / applyValue) for 1.21.11 — confirmed stable across many
 * past versions, but re-check against the actual artifact.
 */
public class ExampleSliderWidget extends AbstractSliderButton {

	private static final double MIN = 0.1;
	private static final double MAX = 5.0;

	private final Consumer<Float> onChange;

	public ExampleSliderWidget(int x, int y, int width, int height, float initialValue, Consumer<Float> onChange) {
		super(x, y, width, height, Component.literal(format(initialValue)), toNormalized(initialValue));
		this.onChange = onChange;
	}

	private static double toNormalized(float value) {
		double clamped = Math.max(MIN, Math.min(MAX, value));
		return (clamped - MIN) / (MAX - MIN);
	}

	private float fromNormalized() {
		return (float) (MIN + this.value * (MAX - MIN));
	}

	private static String format(float value) {
		return String.format("%.1f", value);
	}

	@Override
	protected void updateMessage() {
		setMessage(Component.literal(format(fromNormalized())));
	}

	@Override
	protected void applyValue() {
		onChange.accept(fromNormalized());
	}
}
