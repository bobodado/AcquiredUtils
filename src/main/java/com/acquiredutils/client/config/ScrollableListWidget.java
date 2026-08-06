package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class ScrollableListWidget {

    private static final int ROW_GAP = 5;
    private static final int SCROLLBAR_W = 5;
    private static final int WHEEL_STEP = 30;

    private int x, y, width, height;
    private float scroll = 0f;
    private float targetScroll = 0f;
    private int contentHeight = 0;
    private boolean draggingScrollbar = false;
    private ConfigCategory category;
    private String searchQuery = "";

    public void setBounds(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public void setCategory(ConfigCategory category) {
        if (this.category != category) {
            this.category = category;
            this.scroll = 0f; this.targetScroll = 0f;
        }
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query.toLowerCase();
        this.scroll = 0f; this.targetScroll = 0f;
    }

    private List<GuiElement> getVisibleElements() {
        if (category == null) return List.of();
        if (searchQuery.isEmpty()) return category.getElements();
        List<GuiElement> filtered = new ArrayList<>();
        for (GuiElement e : category.getElements()) {
            if (e instanceof Setting<?> s) {
                if (matches(s.getName(), s.getDescription())) filtered.add(e);
            } else if (e instanceof Section sec) {
                List<Setting<?>> matched = new ArrayList<>();
                for (Setting<?> child : sec.getChildren()) {
                    if (matches(child.getName(), child.getDescription())) matched.add(child);
                }
                if (!matched.isEmpty()) {
                    Section fs = new Section(sec.name, true);
                    for (Setting<?> m : matched) fs.addChild(m);
                    filtered.add(fs);
                }
            }
        }
        return filtered;
    }

    private boolean matches(String name, String desc) {
        return name.toLowerCase().contains(searchQuery) || desc.toLowerCase().contains(searchQuery);
    }

    private int maxScroll() { return Math.max(0, contentHeight - height); }

    private void tickScroll(float partialTick) {
        if (Math.abs(targetScroll - scroll) < 0.5f) scroll = targetScroll;
        else scroll += (targetScroll - scroll) * Math.min(1f, 0.35f * Math.max(partialTick, 1f));
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
        tickScroll(partialTick);
        List<GuiElement> elements = getVisibleElements();

        if (elements.isEmpty()) {
            String msg = searchQuery.isEmpty() ? "No settings in this category" : "No matches found";
            graphics.drawCenteredString(font, msg, x + width / 2, y + height / 2 - 4, 0xFF808080);
            contentHeight = 0; return;
        }

        graphics.enableScissor(x, y, x + width, y + height);

        int cursorY = y - (int) scroll;
        for (GuiElement element : elements) {
            int elemHeight = element.getHeight();
            if (cursorY + elemHeight >= y && cursorY <= y + height) {
                if (!(element instanceof Section)) drawRowChrome(graphics, x, cursorY, width, elemHeight);
                element.render(graphics, font, x + 10, cursorY, width - 20, mouseX, mouseY, partialTick);
            }
            cursorY += elemHeight + ROW_GAP;
        }

        graphics.disableScissor();

        // Overlay pass - tooltips draw above scissor
        cursorY = y - (int) scroll;
        for (GuiElement element : elements) {
            int elemHeight = element.getHeight();
            if (cursorY + elemHeight >= y && cursorY <= y + height) {
                element.renderOverlay(graphics, font, x + 10, cursorY, width - 20, mouseX, mouseY, partialTick);
            }
            cursorY += elemHeight + ROW_GAP;
        }

        contentHeight = (cursorY + (int) scroll) - y - ROW_GAP;
        if (targetScroll > maxScroll()) targetScroll = maxScroll();

        drawScrollbar(graphics);
    }

    private void drawRowChrome(GuiGraphics g, int rx, int ry, int rw, int rh) {
        g.fill(rx, ry, rx + 1, ry + rh, 0xFF08080E);
        g.fill(rx + 1, ry, rx + rw, ry + 1, 0xFF08080E);
        g.fill(rx + rw - 1, ry + 1, rx + rw, ry + rh, 0xFF28282E);
        g.fill(rx + 1, ry + rh - 1, rx + rw - 1, ry + rh, 0xFF28282E);
        g.fill(rx + 1, ry + 1, rx + rw - 1, ry + rh - 1, 0x6008080E);
    }

    private void drawScrollbar(GuiGraphics g) {
        int max = maxScroll(); if (max <= 0) return;
        int tx1 = x + width - SCROLLBAR_W, tx2 = x + width, ty1 = y + 5, ty2 = y + height - 5;
        g.fill(tx1, ty1, tx2, ty2, 0xFF101010);
        float barSize = Math.min(1f, (float) height / contentHeight);
        int th = Math.max(10, Math.round((ty2 - ty1) * barSize));
        float ratio = max == 0 ? 0 : scroll / max;
        int thumbY = ty1 + Math.round((ty2 - ty1 - th) * ratio);
        g.fill(tx1 + 1, thumbY, tx2 - 1, thumbY + th, 0xFF303030);
    }

    public boolean mouseScrolled(double mx, double my, double sy) {
        if (!isInside(mx, my)) return false;
        int notch = sy > 0 ? -1 : (sy < 0 ? 1 : 0);
        targetScroll = clamp(targetScroll + notch * WHEEL_STEP); return true;
    }

    private float clamp(float v) { return Math.max(0, Math.min(maxScroll(), v)); }
    private boolean isInside(double mx, double my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }

    public boolean mouseClicked(double mx, double my, int btn) {
        if (category == null) return false;
        if (maxScroll() > 0 && mx >= x + width - SCROLLBAR_W && mx <= x + width) {
            draggingScrollbar = true; scrollToThumbPosition(my); return true;
        }
        if (!isInside(mx, my)) return false;
        int cursorY = y - (int) scroll;
        for (GuiElement e : getVisibleElements()) {
            int eh = e.getHeight();
            if (my >= cursorY && my <= cursorY + eh) return e.mouseClicked(mx, my, btn, x + 10, cursorY, width - 20);
            cursorY += eh + ROW_GAP;
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (draggingScrollbar) { scrollToThumbPosition(my); return true; }
        if (category == null) return false;
        for (GuiElement e : getVisibleElements()) {
            if (e.mouseDragged(mx, my, btn, dx, dy)) return true;
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int btn) {
        boolean was = draggingScrollbar; draggingScrollbar = false;
        if (category != null) {
            for (GuiElement e : getVisibleElements()) e.mouseReleased(mx, my, btn);
        }
        return was;
    }

    private void scrollToThumbPosition(double my) {
        int ty1 = y + 5, ty2 = y + height - 5;
        float p = (float) (my - ty1) / (ty2 - ty1);
        scroll = clamp(p * maxScroll()); targetScroll = scroll;
    }
}