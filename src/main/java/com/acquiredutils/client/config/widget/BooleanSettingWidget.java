package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/**
 * A boolean toggle, drawn as a sliding pill switch.
 * <p>
 * NEU's {@code GuiElementBoolean} animated between 4 bound textures
 * (OFF -> ONE -> TWO -> THREE -> ON) over ~360ms using a sigmoid curve.
 * We have no legacy texture atlas to pull from in 1.21.11, so this is
 * redrawn as a vector pill with the same "knob slides across a track"
 * motion, eased the same way (a 0-1 progress value run through a
 * smoothstep-style curve) rather than a linear slide.
 */
public class BooleanSettingWidget extends Setting<Boolean> {

    private static final int TRACK_W = 32;
    private static final int TRACK_H = 14;
    private static final int KNOB_SIZE = 12;

    private static final int COLOR_ON = 0xFFA368EF;   // matches NEU's purple accent (0xa368ef)
    private static final int COLOR_OFF = 0xFF3A3A42;
    private static final int COLOR_KNOB = 0xFFE8E8EC;

    private float animProgress; // 0 = off, 1 = on
    private long lastFrameMillis = System.currentTimeMillis();

    public BooleanSettingWidget(String name, String description, boolean defaultValue, Consumer<Boolean> onChange) {
        super(name, description, defaultValue, onChange);
        this.animProgress = defaultValue ? 1f : 0f;
    }

    private static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    @Override
    public void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        long now = System.currentTimeMillis();
        float delta = Math.min(64, now - lastFrameMillis) / 220f; // ~220ms full travel, matches NEU's feel
        lastFrameMillis = now;

        float target = value ? 1f : 0f;
        if (animProgress != target) {
            float step = Math.max(delta, 0.02f);
            animProgress += (target - animProgress > 0 ? step : -step);
            animProgress = Math.max(0f, Math.min(1f, animProgress));
        }

        // Name label, left-aligned in the left third of the row (NEU: x + width / 6)
        graphics.drawString(font, name, x + 6, y + 4, 0xC0C0C0, true);

        // Description, wrapped in the remaining ~2/3 of the row
        int descWidth = (width * 2 / 3) - 10;
        int descX = x + width / 3;
        graphics.drawWordWrap(font, net.minecraft.network.chat.Component.literal(description), descX, y + 4, descWidth, 0x909090);

        // Toggle control sits in the left column under the name, like NEU's bool.x = x + width/6 - 24
        int toggleX = x + Math.max(6, width / 6 - TRACK_W / 2);
        int toggleY = y + getHeight() - TRACK_H - 4;

        float eased = smoothstep(animProgress);
        int trackColor = lerpColor(COLOR_OFF, COLOR_ON, eased);
        graphics.fill(toggleX, toggleY, toggleX + TRACK_W, toggleY + TRACK_H, trackColor);

        int knobTravel = TRACK_W - KNOB_SIZE - 2;
        int knobX = toggleX + 1 + Math.round(knobTravel * eased);
        int knobY = toggleY + 1;
        graphics.fill(knobX, knobY, knobX + KNOB_SIZE, knobY + KNOB_SIZE, COLOR_KNOB);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (button != 0) return false;

        int toggleX = x + Math.max(6, width / 6 - TRACK_W / 2);
        int toggleY = y + getHeight() - TRACK_H - 4;

        // Slightly generous hit box (NEU passed a clickRadius of 10 into GuiElementBoolean for the same reason)
        int pad = 4;
        if (mouseX >= toggleX - pad && mouseX <= toggleX + TRACK_W + pad &&
            mouseY >= toggleY - pad && mouseY <= toggleY + TRACK_H + pad) {
            updateValue(!value);
            return true;
        }
        return false;
    }
}
