package dev.bobodado.acquiredutils.client.gui.widget;

import dev.bobodado.acquiredutils.client.gui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ExampleSliderWidget extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final Consumer<Float> onChange;

    public ExampleSliderWidget(
        int x,
        int y,
        int width,
        int height,
        float initialValue,
        Consumer<Float> onChange
    ) {
        this(x, y, width, height, initialValue, 0.1f, 5.0f, onChange);
    }

    public ExampleSliderWidget(
        int x,
        int y,
        int width,
        int height,
        float initialValue,
        float min,
        float max,
        Consumer<Float> onChange
    ) {
        super(
            x,
            y,
            width,
            height,
            Component.literal(format(initialValue, min, max)),
            toNormalized(initialValue, min, max)
        );

        this.min = min;
        this.max = max;
        this.onChange = onChange;
    }

    private static double toNormalized(float value, double min, double max) {
        double clamped = Math.max(min, Math.min(max, value));
        return (clamped - min) / (max - min);
    }

    private float value() {
        return (float) (min + this.value * (max - min));
    }

    private static String format(float value, double min, double max) {
        if (min == 0.5 && max == 2.0) {
            return String.format("%.2f", value);
        }

        return String.format("%.1f", value);
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(format(value(), min, max)));
    }

    @Override
    protected void applyValue() {
        onChange.accept(value());
    }

    @Override
    public void renderWidget(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        Theme theme = Theme.current();
        var font = Minecraft.getInstance().font;

        int trackY = getY() + Math.max(1, height / 2 - 2);

        graphics.fill(
            getX(),
            trackY,
            getX() + width,
            trackY + 4,
            theme.footerBottom
        );

        int filled = (int) (this.value * width);

        if (filled > 0) {
            graphics.fill(
                getX(),
                trackY,
                getX() + filled,
                trackY + 4,
                theme.accent
            );
        }

        graphics.renderOutline(
            getX(),
            getY(),
            width,
            height,
            theme.frameMid
        );

        int handleX = getX() + (int) (this.value * (width - 6));

        graphics.fill(
            handleX,
            getY() + 1,
            handleX + 6,
            getY() + height - 1,
            theme.accentBright
        );

        graphics.renderOutline(
            handleX,
            getY() + 1,
            6,
            height - 2,
            theme.frameOuter
        );

        int textWidth = font.width(getMessage());

        graphics.drawString(
            font,
            getMessage(),
            getX() + (width - textWidth) / 2,
            getY() + (height - 8) / 2,
            theme.text,
            false
        );
    }
}
