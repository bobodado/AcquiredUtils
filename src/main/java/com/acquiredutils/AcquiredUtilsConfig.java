package com.acquiredutils;

/**
 * Plain data holder for whatever your settings actually control.
 * Settings registered via {@link com.acquiredutils.client.config.ConfigRegistry}
 * write into fields here through their onChange callback - this class has
 * no knowledge of the GUI at all, keeping the two decoupled.
 * <p>
 * Wire this up to your own save/load (Gson to a file in the config dir, etc.)
 * separately; that's outside the scope of the GUI itself.
 */
public class AcquiredUtilsConfig {

    public static final AcquiredUtilsConfig INSTANCE = new AcquiredUtilsConfig();

    public boolean showOverlay = true;
    public double overlayScale = 1.0;

    private AcquiredUtilsConfig() {
    }
}
