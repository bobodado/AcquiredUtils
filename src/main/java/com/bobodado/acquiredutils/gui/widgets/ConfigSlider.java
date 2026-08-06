package com.bobodado.acquiredutils.gui.widgets;

import com.bobodado.acquiredutils.gui.ModConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * Custom slider extending AbstractSliderButton for numeric values.
 * Mojang mappings: AbstractSliderButton (was SliderWidget in Yarn).
 */
public class ConfigSlider extends AbstractSliderButton {

    private final double minValue;
    private final double maxValue;
    private final double step;
    private final Consumer<Double> onChange;

    public ConfigSlider(int x, int y, int width, int height,
                        double initialValue, double min, double max, double step,
                        Consumer<Double> onChange) {
        super(x, y, width, height, Component.empty(), 
              Mth.clamp((initialValue - min) / (max - min), 0.0, 1.0));
        this.minValue = min;
        this.maxValue = max;
        this.step = step;
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        double current = getCurrentValue();
        setMessage(Component.literal(String.format("%.1f", current)));
    }

    @Override
    protected void applyValue() {
        double current = getCurrentValue();
        if (onChange != null) {
            onChange.accept(current);
        }
    }

    private double getCurrentValue() {
        double raw = minValue + (maxValue - minValue) * this.value;
        return Math.round(raw / step) * step;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Track background
        int trackY = y + (h - 4) / 2;
        graphics.fill(x, trackY, x + w, trackY + 4, 0xFF3E3B36);
        graphics.fill(x, trackY, x + w, trackY + 1, 0xFF2A2825);
        graphics.fill(x, trackY + 3, x + w, trackY + 4, 0xFF4A4845);

        // Handle
        int handleWidth = 8;
        int handleHeight = 12;
        int handleX = x + (int) (this.value * (w - handleWidth));
        int handleY = y + (h - handleHeight) / 2;

        int handleColor = isHovered() ? 0xFF6A6A6A : 0xFF5A5A5A;
        graphics.fill(handleX, handleY, handleX + handleWidth, handleY + handleHeight, handleColor);
        graphics.fill(handleX, handleY, handleX + handleWidth, handleY + 1, 0xFF707070);
        graphics.fill(handleX, handleY, handleX + 1, handleY + handleHeight, 0xFF707070);
        graphics.fill(handleX, handleY + handleHeight - 1, handleX + handleWidth, handleY + handleHeight, 0xFF404040);
        graphics.fill(handleX + handleWidth - 1, handleY, handleX + handleWidth, handleY + handleHeight, 0xFF404040);
    }
}
