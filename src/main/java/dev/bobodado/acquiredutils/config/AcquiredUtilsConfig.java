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

public final class AcquiredUtilsConfig {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final String FILE_NAME =
            "acquiredutils.json";

    private static final int PLAYER_INVENTORY_SIZE = 41;

    private static AcquiredUtilsConfig INSTANCE =
            new AcquiredUtilsConfig();

    public boolean showHudOverlay = true;

    public float exampleSliderValue = 2.5f;

    public GuiTheme guiTheme =
            GuiTheme.DARK;

    public float menuScale = 1.0f;

    /*
     * Slot Lock configuration.
     *
     * slotLockKey:
     * -1 = unbound
     */
    public boolean slotLockEnabled = false;

    public int slotLockKey = -1;

    /*
     * Player inventory:
     *
     * 0-8   = hotbar
     * 9-35  = main inventory
     * 36-39 = armor
     * 40    = offhand
     */
    public boolean[] lockedSlots =
            new boolean[PLAYER_INVENTORY_SIZE];

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
        return FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FILE_NAME);
    }

    public static void load() {
        Path path = configPath();

        if (!Files.exists(path)) {
            INSTANCE =
                    new AcquiredUtilsConfig();

            normalizeLockedSlots(INSTANCE);

            save();

            return;
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            AcquiredUtilsConfig loaded =
                    GSON.fromJson(
                            reader,
                            AcquiredUtilsConfig.class
                    );

            INSTANCE =
                    loaded != null
                            ? loaded
                            : new AcquiredUtilsConfig();

            if (INSTANCE.guiTheme == null) {
                INSTANCE.guiTheme =
                        GuiTheme.DARK;
            }

            INSTANCE.menuScale =
                    Math.max(
                            0.5f,
                            Math.min(
                                    2.0f,
                                    INSTANCE.menuScale
                            )
                    );

            normalizeLockedSlots(INSTANCE);

            AcquiredUtils.LOGGER.info(
                    "[AcquiredUtils] Loaded config from {}",
                    path
            );

        } catch (IOException e) {

            AcquiredUtils.LOGGER.error(
                    "[AcquiredUtils] Failed to read config, falling back to defaults",
                    e
            );

            INSTANCE =
                    new AcquiredUtilsConfig();
        }
    }

    public static void save() {
        Path path = configPath();

        try {
            normalizeLockedSlots(INSTANCE);

            Files.createDirectories(
                    path.getParent()
            );

            try (Writer writer =
                         Files.newBufferedWriter(
                                 path,
                                 StandardCharsets.UTF_8
                         )) {

                GSON.toJson(
                        INSTANCE,
                        writer
                );
            }

            AcquiredUtils.LOGGER.info(
                    "[AcquiredUtils] Saved config to {}",
                    path
            );

        } catch (IOException e) {

            AcquiredUtils.LOGGER.error(
                    "[AcquiredUtils] Failed to write config",
                    e
            );
        }
    }

    private static void normalizeLockedSlots(
            AcquiredUtilsConfig config
    ) {
        if (config.lockedSlots == null) {
            config.lockedSlots =
                    new boolean[PLAYER_INVENTORY_SIZE];

            return;
        }

        if (config.lockedSlots.length
                != PLAYER_INVENTORY_SIZE) {

            boolean[] normalized =
                    new boolean[PLAYER_INVENTORY_SIZE];

            System.arraycopy(
                    config.lockedSlots,
                    0,
                    normalized,
                    0,
                    Math.min(
                            config.lockedSlots.length,
                            normalized.length
                    )
            );

            config.lockedSlots =
                    normalized;
        }
    }
}