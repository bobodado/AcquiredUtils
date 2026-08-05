package com.acquiredutils.client;

import com.acquiredutils.AcquiredUtils;
import com.acquiredutils.config.ConfigManager;
import com.acquiredutils.hud.HudEditorScreen;
import com.acquiredutils.notification.AcquiredUtilsNotifier;
import com.acquiredutils.notification.NotificationRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AcquiredUtilsClient implements ClientModInitializer {
    public static KeyMapping HUD_EDITOR_KEY;
    @Override
    public void onInitializeClient() {
        AcquiredUtils.init();
        ConfigManager.load();
        HUD_EDITOR_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.acquiredutils.hud_editor",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("acquiredutils", "general"))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HUD_EDITOR_KEY.consumeClick()) {
                if (client.screen == null) client.setScreen(new HudEditorScreen());
            }
        });

        // allow opening from the keybind screen if remapped
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Minecraft.getInstance().screen == null && HUD_EDITOR_KEY.isDown()) {
                Minecraft.getInstance().setScreen(new HudEditorScreen());
            }
        });
        AcquiredUtilsNotifier.init();
        NotificationRenderer.init();
    }
}