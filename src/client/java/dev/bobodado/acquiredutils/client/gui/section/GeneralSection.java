package dev.bobodado.acquiredutils.client.gui.section;

import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ExampleSliderWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GeneralSection extends ModSection {

	// Row labels are drawn 1.4x normal size (via the same pose push/translate/
	// scale/pop pattern already used successfully by drawDescription) to make
	// each row feel more substantial and use the panel's vertical space
	// better, instead of small text floating in a lot of empty room.
	private static final float LABEL_SCALE = 1.4f;

	public GeneralSection(dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen screen) {
		super(screen);
	}

	@Override
	public String getId() {
		return "general";
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("acquiredutils.gui.tab.general");
	}

	@Override
	public void buildContent(int contentX, int contentY, int contentWidth, int contentHeight) {
		AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();
		int rowY = contentY;
		int cw = contentWidth;

		addWidget(Checkbox.builder(Component.empty(), screen.getFont())
				.pos(contentX + cw - 20, rowY)
				.selected(cfg.showHudOverlay)
				.onValueChange((cb, checked) -> cfg.showHudOverlay = checked)
				.build());
		rowY += s(40);

		addWidget(new ExampleSliderWidget(
				contentX + cw - s(140), rowY, s(140), s(14),
				cfg.exampleSliderValue,
				value -> cfg.exampleSliderValue = value
		));
		rowY += s(40);

		AcquiredUtilsConfig.GuiTheme[] themes = AcquiredUtilsConfig.GuiTheme.values();
		addWidget(new DropdownWidget(
				contentX + cw - s(140), rowY, s(140), s(14),
				List.of(
						Component.translatable("acquiredutils.gui.theme.default"),
						Component.translatable("acquiredutils.gui.theme.dark"),
						Component.translatable("acquiredutils.gui.theme.high_contrast")
				),
				cfg.guiTheme.ordinal(),
				index -> cfg.guiTheme = themes[index]
		));
		rowY += s(40);

		addWidget(new ExampleSliderWidget(
				contentX + cw - s(140), rowY, s(140), s(14),
				cfg.menuScale, 0.5f, 2.0f,
				value -> {
					cfg.menuScale = value;
					screen.rebuild();
				}
		));
	}

	/** Draws a row label at LABEL_SCALE, matching drawDescription's pose-transform pattern. */
	private void drawLabel(GuiGraphics graphics, Component label, int x, int y) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(LABEL_SCALE, LABEL_SCALE);
		graphics.drawString(screen.getFont(), label, 0, 0, 0xFFF2F2F2, false);
		graphics.pose().popMatrix();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
	                   int contentX, int contentY, int contentWidth, int contentHeight) {
		int rowY = contentY;

		drawLabel(graphics, Component.translatable("acquiredutils.gui.setting.show_hud_overlay"), contentX, rowY);
		screen.drawDescription(graphics, "acquiredutils.gui.desc.show_hud_overlay", contentX, rowY + s(14));
		rowY += s(40);

		drawLabel(graphics, Component.translatable("acquiredutils.gui.setting.example_slider"), contentX, rowY);
		screen.drawDescription(graphics, "acquiredutils.gui.desc.example_slider", contentX, rowY + s(14));
		rowY += s(40);

		drawLabel(graphics, Component.translatable("acquiredutils.gui.setting.gui_theme"), contentX, rowY);
		screen.drawDescription(graphics, "acquiredutils.gui.desc.gui_theme", contentX, rowY + s(14));
		rowY += s(40);

		drawLabel(graphics, Component.translatable("acquiredutils.gui.setting.menu_scale"), contentX, rowY);
		screen.drawDescription(graphics, "acquiredutils.gui.desc.menu_scale", contentX, rowY + s(14));
	}
}