package dev.bobodado.acquiredutils.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Client-only entrypoint. Registers the keybinding used to open
 * {@link AcquiredUtilsConfigScreen} and wires up the open-screen action.
 * <p>
 * VERIFY: KeyBindingHelper / KeyMapping / InputConstants package paths and
 * constructor signatures against the actual 1.21.11 Fabric API + Mojang
 * mappings artifacts. As of 1.21.11, KeyMapping.Category replaced the old
 * plain-string category argument — confirm ResourceLocation vs. Identifier
 * naming matches whatever your IDE resolves; both names have been used
 * across different Minecraft version eras for the same concept.
 */
public class AcquiredUtilsClient implements ClientModInitializer {

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "acquiredutils"));

	private static KeyMapping openConfigKey;

	@Override
	public void onInitializeClient() {
		AcquiredUtils.LOGGER.info("[AcquiredUtils] Initializing client entrypoint");

		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.acquiredutils.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_APOSTROPHE, // arbitrary default; unbound-friendly, low collision risk
				CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new AcquiredUtilsConfigScreen(null));
				}
			}
		});
	}
}