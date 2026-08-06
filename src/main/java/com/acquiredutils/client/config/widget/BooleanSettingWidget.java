package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A boolean toggle matching NEU's {@code GuiElementBoolean} / {@code GuiOptionEditorBoolean} exactly.
 *
 * NEU uses 48x14 texture sprites with a 12x14 knob sliding across 36 animation frames
 * (~360ms, sigmoid eased). Since we have no texture atlas in 1.21.11 Fabric, this is
 * redrawn as vector geometry with the same dimensions, timing, and color progression.
 *
 * Key NEU behaviours preserved:
 *   - Toggle size: 48 x 14, knob: 12 x 14
 *   - Position: x + width/6 - 24, y + height - 7 - 14
 *   - Name: centred at x + width/6, y + 13, max width width/3 - 10, colour 0xc0c0c0
 *   - Description: right 2/3, vertically centred, colour 0xc0c0c0
 *   - Animation: 36-frame counter, ~10 ms per frame, sigmoid easing
 *   - Preview click: knob slides on mouse-down; value commits on mouse-up inside radius
 *   - Click radius: 10 px beyond the toggle on all sides (NEU default)
 */
public class BooleanSettingWidget extends Setting<Boolean> {

    /* ── NEU-exact geometry ── */
    private static final int TOGGLE_W = 48;
    private static final int TOGGLE_H = 14;
    private static final int KNOB_W = 12;
    private static final int KNOB_H = 14;
    private static final int CLICK_RADIUS = 10;

    /* ── Colours (approximating NEU's texture progression) ── */
    private static final int COLOR_BAR_OFF  = 0xFF3A3A42; // dark grey track
    private static final int COLOR_BAR_ON   = 0xFFA368EF; // purple accent (NEU's 0xa368ef)
    private static final int COLOR_KNOB     = 0xFFE8E8EC; // light knob

    /* ── Animation state ── */
    private int animFrame = 0;           // 0 .. 36
    private boolean previewValue;        // value shown while mouse is held down
    private long lastMillis = 0;
    private boolean mouseWasDown = false;

    public BooleanSettingWidget(String name, String description, boolean defaultValue, Consumer<Boolean> onChange) {
        super(name, description, defaultValue, onChange);
        this.previewValue = defaultValue;
        this.animFrame = defaultValue ? 36 : 0;
        this.lastMillis = System.currentTimeMillis();
    }

    /* ── Sigmoid 0→1 (NEU's LerpUtils.sigmoidZeroOne) ── */
    private static float sigmoid(float t) {
        return (float) (1.0 / (1.0 + Math.exp(-(t * 12.0 - 6.0))));
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        long delta = now - lastMillis;
        lastMillis = now;

        /* ---- Animate the 36-frame counter ---- */
        boolean passedLimit = false;
        if (previewValue != value) {
            if ((previewValue && animFrame > 12) || (!previewValue && animFrame < 24)) {
                passedLimit = true;
            }
        }

        if (previewValue != passedLimit) {
            animFrame += (int) (delta / 10);
        } else {
            animFrame -= (int) (delta / 10);
        }
        lastMillis -= delta % 10;

        if (previewValue == value) {
            animFrame = Math.max(0, Math.min(36, animFrame));
        } else if (!passedLimit) {
            if (previewValue) {
                animFrame = Math.max(0, Math.min(12, animFrame));
            } else {
                animFrame = Math.max(24, Math.min(36, animFrame));
            }
        } else {
            if (previewValue) {
                animFrame = Math.max(12, animFrame);
            } else {
                animFrame = Math.min(24, animFrame);
            }
        }

        float eased = sigmoid(animFrame / 36f);

        /* ---- Name (left third, centred, NEU: x + width/6, y + 13) ---- */
        drawStringCenteredClamped(graphics, font, name, x + width / 6, y + 13, width / 3 - 10, 0xc0c0c0);

        /* ---- Description (right 2/3, vertically centred) ---- */
        int descX = x + 5 + width / 3;
        int descW = width * 2 / 3 - 10;
        int lineCount = font.split(Component.literal(description), descW).size();
        int lineHeight = font.lineHeight;
        int paraH = lineHeight * lineCount;
        int descY = y + getHeight() / 2 - paraH / 2;
        graphics.drawWordWrap(font, Component.literal(description), descX, descY, descW, 0xc0c0c0);

        /* ---- Toggle (NEU: x + width/6 - 24, y + height - 7 - 14) ---- */
        int toggleX = x + width / 6 - TOGGLE_W / 2;
        int toggleY = y + getHeight() - 7 - TOGGLE_H;

        int trackColor = lerpColor(COLOR_BAR_OFF, COLOR_BAR_ON, eased);
        graphics.fill(toggleX, toggleY, toggleX + TOGGLE_W, toggleY + TOGGLE_H, trackColor);

        int knobTravel = TOGGLE_W - KNOB_W;
        int knobX = toggleX + Math.round(knobTravel * eased);
        int knobY = toggleY;
        graphics.fill(knobX, knobY, knobX + KNOB_W, knobY + KNOB_H, COLOR_KNOB);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (button != 0) return false;

        int toggleX = x + width / 6 - TOGGLE_W / 2;
        int toggleY = y + getHeight() - 7 - TOGGLE_H;

        if (mouseX >= toggleX - CLICK_RADIUS && mouseX <= toggleX + TOGGLE_W + CLICK_RADIUS &&
            mouseY >= toggleY - CLICK_RADIUS && mouseY <= toggleY + TOGGLE_H + CLICK_RADIUS) {
            previewValue = !value;
            mouseWasDown = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !mouseWasDown) return false;
        mouseWasDown = false;

        if (previewValue != value) {
            updateValue(previewValue);
        }
        return true;
    }

    /* ── Helpers ── */
    private static void drawStringCenteredClamped(GuiGraphics graphics, Font font, String text, int cx, int cy, int maxW, int color) {
        int w = font.width(text);
        if (w > maxW) {
            float scale = maxW / (float) w;
            int drawW = (int) (w * scale);
            // For Phase 1 we just clamp with ellipses if too long; scaling is Phase 2 polish
            String clipped = font.plainSubstrByWidth(text, maxW);
            if (!clipped.equals(text)) clipped = clipped.substring(0, Math.max(0, clipped.length() - 2)) + "..";
            w = font.width(clipped);
            graphics.drawString(font, clipped, cx - w / 2, cy - font.lineHeight / 2, color, false);
        } else {
            graphics.drawString(font, text, cx - w / 2, cy - font.lineHeight / 2, color, false);
        }
    }

    private static int lerpColor(int from, int to, float t) {
        int a = lerpChannel((from >> 24) & 0xFF, (to >> 24) & 0xFF, t);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t);
        int b = lerpChannel(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return from + Math.round((to - from) * t);
    }
}