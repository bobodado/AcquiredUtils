package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A draggable slider bound to a double value, snapped to {@code step}.
 * <p>
 * NEU's {@code GuiElementSlider} rendered a filled bar behind a knob and
 * tracked drag deltas against the mouse; this reproduces that behaviour
 * with modern GuiGraphics fills instead of the legacy immediate-mode
 * quads NEU used.
 */
public class SliderSettingWidget extends Setting<Double> {

    private static final int TRACK_H = 4;
    private static final int KNOB_W = 6;
    private static final int KNOB_H = 14;
    private static final int COLOR_TRACK_BG = 0xFF303036;
    private static final int COLOR_TRACK_FILL = 0xFFA368EF;
    private static final int COLOR_KNOB = 0xFFE8E8EC;

    private final double min;
    private final double max;
    private final double step;

    private boolean dragging = false;
    private int lastTrackX;
    private int lastTrackW;

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
    public void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(font, name, x + 6, y + 4, 0xC0C0C0, true);

        int descWidth = (width * 2 / 3) - 10;
        int descX = x + width / 3;
        graphics.drawWordWrap(font, Component.literal(description), descX, y + 4, descWidth, 0x909090);

        int trackW = Math.max(40, width / 6 - 10);
        int trackX = x + 6;
        int trackY = y + getHeight() - TRACK_H - 10;
        lastTrackX = trackX;
        lastTrackW = trackW;

        graphics.fill(trackX, trackY, trackX + trackW, trackY + TRACK_H, COLOR_TRACK_BG);

        double p = clamp(progress(), 0, 1);
        int filledW = (int) Math.round(trackW * p);
        graphics.fill(trackX, trackY, trackX + filledW, trackY + TRACK_H, COLOR_TRACK_FILL);

        int knobX = trackX + filledW - KNOB_W / 2;
        int knobY = trackY + TRACK_H / 2 - KNOB_H / 2;
        graphics.fill(knobX, knobY, knobX + KNOB_W, knobY + KNOB_H, COLOR_KNOB);

        String valueText = formatValue(value);
        graphics.drawString(font, valueText, trackX + trackW + 8, trackY - 3, 0xA0A0A0, false);
    }

    private String formatValue(double v) {
        if (step >= 1) {
            return String.valueOf(Math.round(v));
        }
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

        int trackW = Math.max(40, width / 6 - 10);
        int trackX = x + 6;
        int trackY = y + getHeight() - TRACK_H - 10;

        // Generous vertical hitbox since the knob overhangs the thin track
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
