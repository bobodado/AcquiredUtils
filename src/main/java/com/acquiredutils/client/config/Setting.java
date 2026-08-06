package com.acquiredutils.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all config settings.
 * Provides tooltip rendering, hover detection, and value management.
 */
public abstract class Setting<T> implements GuiElement {
    protected final String name;
    protected final String description;
    protected T value;
    protected T pendingValue;
    protected boolean hovered = false;
    protected boolean focused = false;
    protected int x, y, width;
    protected static final int HEIGHT = 22;
    protected static final int TEXT_COLOR = 0xFFE8E8EC;
    protected static final int DESC_COLOR = 0xFF909096;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.pendingValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    public T getPendingValue() {
        return pendingValue;
    }

    public void commit() {
        this.value = this.pendingValue;
    }

    public void reset() {
        this.pendingValue = this.value;
    }

    public boolean isHovered() {
        return hovered;
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) return true;
        String lower = query.toLowerCase();
        return name.toLowerCase().contains(lower) || description.toLowerCase().contains(lower);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    /**
     * Draws the setting name at the given position.
     * Subclasses should call super.render() or draw the name themselves.
     */
    protected void drawName(GuiGraphics graphics, int x, int y) {
        graphics.drawString(Minecraft.getInstance().font, name, x, y + 6, TEXT_COLOR, false);
    }

    /**
     * Renders the tooltip/description as an overlay AFTER the scissor is disabled.
     * Called by ScrollableListWidget in a second pass.
     * Uses manual rendering to avoid mapping-dependent renderTooltip signatures.
     */
    @Override
    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (hovered && !description.isEmpty()) {
            drawManualTooltip(graphics, mouseX, mouseY, description);
        }
    }

    /**
     * Manually draws a multi-line tooltip with word wrapping.
     * Avoids dependency on renderTooltip overloads that vary by mapping.
     */
    protected void drawManualTooltip(GuiGraphics graphics, int mouseX, int mouseY, String text) {
        if (text == null || text.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        // Word wrap at 200px
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (font.width(test) > 200 && !current.isEmpty()) {
                lines.add(current.toString().trim());
                current = new StringBuilder(word).append(" ");
            } else {
                current.append(word).append(" ");
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString().trim());
        }

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }
        int lineHeight = 10;
        int padding = 4;
        int tooltipWidth = maxWidth + padding * 2;
        int tooltipHeight = lines.size() * lineHeight + padding * 2;

        int tx = mouseX + 12;
        int ty = mouseY - tooltipHeight / 2;

        // Keep on screen
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        if (tx + tooltipWidth > screenW) tx = mouseX - tooltipWidth - 12;
        if (tx < 0) tx = 0;
        if (ty < 0) ty = 0;
        if (ty + tooltipHeight > screenH) ty = screenH - tooltipHeight;

        // Background
        graphics.fill(tx, ty, tx + tooltipWidth, ty + tooltipHeight, 0xF0100010);
        // Border
        graphics.fill(tx, ty, tx + tooltipWidth, ty + 1, 0x505000FF);
        graphics.fill(tx, ty + tooltipHeight - 1, tx + tooltipWidth, ty + tooltipHeight, 0x505000FF);
        graphics.fill(tx, ty, tx + 1, ty + tooltipHeight, 0x505000FF);
        graphics.fill(tx + tooltipWidth - 1, ty, tx + tooltipWidth, ty + tooltipHeight, 0x505000FF);

        // Text
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), tx + padding, ty + padding + i * lineHeight, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}