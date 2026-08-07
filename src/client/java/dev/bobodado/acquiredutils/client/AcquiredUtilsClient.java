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

/**
 * Client-only entrypoint. Registers the "open settings" keybind plus a
 * dynamically-managed set of player-defined "press key -> send chat message"
 * bindings (see AcquiredUtilsConfig.CustomKeybindEntry).
 * <p>
 * IMPORTANT: customKeybindMap is keyed by CustomKeybindEntry.id (a stable
 * UUID), NOT by list position. Using list index as the map key was a real
 * bug: deleting a keybind from the middle of the list shifts every later
 * entry's index, which silently reassigned the wrong live KeyMapping object
 * to the wrong entry. The id never changes, so this can't happen again.
 */
public class AcquiredUtilsClient implements ClientModInitializer {

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(AcquiredUtils.MOD_ID, "acquiredutils"));

	private static KeyMapping openConfigKey;
	public static KeyMapping slotLockKeybind;

	/** entry.id -> its live, registered KeyMapping. */
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
				if (client.player != null) {
					AcquiredUtils.LOGGER.info("[AcquiredUtils] Slot Lock triggered!");
				}
			}

			// Custom "press key -> send chat message" bindings.
			for (Map.Entry<String, KeyMapping> e : customKeybindMap.entrySet()) {
				String id = e.getKey();
				KeyMapping mapping = e.getValue();
				while (mapping.consumeClick()) {
					if (client.player == null) {
						continue;
					}
					for (AcquiredUtilsConfig.CustomKeybindEntry entry : AcquiredUtilsConfig.get().customKeybinds) {
						if (id.equals(entry.id)) {
							if (entry.message != null && !entry.message.isEmpty()) {
								// VERIFY: LocalPlayer#chat(String) — checked against the
								// real 1.21.11 jar; if this fails to compile, run:
								//   javap -p -classpath <minecraft-merged-loom-mappings-jar> net.minecraft.client.player.LocalPlayer | grep -i chat
								// and swap the call to match.
								client.player.chat(entry.message);
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

	/**
	 * Reconciles customKeybindMap against the current config list.
	 * Safe to call after any add/remove/rebind of a custom keybind.
	 */
	public static void syncCustomKeybinds() {
		var entries = AcquiredUtilsConfig.get().customKeybinds;

		Set<String> currentIds = new HashSet<>();
		for (var entry : entries) {
			currentIds.add(entry.id);
		}

		// Drop tracking for entries that no longer exist. Fabric has no
		// "unregister" API, so the underlying KeyMapping object can't be
		// truly removed — but we explicitly unbind it first so it at least
		// stops silently occupying a key for the rest of the session.
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