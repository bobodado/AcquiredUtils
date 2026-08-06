package com.bobodado.acquiredutils.config;

import com.bobodado.acquiredutils.AcquiredUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/acquiredutils.json");

    private static ModConfig INSTANCE = new ModConfig();

    public boolean showHudOverlay = true;
    public double exampleValue = 2.5;
    public String guiTheme = "dark";

    private transient boolean dirty = false;

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) INSTANCE = new ModConfig();
                AcquiredUtils.LOGGER.info("Loaded AcquiredUtils config");
            } catch (IOException e) {
                AcquiredUtils.LOGGER.error("Failed to load config", e);
                INSTANCE = new ModConfig();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
            INSTANCE.dirty = false;
            AcquiredUtils.LOGGER.info("Saved AcquiredUtils config");
        } catch (IOException e) {
            AcquiredUtils.LOGGER.error("Failed to save config", e);
        }
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }
}
