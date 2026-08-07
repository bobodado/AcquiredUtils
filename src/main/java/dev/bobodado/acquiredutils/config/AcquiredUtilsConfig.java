package dev.bobodado.acquiredutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.bobodado.acquiredutils.AcquiredUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AcquiredUtilsConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "acquiredutils.json";
	private static AcquiredUtilsConfig INSTANCE = new AcquiredUtilsConfig();

	public boolean showHudOverlay = true;
	public float exampleSliderValue = 2.5f;
	public GuiTheme guiTheme = GuiTheme.DARK;
	public float menuScale = 1.0f;

	public int slotLockKey = -1; // InputConstants.UNKNOWN value

	/** Player-defined "press this key, send this chat message" bindings. */
	public List<CustomKeybindEntry> customKeybinds = new ArrayList<>();

	public enum GuiTheme {
		DEFAULT,
		DARK,
		HIGH_CONTRAST
	}

	public static class CustomKeybindEntry {
		/**
		 * Stable identity, independent of this entry's position in the list.
		 * MUST be used as the map key anywhere this entry's live KeyMapping is
		 * tracked (see AcquiredUtilsClient.customKeybindMap) — using the list
		 * index instead was the root cause of a real bug where deleting a
		 * middle entry silently reassigned the wrong KeyMapping to the wrong
		 * entry.
		 */
		public String id;
		/** The literal chat message sent when this key is pressed. */
		public String message;
		public int keyCode; // -1 = unbound

		public CustomKeybindEntry() {
		}

		public CustomKeybindEntry(String message, int keyCode) {
			this.id = UUID.randomUUID().toString();
			this.message = message;
			this.keyCode = keyCode;
		}
	}

	private AcquiredUtilsConfig() {
	}

	public static AcquiredUtilsConfig get() {
		return INSTANCE;
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static void load() {
		Path path = configPath();
		if (!Files.exists(path)) {
			INSTANCE = new AcquiredUtilsConfig();
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			AcquiredUtilsConfig loaded = GSON.fromJson(reader, AcquiredUtilsConfig.class);
			INSTANCE = (loaded != null) ? loaded : new AcquiredUtilsConfig();
			if (INSTANCE.guiTheme == null) {
				INSTANCE.guiTheme = GuiTheme.DARK;
			}
			if (INSTANCE.customKeybinds == null) {
				INSTANCE.customKeybinds = new ArrayList<>();
			}
			// Backfill stable ids for entries saved before this field existed,
			// so old config files don't collide/misbehave in the id-keyed map.
			for (CustomKeybindEntry entry : INSTANCE.customKeybinds) {
				if (entry.id == null) {
					entry.id = UUID.randomUUID().toString();
				}
			}
			INSTANCE.menuScale = Math.max(0.5f, Math.min(2.0f, INSTANCE.menuScale));
			AcquiredUtils.LOGGER.info("[AcquiredUtils] Loaded config from {}", path);
		} catch (IOException e) {
			AcquiredUtils.LOGGER.error("[AcquiredUtils] Failed to read config, falling back to defaults", e);
			INSTANCE = new AcquiredUtilsConfig();
		}
	}

	public static void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(INSTANCE, writer);
			}
			AcquiredUtils.LOGGER.info("[AcquiredUtils] Saved config to {}", path);
		} catch (IOException e) {
			AcquiredUtils.LOGGER.error("[AcquiredUtils] Failed to write config", e);
		}
	}
}