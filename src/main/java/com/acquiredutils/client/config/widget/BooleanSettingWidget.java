package com.acquiredutils.client.config.widget;

import com.acquiredutils.client.config.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/**
 * NEU-style 14x14 square toggle button.
 * OFF: dark fill + border. ON: purple fill + lighter border + white inner dot.
 * 150ms color lerp animation. Preview on click, commit on release.
 */
public class BooleanSettingWidget extends Setting<Boolean> {
    private static final int BUTTON_SIZE = 14;
    private static final int ANIMATION_DURATION = 150; // ms

    // OFF colors
    private static final int OFF_FILL = 0xFF2A2A32;
    private static final int OFF_BORDER = 0xFF404046;

    // ON colors
    private static final int ON_FILL = 0xFFA368EF;
    private static final int ON_BORDER = 0xFFC090FF;
    private static final int ON_DOT = 0xFFFFFFFF;

    // Animation state
    private float animationProgress = 0f;
    private long lastFrameTime = 0;
    private boolean previewValue;
    private boolean clicking = false;
    private final Consumer<Boolean> onChanged;

    public BooleanSettingWidget(String name, String description, boolean defaultValue) {
        this(name, description, defaultValue, null);
    }

    public BooleanSettingWidget(String name, String description, boolean defaultValue, Consumer<Boolean> onChanged) {
        super(name, description, defaultValue);
        this.onChanged = onChanged;
        this.previewValue = defaultValue;
        this.animationProgress = defaultValue ? 1f : 0f;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, float delta) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + getHeight();

        // Update animation
        long now = System.currentTimeMillis();
        if (lastFrameTime == 0) lastFrameTime = now;
        float dt = (now - lastFrameTime) / 1000f;
        lastFrameTime = now;

        boolean target = clicking ? previewValue : pendingValue;
        if (target) {
            animationProgress = Math.min(1f, animationProgress + dt * 1000f / ANIMATION_DURATION);
        } else {
            animationProgress = Math.max(0f, animationProgress - dt * 1000f / ANIMATION_DURATION);
        }

        // Button position: left of name
        int btnX = x + 4;
        int btnY = y + (getHeight() - BUTTON_SIZE) / 2;

        // Lerp colors
        int fill = lerpColor(OFF_FILL, ON_FILL, animationProgress);
        int border = lerpColor(OFF_BORDER, ON_BORDER, animationProgress);

        // Draw border (1px larger than fill)
        graphics.fill(btnX - 1, btnY - 1, btnX + BUTTON_SIZE + 1, btnY + BUTTON_SIZE + 1, border);
        // Draw button background
        graphics.fill(btnX, btnY, btnX + BUTTON_SIZE, btnY + BUTTON_SIZE, fill);

        // Inner dot when ON
        if (animationProgress > 0.5f) {
            float dotAlpha = (animationProgress - 0.5f) * 2f;
            int dotColor = blendAlpha(ON_DOT, dotAlpha);
            int dotSize = 4;
            int dotX = btnX + (BUTTON_SIZE - dotSize) / 2;
            int dotY = btnY + (BUTTON_SIZE - dotSize) / 2;
            graphics.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, dotColor);
        }

        // Draw name to the right of button
        drawName(graphics, x + BUTTON_SIZE + 10, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hovered) {
            clicking = true;
            previewValue = !pendingValue;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && clicking) {
            clicking = false;
            pendingValue = previewValue;
            if (onChanged != null) {
                onChanged.accept(pendingValue);
            }
            return true;
        }
        return false;
    }

    private static int lerpColor(int from, int to, float t) {
        int a1 = (from >> 24) & 0xFF;
        int r1 = (from >> 16) & 0xFF;
        int g1 = (from >> 8) & 0xFF;
        int b1 = from & 0xFF;

        int a2 = (to >> 24) & 0xFF;
        int r2 = (to >> 16) & 0xFF;
        int g2 = (to >> 8) & 0xFF;
        int b2 = to & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int blendAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}