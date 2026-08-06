package com.acquiredutils.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Accordion-style section that can be expanded/collapsed.
 * Holds child settings and renders with a header row.
 */
public class Section implements GuiElement {
    private final String name;
    private final String description;
    private boolean expanded = true;
    private boolean hovered = false;
    private boolean focused = false;
    private final List<Setting<?>> children = new ArrayList<>();
    private static final int HEADER_HEIGHT = 24;
    private static final int INDENT = 12;
    private static final int CHILD_FILL = 0x5008080E;
    private static final int HEADER_TEXT_COLOR = 0xFFE8E8EC;
    private static final int HOVER_COLOR = 0x20FFFFFF;
    private static final int ARROW_COLOR = 0xFFB0B0B8;

    // Track positions for overlay rendering
    private int lastRenderChildX = 0;
    private int lastRenderChildY = 0;
    private int lastRenderChildWidth = 0;

    public Section(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addChild(Setting<?> child) {
        children.add(child);
    }

    public List<Setting<?>> getChildren() {
        return children;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty()) return true;
        String lower = query.toLowerCase();
        if (name.toLowerCase().contains(lower) || description.toLowerCase().contains(lower)) return true;
        for (Setting<?> child : children) {
            if (child.matchesSearch(query)) return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        int h = HEADER_HEIGHT;
        if (expanded) {
            for (Setting<?> child : children) {
                h += child.getHeight();
            }
        }
        return h;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, float delta) {
        hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + HEADER_HEIGHT;

        // Hover highlight
        if (hovered) {
            graphics.fill(x, y, x + width, y + HEADER_HEIGHT, HOVER_COLOR);
        }

        // Arrow
        String arrow = expanded ? "\u25BC" : "\u25B6";
        graphics.drawString(Minecraft.getInstance().font, arrow, x + 6, y + 7, ARROW_COLOR, false);

        // Name
        graphics.drawString(Minecraft.getInstance().font, name, x + 22, y + 6, HEADER_TEXT_COLOR, false);

        // Children
        if (expanded) {
            int childY = y + HEADER_HEIGHT;
            int childX = x + INDENT;
            int childWidth = width - INDENT;

            // Store positions for overlay pass
            lastRenderChildX = childX;
            lastRenderChildY = childY;
            lastRenderChildWidth = childWidth;

            // Lighter chrome background for children
            int totalChildHeight = 0;
            for (Setting<?> child : children) {
                totalChildHeight += child.getHeight();
            }
            graphics.fill(childX, childY, childX + childWidth, childY + totalChildHeight, CHILD_FILL);

            for (Setting<?> child : children) {
                child.render(graphics, childX, childY, childWidth, mouseX, mouseY, delta);
                childY += child.getHeight();
            }
        }
    }

    @Override
    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Section header tooltip
        if (hovered && !description.isEmpty()) {
            // Use a dummy setting just for the tooltip helper, or inline it
            // We'll inline manual tooltip drawing here
            drawManualTooltip(graphics, mouseX, mouseY, description);
        }
        if (expanded) {
            int childY = lastRenderChildY;
            for (Setting<?> child : children) {
                child.renderOverlay(graphics, mouseX, mouseY, delta);
                childY += child.getHeight();
            }
        }
    }

    private void drawManualTooltip(GuiGraphics graphics, int mouseX, int mouseY, String text) {
        if (text == null || text.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        var font = mc.font;

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

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        if (tx + tooltipWidth > screenW) tx = mouseX - tooltipWidth - 12;
        if (tx < 0) tx = 0;
        if (ty < 0) ty = 0;
        if (ty + tooltipHeight > screenH) ty = screenH - tooltipHeight;

        graphics.fill(tx, ty, tx + tooltipWidth, ty + tooltipHeight, 0xF0100010);
        graphics.fill(tx, ty, tx + tooltipWidth, ty + 1, 0x505000FF);
        graphics.fill(tx, ty + tooltipHeight - 1, tx + tooltipWidth, ty + tooltipHeight, 0x505000FF);
        graphics.fill(tx, ty, tx + 1, ty + tooltipHeight, 0x505000FF);
        graphics.fill(tx + tooltipWidth - 1, ty, tx + tooltipWidth, ty + tooltipHeight, 0x505000FF);

        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), tx + padding, ty + padding + i * lineHeight, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            expanded = !expanded;
            return true;
        }
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

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }
}