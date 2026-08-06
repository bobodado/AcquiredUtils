package com.acquiredutils.client.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global registry of {@link ConfigCategory} instances, keyed by name.
 * LinkedHashMap preserves registration order, which is what decides
 * sidebar order - mirrors NEU's LinkedHashMap<String, ProcessedCategory>.
 */
public final class ConfigRegistry {

    private static final Map<String, ConfigCategory> CATEGORIES = new LinkedHashMap<>();

    private ConfigRegistry() {
    }

    public static ConfigCategory getOrCreate(String name, String description) {
        return CATEGORIES.computeIfAbsent(name, k -> new ConfigCategory(name, description));
    }

    public static Map<String, ConfigCategory> getCategories() {
        return CATEGORIES;
    }
}
