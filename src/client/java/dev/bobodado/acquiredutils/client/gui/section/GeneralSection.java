package dev.bobodado.acquiredutils.client.gui.section;

import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ExampleSliderWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GeneralSection extends ModSection {

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

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                       int contentX, int contentY, int contentWidth, int contentHeight) {
        int rowY = contentY;

        // + s(3) centers the 8px text vertically with the widgets
        graphics.drawString(screen.getFont(),
                Component.translatable("acquiredutils.gui.setting.show_hud_overlay"),
                contentX, rowY + s(3), 0xFFF2F2F2, false);
        screen.drawDescription(graphics, "acquiredutils.gui.desc.show_hud_overlay", contentX, rowY + s(11));
        rowY += s(40);

        graphics.drawString(screen.getFont(),
                Component.translatable("acquiredutils.gui.setting.example_slider"),
                contentX, rowY + s(3), 0xFFF2F2F2, false);
        screen.drawDescription(graphics, "acquiredutils.gui.desc.example_slider", contentX, rowY + s(11));
        rowY += s(40);

        graphics.drawString(screen.getFont(),
                Component.translatable("acquiredutils.gui.setting.gui_theme"),
                contentX, rowY + s(3), 0xFFF2F2F2, false);
        screen.drawDescription(graphics, "acquiredutils.gui.desc.gui_theme", contentX, rowY + s(11));
        rowY += s(40);

        graphics.drawString(screen.getFont(),
                Component.translatable("acquiredutils.gui.setting.menu_scale"),
                contentX, rowY + s(3), 0xFFF2F2F2, false);
        screen.drawDescription(graphics, "acquiredutils.gui.desc.menu_scale", contentX, rowY + s(11));
    }
}
