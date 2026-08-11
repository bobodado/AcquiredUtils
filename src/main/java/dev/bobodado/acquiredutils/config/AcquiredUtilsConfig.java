package dev.bobodado.acquiredutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.bobodado.acquiredutils.AcquiredUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

public final class AcquiredUtilsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "acquiredutils.json";
    private static AcquiredUtilsConfig INSTANCE = new AcquiredUtilsConfig();

    public boolean showHudOverlay = true;
    public float exampleSliderValue = 2.5f;
    public GuiTheme guiTheme = GuiTheme.DARK;
    public float menuScale = 1.0f;

    public boolean slotLockEnabled = false;
    public int slotLockKey = -1;

    public Set<Integer> lockedSlots = new LinkedHashSet<>();

    private transient boolean dirty = false;

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

    public void markDirty() {
        this.dirty = true;
    }

    public void sanitize() {
        if (guiTheme == null) {
            guiTheme = GuiTheme.DARK;
        }
        if (lockedSlots == null) {
            lockedSlots = new LinkedHashSet<>();
        }
        menuScale = Math.max(0.5f, Math.min(2.0f, menuScale));
        exampleSliderValue = Math.max(0.1f, Math.min(5.0f, exampleSliderValue));
        lockedSlots.removeIf(idx -> idx == null || idx < 0 || idx > 40);
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            INSTANCE = new AcquiredUtilsConfig();
            INSTANCE.sanitize();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            AcquiredUtilsConfig loaded = GSON.fromJson(reader, AcquiredUtilsConfig.class);
            INSTANCE = (loaded != null) ? loaded : new AcquiredUtilsConfig();
            INSTANCE.sanitize();
            AcquiredUtils.LOGGER.info("[AcquiredUtils] Loaded config from {}", path);
        } catch (Exception e) {
            AcquiredUtils.LOGGER.error("[AcquiredUtils] Failed to read config, falling back to defaults", e);
            INSTANCE = new AcquiredUtilsConfig();
            INSTANCE.sanitize();
        }
    }

    public static void save() {
        Path path = configPath();
        Path temp = path.resolveSibling(FILE_NAME + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(INSTANCE, writer);
            }
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            INSTANCE.dirty = false;
            AcquiredUtils.LOGGER.info("[AcquiredUtils] Saved config to {}", path);
        } catch (Exception e) {
            AcquiredUtils.LOGGER.error("[AcquiredUtils] Failed to write config", e);
            try {
                Files.deleteIfExists(temp);
            } catch (Exception ignored) {
            }
        }
    }

    public static void saveIfDirty() {
        if (INSTANCE != null && INSTANCE.dirty) {
            save();
        }
    }
}