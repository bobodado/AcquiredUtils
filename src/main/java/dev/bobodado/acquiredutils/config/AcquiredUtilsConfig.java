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

/**
 * Plain hand-rolled JSON config backend (via Gson, which ships with
 * Minecraft/Fabric so no extra dependency is needed).
 * <p>
 * NOTE (flagged in the layout map, §9): this is one valid option among
 * several (Cloth Config is the other common choice). If you'd rather use
 * Cloth Config for its built-in config-screen widgets, this class can be
 * deleted and AcquiredUtilsConfigScreen swapped for a Cloth Config builder
 * screen instead — the settings model (fields below) stays the same either way.
 * <p>
 * File lives at: .minecraft/config/acquiredutils.json
 */
public final class AcquiredUtilsConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "acquiredutils.json";

	/** In-memory singleton instance. Mutate fields directly, then call {@link #save()}. */
	private static AcquiredUtilsConfig INSTANCE = new AcquiredUtilsConfig();

	// --- Settings, matching the three rows in the reference GUI ---

	/** Item 1: "Show HUD Overlay" checkbox. Reference image shows this checked by default. */
	public boolean showHudOverlay = true;

	/** Item 2: "Example" slider, range 0.1–5.0. */
	public float exampleSliderValue = 2.5f; // midpoint — reference image only shows the handle
	                                          // centered with no numeric label; confirm intended default.

	/** Item 3: "Gui Theme" dropdown. */
	public GuiTheme guiTheme = GuiTheme.DARK; // reference shows "Dark" selected

	public enum GuiTheme {
		DEFAULT,
		DARK,
		HIGH_CONTRAST
	}

	private AcquiredUtilsConfig() {
	}

	public static AcquiredUtilsConfig get() {
		return INSTANCE;
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/** Loads config from disk, or writes fresh defaults if the file doesn't exist yet. */
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
		AcquiredUtils.LOGGER.info("[AcquiredUtils] Loaded config from {}", path);
	} catch (IOException e) {
		AcquiredUtils.LOGGER.error("[AcquiredUtils] Failed to read config, falling back to defaults", e);
		INSTANCE = new AcquiredUtilsConfig();
	}
}

	/** Persists the current in-memory config to disk. Called by the SAVE CHANGES button. */
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
