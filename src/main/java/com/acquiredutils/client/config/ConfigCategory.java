package com.acquiredutils.client.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategory {

    private final String name;
    private final String description;
    private final List<GuiElement> elements = new ArrayList<>();

    public ConfigCategory(String name) { this(name, ""); }
    public ConfigCategory(String name, String description) {
        this.name = name; this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public ConfigCategory add(Setting<?> setting) { return addSetting(setting); }
    public ConfigCategory addSetting(Setting<?> setting) { elements.add(setting); return this; }
    public ConfigCategory addSection(Section section) { elements.add(section); return this; }
    public List<GuiElement> getElements() { return elements; }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settings = new ArrayList<>();
        for (GuiElement e : elements) { if (e instanceof Setting<?>) settings.add((Setting<?>) e); }
        return settings;
    }
}