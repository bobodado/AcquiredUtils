package com.acquiredutils.client;

import com.acquiredutils.AcquiredUtilsConfig;
import com.acquiredutils.client.config.ConfigCategory;
import com.acquiredutils.client.config.ConfigRegistry;
import com.acquiredutils.client.config.NeuStyleConfigScreen;
import com.acquiredutils.client.config.widget.BooleanSettingWidget;
import com.acquiredutils.client.config.widget.SliderSettingWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class AcquiredUtilsClient implements ClientModInitializer {

    private static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        registerSettings();

        // 1.21.2+ KeyMapping categories are now an enum
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.acquiredutils.open_config",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            net.minecraft.client.KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new NeuStyleConfigScreen());
                }
            }
        });
    }

    private void registerSettings() {
        AcquiredUtilsConfig config = AcquiredUtilsConfig.INSTANCE;

        ConfigCategory overlays = ConfigRegistry.getOrCreate("Overlays", "Toggle in-world overlays");
        overlays.add(new BooleanSettingWidget(
            "Show Overlay", "Toggles the rarity overlay in your inventory",
            config.showOverlay, val -> config.showOverlay = val));
        overlays.add(new SliderSettingWidget(
            "Overlay Scale", "Size of the overlay text",
            config.overlayScale, 0.5, 2.0, 0.05, val -> config.overlayScale = val));

        ConfigCategory misc = ConfigRegistry.getOrCreate("Misc", "Everything else");
        misc.add(new BooleanSettingWidget(
            "Example Toggle", "A second example setting, in its own category",
            false, val -> {}));
    }
}