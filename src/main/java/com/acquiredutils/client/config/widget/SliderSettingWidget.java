package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * NEU-style slider. Thicker track, larger knob, proper vertical centreing,
 * value label on the right, exact NEU colour scheme.
 */
public class SliderSettingWidget extends Setting<Double> {

    private static final int TRACK_H = 6;
    private static final int KNOB_W = 8;
    private static final int KNOB_H = 16;
    private static final int COLOR_TRACK_BG = 0xFF303036;
    private static final int COLOR_TRACK_FILL = 0xFFA368EF;
    private static final int COLOR_KNOB = 0xFFE8E8EC;

    private final double min;
    private final double max;
    private final double step;

    private boolean dragging = false;
    private int lastTrackX = 0;
    private int lastTrackW = 0;

    public SliderSettingWidget(String name, String description, double defaultValue,
                               double min, double max, double step, Consumer<Double> onChange) {
        super(name, description, clamp(defaultValue, min, max), onChange);
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 1e-6 : step;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double progress() {
        return (value - min) / (max - min);
    }

    @Override
    public void render(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        // Name: left column, beside a small spacer (same indent as bool button)
        int nameX = x + 10 + 14 + 8;
        g.drawString(font, name, nameX, y + getHeight() / 2 - font.lineHeight / 2, 0xFFc0c0c0, false);

        // Description: right 2/3, vertically centred
        int descX = x + 5 + width / 3;
        int descW = width * 2 / 3 - 10;
        int lineCount = font.split(Component.literal(description), descW).size();
        int paraH = font.lineHeight * lineCount;
        g.drawWordWrap(font, Component.literal(description), descX, y + getHeight() / 2 - paraH / 2, descW, 0xFFc0c0c0);

        // Slider track sits in the left column under the name
        int trackW = Math.max(60, width / 6 - 10);
        int trackX = x + 10 + 14 + 8 + width / 6 - trackW / 2 - (14 + 8) / 2;
        int trackY = y + getHeight() - TRACK_H - 8;
        lastTrackX = trackX;
        lastTrackW = trackW;

        g.fill(trackX, trackY, trackX + trackW, trackY + TRACK_H, COLOR_TRACK_BG);

        double p = clamp(progress(), 0, 1);
        int filledW = (int) Math.round(trackW * p);
        g.fill(trackX, trackY, trackX + filledW, trackY + TRACK_H, COLOR_TRACK_FILL);

        int knobX = trackX + filledW - KNOB_W / 2;
        int knobY = trackY + TRACK_H / 2 - KNOB_H / 2;
        g.fill(knobX, knobY, knobX + KNOB_W, knobY + KNOB_H, COLOR_KNOB);

        // Value text to the right of the track
        String valueText = formatValue(value);
        g.drawString(font, valueText, trackX + trackW + 8, trackY + TRACK_H / 2 - font.lineHeight / 2, 0xFFa0a0a0, false);
    }

    private String formatValue(double v) {
        if (step >= 1) return String.valueOf(Math.round(v));
        return String.format("%.2f", v);
    }

    private double snap(double raw) {
        double snapped = min + Math.round((raw - min) / step) * step;
        return clamp(snapped, min, max);
    }

    private void updateFromMouseX(double mouseX) {
        double p = clamp((mouseX - lastTrackX) / (double) lastTrackW, 0, 1);
        double raw = min + p * (max - min);
        updateValue(snap(raw));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (button != 0) return false;
        int trackW = Math.max(60, width / 6 - 10);
        int trackX = x + 10 + 14 + 8 + width / 6 - trackW / 2 - (14 + 8) / 2;
        int trackY = y + getHeight() - TRACK_H - 8;

        if (mouseX >= trackX - KNOB_W && mouseX <= trackX + trackW + KNOB_W &&
            mouseY >= trackY - KNOB_H && mouseY <= trackY + TRACK_H + KNOB_H) {
            lastTrackX = trackX;
            lastTrackW = trackW;
            dragging = true;
            updateFromMouseX(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging) {
            updateFromMouseX(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean was = dragging;
        dragging = false;
        return was;
    }
}