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

    // --- "Ancient Forge" theme: dark charcoal base, warm bronze/copper frame,
    // ember-orange accents. Built entirely from fill()/renderOutline()/
    // drawString() calls already proven to compile — gradients are simulated
    // with per-row fill() bands rather than an unverified fillGradient call.
    private static final int COLOR_WHITE = 0xFFF2F2F2;
    private static final int COLOR_ACCENT = 0xFFE38A2D;          // ember orange — matches existing title accent
    private static final int COLOR_ACCENT_BRIGHT = 0xFFD98F3E;   // frame highlight / active-tab edge

    private static final int COLOR_PANEL_TOP = 0xFF32241C;
    private static final int COLOR_PANEL_BOTTOM = 0xFF1C1512;
    private static final int COLOR_FRAME_OUTER = 0xFF140D08;
    private static final int COLOR_FRAME_MID = 0xFF8B5A2B;
    private static final int COLOR_HEADER_TOP = 0xFF3A2A1E;
    private static final int COLOR_HEADER_BOTTOM = 0xFF1F1611;
    private static final int COLOR_SIDEBAR_TOP = 0xFF241A14;
    private static final int COLOR_SIDEBAR_BOTTOM = 0xFF1A130F;
    private static final int COLOR_FOOTER_TOP = 0xFF1F1611;
    private static final int COLOR_FOOTER_BOTTOM = 0xFF140E0A;
    private static final int COLOR_DIVIDER = 0x40D98F3E;
    private static final int COLOR_SHADOW = 0x60000000;
    private static final int COLOR_TAB_ACTIVE_BG = 0x33D98F3E;

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
        graphics.drawString(this.font, Component.literal("AcquiredUtils"), afterFirst, titleY, COLOR_ACCENT, false);
        int afterSecond = afterFirst + this.font.width("AcquiredUtils");
        graphics.drawString(this.font, Component.literal(" v1.0.0"), afterSecond, titleY, COLOR_WHITE, false);

        // Subtle accent underline beneath the header text, tying it to the frame color.
        int underlineY = panelY + headerHeight - 1;
        graphics.fill(panelX, underlineY, panelX + panelWidth, underlineY + 1, COLOR_DIVIDER);

        // Divider between the sidebar and the content panel.
        graphics.fill(panelX + sidebarWidth, panelY + headerHeight,
                panelX + sidebarWidth + 1, panelY + panelHeight - footerHeight, COLOR_DIVIDER);

        ModSection active = sections.get(activeSectionId);
        if (active != null) {
            int cx = panelX + sidebarWidth + padding;
            int cy = panelY + headerHeight + padding;

            active.render(graphics, mouseX, mouseY, partialTick,
                    cx, cy, panelWidth - sidebarWidth - padding * 2,
                    panelHeight - headerHeight - footerHeight - padding * 2);
        }

        // Buttons (including tab buttons) render here. The active-tab
        // highlight below is drawn AFTER this on purpose — drawing it before
        // super.render() meant the vanilla button background painted right
        // over it, making the active tab indicator invisible. This ordering
        // is the actual fix, not just a visual tweak.
        super.render(graphics, mouseX, mouseY, partialTick);

        for (TabPos tab : tabPositions) {
            if (tab.id().equals(activeSectionId)) {
                graphics.fill(tab.x(), tab.y(), tab.x() + tab.w(), tab.y() + tab.h(), COLOR_TAB_ACTIVE_BG);
                int edgeW = Math.max(1, s(2));
                graphics.fill(tab.x(), tab.y(), tab.x() + edgeW, tab.y() + tab.h(), COLOR_ACCENT_BRIGHT);
            }
        }
    }

    private void drawPanelChrome(GuiGraphics graphics) {
        int ft = Math.max(1, s(4));

        // Soft drop shadow behind the whole panel.
        int shadowOffset = Math.max(1, s(3));
        graphics.fill(panelX - ft + shadowOffset, panelY - ft + shadowOffset,
                panelX + panelWidth + ft + shadowOffset, panelY + panelHeight + ft + shadowOffset, COLOR_SHADOW);

        // Panel background: vertical gradient instead of a flat fill.
        fillVerticalGradient(graphics, panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                COLOR_PANEL_TOP, COLOR_PANEL_BOTTOM);

        // Beveled frame: dark outer edge, warm bronze mid layer, bright inner highlight line.
        graphics.renderOutline(panelX - ft, panelY - ft, panelWidth + ft * 2, panelHeight + ft * 2, COLOR_FRAME_OUTER);
        graphics.renderOutline(panelX - ft + 1, panelY - ft + 1, panelWidth + ft * 2 - 2, panelHeight + ft * 2 - 2, COLOR_FRAME_MID);
        graphics.renderOutline(panelX - 1, panelY - 1, panelWidth + 2, panelHeight + 2, COLOR_ACCENT_BRIGHT);

        // Corner accent brackets on the outer frame.
        drawCornerAccents(graphics, panelX - ft, panelY - ft, panelWidth + ft * 2, panelHeight + ft * 2);

        // Header / sidebar / footer, each a subtle vertical gradient.
        fillVerticalGradient(graphics, panelX, panelY, panelX + panelWidth, panelY + headerHeight,
                COLOR_HEADER_TOP, COLOR_HEADER_BOTTOM);
        fillVerticalGradient(graphics, panelX, panelY + headerHeight, panelX + sidebarWidth, panelY + panelHeight - footerHeight,
                COLOR_SIDEBAR_TOP, COLOR_SIDEBAR_BOTTOM);
        fillVerticalGradient(graphics, panelX, panelY + panelHeight - footerHeight, panelX + panelWidth, panelY + panelHeight,
                COLOR_FOOTER_TOP, COLOR_FOOTER_BOTTOM);
    }

    /** Small L-shaped accent brackets at each corner of the given rectangle, for an ornate-frame look. */
    private void drawCornerAccents(GuiGraphics graphics, int x, int y, int w, int h) {
        int len = Math.max(2, s(9));
        int thick = Math.max(1, s(2));

        // Top-left
        graphics.fill(x, y, x + len, y + thick, COLOR_ACCENT_BRIGHT);
        graphics.fill(x, y, x + thick, y + len, COLOR_ACCENT_BRIGHT);
        // Top-right
        graphics.fill(x + w - len, y, x + w, y + thick, COLOR_ACCENT_BRIGHT);
        graphics.fill(x + w - thick, y, x + w, y + len, COLOR_ACCENT_BRIGHT);
        // Bottom-left
        graphics.fill(x, y + h - thick, x + len, y + h, COLOR_ACCENT_BRIGHT);
        graphics.fill(x, y + h - len, x + thick, y + h, COLOR_ACCENT_BRIGHT);
        // Bottom-right
        graphics.fill(x + w - len, y + h - thick, x + w, y + h, COLOR_ACCENT_BRIGHT);
        graphics.fill(x + w - thick, y + h - len, x + w, y + h, COLOR_ACCENT_BRIGHT);
    }

    /** Linear-interpolates two ARGB ints. Used to simulate a gradient with plain fill() calls. */
    private static int lerpColor(int colorA, int colorB, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a1 = (colorA >> 24) & 0xFF, r1 = (colorA >> 16) & 0xFF, g1 = (colorA >> 8) & 0xFF, b1 = colorA & 0xFF;
        int a2 = (colorB >> 24) & 0xFF, r2 = (colorB >> 16) & 0xFF, g2 = (colorB >> 8) & 0xFF, b2 = colorB & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Draws a top-to-bottom gradient using one fill() call per pixel row — no unverified GuiGraphics API needed. */
    private static void fillVerticalGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        int height = y2 - y1;
        if (height <= 0) return;
        for (int row = 0; row < height; row++) {
            float t = height <= 1 ? 0f : (float) row / (height - 1);
            int color = lerpColor(colorTop, colorBottom, t);
            graphics.fill(x1, y1 + row, x2, y1 + row + 1, color);
        }
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