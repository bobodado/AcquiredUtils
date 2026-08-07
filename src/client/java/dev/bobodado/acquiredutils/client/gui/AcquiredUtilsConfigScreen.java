package dev.bobodado.acquiredutils.client.gui;

import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ExampleSliderWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Root screen for the AcquiredUtils mod settings GUI.
 * <p>
 * This class is a direct implementation of the "Master Layout Map" doc
 * (AcquiredUtils_GUI_Master_Layout_Map.md) — section numbers in comments
 * below refer back to that document so the two stay in sync if the design
 * changes.
 * <p>
 * VERIFY before first compile (see README "Build & Verify" section):
 *   - Screen's constructor/abstract members for 1.21.11
 *   - GuiGraphics method names (fill, drawString, blit, renderOutline)
 *   - Checkbox's builder API (Checkbox.builder(...)) — this moved to a
 *     builder pattern at some point pre-1.21.11; confirm signature.
 *   - Button.builder(...) fluent API — long-stable, but re-check.
 * <p>
 * This screen intentionally does NOT pause or darken the game (see layout
 * map §9: reference image shows the live world, un-blurred, behind the
 * panel) — {@link #isPauseScreen()} returns false and no background dimming
 * is drawn.
 */
public class AcquiredUtilsConfigScreen extends Screen {

	// --- Layout constants (panel proportions approximate the 2:1 reference image) ---
	private static final int PANEL_WIDTH = 480;
	private static final int PANEL_HEIGHT = 260;
	private static final int HEADER_HEIGHT = 22;
	private static final int FOOTER_HEIGHT = 26;
	private static final int SIDEBAR_WIDTH = 110;
	private static final int PADDING = 8;
	private static final int TAB_HEIGHT = 18;

	// --- Placeholder color palette — matches layout map §6 variable names ---
	private static final int COLOR_PANEL_BG = 0xFF2B2B2B;      // --panel-bg-dark
	private static final int COLOR_FRAME_WOOD = 0xFF6B4A2E;    // --panel-frame-wood
	private static final int COLOR_BAR_BG = 0xFF1F1F1F;        // --header-footer-bg
	private static final int COLOR_SIDEBAR_BG = 0xFF232323;    // slightly darker than content, per reference
	private static final int COLOR_ORANGE = 0xFFE38A2D;        // --text-accent-orange
	private static final int COLOR_WHITE = 0xFFF2F2F2;         // --text-primary-white
	private static final int COLOR_TAB_ACTIVE_BORDER = 0xFF7A5A34; // --tab-active-border

	private final Screen parent;

	private int panelX;
	private int panelY;

	// Tabs (§3.3 / §4.3): only "general" is implemented; "keybinds" is a stub per §10 open item.
	private enum Tab { GENERAL, KEYBINDS }
	private Tab activeTab = Tab.GENERAL;

	public AcquiredUtilsConfigScreen(Screen parent) {
		super(Component.translatable("acquiredutils.gui.title"));
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false; // world keeps rendering live behind the panel, per reference image
	}

	@Override
	protected void init() {
		panelX = (this.width - PANEL_WIDTH) / 2;
		panelY = (this.height - PANEL_HEIGHT) / 2;

		clearWidgets();
		buildHeader();
		buildSidebar();
		if (activeTab == Tab.GENERAL) {
			buildGeneralContent();
		}
		buildFooter();
	}

	// --- 3.2 HeaderBar ---
	private void buildHeader() {
		int closeSize = 12;
		addRenderableWidget(Button.builder(Component.literal("X"), b -> onClose())
				.bounds(panelX + PANEL_WIDTH - closeSize - 6, panelY + 5, closeSize, closeSize)
				.build());
	}

	// --- 3.3 SidebarPanel: TabWidget[general], TabWidget[keybinds] (§4.3) ---
	private void buildSidebar() {
		int tabX = panelX + PADDING;
		int tabWidth = SIDEBAR_WIDTH - PADDING * 2;
		int tabY = panelY + HEADER_HEIGHT + PADDING;

		addRenderableWidget(Button.builder(Component.translatable("acquiredutils.gui.tab.general"), b -> {
					activeTab = Tab.GENERAL;
					init();
				})
				.bounds(tabX, tabY, tabWidth, TAB_HEIGHT)
				.build());

		addRenderableWidget(Button.builder(Component.translatable("acquiredutils.gui.tab.keybinds"), b -> {
					activeTab = Tab.KEYBINDS;
					init();
				})
				.bounds(tabX, tabY + TAB_HEIGHT + 4, tabWidth, TAB_HEIGHT)
				.build());

		// NOTE: using Button for tabs is a placeholder — the reference shows a
		// custom active-state frame + icon (gear/keyboard) rather than a
		// standard vanilla button. Swap for a custom TabWidget (per layout
		// map §4.3) once icon textures + a click-region-only (no button
		// background) rendering approach is wired up.
	}

	// --- 3.4 ContentPanel > 3.5 SettingsList (General tab only) ---
	private void buildGeneralContent() {
		int contentX = panelX + SIDEBAR_WIDTH + PADDING;
		int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - PADDING * 2;
		int rowY = panelY + HEADER_HEIGHT + PADDING + 20; // leave room for ContentTitle (§4.4), drawn in render()

		AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

		// Item 1: Show HUD Overlay -> Checkbox (§4.5)
		addRenderableWidget(Checkbox.builder(Component.empty(), this.font)
				.pos(contentX + contentWidth - 20, rowY)
				.selected(cfg.showHudOverlay)
				.onValueChange((checkbox, checked) -> cfg.showHudOverlay = checked)
				.build());
		rowY += 24;

		// Item 2: Example -> Slider 0.1-5.0 (§4.6)
		addRenderableWidget(new ExampleSliderWidget(
				contentX + contentWidth - 140, rowY, 140, 14,
				cfg.exampleSliderValue,
				value -> cfg.exampleSliderValue = value
		));
		rowY += 24;

		// Item 3: Gui Theme -> Dropdown, open by default per reference image (§4.7)
		AcquiredUtilsConfig.GuiTheme[] themes = AcquiredUtilsConfig.GuiTheme.values();
		int selectedIndex = cfg.guiTheme.ordinal();
		addRenderableWidget(new DropdownWidget(
				contentX + contentWidth - 140, rowY, 140, 14,
				List.of(
						Component.translatable("acquiredutils.gui.theme.default"),
						Component.translatable("acquiredutils.gui.theme.dark"),
						Component.translatable("acquiredutils.gui.theme.high_contrast")
				),
				selectedIndex,
				index -> cfg.guiTheme = themes[index]
		));
	}

	// --- 3.6 FooterBar > 4.8 ActionButton ---
	private void buildFooter() {
		int buttonWidth = 90;
		int buttonHeight = 16;
		addRenderableWidget(Button.builder(Component.translatable("acquiredutils.gui.save"), b -> {
					AcquiredUtilsConfig.save();
					// UX open item (layout map §10): reference doesn't specify post-save
					// behavior. Currently: stays open with a log line. Change to
					// onClose() here if you want the screen to close on save instead.
					AcquiredUtils.LOGGER.info("[AcquiredUtils] Settings saved from GUI");
				})
				.bounds(panelX + PANEL_WIDTH - buttonWidth - PADDING,
						panelY + PANEL_HEIGHT - FOOTER_HEIGHT + (FOOTER_HEIGHT - buttonHeight) / 2,
						buttonWidth, buttonHeight)
				.build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// No renderBackground() call — see isPauseScreen() note above; the
		// live world is already being drawn behind this screen by the
		// Minecraft render pipeline for non-pausing screens.

		drawPanelChrome(graphics);
		super.render(graphics, mouseX, mouseY, partialTick); // draws all addRenderableWidget() children
		drawOverlayText(graphics);
	}

	/** 3.1 GuiWindowPanel + 3.2/3.3/3.4/3.6 container chrome (backgrounds, borders, static labels). */
	private void drawPanelChrome(GuiGraphics graphics) {
		// Panel background + wood frame (9-slice in the real texture; flat fill + outline here as placeholder)
		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, COLOR_PANEL_BG);
		graphics.renderOutline(panelX - 4, panelY - 4, PANEL_WIDTH + 8, PANEL_HEIGHT + 8, COLOR_FRAME_WOOD);

		// Header bar
		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + HEADER_HEIGHT, COLOR_BAR_BG);

		// Sidebar background, slightly darker than content per reference
		graphics.fill(panelX, panelY + HEADER_HEIGHT, panelX + SIDEBAR_WIDTH, panelY + PANEL_HEIGHT - FOOTER_HEIGHT, COLOR_SIDEBAR_BG);

		// Active-tab frame (§4.3 active state) — General is always active in this stub
		if (activeTab == Tab.GENERAL) {
			int tabX = panelX + PADDING;
			int tabY = panelY + HEADER_HEIGHT + PADDING;
			int tabWidth = SIDEBAR_WIDTH - PADDING * 2;
			graphics.renderOutline(tabX, tabY, tabWidth, TAB_HEIGHT, COLOR_TAB_ACTIVE_BORDER);
		}

		// Footer bar
		graphics.fill(panelX, panelY + PANEL_HEIGHT - FOOTER_HEIGHT, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, COLOR_BAR_BG);
	}

	/** Title gradient text (§4.1) + content title (§4.4) + settings-row labels (§5) — drawn as text, not widgets. */
	private void drawOverlayText(GuiGraphics graphics) {
		// Title: simple two-tone approximation of the orange->white gradient described in §4.1.
		// A true per-character gradient needs either custom font glyph coloring or a baked
		// texture strip (see layout map §4.1) — this is a placeholder two-segment version.
		int titleX = panelX + PADDING;
		int titleY = panelY + (HEADER_HEIGHT - 8) / 2;
		graphics.drawString(this.font, Component.literal("MOD SETTINGS: "), titleX, titleY, COLOR_WHITE, false);
		int afterFirst = titleX + this.font.width("MOD SETTINGS: ");
		graphics.drawString(this.font, Component.literal("AcquiredUtils"), afterFirst, titleY, COLOR_ORANGE, false);
		int afterSecond = afterFirst + this.font.width("AcquiredUtils");
		graphics.drawString(this.font, Component.literal(" v1.0.0"), afterSecond, titleY, COLOR_WHITE, false);

		if (activeTab != Tab.GENERAL) {
			// Keybinds tab stub (§10 open item — not in original spec, added so the tab isn't dead)
			graphics.drawString(this.font, Component.translatable("acquiredutils.gui.tab.keybinds.placeholder"),
					panelX + SIDEBAR_WIDTH + PADDING, panelY + HEADER_HEIGHT + PADDING, COLOR_WHITE, false);
			return;
		}

		int contentX = panelX + SIDEBAR_WIDTH + PADDING;
		int contentTitleY = panelY + HEADER_HEIGHT + PADDING;
		graphics.pose().pushPose();
		graphics.pose().translate(contentX, contentTitleY, 0);
		graphics.pose().scale(1.5f, 1.5f, 1.0f);
		graphics.drawString(this.font, Component.translatable("acquiredutils.gui.content_title.general"),
				0, 0, COLOR_WHITE, false);
		graphics.pose().popPose();

		int rowY = contentTitleY + 20;
		graphics.drawString(this.font, Component.translatable("acquiredutils.gui.setting.show_hud_overlay"),
		contentX, rowY + 3, COLOR_WHITE, false);
		rowY += 24;

		graphics.drawString(this.font, Component.literal("2."), contentX, rowY + 3, COLOR_ORANGE, false);
		graphics.drawString(this.font, Component.translatable("acquiredutils.gui.setting.example_slider"),
				contentX + 12, rowY + 3, COLOR_WHITE, false);
		rowY += 24;

		graphics.drawString(this.font, Component.literal("3."), contentX, rowY + 3, COLOR_ORANGE, false);
		graphics.drawString(this.font, Component.translatable("acquiredutils.gui.setting.gui_theme"),
				contentX + 12, rowY + 3, COLOR_WHITE, false);
	}

	@Override
	public void onClose() {
		if (this.minecraft != null) {
			this.minecraft.setScreen(parent);
		}
	}
}
