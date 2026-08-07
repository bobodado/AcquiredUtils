package dev.bobodado.acquiredutils.client.gui;

import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.gui.section.ModSection;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AcquiredUtilsConfigScreen extends Screen {

    public static final int BASE_PANEL_WIDTH = 480;
    public static final int BASE_PANEL_HEIGHT = 260;
    public static final int BASE_HEADER_HEIGHT = 22;
    public static final int BASE_FOOTER_HEIGHT = 26;
    public static final int BASE_SIDEBAR_WIDTH = 110;
    public static final int BASE_PADDING = 8;
    public static final int BASE_TAB_HEIGHT = 18;

    private static final int COLOR_PANEL_BG = 0xFF2B2B2B;
    private static final int COLOR_FRAME_WOOD = 0xFF6B4A2E;
    private static final int COLOR_BAR_BG = 0xFF1F1F1F;
    private static final int COLOR_SIDEBAR_BG = 0xFF232323;
    private static final int COLOR_ORANGE = 0xFFE38A2D;
    private static final int COLOR_WHITE = 0xFFF2F2F2;
    private static final int COLOR_TAB_ACTIVE_BORDER = 0xFF7A5A34;

    private final Screen parent;

    private float menuScale;
    private int panelWidth, panelHeight, headerHeight, footerHeight;
    private int sidebarWidth, padding, tabHeight;
    private int panelX, panelY;

    private final Map<String, ModSection> sections = new LinkedHashMap<>();
    private String activeSectionId;
    private final List<AbstractWidget> sectionWidgets = new ArrayList<>();

    private final List<TabPos> tabPositions = new ArrayList<>();
    private record TabPos(int x, int y, int w, int h, String id) {}

    public AcquiredUtilsConfigScreen(Screen parent) {
        super(Component.translatable("acquiredutils.gui.title"));
        this.parent = parent;
    }

    public void registerSection(ModSection section) {
        sections.put(section.getId(), section);
        if (activeSectionId == null) activeSectionId = section.getId();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public float getMenuScale() {
        return menuScale;
    }

    public int s(int base) {
        return (int) (base * menuScale);
    }

    public void addSectionWidget(AbstractWidget widget) {
        sectionWidgets.add(widget);
        addRenderableWidget(widget);
    }

    public void rebuild() {
        init();
    }

    private void computeLayout() {
        this.menuScale = AcquiredUtilsConfig.get().menuScale;
        this.panelWidth = s(BASE_PANEL_WIDTH);
        this.panelHeight = s(BASE_PANEL_HEIGHT);
        this.headerHeight = s(BASE_HEADER_HEIGHT);
        this.footerHeight = s(BASE_FOOTER_HEIGHT);
        this.sidebarWidth = s(BASE_SIDEBAR_WIDTH);
        this.padding = s(BASE_PADDING);
        this.tabHeight = s(BASE_TAB_HEIGHT);
        this.panelX = (this.width - panelWidth) / 2;
        this.panelY = (this.height - panelHeight) / 2;
    }

    @Override
    protected void init() {
        computeLayout();
        clearWidgets();
        sectionWidgets.clear();
        tabPositions.clear();

        buildHeader();
        buildSidebarTabs();

        ModSection active = sections.get(activeSectionId);
        if (active != null) {
            int cx = panelX + sidebarWidth + padding;
            int cy = panelY + headerHeight + padding;
            int cw = panelWidth - sidebarWidth - padding * 2;
            int ch = panelHeight - headerHeight - footerHeight - padding * 2;
            active.buildContent(cx, cy, cw, ch);
        }
    }

    private void buildHeader() {
        int closeSize = s(12);
        addRenderableWidget(Button.builder(Component.literal("X"), b -> onClose())
                .bounds(panelX + panelWidth - closeSize - padding, panelY + padding, closeSize, closeSize)
                .build());
    }

    private void buildSidebarTabs() {
        int tabX = panelX + padding;
        int tabWidth = sidebarWidth - padding * 2;
        int tabY = panelY + headerHeight + padding;

        int i = 0;
        for (ModSection section : sections.values()) {
            final String id = section.getId();
            int y = tabY + i * (tabHeight + s(4));
            tabPositions.add(new TabPos(tabX, y, tabWidth, tabHeight, id));

            addRenderableWidget(Button.builder(section.getDisplayName(), b -> {
                switchTab(id);
            }).bounds(tabX, y, tabWidth, tabHeight).build());
            i++;
        }
    }

    private void switchTab(String id) {
        ModSection old = sections.get(activeSectionId);
        if (old != null) old.onClose();
        activeSectionId = id;
        init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        drawPanelChrome(graphics);

        int titleX = panelX + padding;
        int titleY = panelY + (headerHeight - s(8)) / 2;
        graphics.drawString(this.font, Component.literal("MOD SETTINGS: "), titleX, titleY, COLOR_WHITE, false);
        int afterFirst = titleX + this.font.width("MOD SETTINGS: ");
        graphics.drawString(this.font, Component.literal("AcquiredUtils"), afterFirst, titleY, COLOR_ORANGE, false);
        int afterSecond = afterFirst + this.font.width("AcquiredUtils");
        graphics.drawString(this.font, Component.literal(" v1.0.0"), afterSecond, titleY, COLOR_WHITE, false);

        for (TabPos tab : tabPositions) {
            if (tab.id.equals(activeSectionId)) {
                graphics.renderOutline(tab.x, tab.y, tab.w, tab.h, COLOR_TAB_ACTIVE_BORDER);
            }
        }

        ModSection active = sections.get(activeSectionId);
        if (active != null) {
            int cx = panelX + sidebarWidth + padding;
            int cy = panelY + headerHeight + padding;

            graphics.pose().pushMatrix();
            graphics.pose().translate(cx, cy);
            graphics.pose().scale(1.5f, 1.5f);
            graphics.pose().popMatrix();

            active.render(graphics, mouseX, mouseY, partialTick,
                    cx, cy, panelWidth - sidebarWidth - padding * 2,
                    panelHeight - headerHeight - footerHeight - padding * 2);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawPanelChrome(GuiGraphics graphics) {
        int ft = Math.max(1, s(4));
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, COLOR_PANEL_BG);
        graphics.renderOutline(panelX - ft, panelY - ft, panelWidth + ft * 2, panelHeight + ft * 2, COLOR_FRAME_WOOD);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + headerHeight, COLOR_BAR_BG);
        graphics.fill(panelX, panelY + headerHeight, panelX + sidebarWidth, panelY + panelHeight - footerHeight, COLOR_SIDEBAR_BG);
        graphics.fill(panelX, panelY + panelHeight - footerHeight, panelX + panelWidth, panelY + panelHeight, COLOR_BAR_BG);
    }

    public void drawDescription(GuiGraphics graphics, String translationKey, int x, int y) {
        Component desc = Component.translatable(translationKey)
                .copy()
                .withStyle(Style.EMPTY.withItalic(true));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(0.75f * menuScale, 0.75f * menuScale);
        graphics.drawString(this.font, desc, 0, 0, 0x80999999, false);
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        ModSection active = sections.get(activeSectionId);
        if (active != null && active.mouseClicked(event.x(), event.y(), event.button())) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ModSection active = sections.get(activeSectionId);
        if (active != null && active.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        ModSection active = sections.get(activeSectionId);
        if (active != null && active.keyPressed(event)) return true;
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        for (ModSection section : sections.values()) section.onClose();
        AcquiredUtilsConfig.save();
        AcquiredUtils.LOGGER.info("[AcquiredUtils] Settings auto-saved on menu close");
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
