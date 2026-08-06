package com.acquiredutils.client.config;

import com.acquiredutils.client.config.widget.BooleanSettingWidget;
import com.acquiredutils.client.config.widget.SliderSettingWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NEU-style config screen with search bar, keyboard navigation, tooltips, and opening animation.
 */
public class ConfigScreen extends Screen {

    private static final int TAB_W = 110;
    private static final int TAB_H = 20;
    private static final int CONTENT_PAD = 10;

    private final Map<String, ConfigCategory> categories = new LinkedHashMap<>();
    private final List<String> categoryNames = new ArrayList<>();
    private String activeCategory = null;

    private final ScrollableListWidget listWidget = new ScrollableListWidget();
    private EditBox searchBox;
    private boolean searching = false;

    // Opening animation
    private long openTime = 0;
    private static final long OPEN_ANIM_MS = 300;

    public ConfigScreen() {
        super(Component.literal("AcquiredUtils Config"));
    }

    public void addCategory(ConfigCategory category) {
        categories.put(category.getName(), category);
        categoryNames.add(category.getName());
        if (activeCategory == null) activeCategory = category.getName();
    }

    @Override
    protected void init() {
        super.init();
        openTime = System.currentTimeMillis();

        int contentX = TAB_W + CONTENT_PAD * 2;
        int contentY = 40;
        int contentW = this.width - contentX - CONTENT_PAD;
        int contentH = this.height - contentY - CONTENT_PAD;

        listWidget.setBounds(contentX, contentY, contentW, contentH);
        if (activeCategory != null) listWidget.setCategory(categories.get(activeCategory));

        // Search box - top right of content area
        int searchW = 140;
        searchBox = new EditBox(this.font, this.width - CONTENT_PAD - searchW, 18, searchW, 16, Component.literal("Search..."));
        searchBox.setMaxLength(50);
        searchBox.setResponder(this::onSearchChanged);
        searchBox.setTextColor(0xFFc0c0c0);
        searchBox.setBordered(false);
        this.addRenderableWidget(searchBox);
    }

    private void onSearchChanged(String query) {
        listWidget.setSearchQuery(query);
        searching = !query.isEmpty();
    }

    private ConfigCategory activeCategory() {
        return categories.get(activeCategory);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Opening animation alpha
        long elapsed = System.currentTimeMillis() - openTime;
        float openT = Math.min(1f, elapsed / (float) OPEN_ANIM_MS);
        float openEased = sigmoid(openT * 2f - 1f) * 0.5f + 0.5f;
        int bgAlpha = Math.round(0xCC * openEased);
        int panelAlpha = Math.round(0xFF * openEased);

        // Background
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, this.width, this.height, (bgAlpha << 24));

        // Main panel
        int panelX = CONTENT_PAD;
        int panelY = CONTENT_PAD;
        int panelW = this.width - CONTENT_PAD * 2;
        int panelH = this.height - CONTENT_PAD * 2;
        drawPanel(graphics, panelX, panelY, panelW, panelH, panelAlpha);

        // Title
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 22, 0xFFc0c0c0);

        // Category tabs
        int tabY = 45;
        for (String name : categoryNames) {
            boolean selected = name.equals(activeCategory);
            int color = selected ? 0xFFa368ef : 0xFF808080;
            if (mouseX >= CONTENT_PAD && mouseX <= CONTENT_PAD + TAB_W &&
                mouseY >= tabY && mouseY <= tabY + TAB_H) {
                color = selected ? 0xFFc090ff : 0xFFa0a0a0;
            }
            graphics.drawString(this.font, name, CONTENT_PAD + 8, tabY + 6, color, false);
            if (selected) {
                graphics.fill(CONTENT_PAD, tabY + TAB_H - 1, CONTENT_PAD + TAB_W, tabY + TAB_H, 0xFFa368ef);
            }
            tabY += TAB_H + 4;
        }

        // Divider
        graphics.fill(TAB_W + CONTENT_PAD, 40, TAB_W + CONTENT_PAD + 1, panelY + panelH, 0xFF28282E);

        // Active category description
        if (activeCategory() != null && !activeCategory().getDescription().isEmpty() && !searching) {
            graphics.drawWordWrap(this.font, Component.literal(activeCategory().getDescription()),
                TAB_W + CONTENT_PAD * 2, 45, this.width - TAB_W - CONTENT_PAD * 3, 0xFF808080);
        }

        // Content list
        listWidget.render(graphics, this.font, mouseX, mouseY, partialTick);

        // Search hint
        if (searchBox.getValue().isEmpty()) {
            graphics.drawString(this.font, "Search...", searchBox.getX() + 4, searchBox.getY() + 4, 0xFF606060, false);
        }
    }

    private void drawPanel(GuiGraphics g, int x, int y, int w, int h, int alpha) {
        int edge = (alpha << 24) | 0x00202026;
        int fill = (Math.round(alpha * 0.75f) << 24) | 0x0018181E;
        g.fill(x, y, x + w, y + 1, edge);
        g.fill(x, y + h - 1, x + w, y + h, edge);
        g.fill(x, y + 1, x + 1, y + h - 1, edge);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, edge);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        // Tab click
        int tabY = 45;
        for (String name : categoryNames) {
            if (mouseX >= CONTENT_PAD && mouseX <= CONTENT_PAD + TAB_W &&
                mouseY >= tabY && mouseY <= tabY + TAB_H) {
                activeCategory = name;
                listWidget.setCategory(categories.get(name));
                listWidget.setSearchQuery(searchBox.getValue());
                return true;
            }
            tabY += TAB_H + 4;
        }

        return listWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) return true;
        return listWidget.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return listWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (listWidget.mouseScrolled(mouseX, mouseY, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC closes and saves
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            this.onClose();
            return true;
        }

        // UP/DOWN arrow navigation
        if (keyCode == 265) { // GLFW_KEY_UP
            listWidget.mouseScrolled(0, 0, 1);
            return true;
        }
        if (keyCode == 264) { // GLFW_KEY_DOWN
            listWidget.mouseScrolled(0, 0, -1);
            return true;
        }

        // Focus search with Ctrl+F
        if (keyCode == 33 && hasControlDown()) { // GLFW_KEY_F
            this.setFocused(searchBox);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void onClose() {
        // TODO: trigger config save here if you have a ConfigManager
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static float sigmoid(float t) {
        return (float) (1.0 / (1.0 + Math.exp(-(t * 6.0))));
    }
}