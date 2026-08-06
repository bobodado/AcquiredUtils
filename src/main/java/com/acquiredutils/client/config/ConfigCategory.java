package com.acquiredutils.client.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a collection of GUI elements (settings and sections) for one config category/tab.
 */
public class ConfigCategory {
    private final String name;
    private final String description;
    private final List<GuiElement> elements = new ArrayList<>();

    public ConfigCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void add(GuiElement element) {
        elements.add(element);
    }

    public void addSetting(Setting<?> setting) {
        elements.add(setting);
    }

    public void addSection(Section section) {
        elements.add(section);
    }

    public List<GuiElement> getElements() {
        return elements;
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> settings = new ArrayList<>();
        for (GuiElement element : elements) {
            if (element instanceof Setting<?> setting) {
                settings.add(setting);
            } else if (element instanceof Section section) {
                settings.addAll(section.getChildren());
            }
        }
        return settings;
    }
}