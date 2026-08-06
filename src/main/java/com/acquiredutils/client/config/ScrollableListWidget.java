package com.acquiredutils.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrollable list widget that renders GuiElements with scissor, scrollbar, and search filtering.
 * Performs a two-pass render: main content, then overlay (tooltips) after scissor is disabled.
 */
public class ScrollableListWidget {
    private final Minecraft mc;
    private final List<GuiElement> elements = new ArrayList<>();
    private String searchQuery = "";
    private double scrollOffset = 0;
    private int x, y, width, height;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_BG = 0xFF18181C;
    private static final int SCROLLBAR_COLOR = 0xFF606068;
    private static final int SCROLLBAR_HOVER = 0xFF808088;

    public ScrollableListWidget(Minecraft mc) {
        this.mc = mc;
    }

    /** No-arg constructor for compatibility with existing code. */
    public ScrollableListWidget() {
        this(Minecraft.getInstance());
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setElements(List<GuiElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    /** Convenience method for compatibility with code that passes a category directly. */
    public void setCategory(ConfigCategory category) {
        if (category != null) {
            setElements(new ArrayList<>(category.getElements()));
        }
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query.toLowerCase();
        this.scrollOffset = 0;
    }

    /**
     * Returns visible elements based on search filtering.
     * Sections are included if they match or have matching children.
     * Settings are included if they match.
     */
    public List<GuiElement> getVisibleElements() {
        List<GuiElement> visible = new ArrayList<>();
        if (searchQuery.isEmpty()) {
            visible.addAll(elements);
            return visible;
        }
        for (GuiElement element : elements) {
            if (element instanceof Section section) {
                if (section.matchesSearch(searchQuery)) {
                    visible.add(section);
                }
            } else if (element instanceof Setting<?> setting) {
                if (setting.matchesSearch(searchQuery)) {
                    visible.add(setting);
                }
            }
        }
        return visible;
    }

    private int getTotalContentHeight() {
        int h = 0;
        for (GuiElement element : getVisibleElements()) {
            h += element.getHeight();
        }
        return h;
    }

    private double getMaxScroll() {
        int total = getTotalContentHeight();
        return Math.max(0, total - height);
    }

    public void scroll(double amount) {
        scrollOffset = Math.max(0, Math.min(getMaxScroll(), scrollOffset - amount * 20));
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        List<GuiElement> visible = getVisibleElements();
        int totalHeight = getTotalContentHeight();
        double maxScroll = getMaxScroll();

        // Clamp scroll
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        // Scissor
        graphics.enableScissor(x, y, x + width, y + height);

        int renderY = y - (int) scrollOffset;
        for (GuiElement element : visible) {
            int elemHeight = element.getHeight();
            if (renderY + elemHeight >= y && renderY <= y + height) {
                element.render(graphics, x, renderY, width - SCROLLBAR_WIDTH - 2, mouseX, mouseY, delta);
            }
            renderY += elemHeight;
        }

        graphics.disableScissor();

        // Scrollbar
        if (totalHeight > height) {
            int scrollbarHeight = Math.max(20, (int) ((double) height / totalHeight * height));
            int scrollbarY = y + (int) ((scrollOffset / maxScroll) * (height - scrollbarHeight));
            int scrollbarX = x + width - SCROLLBAR_WIDTH;

            boolean hovered = mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH
                && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight;

            graphics.fill(scrollbarX, y, scrollbarX + SCROLLBAR_WIDTH, y + height, SCROLLBAR_BG);
            graphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight,
                hovered ? SCROLLBAR_HOVER : SCROLLBAR_COLOR);
        }
    }

    /** Compatibility overload for code that passes Font as a parameter. */
    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float delta) {
        render(graphics, mouseX, mouseY, delta);
    }

    /**
     * Second pass: render overlays (tooltips) after scissor is disabled.
     */
    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        List<GuiElement> visible = getVisibleElements();
        int renderY = y - (int) scrollOffset;
        for (GuiElement element : visible) {
            int elemHeight = element.getHeight();
            if (renderY + elemHeight >= y && renderY <= y + height) {
                element.renderOverlay(graphics, mouseX, mouseY, delta);
            }
            renderY += elemHeight;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;

        List<GuiElement> visible = getVisibleElements();
        int renderY = y - (int) scrollOffset;
        for (GuiElement element : visible) {
            int elemHeight = element.getHeight();
            if (mouseY >= renderY && mouseY < renderY + elemHeight) {
                if (element.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
            renderY += elemHeight;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;

        List<GuiElement> visible = getVisibleElements();
        int renderY = y - (int) scrollOffset;
        for (GuiElement element : visible) {
            int elemHeight = element.getHeight();
            if (mouseY >= renderY && mouseY < renderY + elemHeight) {
                if (element.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
            renderY += elemHeight;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;

        List<GuiElement> visible = getVisibleElements();
        int renderY = y - (int) scrollOffset;
        for (GuiElement element : visible) {
            int elemHeight = element.getHeight();
            if (mouseY >= renderY && mouseY < renderY + elemHeight) {
                if (element.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                    return true;
                }
            }
            renderY += elemHeight;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) return false;
        scroll(verticalAmount);
        return true;
    }

    /** Compatibility overload for code that passes only 3 doubles. */
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        return mouseScrolled(mouseX, mouseY, 0, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}