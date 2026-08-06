package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class SliderSettingWidget extends Setting<Double> {
    private static final int TRACK_H = 6, KNOB_W = 8, KNOB_H = 16;
    private static final int COLOR_TRACK_BG = 0xFF303036, COLOR_TRACK_FILL = 0xFFA368EF, COLOR_KNOB = 0xFFE8E8EC;
    private final double min, max, step;
    private boolean dragging = false; private int lastTrackX = 0, lastTrackW = 0;

    public SliderSettingWidget(String name, String description, double defaultValue, double min, double max, double step, Consumer<Double> onChange) {
        super(name, description, clamp(defaultValue, min, max), onChange);
        this.min = min; this.max = max; this.step = step <= 0 ? 1e-6 : step;
    }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
    private double progress() { return (value - min) / (max - min); }

    @Override
    public void render(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        drawStringCenteredMaxWidth(g, font, name, x + width / 6, y + 13, width / 3 - 10, 0xFFc0c0c0);
        int descX = x + 5 + width / 3, descW = width * 2 / 3 - 10;
        int paraH = font.lineHeight * font.split(Component.literal(description), descW).size();
        g.drawWordWrap(font, Component.literal(description), descX, y + getHeight() / 2 - paraH / 2, descW, 0xFFc0c0c0);

        int trackW = Math.max(60, width / 6 - 10);
        int trackX = x + width / 6 - trackW / 2;
        int trackY = y + getHeight() - TRACK_H - 8;
        lastTrackX = trackX; lastTrackW = trackW;

        g.fill(trackX, trackY, trackX + trackW, trackY + TRACK_H, COLOR_TRACK_BG);
        int filled = (int) Math.round(trackW * clamp(progress(), 0, 1));
        g.fill(trackX, trackY, trackX + filled, trackY + TRACK_H, COLOR_TRACK_FILL);
        int knobX = trackX + filled - KNOB_W / 2;
        int knobY = trackY + TRACK_H / 2 - KNOB_H / 2;
        g.fill(knobX, knobY, knobX + KNOB_W, knobY + KNOB_H, COLOR_KNOB);

        g.drawString(font, formatValue(value), trackX + trackW + 8, trackY + TRACK_H / 2 - font.lineHeight / 2, 0xFFa0a0a0, false);
    }

    private String formatValue(double v) { return step >= 1 ? String.valueOf(Math.round(v)) : String.format("%.2f", v); }
    private double snap(double raw) { return clamp(min + Math.round((raw - min) / step) * step, min, max); }
    private void updateFromMouseX(double mx) { double p = clamp((mx - lastTrackX) / (double) lastTrackW, 0, 1); updateValue(snap(min + p * (max - min))); }

    @Override
    public boolean mouseClicked(double mx, double my, int btn, int x, int y, int width) {
        if (btn != 0) return false;
        int trackW = Math.max(60, width / 6 - 10);
        int trackX = x + width / 6 - trackW / 2, trackY = y + getHeight() - TRACK_H - 8;
        if (mx >= trackX - KNOB_W && mx <= trackX + trackW + KNOB_W && my >= trackY - KNOB_H && my <= trackY + TRACK_H + KNOB_H) {
            lastTrackX = trackX; lastTrackW = trackW; dragging = true; updateFromMouseX(mx); return true;
        }
        return false;
    }
    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) { if (dragging) { updateFromMouseX(mx); return true; } return false; }
    @Override
    public boolean mouseReleased(double mx, double my, int btn) { boolean was = dragging; dragging = false; return was; }

    private static void drawStringCenteredMaxWidth(GuiGraphics g, Font font, String text, int cx, int cy, int maxW, int color) {
        int w = font.width(text); String draw = text;
        if (w > maxW) { draw = font.plainSubstrByWidth(text, maxW); if (!draw.equals(text)) draw = draw.substring(0, Math.max(0, draw.length() - 2)) + ".."; w = font.width(draw); }
        g.drawString(font, draw, cx - w / 2, cy - font.lineHeight / 2, color, false);
    }
}
