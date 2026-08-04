package com.acquiredutils.client;

import com.acquiredutils.AcquiredUtils;
import com.acquiredutils.config.ConfigManager;
import com.acquiredutils.hud.HudEditorScreen;
import com.acquiredutils.notification.AcquiredUtilsNotifier;
import com.acquiredutils.notification.NotificationRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AcquiredUtilsClient implements ClientModInitializer {
    public static KeyMapping HUD_EDITOR_KEY;
    @Override
    public void onInitializeClient() {
        AcquiredUtils.init();
        ConfigManager.load();
        HUD_EDITOR_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.acquiredutils.hud_editor", GLFW.GLFW_KEY_U, "category.acquiredutils.general"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (HUD_EDITOR_KEY.consumeClick()) {
                if (client.screen == null) client.setScreen(new HudEditorScreen());
            }
        });
        AcquiredUtilsNotifier.init();
        NotificationRenderer.init();
    }
}