package dev.bobodado.acquiredutils.client.gui.section;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.client.AcquiredUtilsClient;
import dev.bobodado.acquiredutils.client.SlotLockHandler;
import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import dev.bobodado.acquiredutils.client.gui.widget.KeyListenerSlot;
import dev.bobodado.acquiredutils.client.gui.widget.LockedKeybindWidget;
import dev.bobodado.acquiredutils.client.gui.widget.ThemedButtonWidget;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class KeybindsSection extends ModSection {

    private final KeyListenerSlot keybindSlot = new KeyListenerSlot();

    public KeybindsSection(AcquiredUtilsConfigScreen screen) {
        super(screen);
    }

    @Override
    public String getId() {
        return "keybinds";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("acquiredutils.gui.tab.keybinds");
    }

    @Override
    public List<GuiRow> getRows() {
        AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

        return List.of(
            new GuiRow(
                "acquiredutils.gui.keybind.slot_lock",
                "acquiredutils.gui.desc.slot_lock",
                22, -1, 22, 40,
                (x, y, w, h) -> new LockedKeybindWidget(
                    x, y, w, h,
                    Component.translatable("acquiredutils.gui.keybind.slot_lock"),
                    keybindSlot,
                    () -> cfg.slotLockEnabled,
                    enabled -> {
                        cfg.slotLockEnabled = enabled;
                        if (enabled && cfg.slotLockKey < 0) {
                            cfg.slotLockKey = InputConstants.KEY_Z;
                        }
                        cfg.markDirty();
                        AcquiredUtilsConfig.saveIfDirty();
                    },
                    () -> cfg.slotLockKey,
                    keyCode -> {
                        cfg.slotLockKey = keyCode;
                        cfg.markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.auto_hotbar",
                "acquiredutils.gui.desc.auto_hotbar",
                22, 20, 20, 48,
                (x, y, w, h) -> net.minecraft.client.gui.components.Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.autoProtectHotbar)
                    .onValueChange((cb, checked) -> {
                        cfg.autoProtectHotbar = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.auto_inventory",
                "acquiredutils.gui.desc.auto_inventory",
                22, 20, 20, 48,
                (x, y, w, h) -> net.minecraft.client.gui.components.Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.autoProtectInventory)
                    .onValueChange((cb, checked) -> {
                        cfg.autoProtectInventory = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.auto_armor",
                "acquiredutils.gui.desc.auto_armor",
                22, 20, 20, 48,
                (x, y, w, h) -> net.minecraft.client.gui.components.Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.autoProtectArmor)
                    .onValueChange((cb, checked) -> {
                        cfg.autoProtectArmor = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.auto_offhand",
                "acquiredutils.gui.desc.auto_offhand",
                22, 20, 20, 48,
                (x, y, w, h) -> net.minecraft.client.gui.components.Checkbox.builder(Component.empty(), screen.getFont())
                    .pos(x + w - s(20), y)
                    .selected(cfg.autoProtectOffhand)
                    .onValueChange((cb, checked) -> {
                        cfg.autoProtectOffhand = checked;
                        cfg.markDirty();
                    })
                    .build()
            ),
            new GuiRow(
                "acquiredutils.gui.setting.lock_all",
                "acquiredutils.gui.desc.lock_all",
                22, 150, 20, 48,
                (x, y, w, h) -> new ThemedButtonWidget(
                    x, y, w, h,
                    Component.translatable("acquiredutils.gui.button.lock_all"),
                    () -> {
                        SlotLockHandler.lockAll();
                        AcquiredUtilsConfig.get().markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.setting.unlock_all",
                "acquiredutils.gui.desc.unlock_all",
                22, 150, 20, 48,
                (x, y, w, h) -> new ThemedButtonWidget(
                    x, y, w, h,
                    Component.translatable("acquiredutils.gui.button.unlock_all"),
                    () -> {
                        SlotLockHandler.unlockAllManual();
                        AcquiredUtilsConfig.get().markDirty();
                    }
                )
            ),
            new GuiRow(
                "acquiredutils.gui.keybind.inventory_search",
                "acquiredutils.gui.desc.keybind_inventory_search",
                22, -1, 22, 40,
                (x, y, w, h) -> new LockedKeybindWidget(
                    x, y, w, h,
                    Component.translatable("acquiredutils.gui.keybind.inventory_search"),
                    keybindSlot,
                    () -> true,
                    enabled -> {},
                    () -> cfg.inventorySearchKey,
                    keyCode -> {
                        cfg.inventorySearchKey = keyCode;
                        cfg.markDirty();
                    }
                )
            )
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keybindSlot.current != null) {
            if (keyCode == 256) {
                keyCode = -1;
            }
            keybindSlot.current.applyKeyCode(keyCode);
            return true;
        }
        return false;
    }

    @Override
    public void onClose() {
        keybindSlot.clear();
    }
}
