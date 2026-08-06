package com.bobodado.acquiredutils.gui;

import com.bobodado.acquiredutils.AcquiredUtils;
import com.bobodado.acquiredutils.config.ModConfig;
import com.bobodado.acquiredutils.gui.widgets.ConfigSlider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main configuration screen for AcquiredUtils.
 * Spruce wood theme with custom texture support.
 * Target: Minecraft 1.21.11 with Mojang Mappings.
 */
public class ModConfigScreen extends Screen {

    // === TEXTURE REGISTRY ===
    // Place these files in: src/main/resources/assets/acquiredutils/textures/gui/
    public static final ResourceLocation TEX_PANEL_BG = ResourceLocation.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "src/main/resources/assets/acquiredutils/textures/gui/panel_bg.png");
    public static final ResourceLocation TEX_SIDEBAR_BG = ResourceLocation.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "src/main/resources/assets/acquiredutils/textures/gui/sidebar_bg.png");
    public static final ResourceLocation TEX_CONTENT_BG = ResourceLocation.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "src/main/resources/assets/acquiredutils/textures/gui/content_bg.png");
    public static final ResourceLocation TEX_FRAME = ResourceLocation.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "src/main/resources/assets/acquiredutils/textures/gui/frame.png");
    public static final ResourceLocation TEX_ICON_GEAR = ResourceLocation.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "src/main/resources/assets/acquiredutils/textures/gui/icons/gear.png");
    public static final ResourceLocation TEX_ICON_KEYBOARD = ResourceLocation.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "src/main/resources/assets/acquiredutils/textures/gui/icons/keyboard.png");

    // === COLORS ===
    public static final int COLOR_FRAME_OUTER = 0xFF1A120A;
    public static final int COLOR_FRAME_INNER = 0xFF2A1D12;
    public static final int COLOR_PANEL_BG = 0xFF2E2015;
    public static final int COLOR_SIDEBAR_BG = 0xFF3A2A1D;
    public static final int COLOR_CONTENT_BG = 0xFF2E2015;
    public static final int COLOR_TEXT_WHITE = 0xFFFFFFFF;
    public static final int COLOR_TEXT_OFFWHITE = 0xFFE8DCC8;
    public static final int COLOR_TEXT_ORANGE = 0xFFE9864B;
    public static final int COLOR_TEXT_COOL_WHITE = 0xFFF7FAFF;
    public static final int COLOR_TEXT_MUTED = 0xFF9A8B7A;
    public static final int COLOR_TAB_FRAME = 0xFF8B7355;
    public static final int COLOR_TAB_ACTIVE_BG = 0xFF4A3A2A;
    public static final int COLOR_BUTTON_GREEN = 0xFF386A22;
    public static final int COLOR_BUTTON_GREEN_HOVER = 0xFF4A8A2E;
    public static final int COLOR_BUTTON_GREEN_SHADOW = 0xFF2A4A1A;
    public static final int COLOR_CHECKBOX_GREEN = 0xFF386A22;
    public static final int COLOR_DROPDOWN_HIGHLIGHT = 0xFF4A3525;
    public static final int COLOR_SEPARATOR = 0xFF4A3A2A;
    public static final int COLOR_SLIDER_TRACK = 0xFF4A3A2A;
    public static final int COLOR_SETTING_TITLE = 0xFFFFFFFF;
    public static final int COLOR_TITLE_BG = 0xFF3A2A1D;

    private static final int PANEL_WIDTH = 528;
    private static final int PANEL_HEIGHT = 430;
    private static final int HEADER_HEIGHT = 42;
    private static final int SIDEBAR_WIDTH = 130;

    private final Screen parentScreen;
    private int panelX;
    private int panelY;

    private int activeTab = 0;

    private Button closeBtn;
    private Button generalTabBtn;
    private Button keybindsTabBtn;
    private Button hudCheckboxBtn;
    private ConfigSlider exampleSlider;
    private Button themeDropdownBtn;
    private Button saveBtn;

    private boolean dropdownExpanded = false;
    private final String[] themeOptions = {"default", "dark", "high_contrast"};
    private int themeIndex = 1;

    public ModConfigScreen(Screen parent) {
        super(Component.literal("Mod Settings"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - PANEL_HEIGHT) / 2;

        clearWidgets();

        ModConfig config = ModConfig.get();

        themeIndex = 0;
        for (int i = 0; i < themeOptions.length; i++) {
            if (themeOptions[i].equals(config.guiTheme)) {
                themeIndex = i;
                break;
            }
        }

        initTabs();

        if (activeTab == 0) {
            initGeneralTab(config);
        }

        initFooter(config);
        initCloseButton();
    }

    private void initTabs() {
        int tabX = panelX + 8;
        int tabY = panelY + HEADER_HEIGHT + 4;
        int tabWidth = SIDEBAR_WIDTH - 8;
        int tabHeight = 28;

        generalTabBtn = Button.builder(Component.empty(), btn -> {
            if (activeTab != 0) {
                activeTab = 0;
                init();
            }
        }).bounds(tabX, tabY, tabWidth, tabHeight).build();
        addRenderableWidget(generalTabBtn);

        keybindsTabBtn = Button.builder(Component.empty(), btn -> {
            if (activeTab != 1) {
                activeTab = 1;
                init();
            }
        }).bounds(tabX, tabY + tabHeight + 4, tabWidth, tabHeight).build();
        addRenderableWidget(keybindsTabBtn);
    }

    private void initGeneralTab(ModConfig config) {
        int contentX = panelX + SIDEBAR_WIDTH + 12;
        int contentY = panelY + HEADER_HEIGHT + 12;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 24;

        // Setting 1: Show HUD Overlay
        int row1Y = contentY + 32;
        int checkboxX = contentX + contentWidth - 80;
        hudCheckboxBtn = Button.builder(Component.empty(), btn -> {
            config.showHudOverlay = !config.showHudOverlay;
            config.markDirty();
        }).bounds(checkboxX, row1Y, 18, 18).build();
        addRenderableWidget(hudCheckboxBtn);

        // Setting 2: Example (slider)
        int row2Y = contentY + 72;
        exampleSlider = new ConfigSlider(
            contentX + contentWidth - 140, row2Y,
            120, 16,
            config.exampleValue,
            0.1, 5.0, 0.1,
            value -> {
                config.exampleValue = value;
                config.markDirty();
            }
        );
        addRenderableWidget(exampleSlider);

        // Setting 3: GUI Theme (dropdown)
        int row3Y = contentY + 112;
        AtomicInteger tIndex = new AtomicInteger(themeIndex);
        themeDropdownBtn = Button.builder(Component.empty(), btn -> {
            int next = (tIndex.get() + 1) % themeOptions.length;
            tIndex.set(next);
            themeIndex = next;
            config.guiTheme = themeOptions[next];
            config.markDirty();
        }).bounds(contentX + contentWidth - 150, row3Y, 140, 20).build();
        addRenderableWidget(themeDropdownBtn);
    }

    private void initFooter(ModConfig config) {
        saveBtn = Button.builder(Component.empty(), btn -> {
            ModConfig.save();
            if (this.minecraft != null && parentScreen != null) {
                this.minecraft.setScreen(parentScreen);
            } else {
                this.onClose();
            }
        }).bounds(panelX + PANEL_WIDTH - 130, panelY + PANEL_HEIGHT - 30, 110, 22).build();
        addRenderableWidget(saveBtn);
    }

    private void initCloseButton() {
        closeBtn = Button.builder(Component.empty(), btn -> this.onClose())
            .bounds(panelX + PANEL_WIDTH - 28, panelY + 12, 18, 18)
            .build();
        addRenderableWidget(closeBtn);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        renderPanel(graphics);
        renderHeader(graphics);
        renderSidebar(graphics);
        renderContentBackground(graphics);

        if (activeTab == 0) {
            renderContentTitle(graphics);
            renderSettingLabels(graphics);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        renderCustomTabs(graphics, mouseX, mouseY);
        if (activeTab == 0) {
            renderCustomCheckbox(graphics);
            renderCustomDropdown(graphics, mouseX, mouseY);
        }
        renderCustomSaveButton(graphics, mouseX, mouseY);
        renderCloseButtonText(graphics);
    }

    // === TEXTURE HELPERS ===

    /**
     * Tiles a texture across an area. Texture should be square and power-of-2 sized.
     */
    private void tileTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h, int texSize) {
        for (int ty = y; ty < y + h; ty += texSize) {
            for (int tx = x; tx < x + w; tx += texSize) {
                int drawW = Math.min(texSize, x + w - tx);
                int drawH = Math.min(texSize, y + h - ty);
                graphics.blit(texture, tx, ty, 0, 0, drawW, drawH, texSize, texSize);
            }
        }
    }

    /**
     * Stretches a texture to fit an area.
     */
    private void stretchTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int w, int h, int texW, int texH) {
        graphics.blit(texture, x, y, 0, 0, w, h, texW, texH);
    }

    private void renderPanel(GuiGraphics graphics) {
        // Outer frame (4px)
        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 4, COLOR_FRAME_OUTER);
        graphics.fill(panelX, panelY + PANEL_HEIGHT - 4, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, COLOR_FRAME_OUTER);
        graphics.fill(panelX, panelY, panelX + 4, panelY + PANEL_HEIGHT, COLOR_FRAME_OUTER);
        graphics.fill(panelX + PANEL_WIDTH - 4, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, COLOR_FRAME_OUTER);

        // Inner frame (2px)
        graphics.fill(panelX + 4, panelY + 4, panelX + PANEL_WIDTH - 4, panelY + 6, COLOR_FRAME_INNER);
        graphics.fill(panelX + 4, panelY + PANEL_HEIGHT - 6, panelX + PANEL_WIDTH - 4, panelY + PANEL_HEIGHT - 4, COLOR_FRAME_INNER);
        graphics.fill(panelX + 4, panelY + 4, panelX + 6, panelY + PANEL_HEIGHT - 4, COLOR_FRAME_INNER);
        graphics.fill(panelX + PANEL_WIDTH - 6, panelY + 4, panelX + PANEL_WIDTH - 4, panelY + PANEL_HEIGHT - 4, COLOR_FRAME_INNER);

        // Panel background - try texture, fallback to solid color
        int bgX = panelX + 6;
        int bgY = panelY + 6;
        int bgW = PANEL_WIDTH - 12;
        int bgH = PANEL_HEIGHT - 12;
        tileTexture(graphics, TEX_PANEL_BG, bgX, bgY, bgW, bgH, 64);
    }

    private void renderHeader(GuiGraphics graphics) {
        int headerY = panelY + 6;
        int headerBottom = headerY + HEADER_HEIGHT;

        graphics.fill(panelX + 6, headerBottom - 1, panelX + PANEL_WIDTH - 6, headerBottom, COLOR_SEPARATOR);

        int titleY = headerY + 14;
        int textX = panelX + 26;

        String part1 = "MOD SETTINGS: ";
        graphics.drawString(this.font, part1, textX, titleY, COLOR_TEXT_WHITE, true);
        textX += this.font.width(part1);

        String part2 = "AcquiredUtils ";
        graphics.drawString(this.font, part2, textX, titleY, COLOR_TEXT_ORANGE, true);
        textX += this.font.width(part2);

        String part3 = "v1.0.0";
        graphics.drawString(this.font, part3, textX, titleY, COLOR_TEXT_COOL_WHITE, true);
    }

    private void renderSidebar(GuiGraphics graphics) {
        int sidebarX = panelX + 6;
        int sidebarY = panelY + HEADER_HEIGHT + 6;
        int sidebarH = PANEL_HEIGHT - HEADER_HEIGHT - 12;
        tileTexture(graphics, TEX_SIDEBAR_BG, sidebarX, sidebarY, SIDEBAR_WIDTH, sidebarH, 64);
    }

    private void renderContentBackground(GuiGraphics graphics) {
        int contentX = panelX + SIDEBAR_WIDTH + 6;
        int contentY = panelY + HEADER_HEIGHT + 6;
        int contentW = PANEL_WIDTH - SIDEBAR_WIDTH - 12;
        int contentH = PANEL_HEIGHT - HEADER_HEIGHT - 12;
        tileTexture(graphics, TEX_CONTENT_BG, contentX, contentY, contentW, contentH, 64);
    }

    private void renderContentTitle(GuiGraphics graphics) {
        int titleX = panelX + SIDEBAR_WIDTH + 18;
        int titleY = panelY + HEADER_HEIGHT + 18;

        // Try texture icon, fallback to procedural
        graphics.blit(TEX_ICON_GEAR, titleX, titleY, 0, 0, 16, 16, 16, 16);

        graphics.drawString(this.font, "GENERAL CONFIGURATION", titleX + 20, titleY + 4, COLOR_TEXT_OFFWHITE, true);
    }

    private void renderSettingLabels(GuiGraphics graphics) {
        int contentX = panelX + SIDEBAR_WIDTH + 18;
        int startY = panelY + HEADER_HEIGHT + 56;
        int lineHeight = 40;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 24;

        int sliderX = contentX + contentWidth - 140;
        int sliderWidth = 120;
        int sliderRight = sliderX + sliderWidth;

        // Setting titles - scaled up 1.3x for bigger font look
        drawScaledTitle(graphics, "Show HUD Overlay", contentX, startY, COLOR_SETTING_TITLE, 1.3f);
        drawScaledTitle(graphics, "Example", contentX, startY + lineHeight, COLOR_SETTING_TITLE, 1.3f);
        drawScaledTitle(graphics, "Gui Theme", contentX, startY + lineHeight * 2, COLOR_SETTING_TITLE, 1.3f);

        // Slider min/max labels - MOVED UP (was +18, now +12)
        int labelY = startY + lineHeight + 12;
        graphics.drawString(this.font, "0.1", sliderX - 4, labelY, COLOR_TEXT_MUTED, true);
        graphics.drawString(this.font, "5.0", sliderRight - this.font.width("5.0") + 4, labelY, COLOR_TEXT_MUTED, true);
    }

    /**
     * Draws text scaled up using PoseStack. Creates a bold, larger title effect.
     */
    private void drawScaledTitle(GuiGraphics graphics, String text, int x, int y, int color, float scale) {
        PoseStack pose = graphics.pose();
        pose.pushPose();

        // Center the scaling around the text position
        float offsetX = x / scale - x;
        float offsetY = y / scale - y;
        pose.translate(x * (1 - scale), y * (1 - scale), 0);
        pose.scale(scale, scale, 1.0f);

        // Draw background strip behind title for readability
        int textW = this.font.width(text);
        int pad = 2;
        graphics.fill(x - pad, y - pad, x + textW + pad, y + 8 + pad, COLOR_TITLE_BG);

        // Draw text with shadow
        graphics.drawString(this.font, text, x, y, color, true);

        pose.popPose();
    }

    private void renderCustomTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        if (generalTabBtn != null) {
            int x = generalTabBtn.getX();
            int y = generalTabBtn.getY();
            int w = generalTabBtn.getWidth();
            int h = generalTabBtn.getHeight();
            boolean hovered = generalTabBtn.isHovered();

            if (activeTab == 0) {
                graphics.fill(x, y, x + w, y + 1, 0xFF9A8A6A);
                graphics.fill(x, y, x + 1, y + h, 0xFF9A8A6A);
                graphics.fill(x, y + h - 1, x + w, y + h, 0xFF5A4A3A);
                graphics.fill(x + w - 1, y, x + w, y + h, 0xFF5A4A3A);
                graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_TAB_ACTIVE_BG);
            } else if (hovered) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF3A2A1D);
            }

            // Try texture icon, fallback to procedural
            graphics.blit(TEX_ICON_GEAR, x + 6, y + 6, 0, 0, 16, 16, 16, 16);

            // Section names now WHITE instead of muted gray
            graphics.drawString(this.font, "General", x + 30, y + 10, COLOR_TEXT_WHITE, true);
        }

        if (keybindsTabBtn != null) {
            int x = keybindsTabBtn.getX();
            int y = keybindsTabBtn.getY();
            int w = keybindsTabBtn.getWidth();
            int h = keybindsTabBtn.getHeight();
            boolean hovered = keybindsTabBtn.isHovered();

            if (activeTab == 1) {
                graphics.fill(x, y, x + w, y + 1, 0xFF9A8A6A);
                graphics.fill(x, y, x + 1, y + h, 0xFF9A8A6A);
                graphics.fill(x, y + h - 1, x + w, y + h, 0xFF5A4A3A);
                graphics.fill(x + w - 1, y, x + w, y + h, 0xFF5A4A3A);
                graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, COLOR_TAB_ACTIVE_BG);
            } else if (hovered) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF3A2A1D);
            }

            graphics.blit(TEX_ICON_KEYBOARD, x + 6, y + 6, 0, 0, 16, 16, 16, 16);

            // Section names now WHITE
            graphics.drawString(this.font, "Keybinds", x + 30, y + 10, COLOR_TEXT_WHITE, true);
        }
    }

    private void renderCustomCheckbox(GuiGraphics graphics) {
        if (hudCheckboxBtn == null) return;

        ModConfig config = ModConfig.get();
        int x = hudCheckboxBtn.getX();
        int y = hudCheckboxBtn.getY();
        int w = hudCheckboxBtn.getWidth();
        int h = hudCheckboxBtn.getHeight();

        graphics.fill(x, y, x + w, y + h, COLOR_CONTENT_BG);

        if (config.showHudOverlay) {
            graphics.fill(x, y, x + w, y + h, COLOR_CHECKBOX_GREEN);
            graphics.fill(x, y, x + w, y + 1, 0xFF2A5A1A);
            graphics.fill(x, y, x + 1, y + h, 0xFF2A5A1A);
            graphics.fill(x, y + h - 1, x + w, y + h, 0xFF1A4A0A);
            graphics.fill(x + w - 1, y, x + w, y + h, 0xFF1A4A0A);

            int cx = x + w / 2;
            int cy = y + h / 2;
            graphics.fill(cx - 3, cy + 1, cx - 1, cy + 3, COLOR_TEXT_WHITE);
            graphics.fill(cx - 1, cy - 1, cx + 1, cy + 3, COLOR_TEXT_WHITE);
            graphics.fill(cx + 1, cy - 3, cx + 3, cy + 1, COLOR_TEXT_WHITE);
        } else {
            graphics.fill(x, y, x + w, y + h, 0xFF2D2D2D);
            graphics.fill(x, y, x + w, y + 1, 0xFF3D3D3D);
            graphics.fill(x, y, x + 1, y + h, 0xFF3D3D3D);
            graphics.fill(x, y + h - 1, x + w, y + h, 0xFF1D1D1D);
            graphics.fill(x + w - 1, y, x + w, y + h, 0xFF1D1D1D);
        }

        if (hudCheckboxBtn.isHovered()) {
            graphics.fill(x, y, x + w, y + h, config.showHudOverlay ? 0x304A8A2E : 0x30555555);
        }
    }

    private void renderCustomDropdown(GuiGraphics graphics, int mouseX, int mouseY) {
        if (themeDropdownBtn == null) return;

        ModConfig config = ModConfig.get();
        int x = themeDropdownBtn.getX();
        int y = themeDropdownBtn.getY();
        int w = themeDropdownBtn.getWidth();
        int h = themeDropdownBtn.getHeight();

        graphics.fill(x, y, x + w, y + h, COLOR_CONTENT_BG);

        graphics.fill(x, y, x + w, y + h, 0xFF3A2A1D);
        graphics.fill(x, y, x + w, y + 1, 0xFF5A4A3A);
        graphics.fill(x, y, x + 1, y + h, 0xFF5A4A3A);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF1A120A);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF1A120A);

        String displayText = formatOption(config.guiTheme);
        graphics.drawString(this.font, displayText, x + 8, y + 6, COLOR_TEXT_OFFWHITE, true);

        int arrowX = x + w - 14;
        int arrowY = y + h / 2 - 2;
        graphics.fill(arrowX, arrowY, arrowX + 8, arrowY + 1, COLOR_TEXT_MUTED);
        graphics.fill(arrowX + 1, arrowY + 1, arrowX + 7, arrowY + 2, COLOR_TEXT_MUTED);
        graphics.fill(arrowX + 2, arrowY + 2, arrowX + 6, arrowY + 3, COLOR_TEXT_MUTED);

        if (themeDropdownBtn.isHovered()) {
            graphics.fill(x, y, x + w, y + h, 0x20FFFFFF);
        }
    }

    private void renderCustomSaveButton(GuiGraphics graphics, int mouseX, int mouseY) {
        if (saveBtn == null) return;

        int x = saveBtn.getX();
        int y = saveBtn.getY();
        int w = saveBtn.getWidth();
        int h = saveBtn.getHeight();
        boolean hovered = saveBtn.isHovered();

        graphics.fill(x, y, x + w, y + h, COLOR_PANEL_BG);

        int bgColor = hovered ? COLOR_BUTTON_GREEN_HOVER : COLOR_BUTTON_GREEN;
        int highlightColor = hovered ? 0xFF5AAA3E : 0xFF4A8A2E;

        graphics.fill(x, y, x + w, y + h, bgColor);
        graphics.fill(x, y, x + w, y + 1, highlightColor);
        graphics.fill(x, y, x + 1, y + h, highlightColor);
        graphics.fill(x, y + h - 1, x + w, y + h, COLOR_BUTTON_GREEN_SHADOW);
        graphics.fill(x + w - 1, y, x + w, y + h, COLOR_BUTTON_GREEN_SHADOW);

        String text = "SAVE CHANGES";
        int textWidth = this.font.width(text);
        int textX = x + (w - textWidth) / 2;
        int textY = y + (h - 8) / 2;
        graphics.drawString(this.font, text, textX, textY, COLOR_TEXT_OFFWHITE, true);
    }

    private void renderCloseButtonText(GuiGraphics graphics) {
        if (closeBtn == null) return;
        int x = closeBtn.getX();
        int y = closeBtn.getY();
        graphics.fill(x, y, x + closeBtn.getWidth(), y + closeBtn.getHeight(), COLOR_PANEL_BG);
        graphics.drawString(this.font, "\u00D7", x + 4, y + 4, closeBtn.isHovered() ? 0xFFFF6464 : COLOR_TEXT_MUTED, true);
    }

    private String formatOption(String option) {
        String[] words = option.split("[_\s]");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null && parentScreen != null) {
            this.minecraft.setScreen(parentScreen);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
