package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * The scrollable content area that lays out an arbitrary list of {@link Setting}s
 * top-to-bottom and clips them to its bounds.
 * <p>
 * This is a direct translation of the scrolling math in NEU's
 * {@code NEUConfigEditor} (the {@code optionsScroll} LerpingInteger, the
 * wheel-delta handling in {@code mouseInput()}, and the scrollbar geometry
 * drawn alongside the option list):
 * <ul>
 *   <li>Mouse wheel moves the scroll target by 30px per notch (NEU: {@code dWheel * 30}).</li>
 *   <li>The target is clamped to {@code [0, contentHeight - viewHeight]} so you can't
 *       scroll past the last row (NEU computed this as {@code barMax}).</li>
 *   <li>The scrollbar thumb's height is {@code viewHeight / contentHeight} of the track,
 *       and its position is {@code scroll / contentHeight} down the track - same ratio
 *       NEU used for {@code barSize} / {@code barStart} / {@code barEnd}.</li>
 * </ul>
 * The only meaningful change is swapping NEU's {@code GlScissorStack} (a manual
 * push/pop stack over raw GL11 scissor calls, needed because 1.8.9 had no scissor
 * helper) for {@code GuiGraphics#enableScissor}, which does the same clipping
 * natively in modern Minecraft.
 */
public class ScrollableListWidget {

    private static final int ROW_GAP = 6;
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

    /** Swapping categories resets scroll, mirroring NEU's setSelectedCategory() calling optionsScroll.setValue(0). */
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
            // Simple eased approach toward target - same intent as NEU's LerpingInteger
            // (start at value, glide to target over a short window) without needing a
            // dedicated timing class.
            scroll += (targetScroll - scroll) * Math.min(1f, 0.35f * Math.max(partialTick, 1f));
        }
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTick) {
        tickScroll(partialTick);

        if (category == null || category.getSettings().isEmpty()) {
            graphics.drawCenteredString(font, "No settings in this category", x + width / 2, y + height / 2 - 4, 0x808080);
            contentHeight = 0;
            return;
        }

        graphics.enableScissor(x, y, x + width, y + height);

        int cursorY = y - (int) scroll;
        List<Setting<?>> settings = category.getSettings();
        for (Setting<?> setting : settings) {
            int rowHeight = setting.getHeight();

            // Cull rows fully outside the viewport (NEU did the same bounds check
            // before calling editor.render(), avoiding wasted draw calls off-screen).
            if (cursorY + rowHeight >= y && cursorY <= y + height) {
                drawRowChrome(graphics, x, cursorY, width, rowHeight);
                setting.render(graphics, font, x + 8, cursorY + 4, width - 16, mouseX, mouseY, partialTick);
            }

            cursorY += rowHeight + ROW_GAP;
        }

        graphics.disableScissor();

        contentHeight = (cursorY + (int) scroll) - y - ROW_GAP;
        if (targetScroll > maxScroll()) {
            targetScroll = maxScroll();
        }

        drawScrollbar(graphics);
    }

    /** Row background - NEU's shared floating-rect look (dark fill, light top/left edge, dark bottom/right edge). */
    private void drawRowChrome(GuiGraphics graphics, int rx, int ry, int rw, int rh) {
        int main = 0xD0202026;
        int light = 0xFF303036;
        int dark = 0xFF101016;
        graphics.fill(rx, ry, rx + 1, ry + rh, light);
        graphics.fill(rx + 1, ry, rx + rw, ry + 1, light);
        graphics.fill(rx + rw - 1, ry + 1, rx + rw, ry + rh, dark);
        graphics.fill(rx + 1, ry + rh - 1, rx + rw - 1, ry + rh, dark);
        graphics.fill(rx + 1, ry + 1, rx + rw - 1, ry + rh - 1, main);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int max = maxScroll();
        if (max <= 0) return;

        int trackX1 = x + width - SCROLLBAR_W;
        int trackX2 = x + width;
        int trackY1 = y + 2;
        int trackY2 = y + height - 2;
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
        for (Setting<?> setting : category.getSettings()) {
            int rowHeight = setting.getHeight();
            if (mouseY >= cursorY && mouseY <= cursorY + rowHeight) {
                return setting.mouseClicked(mouseX, mouseY, button, x + 8, cursorY + 4, width - 16);
            }
            cursorY += rowHeight + ROW_GAP;
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
        for (Setting<?> setting : category.getSettings()) {
            int rowHeight = setting.getHeight();
            if (setting.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
            cursorY += rowHeight + ROW_GAP;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingScrollbar;
        draggingScrollbar = false;

        if (category != null) {
            for (Setting<?> setting : category.getSettings()) {
                setting.mouseReleased(mouseX, mouseY, button);
            }
        }
        return wasDragging;
    }

    private void scrollToThumbPosition(double mouseY) {
        int trackY1 = y + 2;
        int trackY2 = y + height - 2;
        float p = (float) (mouseY - trackY1) / (trackY2 - trackY1);
        scroll = clamp(p * maxScroll());
        targetScroll = scroll;
    }
}
