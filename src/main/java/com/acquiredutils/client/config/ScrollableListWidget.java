package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

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

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setCategory(ConfigCategory category) {
        if (this.category != category) {
            this.category = category;
            this.scroll = 0f;
            this.targetScroll = 0f;
        }
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - height);
    }

    private void tickScroll(float partialTick) {
        if (Math.abs(targetScroll - scroll) < 0.5f) {
            scroll = targetScroll;
        } else {
            scroll += (targetScroll - scroll) * Math.min(1f, 0.35f * Math.max(partialTick, 1f));
        }
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
        tickScroll(partialTick);

        if (category == null || category.getElements().isEmpty()) {
            graphics.drawCenteredString(font, "No settings in this category", x + width / 2, y + height / 2 - 4, 0xFF808080);
            contentHeight = 0;
            return;
        }

        graphics.enableScissor(x, y, x + width, y + height);

        int cursorY = y - (int) scroll;
        List<GuiElement> elements = category.getElements();
        for (GuiElement element : elements) {
            int elemHeight = element.getHeight();

            if (cursorY + elemHeight >= y && cursorY <= y + height) {
                // Only draw chrome for plain Settings; Sections draw their own chrome
                if (!(element instanceof Section)) {
                    drawRowChrome(graphics, x, cursorY, width, elemHeight);
                }
                element.render(graphics, font, x + 10, cursorY, width - 20, mouseX, mouseY, partialTick);
            }

            cursorY += elemHeight + ROW_GAP;
        }

        graphics.disableScissor();

        contentHeight = (cursorY + (int) scroll) - y - ROW_GAP;
        if (targetScroll > maxScroll()) {
            targetScroll = maxScroll();
        }

        drawScrollbar(graphics);
    }

    private void drawRowChrome(GuiGraphics graphics, int rx, int ry, int rw, int rh) {
        graphics.fill(rx, ry, rx + 1, ry + rh, 0xFF08080E);
        graphics.fill(rx + 1, ry, rx + rw, ry + 1, 0xFF08080E);
        graphics.fill(rx + rw - 1, ry + 1, rx + rw, ry + rh, 0xFF28282E);
        graphics.fill(rx + 1, ry + rh - 1, rx + rw - 1, ry + rh, 0xFF28282E);
        graphics.fill(rx + 1, ry + 1, rx + rw - 1, ry + rh - 1, 0x6008080E);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int max = maxScroll();
        if (max <= 0) return;

        int trackX1 = x + width - SCROLLBAR_W;
        int trackX2 = x + width;
        int trackY1 = y + 5;
        int trackY2 = y + height - 5;
        graphics.fill(trackX1, trackY1, trackX2, trackY2, 0xFF101010);

        float barSize = Math.min(1f, (float) height / contentHeight);
        int trackHeight = trackY2 - trackY1;
        int thumbH = Math.max(10, Math.round(trackHeight * barSize));
        float scrollRatio = max == 0 ? 0 : scroll / max;
        int thumbY = trackY1 + Math.round((trackHeight - thumbH) * scrollRatio);

        graphics.fill(trackX1 + 1, thumbY, trackX2 - 1, thumbY + thumbH, 0xFF303030);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!isInside(mouseX, mouseY)) return false;
        int notch = scrollY > 0 ? -1 : (scrollY < 0 ? 1 : 0);
        targetScroll = clamp(targetScroll + notch * WHEEL_STEP);
        return true;
    }

    private float clamp(float v) {
        return Math.max(0, Math.min(maxScroll(), v));
    }

    private boolean isInside(double mx, double my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (category == null) return false;

        int max = maxScroll();
        if (max > 0 && mouseX >= x + width - SCROLLBAR_W && mouseX <= x + width) {
            draggingScrollbar = true;
            scrollToThumbPosition(mouseY);
            return true;
        }

        if (!isInside(mouseX, mouseY)) return false;

        int cursorY = y - (int) scroll;
        for (GuiElement element : category.getElements()) {
            int elemHeight = element.getHeight();
            if (mouseY >= cursorY && mouseY <= cursorY + elemHeight) {
                return element.mouseClicked(mouseX, mouseY, button, x + 10, cursorY, width - 20);
            }
            cursorY += elemHeight + ROW_GAP;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            scrollToThumbPosition(mouseY);
            return true;
        }
        if (category == null) return false;

        int cursorY = y - (int) scroll;
        for (GuiElement element : category.getElements()) {
            int elemHeight = element.getHeight();
            if (element.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            cursorY += elemHeight + ROW_GAP;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingScrollbar;
        draggingScrollbar = false;

        if (category != null) {
            for (GuiElement element : category.getElements()) {
                element.mouseReleased(mouseX, mouseY, button);
            }
        }
        return wasDragging;
    }

    private void scrollToThumbPosition(double mouseY) {
        int trackY1 = y + 5;
        int trackY2 = y + height - 5;
        float p = (float) (mouseY - trackY1) / (trackY2 - trackY1);
        scroll = clamp(p * maxScroll());
        targetScroll = scroll;
    }
}