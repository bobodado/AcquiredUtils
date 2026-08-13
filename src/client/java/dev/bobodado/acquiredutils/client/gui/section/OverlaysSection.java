package dev.bobodado.acquiredutils.client.gui.section;

import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ValueSliderWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OverlaysSection extends ModSection {

    public OverlaysSection(AcquiredUtilsConfigScreen screen) {
        super(screen);
    }

    @Override
    public String getId() {
        return "overlays";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("acquiredutils.gui.tab.overlays");
    }

    @Override
    public List<GuiRow> getRows() {
        AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

        List<Component> rarities = List.of(
            Component.literal("Common"),
            Component.literal("Uncommon"),
            Component.literal("Rare"),
            Component.literal("Epic"),
            Component.literal("Legendary"),
            Component.literal("Mythic")
        );

        return List.of(
            new GuiRow(
                "acquiredutils.gui.setting.recipe_unlock_highlight",
                "acquiredutils.gui.desc.recipe_unlock_highlight",
                22, 20, 20, 48,
                (x, y, w, h) -> Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.recipeUnlockHighlightEnabled)
                    .onValueChange((cb, checked) -> {
                        cfg.recipeUnlockHighlightEnabled = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.rarity_circle",
                "acquiredutils.gui.desc.rarity_circle",
                22, 20, 20, 48,
                (x, y, w, h) -> Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.rarityCircleEnabled)
                    .onValueChange((cb, checked) -> {
                        cfg.rarityCircleEnabled = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.rarity_minimum",
                "acquiredutils.gui.desc.rarity_minimum",
                22, 150, 20, 48,
                (x, y, w, h) -> new DropdownWidget(
                    x, y, w, h,
                    rarities,
                    Math.max(0, Math.min(rarities.size() - 1,
                        dev.bobodado.acquiredutils.client.pickup.ItemRarity.fromName(cfg.rarityCircleMinRarity).ordinal())),
                    index -> {
                        cfg.rarityCircleMinRarity =
                            dev.bobodado.acquiredutils.client.pickup.ItemRarity.values()[index].name();
                        cfg.markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.rarity_circle_size",
                "acquiredutils.gui.desc.rarity_circle_size",
                22, 150, 20, 48,
                (x, y, w, h) -> new ValueSliderWidget(
                    x, y, w, h,
                    cfg.rarityCircleSize,
                    3.0f,
                    8.0f,
                    1.0f,
                    false,
                    "px",
                    value -> {
                        cfg.rarityCircleSize = value;
                        cfg.markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.rarity_circle_opacity",
                "acquiredutils.gui.desc.rarity_circle_opacity",
                22, 150, 20, 48,
                (x, y, w, h) -> new ValueSliderWidget(
                    x, y, w, h,
                    cfg.rarityCircleOpacity,
                    0.15f,
                    1.0f,
                    0.05f,
                    false,
                    "",
                    value -> {
                        cfg.rarityCircleOpacity = value;
                        cfg.markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.item_comparison",
                "acquiredutils.gui.desc.item_comparison",
                22, 20, 20, 48,
                (x, y, w, h) -> Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.itemComparisonEnabled)
                    .onValueChange((cb, checked) -> {
                        cfg.itemComparisonEnabled = checked;
                        cfg.markDirty();
                    })
                    .build()
            )
        );
    }
}
