package dev.bobodado.acquiredutils.client.gui.section;

import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import dev.bobodado.acquiredutils.client.gui.PickupHudEditorScreen;
import dev.bobodado.acquiredutils.client.gui.widget.ThemedButtonWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ValueSliderWidget;
import dev.bobodado.acquiredutils.client.gui.widget.DropdownWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ItemPickupSection extends ModSection {

    public ItemPickupSection(AcquiredUtilsConfigScreen screen) {
        super(screen);
    }

    @Override
    public String getId() {
        return "item_pickup";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("acquiredutils.gui.tab.item_pickup");
    }

    @Override
    public List<GuiRow> getRows() {
        AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

        return List.of(
            new GuiRow(
                "acquiredutils.gui.setting.item_pickup_notifier",
                "acquiredutils.gui.desc.item_pickup_notifier",
                22, 20, 20, 48,
                (x, y, w, h) -> Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.itemPickupNotifierEnabled)
                    .onValueChange((cb, checked) -> {
                        cfg.itemPickupNotifierEnabled = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.notification_duration",
                "acquiredutils.gui.desc.notification_duration",
                22, 150, 20, 48,
                (x, y, w, h) -> new ValueSliderWidget(
                    x, y, w, h,
                    cfg.notificationDuration,
                    0.5f,
                    10.0f,
                    0.5f,
                    false,
                    "s",
                    value -> {
                        cfg.notificationDuration = value;
                        cfg.markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.hud_preset",
                "acquiredutils.gui.desc.hud_preset",
                22, 150, 20, 48,
                (x, y, w, h) -> {
                    List<Component> options = List.of(
                        Component.literal("Custom"),
                        Component.literal("Left"),
                        Component.literal("Center"),
                        Component.literal("Right"),
                        Component.literal("Top Center"),
                        Component.literal("Bottom Center")
                    );

                    List<String> ids = List.of(
                        "custom", "left", "center", "right", "top_center", "bottom_center"
                    );

                    int selected = Math.max(0, ids.indexOf(cfg.notificationHudPreset));
                    return new DropdownWidget(
                        x,
                        y,
                        w,
                        h,
                        options,
                        selected,
                        index -> {
                            String id;
                            switch (index) {
                                case 1 -> id = "left";
                                case 2 -> id = "center";
                                case 3 -> id = "right";
                                case 4 -> id = "top_center";
                                case 5 -> id = "bottom_center";
                                default -> id = "custom";
                            }

                            cfg.notificationHudPreset = id;
                            applyPreset(cfg, id);
                            cfg.markDirty();
                            AcquiredUtilsConfig.saveIfDirty();
                        }
                    );
                }
            ),
            new GuiRow(
                "acquiredutils.gui.setting.edit_hud",
                "acquiredutils.gui.desc.edit_hud",
                22, 150, 20, 48,
                (x, y, w, h) -> new ThemedButtonWidget(
                    x,
                    y,
                    w,
                    h,
                    Component.translatable("acquiredutils.gui.button.edit_hud"),
                    () -> screen.getMinecraft().setScreen(new PickupHudEditorScreen(screen))
                )
            )
        );
    }

    private static void applyPreset(AcquiredUtilsConfig cfg, String preset) {
        switch (preset) {
            case "left" -> {
                cfg.notificationPositionX = 0.08f;
                cfg.notificationPositionY = 0.25f;
            }
            case "center" -> {
                cfg.notificationPositionX = 0.5f;
                cfg.notificationPositionY = 0.25f;
            }
            case "right" -> {
                cfg.notificationPositionX = 0.92f;
                cfg.notificationPositionY = 0.25f;
            }
            case "top_center" -> {
                cfg.notificationPositionX = 0.5f;
                cfg.notificationPositionY = 0.10f;
            }
            case "bottom_center" -> {
                cfg.notificationPositionX = 0.5f;
                cfg.notificationPositionY = 0.82f;
            }
            default -> {
                // Custom keeps the player's current position.
            }
        }
    }

}
