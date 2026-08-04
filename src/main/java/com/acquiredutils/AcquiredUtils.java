package com.acquiredutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcquiredUtils {
    public static final String MOD_ID = "acquiredutils";
    public static final String MOD_NAME = "AcquiredUtils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static void init() { LOGGER.info("Initializing {}", MOD_NAME); }
}