package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * NEU-style small square boolean button.
 * 14x14 square, positioned to the left of the name.
 */
public class BooleanSettingWidget extends Setting<Boolean> {

    private static final int BTN_SIZE = 14;
    private static final int CLICK_RADIUS = 6;
    private static final int COLOR_OFF_BG = 0xFF2A2A32;
    private static final int COLOR_OFF_BORDER = 0xFF404046;
    private static final int COLOR_ON_BG = 0xFFA368EF;
    private static final int COLOR_ON_BORDER = 0xFFC090FF;
    private static final int COLOR_INNER = 0xFFE8E8EC;

    private boolean previewValue;
    private long animStart = 0;
    private boolean mouseWasDown = false;
    private int lastBtnX = 0, lastBtnY = 0;

    public BooleanSettingWidget(String name, String description, boolean defaultValue, Consumer<Boolean> onChange) {
        super(name, description, defaultValue, onChange);
        this.previewValue = defaultValue;
    }

    @Override
    public boolean isHovered(int mouseX, int mouseY, int x, int y, int width) {
        int btnX = x + 10;
        int btnY = y + getHeight() / 2 - BTN_SIZE / 2;
        lastBtnX = btnX; lastBtnY = btnY;
        return mouseX >= btnX - CLICK_RADIUS && mouseX <= btnX + BTN_SIZE + CLICK_RADIUS &&
               mouseY >= btnY - CLICK_RADIUS && mouseY <= btnY + BTN_SIZE + CLICK_RADIUS;
    }

    @Override
    public void render(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        boolean current = previewValue;
        float t = 1f;
        if (animStart > 0) {
            t = Math.min(1f, (now - animStart) / 150f);
            if (t >= 1f) animStart = 0;
        }

        int btnX = x + 10;
        int btnY = y + getHeight() / 2 - BTN_SIZE / 2;
        lastBtnX = btnX; lastBtnY = btnY;

        // Name
        int nameX = btnX + BTN_SIZE + 8;
        g.drawString(font, name, nameX, y + getHeight() / 2 - font.lineHeight / 2, 0xFFc0c0c0, false);

        // Description
        int descX = x + 5 + width / 3;
        int descW = width * 2 / 3 - 10;
        int lineCount = font.split(Component.literal(description), descW).size();
        int paraH = font.lineHeight * lineCount;
        g.drawWordWrap(font, Component.literal(description), descX, y + getHeight() / 2 - paraH / 2, descW, 0xFFc0c0c0);

        // Square button
        int bg = current ? lerpColor(COLOR_OFF_BG, COLOR_ON_BG, t) : lerpColor(COLOR_ON_BG, COLOR_OFF_BG, t);
        int border = current ? lerpColor(COLOR_OFF_BORDER, COLOR_ON_BORDER, t) : lerpColor(COLOR_ON_BORDER, COLOR_OFF_BORDER, t);

        g.fill(btnX - 1, btnY - 1, btnX + BTN_SIZE + 1, btnY, border);
        g.fill(btnX - 1, btnY + BTN_SIZE, btnX + BTN_SIZE + 1, btnY + BTN_SIZE + 1, border);
        g.fill(btnX - 1, btnY, btnX, btnY + BTN_SIZE, border);
        g.fill(btnX + BTN_SIZE, btnY, btnX + BTN_SIZE + 1, btnY + BTN_SIZE, border);
        g.fill(btnX, btnY, btnX + BTN_SIZE, btnY + BTN_SIZE, bg);

        if (current || t > 0) {
            int innerAlpha = current ? Math.round(255 * t) : Math.round(255 * (1f - t));
            if (innerAlpha > 0) {
                int innerColor = (innerAlpha << 24) | (COLOR_INNER & 0x00FFFFFF);
                int dotSize = 4;
                int dotX = btnX + BTN_SIZE / 2 - dotSize / 2;
                int dotY = btnY + BTN_SIZE / 2 - dotSize / 2;
                g.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, innerColor);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (button != 0) return false;
        int btnX = x + 10;
        int btnY = y + getHeight() / 2 - BTN_SIZE / 2;
        if (mouseX >= btnX - CLICK_RADIUS && mouseX <= btnX + BTN_SIZE + CLICK_RADIUS &&
            mouseY >= btnY - CLICK_RADIUS && mouseY <= btnY + BTN_SIZE + CLICK_RADIUS) {
            previewValue = !value;
            animStart = System.currentTimeMillis();
            mouseWasDown = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !mouseWasDown) return false;
        mouseWasDown = false;
        if (previewValue != value) updateValue(previewValue);
        return true;
    }

    private static int lerpColor(int from, int to, float t) {
        int a = lerpChannel((from >> 24) & 0xFF, (to >> 24) & 0xFF, t);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t);
        int b = lerpChannel(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    private static int lerpChannel(int from, int to, float t) { return from + Math.round((to - from) * t); }
}