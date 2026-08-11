package dev.bobodado.acquiredutils.client.gui;

import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.gui.section.GuiRow;
import dev.bobodado.acquiredutils.client.gui.section.ModSection;
import dev.bobodado.acquiredutils.client.gui.theme.Theme;
import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ThemedButtonWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AcquiredUtilsConfigScreen extends Screen {

    public static final int BASE_PANEL_WIDTH = 480;
    public static final int BASE_PANEL_HEIGHT = 260;
    public static final int BASE_HEADER_HEIGHT = 32;
    public static final int BASE_FOOTER_HEIGHT = 26;
    public static final int BASE_SIDEBAR_WIDTH = 110;
    public static final int BASE_PADDING = 8;
    public static final int BASE_TAB_HEIGHT = 18;

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

    private final List<StoredText> storedTexts = new ArrayList<>();
    private record StoredText(String translationKey, int x, int y, boolean isLabel) {}

    private boolean needsRebuild = false;

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

    public void scheduleRebuild() {
        needsRebuild = true;
    }

    @Override
    public void tick() {
        if (needsRebuild) {
            needsRebuild = false;
            init();
        }
    }

    private void computeLayout() {
        this.menuScale = AcquiredUtilsConfig.get().menuScale;
        float maxScaleX = (float) this.width / BASE_PANEL_WIDTH;
        float maxScaleY = (float) this.height / BASE_PANEL_HEIGHT;
        this.menuScale = Math.max(0.5f, Math.min(this.menuScale, Math.min(maxScaleX, maxScaleY)));

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
        storedTexts.clear();

        buildHeader();
        buildSidebarTabs();
        buildContent();
    }

    private void buildHeader() {
        int closeSize = s(12);
        addRenderableWidget(new ThemedButtonWidget(
            panelX + panelWidth - closeSize - padding, panelY + padding, closeSize, closeSize,
            Component.literal("X"), this::onClose));
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

            addRenderableWidget(new ThemedButtonWidget(tabX, y, tabWidth, tabHeight,
                section.getDisplayName(), () -> switchTab(id), true));
            i++;
        }
    }

    private void buildContent() {
        ModSection active = sections.get(activeSectionId);
        if (active == null) return;

        int contentX = panelX + sidebarWidth + padding;
        int contentY = panelY + headerHeight + padding;
        int contentW = panelWidth - sidebarWidth - padding * 2;
        int contentH = panelHeight - headerHeight - footerHeight - padding * 2;

        List<GuiRow> rows = active.getRows();
        int rowY = contentY;

        for (GuiRow row : rows) {
            int controlW = row.controlWidth() < 0 ? contentW : s(row.controlWidth());
            int controlH = s(row.controlHeight());
            int controlX = contentX + contentW - controlW;

            AbstractWidget widget = row.factory().create(controlX, rowY, controlW, controlH);
            addSectionWidget(widget);

            if (row.labelKey() != null) {
                storedTexts.add(new StoredText(row.labelKey(), contentX, rowY, true));
            }
            if (row.descKey() != null) {
                storedTexts.add(new StoredText(row.descKey(), contentX, rowY + s(row.descOffsetY()), false));
            }

            rowY += s(row.rowSpacing());
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
        Theme theme = Theme.current();

        drawPanelChrome(graphics, theme);

        int titleX = panelX + padding;
        int titleY = panelY + s(6);
        graphics.drawString(this.font, Component.literal("MOD SETTINGS: "), titleX, titleY, theme.text, false);
        int afterFirst = titleX + this.font.width("MOD SETTINGS: ");

        Component boldName = Component.literal("AcquiredUtils").copy().withStyle(Style.EMPTY.withBold(true));
        graphics.drawString(this.font, boldName, afterFirst, titleY, theme.accent, false);
        int afterSecond = afterFirst + this.font.width(boldName);

        graphics.drawString(this.font, Component.literal(" v1.0.0"), afterSecond, titleY, theme.text, false);

        Component credit = Component.literal("Interface designed by ii8we")
            .copy().withStyle(Style.EMPTY.withItalic(true));
        float creditScale = 0.75f;
        int creditRawWidth = this.font.width(credit);
        int creditRenderedWidth = (int) (creditRawWidth * creditScale);
        int creditX = panelX + (panelWidth - creditRenderedWidth) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(creditX, panelY + s(17));
        graphics.pose().scale(creditScale, creditScale);
        graphics.drawString(this.font, credit, 0, 0, theme.credit, false);
        graphics.pose().popPose();

        int underlineY = panelY + headerHeight - 1;
        graphics.fill(panelX, underlineY, panelX + panelWidth, underlineY + 1, theme.divider);

        graphics.fill(panelX + sidebarWidth, panelY + headerHeight,
            panelX + sidebarWidth + 1, panelY + panelHeight - footerHeight, theme.divider);

        int contentX = panelX + sidebarWidth + padding;
        int contentY = panelY + headerHeight + padding;
        int contentW = panelWidth - sidebarWidth - padding * 2;
        int contentH = panelHeight - headerHeight - footerHeight - padding * 2;

        ModSection active = sections.get(activeSectionId);
        if (active != null) {
            graphics.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
            active.render(graphics, mouseX, mouseY, partialTick, contentX, contentY, contentW, contentH);
            graphics.disableScissor();
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
        for (StoredText st : storedTexts) {
            if (st.isLabel()) {
                drawLabel(graphics, Component.translatable(st.translationKey()), st.x(), st.y());
            } else {
                drawDescription(graphics, st.translationKey(), st.x(), st.y());
            }
        }
        graphics.disableScissor();

        for (TabPos tab : tabPositions) {
            if (tab.id().equals(activeSectionId)) {
                graphics.fill(tab.x(), tab.y(), tab.x() + tab.w(), tab.y() + tab.h(), theme.tabActiveBg);
                int edgeW = Math.max(1, s(2));
                graphics.fill(tab.x(), tab.y(), tab.x() + edgeW, tab.y() + tab.h(), theme.accentBright);
            }
        }

        for (AbstractWidget w : sectionWidgets) {
            if (w instanceof DropdownWidget d && d.isOpen()) {
                d.renderOverlay(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private void drawPanelChrome(GuiGraphics graphics, Theme theme) {
        int ft = Math.max(1, s(4));

        int shadowOffset = Math.max(1, s(3));
        graphics.fill(panelX - ft + shadowOffset, panelY - ft + shadowOffset,
            panelX + panelWidth + ft + shadowOffset, panelY + panelHeight + ft + shadowOffset, theme.shadow);

        graphics.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
            theme.panelTop, theme.panelBottom);

        graphics.renderOutline(panelX - ft, panelY - ft, panelWidth + ft * 2, panelHeight + ft * 2, theme.frameOuter);
        graphics.renderOutline(panelX - ft + 1, panelY - ft + 1, panelWidth + ft * 2 - 2, panelHeight + ft * 2 - 2, theme.frameMid);
        graphics.renderOutline(panelX - 1, panelY - 1, panelWidth + 2, panelHeight + 2, theme.frameAccent);

        drawCornerAccents(graphics, panelX - ft, panelY - ft, panelWidth + ft * 2, panelHeight + ft * 2, theme);

        graphics.fillGradient(panelX, panelY, panelX + panelWidth, panelY + headerHeight,
            theme.headerTop, theme.headerBottom);
        graphics.fillGradient(panelX, panelY + headerHeight, panelX + sidebarWidth, panelY + panelHeight - footerHeight,
            theme.sidebarTop, theme.sidebarBottom);
        graphics.fillGradient(panelX, panelY + panelHeight - footerHeight, panelX + panelWidth, panelY + panelHeight,
            theme.footerTop, theme.footerBottom);
    }

    private void drawCornerAccents(GuiGraphics graphics, int x, int y, int w, int h, Theme theme) {
        int len = Math.max(2, s(9));
        int thick = Math.max(1, s(2));

        graphics.fill(x, y, x + len, y + thick, theme.accentBright);
        graphics.fill(x, y, x + thick, y + len, theme.accentBright);
        graphics.fill(x + w - len, y, x + w, y + thick, theme.accentBright);
        graphics.fill(x + w - thick, y, x + w, y + len, theme.accentBright);
        graphics.fill(x, y + h - thick, x + len, y + h, theme.accentBright);
        graphics.fill(x, y + h - len, x + thick, y + h, theme.accentBright);
        graphics.fill(x + w - len, y + h - thick, x + w, y + h, theme.accentBright);
        graphics.fill(x + w - thick, y + h - len, x + w, y + h, theme.accentBright);
    }

    public void drawDescription(GuiGraphics graphics, String translationKey, int x, int y) {
        Component desc = Component.translatable(translationKey)
            .copy()
            .withStyle(Style.EMPTY.withItalic(true));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y);
        graphics.pose().scale(0.75f, 0.75f);
        graphics.drawString(this.font, desc, 0, 0, 0x80999999, false);
        graphics.pose().popPose();
    }

    private void drawLabel(GuiGraphics graphics, Component label, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y);
        graphics.pose().scale(1.4f, 1.4f);
        graphics.drawString(this.font, label, 0, 0, Theme.current().text, false);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget w : sectionWidgets) {
            if (w instanceof DropdownWidget d && d.isOpen()) {
                if (!d.isMouseOver(mouseX, mouseY) && !d.isOverExpandedArea(mouseX, mouseY)) {
                    d.setOpen(false);
                }
            }
        }

        ModSection active = sections.get(activeSectionId);
        if (active != null && active.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ModSection active = sections.get(activeSectionId);
        if (active != null && active.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        ModSection active = sections.get(activeSectionId);
        if (active != null && active.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        for (ModSection section : sections.values()) section.onClose();
        AcquiredUtilsConfig.saveIfDirty();
        AcquiredUtils.LOGGER.info("[AcquiredUtils] Settings saved on menu close");
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}