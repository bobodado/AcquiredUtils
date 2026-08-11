package dev.bobodado.acquiredutils.client.gui.section;

import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ExampleSliderWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GeneralSection extends ModSection {

    public GeneralSection(AcquiredUtilsConfigScreen screen) {
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
    public List<GuiRow> getRows() {
        AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();
        return List.of(
            new GuiRow(
                "acquiredutils.gui.setting.show_hud_overlay",
                "acquiredutils.gui.desc.show_hud_overlay",
                14, 20, 20, 40,
                (x, y, w, h) -> Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.showHudOverlay)
                    .onValueChange((cb, checked) -> {
                        cfg.showHudOverlay = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.example_slider",
                "acquiredutils.gui.desc.example_slider",
                14, 140, 14, 40,
                (x, y, w, h) -> new ExampleSliderWidget(
                    x, y, w, h, cfg.exampleSliderValue,
                    value -> {
                        cfg.exampleSliderValue = value;
                        cfg.markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.gui_theme",
                "acquiredutils.gui.desc.gui_theme",
                14, 140, 14, 40,
                (x, y, w, h) -> {
                    AcquiredUtilsConfig.GuiTheme[] themes = AcquiredUtilsConfig.GuiTheme.values();
                    return new DropdownWidget(
                        x, y, w, h,
                        List.of(
                            Component.translatable("acquiredutils.gui.theme.default"),
                            Component.translatable("acquiredutils.gui.theme.dark"),
                            Component.translatable("acquiredutils.gui.theme.high_contrast")
                        ),
                        cfg.guiTheme.ordinal(),
                        index -> {
                            cfg.guiTheme = themes[index];
                            cfg.markDirty();
                        }
                    );
                }
            ),
            new GuiRow(
                "acquiredutils.gui.setting.menu_scale",
                "acquiredutils.gui.desc.menu_scale",
                14, 140, 14, 40,
                (x, y, w, h) -> new ExampleSliderWidget(
                    x, y, w, h, cfg.menuScale, 0.5f, 2.0f,
                    value -> {
                        cfg.menuScale = value;
                        cfg.markDirty();
                        screen.scheduleRebuild();
                    }
                )
            )
        );
    }
}