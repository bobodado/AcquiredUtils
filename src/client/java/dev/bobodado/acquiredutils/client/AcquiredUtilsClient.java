package dev.bobodado.acquiredutils.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import dev.bobodado.acquiredutils.client.gui.section.GeneralSection;
import dev.bobodado.acquiredutils.client.gui.section.KeybindsSection;
import dev.bobodado.acquiredutils.client.slotlock.SlotLockManager;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class AcquiredUtilsClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            AcquiredUtils.MOD_ID,
                            "acquiredutils"
                    )
            );

    private static KeyMapping openConfigKey;
    public static KeyMapping slotLockKeybind;

    @Override
    public void onInitializeClient() {
        AcquiredUtils.LOGGER.info("[AcquiredUtils] Initializing client entrypoint");

        openConfigKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.acquiredutils.open_config",
                        InputConstants.Type.KEYSYM,
                        InputConstants.KEY_APOSTROPHE,
                        CATEGORY
                )
        );

        int slotLockCode = AcquiredUtilsConfig.get().slotLockKey;

        slotLockKeybind = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.acquiredutils.slot_lock",
                        InputConstants.Type.KEYSYM,
                        slotLockCode >= 0
                                ? slotLockCode
                                : InputConstants.UNKNOWN.getValue(),
                        CATEGORY
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openConfigKey.consumeClick()) {
                if (client.screen == null) {
                    AcquiredUtilsConfigScreen screen =
                            new AcquiredUtilsConfigScreen(null);

                    screen.registerSection(new GeneralSection(screen));
                    screen.registerSection(new KeybindsSection(screen));

                    client.setScreen(screen);
                }
            }

            while (slotLockKeybind.consumeClick()) {
                if (client.player != null
                        && AcquiredUtilsConfig.get().slotLockEnabled) {

                    SlotLockManager.toggleHoveredSlot();
                }
            }
        });
    }

    public static void syncSlotLockKeybind() {
        int code = AcquiredUtilsConfig.get().slotLockKey;

        slotLockKeybind.setKey(
                code >= 0
                        ? InputConstants.Type.KEYSYM.getOrCreate(code)
                        : InputConstants.UNKNOWN
        );
    }
}