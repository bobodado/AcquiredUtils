package dev.bobodado.acquiredutils.client.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ExampleSliderWidget extends AbstractSliderButton {

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
        // Show fewer decimals for menu scale (0.5–2.0) vs example (0.1–5.0)
        if (min == 0.5 && max == 2.0) {
            return String.format("%.2f", value);
        }
        return String.format("%.1f", value);
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