package com.acquiredutils.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeuStyleConfigScreen extends Screen {

    private static final int PANEL_MAX_W = 500;
    private static final int PANEL_MAX_H = 400;
    private static final int SIDEBAR_W = 140;
    private static final int INNER_PADDING = 10;
    private static final int TITLE_BAR_H = 20;
    private static final int CATEGORY_ROW_H = 15;

    private static final int COLOR_DIM_OVERLAY = 0x90101010;
    private static final int COLOR_PANEL_MAIN = 0xFF202026;
    private static final int COLOR_PANEL_LIGHT = 0xFF303036;
    private static final int COLOR_PANEL_DARK = 0xFF101016;
    private static final int COLOR_BORDER_DARK = 0xFF08080E;
    private static final int COLOR_BORDER_SIDEBAR = 0xFF28282E;
    private static final int COLOR_BORDER_CONTENT = 0xFF303036;
    private static final int COLOR_BORDER_FILL = 0x6008080E;
    private static final int COLOR_ACCENT_PURPLE = 0xFFA368EF;
    private static final int COLOR_CATEGORY_SELECTED = 0xFF5DBFBF;
    private static final int COLOR_CATEGORY_UNSELECTED = 0xFFA0A0A0;
    private static final int COLOR_SCROLLBAR_TRACK = 0xFF101010;
    private static final int COLOR_SCROLLBAR_THUMB = 0xFF303030;

    private final List<ConfigCategory> categories = new ArrayList<>();
    private final ScrollableListWidget listWidget = new ScrollableListWidget();

    private ConfigCategory selectedCategory;
    private float categoryScroll = 0f;

    private EditBox searchField;
    private String lastSearch = "";
    private ConfigCategory searchResultsCategory;

    private int panelX, panelY, panelW, panelH;
    private int sidebarInnerLeft, sidebarInnerTop, sidebarInnerRight, sidebarInnerBottom;
    private int contentInnerLeft, contentInnerTop, contentInnerRight, contentInnerBottom;

    private double lastMouseX, lastMouseY;

    public NeuStyleConfigScreen() {
        super(Component.literal("AcquiredUtils Configuration"));

        Map<String, ConfigCategory> registered = ConfigRegistry.getCategories();
        if (registered.isEmpty()) {
            ConfigCategory demo = ConfigRegistry.getOrCreate("General", "General AcquiredUtils settings");
            demo.add(new com.acquiredutils.client.config.widget.BooleanSettingWidget(
                "Show Overlay", "Toggles the rarity overlay in your inventory", true, val -> {}));
            demo.add(new com.acquiredutils.client.config.widget.SliderSettingWidget(
                "Overlay Scale", "Size of the overlay text", 1.0, 0.5, 2.0, 0.05, val -> {}));
        }
        this.categories.addAll(ConfigRegistry.getCategories().values());
        if (!categories.isEmpty()) {
            selectedCategory = categories.get(0);
        }
    }

    @Override
    protected void init() {
        computeLayout();

        searchField = new EditBox(this.font, contentInnerRight - 130, contentInnerTop - 24, 110, 16, Component.literal("Search"));
        searchField.setBordered(false);
        searchField.setMaxLength(64);
        searchField.setTextColor(0xC0C0C0);
        searchField.setHint(Component.literal("Search...").withStyle(style -> style.withColor(0x606060)));
        addRenderableWidget(searchField);

        listWidget.setBounds(contentInnerLeft + 1, contentInnerTop + 1,
            contentInnerRight - contentInnerLeft - 2, contentInnerBottom - contentInnerTop - 2);
        listWidget.setCategory(selectedCategory);
    }

    private void computeLayout() {
        panelW = Math.min(this.width - 40, PANEL_MAX_W);
        panelH = Math.min(this.height - 40, PANEL_MAX_H);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        sidebarInnerLeft = panelX + 4 + INNER_PADDING;
        sidebarInnerRight = panelX + 144 - INNER_PADDING;
        sidebarInnerTop = panelY + 49 + INNER_PADDING;
        sidebarInnerBottom = panelY + panelH - 5 - INNER_PADDING;

        contentInnerLeft = panelX + 149 + INNER_PADDING;
        contentInnerRight = panelX + panelW - 5 - INNER_PADDING;
        contentInnerTop = sidebarInnerTop;
        contentInnerBottom = sidebarInnerBottom;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        graphics.fill(0, 0, this.width, this.height, COLOR_DIM_OVERLAY);

        drawFloatingPanel(graphics, panelX, panelY, panelW, panelH);

        drawFloatingPanel(graphics, panelX + 5, panelY + 5, panelW - 10, TITLE_BAR_H);
        graphics.drawCenteredString(this.font, "AcquiredUtils Configuration",
            panelX + panelW / 2, panelY + 11, 0xA0A0A0);

        drawFloatingPanel(graphics, panelX + 4, panelY + 29, SIDEBAR_W, panelH - 34);
        drawInsetBorder(graphics, sidebarInnerLeft, sidebarInnerTop, sidebarInnerRight, sidebarInnerBottom, COLOR_BORDER_SIDEBAR);

        renderSidebar(graphics, mouseX, mouseY);

        graphics.drawCenteredString(this.font, "Categories", panelX + 75, panelY + 40, COLOR_ACCENT_PURPLE);

        drawFloatingPanel(graphics, panelX + 149, panelY + 29, panelW - 154, panelH - 34);
        drawInsetBorder(graphics, contentInnerLeft, contentInnerTop, contentInnerRight, contentInnerBottom, COLOR_BORDER_CONTENT);

        if (activeCategory() != null) {
            graphics.drawWordWrap(this.font, Component.literal(activeCategory().getDescription()),
                contentInnerLeft + 5, panelY + 40, contentInnerRight - contentInnerLeft - 150, 0xB0B0B0);
        }

        listWidget.setCategory(activeCategory());
        listWidget.render(graphics, this.font, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private ConfigCategory activeCategory() {
        return searchResultsCategory != null ? searchResultsCategory : selectedCategory;
    }

    private void renderSidebar(GuiGraphics graphics, int mouseX, int mouseY) {
        if (categories.isEmpty()) return;

        graphics.enableScissor(sidebarInnerLeft, sidebarInnerTop, sidebarInnerRight, sidebarInnerBottom);

        int catY = sidebarInnerTop + 12 - (int) categoryScroll;
        for (ConfigCategory category : categories) {
            boolean selected = (searchResultsCategory == null) && category == selectedCategory;
            int color = selected ? COLOR_CATEGORY_SELECTED : COLOR_CATEGORY_UNSELECTED;

            Component label = Component.literal(category.getName());
            if (selected) {
                label = label.copy().withStyle(style -> style.withUnderlined(true));
            }
            graphics.drawCenteredString(this.font, label, panelX + 75, catY - 4, color);

            catY += CATEGORY_ROW_H;
        }

        graphics.disableScissor();

        int contentHeight = (categories.size() * CATEGORY_ROW_H);
        int viewHeight = sidebarInnerBottom - sidebarInnerTop;
        if (contentHeight > viewHeight) {
            drawScrollbar(graphics, sidebarInnerLeft + 2, sidebarInnerTop + 5, sidebarInnerLeft + 7,
                sidebarInnerBottom - 5, categoryScroll, contentHeight, viewHeight);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int x1, int y1, int x2, int y2, float scroll, int contentHeight, int viewHeight) {
        graphics.fill(x1, y1, x2, y2, COLOR_SCROLLBAR_TRACK);
        int max = Math.max(1, contentHeight - viewHeight);
        float barSize = Math.min(1f, (float) viewHeight / contentHeight);
        int trackH = y2 - y1;
        int thumbH = Math.max(8, Math.round(trackH * barSize));
        int thumbY = y1 + Math.round((trackH - thumbH) * (scroll / max));
        graphics.fill(x1 + 1, thumbY, x2 - 1, thumbY + thumbH, COLOR_SCROLLBAR_THUMB);
    }

    private void drawFloatingPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + 1, y + h, COLOR_PANEL_LIGHT);
        graphics.fill(x + 1, y, x + w, y + 1, COLOR_PANEL_LIGHT);
        graphics.fill(x + w - 1, y + 1, x + w, y + h, COLOR_PANEL_DARK);
        graphics.fill(x + 1, y + h - 1, x + w - 1, y + h, COLOR_PANEL_DARK);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_PANEL_MAIN);
    }

    private void drawInsetBorder(GuiGraphics graphics, int left, int top, int right, int bottom, int rightBottomColor) {
        graphics.fill(left, top, left + 1, bottom, COLOR_BORDER_DARK);
        graphics.fill(left + 1, top, right, top + 1, COLOR_BORDER_DARK);
        graphics.fill(right - 1, top + 1, right, bottom, rightBottomColor);
        graphics.fill(left + 1, bottom - 1, right - 1, bottom, rightBottomColor);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, COLOR_BORDER_FILL);
    }

    // ---------------------------------------------------------------- input

    private int extractButton(net.minecraft.client.input.MouseButtonEvent event) {
        try { return (int) event.getClass().getMethod("button").invoke(event); } catch (Exception e) {
            try { return (int) event.getClass().getMethod("getButton").invoke(event); } catch (Exception e2) { return 0; }
        }
    }

    private int extractKeyCode(net.minecraft.client.input.KeyEvent event) {
        try { return (int) event.getClass().getMethod("keyCode").invoke(event); } catch (Exception e) {
            try { return (int) event.getClass().getMethod("key").invoke(event); } catch (Exception e2) { return 0; }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean handled) {
        if (super.mouseClicked(event, handled)) {
            return true;
        }

        double mouseX = this.lastMouseX;
        double mouseY = this.lastMouseY;
        int button = extractButton(event);

        if (button == 0 && mouseX >= sidebarInnerLeft && mouseX <= sidebarInnerRight &&
            mouseY >= sidebarInnerTop && mouseY <= sidebarInnerBottom) {
            int catY = sidebarInnerTop + 12 - (int) categoryScroll;
            for (ConfigCategory category : categories) {
                if (mouseY >= catY - 7 && mouseY <= catY + 7) {
                    selectedCategory = category;
                    searchResultsCategory = null;
                    searchField.setValue("");
                    return true;
                }
                catY += CATEGORY_ROW_H;
            }
        }

        return listWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = this.lastMouseX;
        double mouseY = this.lastMouseY;
        int button = extractButton(event);
        if (listWidget.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        double mouseX = this.lastMouseX;
        double mouseY = this.lastMouseY;
        int button = extractButton(event);
        boolean handled = listWidget.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(event) || handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < sidebarInnerLeft) {
            int contentHeight = categories.size() * CATEGORY_ROW_H;
            int viewHeight = sidebarInnerBottom - sidebarInnerTop;
            int max = Math.max(0, contentHeight - viewHeight);
            categoryScroll = Math.max(0, Math.min(max, categoryScroll - (float) (scrollY * 15)));
            return true;
        }
        return listWidget.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        boolean handled = super.charTyped(event);
        runSearchIfChanged();
        return handled;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        boolean handled = super.keyPressed(event);
        runSearchIfChanged();

        if (extractKeyCode(event) == 256) { // GLFW.GLFW_KEY_ESCAPE
            this.onClose();
            return true;
        }
        return handled;
    }

    private void runSearchIfChanged() {
        if (searchField == null) return;
        String search = searchField.getValue().trim().toLowerCase();
        if (search.equals(lastSearch)) return;
        lastSearch = search;

        if (search.isEmpty()) {
            searchResultsCategory = null;
            return;
        }

        ConfigCategory results = new ConfigCategory("Search Results", "Matches for \"" + search + "\"");
        for (ConfigCategory category : categories) {
            for (Setting<?> setting : category.getSettings()) {
                String haystack = (setting.getName() + " " + setting.getDescription()).toLowerCase();
                if (haystack.contains(search)) {
                    results.add(setting);
                }
            }
        }
        searchResultsCategory = results;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}