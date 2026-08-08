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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AcquiredUtilsClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "acquiredutils"));

    private static KeyMapping openConfigKey;
    private static KeyMapping slotLockKeybind;

    private static final Map<String, KeyMapping> customKeybindMap = new HashMap<>();

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
                if (client.player != null && AcquiredUtilsConfig.get().slotLockEnabled) {
                    AcquiredUtils.LOGGER.info("[AcquiredUtils] Slot Lock triggered!");
                }
            }

            for (Map.Entry<String, KeyMapping> e : customKeybindMap.entrySet()) {
                String id = e.getKey();
                KeyMapping mapping = e.getValue();
                while (mapping.consumeClick()) {
                    if (client.player == null) continue;
                    for (AcquiredUtilsConfig.CustomKeybindEntry entry : AcquiredUtilsConfig.get().customKeybinds) {
                        if (id.equals(entry.id)) {
                            if (entry.message != null && !entry.message.isEmpty()) {
                                String msg = entry.message;
                                if (msg.startsWith("/")) {
                                    client.player.connection.sendCommand(msg.substring(1));
                                } else {
                                    client.player.connection.sendChat(msg);
                                }
                            }
                            break;
                        }
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

        Set<String> currentIds = new HashSet<>();
        for (var entry : entries) {
            currentIds.add(entry.id);
        }

        customKeybindMap.entrySet().removeIf(e -> {
            if (!currentIds.contains(e.getKey())) {
                e.getValue().setKey(InputConstants.UNKNOWN);
                return true;
            }
            return false;
        });

        for (var entry : entries) {
            KeyMapping mapping = customKeybindMap.get(entry.id);
            if (mapping == null) {
                if (entry.keyCode >= 0) {
                    mapping = new KeyMapping(
                            "key.acquiredutils.custom." + entry.id,
                            InputConstants.Type.KEYSYM,
                            entry.keyCode,
                            CATEGORY
                    );
                    KeyBindingHelper.registerKeyBinding(mapping);
                    customKeybindMap.put(entry.id, mapping);
                }
            } else {
                mapping.setKey(entry.keyCode >= 0
                        ? InputConstants.Type.KEYSYM.getOrCreate(entry.keyCode)
                        : InputConstants.UNKNOWN);
            }
        }
    }
}