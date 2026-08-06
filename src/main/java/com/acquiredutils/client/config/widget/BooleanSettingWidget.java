package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class BooleanSettingWidget extends Setting<Boolean> {
    private static final int TOGGLE_W = 48, TOGGLE_H = 14, KNOB_W = 12, KNOB_H = 14, CLICK_RADIUS = 10;
    private static final int COLOR_BAR_OFF = 0xFF3A3A42, COLOR_BAR_ON = 0xFFA368EF, COLOR_KNOB = 0xFFE8E8EC;
    private int animFrame = 0; private boolean previewValue; private long lastMillis = 0; private boolean mouseWasDown = false;

    public BooleanSettingWidget(String name, String description, boolean defaultValue, Consumer<Boolean> onChange) {
        super(name, description, defaultValue, onChange);
        this.previewValue = defaultValue; this.animFrame = defaultValue ? 36 : 0; this.lastMillis = System.currentTimeMillis();
    }
    private static float sigmoid(float t) { return (float) (1.0 / (1.0 + Math.exp(-(t * 12.0 - 6.0)))); }

    @Override
    public void render(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis(); long delta = now - lastMillis; lastMillis = now;
        boolean passedLimit = false;
        if (previewValue != value) {
            if ((previewValue && animFrame > 12) || (!previewValue && animFrame < 24)) passedLimit = true;
        }
        if (previewValue != passedLimit) animFrame += (int) (delta / 10);
        else animFrame -= (int) (delta / 10);
        lastMillis -= delta % 10;
        if (previewValue == value) animFrame = Math.max(0, Math.min(36, animFrame));
        else if (!passedLimit) {
            if (previewValue) animFrame = Math.max(0, Math.min(12, animFrame));
            else animFrame = Math.max(24, Math.min(36, animFrame));
        } else {
            if (previewValue) animFrame = Math.max(12, animFrame);
            else animFrame = Math.min(24, animFrame);
        }
        float eased = sigmoid(animFrame / 36f);

        drawStringCenteredMaxWidth(g, font, name, x + width / 6, y + 13, width / 3 - 10, 0xFFc0c0c0);

        int descX = x + 5 + width / 3, descW = width * 2 / 3 - 10;
        int lineCount = font.split(Component.literal(description), descW).size();
        int paraH = font.lineHeight * lineCount;
        g.drawWordWrap(font, Component.literal(description), descX, y + getHeight() / 2 - paraH / 2, descW, 0xFFc0c0c0);

        int toggleX = x + width / 6 - TOGGLE_W / 2;
        int toggleY = y + getHeight() - 7 - TOGGLE_H;
        g.fill(toggleX, toggleY, toggleX + TOGGLE_W, toggleY + TOGGLE_H, lerpColor(COLOR_BAR_OFF, COLOR_BAR_ON, eased));
        int knobX = toggleX + Math.round((TOGGLE_W - KNOB_W) * eased);
        g.fill(knobX, toggleY, knobX + KNOB_W, toggleY + KNOB_H, COLOR_KNOB);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn, int x, int y, int width) {
        if (btn != 0) return false;
        int tx = x + width / 6 - TOGGLE_W / 2, ty = y + getHeight() - 7 - TOGGLE_H;
        if (mx >= tx - CLICK_RADIUS && mx <= tx + TOGGLE_W + CLICK_RADIUS && my >= ty - CLICK_RADIUS && my <= ty + TOGGLE_H + CLICK_RADIUS) {
            previewValue = !value; mouseWasDown = true; return true;
        }
        return false;
    }
    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn != 0 || !mouseWasDown) return false;
        mouseWasDown = false; if (previewValue != value) updateValue(previewValue); return true;
    }

    private static void drawStringCenteredMaxWidth(GuiGraphics g, Font font, String text, int cx, int cy, int maxW, int color) {
        int w = font.width(text); String draw = text;
        if (w > maxW) { draw = font.plainSubstrByWidth(text, maxW); if (!draw.equals(text)) draw = draw.substring(0, Math.max(0, draw.length() - 2)) + ".."; w = font.width(draw); }
        g.drawString(font, draw, cx - w / 2, cy - font.lineHeight / 2, color, false);
    }
    private static int lerpColor(int from, int to, float t) {
        return (lerpChannel((from >> 24) & 0xFF, (to >> 24) & 0xFF, t) << 24)
             | (lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t) << 16)
             | (lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t) << 8)
             | lerpChannel(from & 0xFF, to & 0xFF, t);
    }
    private static int lerpChannel(int a, int b, float t) { return a + Math.round((b - a) * t); }
}
