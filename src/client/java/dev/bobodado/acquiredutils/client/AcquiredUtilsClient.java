package dev.bobodado.acquiredutils.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import dev.bobodado.acquiredutils.client.gui.section.GeneralSection;
import dev.bobodado.acquiredutils.client.gui.section.KeybindsSection;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AcquiredUtilsClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "acquiredutils"));

    private static KeyMapping openConfigKey;
    public static KeyMapping slotLockKeybind;

    // Tracks custom keybinds: config index -> KeyMapping
    private static final Map<Integer, KeyMapping> customKeybindMap = new HashMap<>();

    @Override
    public void onInitializeClient() {
        AcquiredUtils.LOGGER.info("[AcquiredUtils] Initializing client entrypoint");

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.acquiredutils.open_config",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_APOSTROPHE,
                CATEGORY
        ));

        int slotLockCode = AcquiredUtilsConfig.get().slotLockKey;
        slotLockKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.acquiredutils.slot_lock",
                InputConstants.Type.KEYSYM,
                slotLockCode >= 0 ? slotLockCode : InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        syncCustomKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                if (client.screen == null) {
                    AcquiredUtilsConfigScreen screen = new AcquiredUtilsConfigScreen(null);
                    screen.registerSection(new GeneralSection(screen));
                    screen.registerSection(new KeybindsSection(screen));
                    client.setScreen(screen);
                }
            }

            while (slotLockKeybind.consumeClick()) {
                if (client.player != null) {
                    AcquiredUtils.LOGGER.info("[AcquiredUtils] Slot Lock triggered!");
                }
            }

            for (Map.Entry<Integer, KeyMapping> e : customKeybindMap.entrySet()) {
                int idx = e.getKey();
                KeyMapping mapping = e.getValue();
                while (mapping.consumeClick()) {
                    if (client.player != null && idx < AcquiredUtilsConfig.get().customKeybinds.size()) {
                        String name = AcquiredUtilsConfig.get().customKeybinds.get(idx).name;
                        AcquiredUtils.LOGGER.info("[AcquiredUtils] Custom keybind triggered: " + name);
                    }
                }
            }
        });
    }

    public static void syncSlotLockKeybind() {
        int code = AcquiredUtilsConfig.get().slotLockKey;
        slotLockKeybind.setKey(code >= 0
                ? InputConstants.Type.KEYSYM.getOrCreate(code)
                : InputConstants.UNKNOWN);
    }

    public static void syncCustomKeybinds() {
        var entries = AcquiredUtilsConfig.get().customKeybinds;

        customKeybindMap.keySet().removeIf(idx -> idx >= entries.size());

        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            KeyMapping mapping = customKeybindMap.get(i);

            if (mapping == null) {
                if (entry.keyCode >= 0) {
                    mapping = new KeyMapping(
                            "key.acquiredutils.custom." + entry.name,
                            InputConstants.Type.KEYSYM,
                            entry.keyCode,
                            CATEGORY
                    );
                    KeyBindingHelper.registerKeyBinding(mapping);
                    customKeybindMap.put(i, mapping);
                }
            } else {
                mapping.setKey(entry.keyCode >= 0
                        ? InputConstants.Type.KEYSYM.getOrCreate(entry.keyCode)
                        : InputConstants.UNKNOWN);
            }
        }
    }
}
