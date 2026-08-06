package com.acquiredutils.client.config;

import com.acquiredutils.client.config.widget.BooleanSettingWidget;
import com.acquiredutils.client.config.widget.SliderSettingWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main config screen with tabs, search bar, keyboard navigation, and opening animation.
 * Uses Fabric 1.21.11 event-object input signatures (MouseButtonEvent, KeyEvent, CharacterEvent).
 */
public class ConfigScreen extends Screen {
    private final Screen parent;
    private final List<ConfigCategory> categories;
    private final Consumer<ConfigCategory> saveCallback;

    private ScrollableListWidget scrollableList;
    private EditBox searchBox;
    private int selectedTab = 0;
    private long openTime;
    private static final long ANIMATION_DURATION = 300; // ms

    // Layout constants
    private static final int TAB_HEIGHT = 24;
    private static final int SEARCH_WIDTH = 140;
    private static final int SEARCH_HEIGHT = 18;
    private static final int PANEL_MARGIN = 16;
    private static final int PANEL_TOP_MARGIN = 40;

    // Colors
    private static final int BG_COLOR = 0xFF121216;
    private static final int PANEL_BG = 0xFF1A1A1E;
    private static final int TAB_INACTIVE = 0xFF2A2A32;
    private static final int TAB_ACTIVE = 0xFFA368EF;
    private static final int TAB_TEXT = 0xFFE8E8EC;
    private static final int SEARCH_BG = 0xFF242428;
    private static final int SEARCH_BORDER = 0xFF404046;
    private static final int SEARCH_TEXT = 0xFFE8E8EC;

    public ConfigScreen(Screen parent, List<ConfigCategory> categories, Consumer<ConfigCategory> saveCallback) {
        super(Component.literal("AcquiredUtils Config"));
        this.parent = parent;
        this.categories = categories;
        this.saveCallback = saveCallback;
        this.openTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();

        int panelX = PANEL_MARGIN;
        int panelY = PANEL_TOP_MARGIN;
        int panelWidth = this.width - PANEL_MARGIN * 2;
        int panelHeight = this.height - panelY - PANEL_MARGIN;

        // Scrollable list
        scrollableList = new ScrollableListWidget(this.minecraft);
        scrollableList.setBounds(panelX, panelY, panelWidth, panelHeight);
        refreshListElements();

        // Search box (top-right)
        int searchX = this.width - PANEL_MARGIN - SEARCH_WIDTH;
        int searchY = 10;
        searchBox = new EditBox(this.minecraft.font, searchX, searchY, SEARCH_WIDTH, SEARCH_HEIGHT,
            Component.literal("Search..."));
        searchBox.setMaxLength(64);
        searchBox.setResponder(query -> {
            if (scrollableList != null) {
                scrollableList.setSearchQuery(query);
            }
        });
        searchBox.setTextColor(SEARCH_TEXT);
        searchBox.setBordered(true);
        this.addRenderableWidget(searchBox);
    }

    private void refreshListElements() {
        if (categories.isEmpty() || scrollableList == null) return;
        ConfigCategory category = categories.get(selectedTab);
        List<GuiElement> elements = new ArrayList<>(category.getElements());
        scrollableList.setElements(elements);
    }

    /**
     * Sigmoid easing: 1 / (1 + e^(-6*(t-0.5)))
     */
    private float getPanelAlpha() {
        long elapsed = System.currentTimeMillis() - openTime;
        float t = Math.min(1.0f, elapsed / (float) ANIMATION_DURATION);
        return 1.0f / (1.0f + (float) Math.exp(-6.0f * (t - 0.5f)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        float alpha = getPanelAlpha();
        int alphaByte = Math.max(0, Math.min(255, (int) (alpha * 255)));

        // Background
        this.renderBackground(graphics, mouseX, mouseY, delta);

        // Panel background with alpha
        int panelX = PANEL_MARGIN;
        int panelY = PANEL_TOP_MARGIN;
        int panelW = this.width - PANEL_MARGIN * 2;
        int panelH = this.height - panelY - PANEL_MARGIN;
        int panelBgWithAlpha = (alphaByte << 24) | (PANEL_BG & 0x00FFFFFF);
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, panelBgWithAlpha);

        // Render tabs
        renderTabs(graphics, mouseX, mouseY, alphaByte);

        // Render search box (EditBox handles its own rendering via addRenderableWidget)
        super.render(graphics, mouseX, mouseY, delta);

        // Render scrollable list
        if (scrollableList != null) {
            scrollableList.render(graphics, mouseX, mouseY, delta);
        }

        // Second pass: tooltips after scissor is disabled
        if (scrollableList != null) {
            scrollableList.renderOverlay(graphics, mouseX, mouseY, delta);
        }
    }

    private void renderTabs(GuiGraphics graphics, int mouseX, int mouseY, int alphaByte) {
        if (categories.size() <= 1) return;

        int tabY = PANEL_TOP_MARGIN - TAB_HEIGHT;
        int tabX = PANEL_MARGIN;
        int tabSpacing = 4;

        for (int i = 0; i < categories.size(); i++) {
            ConfigCategory cat = categories.get(i);
            int tabWidth = this.minecraft.font.width(cat.getName()) + 20;
            boolean active = i == selectedTab;
            boolean hovered = mouseX >= tabX && mouseX < tabX + tabWidth
                && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;

            int color = active ? TAB_ACTIVE : (hovered ? 0xFF505058 : TAB_INACTIVE);
            color = (alphaByte << 24) | (color & 0x00FFFFFF);

            graphics.fill(tabX, tabY, tabX + tabWidth, tabY + TAB_HEIGHT, color);
            graphics.drawString(this.minecraft.font, cat.getName(),
                tabX + 10, tabY + 7, TAB_TEXT, false);

            tabX += tabWidth + tabSpacing;
        }
    }

    // ------------------------------------------------------------------
    // Input handlers — Fabric 1.21.11 event-object signatures.
    // These deliberately do NOT use @Override because the supertype
    // declares them via ContainerEventListener with event objects.
    // ------------------------------------------------------------------

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        int button = event.getButton();

        // Check tab clicks
        if (categories.size() > 1) {
            int tabY = PANEL_TOP_MARGIN - TAB_HEIGHT;
            int tabX = PANEL_MARGIN;
            int tabSpacing = 4;
            for (int i = 0; i < categories.size(); i++) {
                ConfigCategory cat = categories.get(i);
                int tabWidth = this.minecraft.font.width(cat.getName()) + 20;
                if (mouseX >= tabX && mouseX < tabX + tabWidth
                    && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT) {
                    selectedTab = i;
                    refreshListElements();
                    return true;
                }
                tabX += tabWidth + tabSpacing;
            }
        }

        // Delegate to scrollable list
        if (scrollableList != null && scrollableList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        int button = event.getButton();

        if (scrollableList != null && scrollableList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        int button = event.getButton();

        if (scrollableList != null && scrollableList.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (scrollableList != null && scrollableList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int scanCode = event.getScanCode();
        int modifiers = event.getModifiers();

        // Ctrl+F: focus search
        if (keyCode == GLFW.GLFW_KEY_F && event.hasControlDown()) {
            if (searchBox != null) {
                this.setFocused(searchBox);
                searchBox.setFocused(true);
                searchBox.setHighlightPos(0);
                searchBox.setCursorPosition(searchBox.getValue().length());
            }
            return true;
        }

        // ESC: save and close
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            saveAndClose();
            return true;
        }

        // UP/DOWN: scroll list
        if (scrollableList != null) {
            if (keyCode == GLFW.GLFW_KEY_UP) {
                scrollableList.scroll(1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                scrollableList.scroll(-1);
                return true;
            }
        }

        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.keyPressed(event)) {
                return true;
            }
        }

        return super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.charTyped(event)) {
                return true;
            }
        }
        return super.charTyped(event);
    }

    private void saveAndClose() {
        // Commit all pending values
        for (ConfigCategory cat : categories) {
            for (Setting<?> setting : cat.getSettings()) {
                setting.commit();
            }
            if (saveCallback != null) {
                saveCallback.accept(cat);
            }
        }
        this.minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}