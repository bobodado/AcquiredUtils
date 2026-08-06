package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public abstract class Setting<T> implements GuiElement {

    protected final String name;
    protected final String description;
    protected T value;
    protected final Consumer<T> onChange;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChange) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.onChange = onChange;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }

    protected void updateValue(T newValue) {
        this.value = newValue;
        if (onChange != null) onChange.accept(newValue);
    }

    @Override
    public int getHeight() { return 45; }

    /** Returns true if the mouse is currently hovering over this setting's interactive area. */
    public abstract boolean isHovered(int mouseX, int mouseY, int x, int y, int width);

    @Override
    public void renderOverlay(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        if (isHovered(mouseX, mouseY, x, y, width) && !description.isEmpty()) {
            drawTooltip(graphics, font, mouseX, mouseY, description);
        }
    }

    protected void drawTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, String text) {
        int pad = 4;
        int maxW = 200;
        var lines = font.split(net.minecraft.network.chat.Component.literal(text), maxW);
        int lineH = font.lineHeight;
        int tw = 0;
        for (var line : lines) {
            tw = Math.max(tw, font.width(line));
        }
        int th = lines.size() * lineH;
        int tx = mouseX + 12;
        int ty = mouseY - th - 8;

        // Keep on screen
        if (tx + tw + pad * 2 > graphics.guiWidth()) tx = mouseX - tw - pad * 2 - 8;
        if (ty < 0) ty = mouseY + 12;

        graphics.fill(tx - pad, ty - pad, tx + tw + pad, ty + th + pad, 0xF0101010);
        graphics.fill(tx - pad, ty - pad, tx + tw + pad, ty - pad + 1, 0xFFa368ef);

        int cy = ty;
        for (var line : lines) {
            graphics.drawString(font, line, tx, cy, 0xFFe0e0e0, false);
            cy += lineH;
        }
    }

    @Override
    public abstract void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick);

    @Override
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }

    public boolean charTyped(char chr, int modifiers) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
}