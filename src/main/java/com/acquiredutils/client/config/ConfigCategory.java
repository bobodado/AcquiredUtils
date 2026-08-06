package com.acquiredutils.client.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A sidebar category (equivalent to a NEU {@code ProcessedCategory}).
 * <p>
 * This is what makes registration "one line": build a category once,
 * then keep calling {@code .add(...)} on it from anywhere in your mod's
 * init code, including other files/mixins, without touching the screen class.
 * <p>
 * Example:
 * <pre>{@code
 * ConfigCategory overlays = ConfigRegistry.getOrCreate("Overlays", "Toggle in-world overlays");
 * overlays.add(new BooleanSettingWidget("Show Overlay", "Toggles rarity overlay", true, val -> config.overlay = val));
 * overlays.add(new SliderSettingWidget("Overlay Scale", "Size of the overlay", 1.0, 0.5, 2.0, 0.05, val -> config.overlayScale = val));
 * }</pre>
 */
public class ConfigCategory {

    private final String name;
    private final String description;
    private final List<Setting<?>> settings = new ArrayList<>();

    public ConfigCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** Registers a setting and returns this category, so calls can be chained. */
    public ConfigCategory add(Setting<?> setting) {
        settings.add(setting);
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }
}
