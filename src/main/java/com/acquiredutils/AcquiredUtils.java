package com.acquiredutils;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint. The config screen itself is client-only (it extends
 * Screen), so all of that lives in {@link com.acquiredutils.client.AcquiredUtilsClient}
 * instead - this class is only here for logic that needs to run on a
 * dedicated server too, and to register the demo settings so the GUI
 * has something to show out of the box.
 */
public class AcquiredUtils implements ModInitializer {

    public static final String MOD_ID = "acquiredutils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("AcquiredUtils initializing");
    }
}
