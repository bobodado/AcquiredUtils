package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/**
 * Slider widget with 6px track, 8x16 knob.
 * Track bg: 0xFF303036, fill: 0xFFA368EF, knob: 0xFFE8E8EC.
 * Value label to the right of track.
 */
public class SliderSettingWidget extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private static final int TRACK_HEIGHT = 6;
    private static final int KNOB_WIDTH = 8;
    private static final int KNOB_HEIGHT = 16;
    private static final int TRACK_BG = 0xFF303036;
    private static final int TRACK_FILL = 0xFFA368EF;
    private static final int KNOB_COLOR = 0xFFE8E8EC;
    private static final int VALUE_TEXT_COLOR = 0xFFB0B0B8;

    private boolean dragging = false;
    private int trackX, trackY, trackWidth;
    private final Consumer<Double> onChanged;

    public SliderSettingWidget(String name, String description, double defaultValue, double min, double max, double step) {
        this(name, description, defaultValue, min, max, step, null);
    }

    public SliderSettingWidget(String name, String description, double defaultValue, double min, double max, double step, Consumer<Double> onChanged) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.onChanged = onChanged;
    }

    private double getClampedValue() {
        return Math.max(min, Math.min(max, pendingValue));
    }

    private double snapToStep(double val) {
        if (step <= 0) return val;
        return Math.round((val - min) / step) * step + min;
    }

    private double valueFromMouse(double mouseX) {
        double pct = (mouseX - trackX) / (double) trackWidth;
        pct = Math.max(0, Math.min(1, pct));
        double val = min + pct * (max - min);
        return snapToStep(val);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, float delta) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + getHeight();

        // Layout: name on left, track in middle, value on right
        int nameWidth = Minecraft.getInstance().font.width(name);
        int valueWidth = 50;
        int padding = 8;

        trackX = x + nameWidth + padding;
        trackY = y + (getHeight() - TRACK_HEIGHT) / 2;
        trackWidth = width - nameWidth - valueWidth - padding * 3;

        // Draw name
        drawName(graphics, x, y);

        // Draw track background
        int trackRight = trackX + trackWidth;
        graphics.fill(trackX, trackY, trackRight, trackY + TRACK_HEIGHT, TRACK_BG);

        // Draw track fill
        double pct = (getClampedValue() - min) / (max - min);
        int fillWidth = (int) (trackWidth * pct);
        if (fillWidth > 0) {
            graphics.fill(trackX, trackY, trackX + fillWidth, trackY + TRACK_HEIGHT, TRACK_FILL);
        }

        // Draw knob
        int knobX = trackX + fillWidth - KNOB_WIDTH / 2;
        int knobY = y + (getHeight() - KNOB_HEIGHT) / 2;
        graphics.fill(knobX, knobY, knobX + KNOB_WIDTH, knobY + KNOB_HEIGHT, KNOB_COLOR);

        // Draw value label
        String valueStr = String.format("%.2f", getClampedValue());
        graphics.drawString(Minecraft.getInstance().font, valueStr, trackRight + padding, y + 6, VALUE_TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= trackX && mouseX <= trackX + trackWidth
            && mouseY >= trackY - 4 && mouseY <= trackY + TRACK_HEIGHT + 4) {
            dragging = true;
            pendingValue = valueFromMouse(mouseX);
            if (onChanged != null) {
                onChanged.accept(pendingValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            pendingValue = valueFromMouse(mouseX);
            if (onChanged != null) {
                onChanged.accept(pendingValue);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            pendingValue = valueFromMouse(mouseX);
            if (onChanged != null) {
                onChanged.accept(pendingValue);
            }
            return true;
        }
        return false;
    }
}