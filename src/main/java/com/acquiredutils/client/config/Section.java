package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class Section implements GuiElement {

    private static final int HEADER_H = 24;
    private static final int CHILD_INDENT = 12;
    private static final int ROW_GAP = 5;

    private final String name;
    private boolean expanded = false;
    private final List<Setting<?>> children = new ArrayList<>();

    public Section(String name) { this.name = name; }
    public Section(String name, boolean expanded) { this.name = name; this.expanded = expanded; }

    public void addChild(Setting<?> child) { children.add(child); }
    public List<Setting<?>> getChildren() { return children; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean expanded) { this.expanded = expanded; }

    @Override
    public int getHeight() {
        int h = HEADER_H;
        if (expanded) {
            for (Setting<?> child : children) h += child.getHeight() + ROW_GAP;
        }
        return h;
    }

    @Override
    public void render(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        drawSectionChrome(g, x, y, width, HEADER_H, mouseX, mouseY);
        String arrow = expanded ? "\u25BC" : "\u25B6";
        g.drawString(font, arrow, x + 8, y + HEADER_H / 2 - font.lineHeight / 2, 0xFFa0a0a0, false);
        g.drawString(font, name, x + 22, y + HEADER_H / 2 - font.lineHeight / 2, 0xFFc0c0c0, false);

        if (expanded) {
            int childY = y + HEADER_H;
            for (Setting<?> child : children) {
                int ch = child.getHeight();
                drawChildChrome(g, x + CHILD_INDENT, childY, width - CHILD_INDENT, ch);
                child.render(g, font, x + CHILD_INDENT + 10, childY, width - CHILD_INDENT - 20, mouseX, mouseY, partialTick);
                childY += ch + ROW_GAP;
            }
        }
    }

    @Override
    public void renderOverlay(GuiGraphics g, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
        if (!expanded) return;
        int childY = y + HEADER_H;
        for (Setting<?> child : children) {
            int ch = child.getHeight();
            child.renderOverlay(g, font, x + CHILD_INDENT + 10, childY, width - CHILD_INDENT - 20, mouseX, mouseY, partialTick);
            childY += ch + ROW_GAP;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        if (button != 0) return false;
        if (mouseY >= y && mouseY <= y + HEADER_H) { expanded = !expanded; return true; }
        if (expanded) {
            int childY = y + HEADER_H;
            for (Setting<?> child : children) {
                int ch = child.getHeight();
                if (mouseY >= childY && mouseY <= childY + ch) {
                    return child.mouseClicked(mouseX, mouseY, button, x + CHILD_INDENT + 10, childY, width - CHILD_INDENT - 20);
                }
                childY += ch + ROW_GAP;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!expanded) return false;
        for (Setting<?> child : children) {
            if (child.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!expanded) return false;
        for (Setting<?> child : children) {
            if (child.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }

    private void drawSectionChrome(GuiGraphics g, int rx, int ry, int rw, int rh, int mouseX, int mouseY) {
        boolean hovered = mouseX >= rx && mouseX <= rx + rw && mouseY >= ry && mouseY <= ry + rh;
        int fill = hovered ? 0x70181824 : 0x6010101A;
        g.fill(rx, ry, rx + 1, ry + rh, 0xFF0A0A12);
        g.fill(rx + 1, ry, rx + rw, ry + 1, 0xFF0A0A12);
        g.fill(rx + rw - 1, ry + 1, rx + rw, ry + rh, 0xFF2A2A32);
        g.fill(rx + 1, ry + rh - 1, rx + rw - 1, ry + rh, 0xFF2A2A32);
        g.fill(rx + 1, ry + 1, rx + rw - 1, ry + rh - 1, fill);
    }

    private void drawChildChrome(GuiGraphics g, int rx, int ry, int rw, int rh) {
        g.fill(rx, ry, rx + 1, ry + rh, 0xFF08080E);
        g.fill(rx + 1, ry, rx + rw, ry + 1, 0xFF08080E);
        g.fill(rx + rw - 1, ry + 1, rx + rw, ry + rh, 0xFF28282E);
        g.fill(rx + 1, ry + rh - 1, rx + rw - 1, ry + rh, 0xFF28282E);
        g.fill(rx + 1, ry + 1, rx + rw - 1, ry + rh - 1, 0x5008080E);
    }
}