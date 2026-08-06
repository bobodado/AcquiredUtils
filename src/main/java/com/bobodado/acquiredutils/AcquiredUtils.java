package com.bobodado.acquiredutils;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcquiredUtils implements ModInitializer {
    public static final String MOD_ID = "acquiredutils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("AcquiredUtils v1.0.0 initialized for Minecraft 1.21.11");
        com.bobodado.acquiredutils.config.ModConfig.load();
    }
}
