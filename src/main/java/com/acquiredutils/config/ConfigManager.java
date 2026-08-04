package com.acquiredutils.config;

import com.acquiredutils.AcquiredUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("acquiredutils.json");
    private static AcquiredUtilsConfig config;
    public static AcquiredUtilsConfig get() { if (config == null) load(); return config; }
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try { config = GSON.fromJson(Files.readString(CONFIG_PATH), AcquiredUtilsConfig.class); }
            catch (IOException e) { AcquiredUtils.LOGGER.error("Failed to load config", e); config = new AcquiredUtilsConfig(); }
        } else { config = new AcquiredUtilsConfig(); save(); }
    }
    public static void save() {
        try { Files.createDirectories(CONFIG_PATH.getParent()); Files.writeString(CONFIG_PATH, GSON.toJson(config)); }
        catch (IOException e) { AcquiredUtils.LOGGER.error("Failed to save config", e); }
    }
}