package com.bobodado.acquiredutils;

import com.bobodado.acquiredutils.gui.ModConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class AcquiredUtilsClient implements ClientModInitializer {

    private static KeyMapping configKeyMapping;

    @Override
    public void onInitializeClient() {
        AcquiredUtils.LOGGER.info("AcquiredUtils client initialized for 1.21.11");

        // Use built-in vanilla category MISC to avoid Category.register() issues
        configKeyMapping = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.acquiredutils.config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKeyMapping.consumeClick() && client.screen == null) {
                client.setScreen(new ModConfigScreen(null));
            }
        });
    }
}
