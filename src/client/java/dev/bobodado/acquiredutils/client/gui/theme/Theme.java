package dev.bobodado.acquiredutils.client.gui.theme;

import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;

public enum Theme {
    DEFAULT(
        0xFF32241C, 0xFF1C1512,
        0xFF140D08, 0xFF8B5A2B, 0xFFD98F3E,
        0xFF3A2A1E, 0xFF1F1611,
        0xFF241A14, 0xFF1A130F,
        0xFF1F1611, 0xFF140E0A,
        0x40D98F3E, 0x60000000, 0x33D98F3E,
        0xFFF2F2F2, 0xFFE38A2D, 0xFFD98F3E, 0xCCE0A868
    ),
    DARK(
        0xFF1A1A1A, 0xFF0D0D0D,
        0xFF000000, 0xFF555555, 0xFF888888,
        0xFF222222, 0xFF111111,
        0xFF1A1A1A, 0xFF111111,
        0xFF111111, 0xFF0A0A0A,
        0x40888888, 0x60000000, 0x33888888,
        0xFFF2F2F2, 0xFFAAAAAA, 0xFF888888, 0xCCAAAAAA
    ),
    HIGH_CONTRAST(
        0xFF000000, 0xFF000000,
        0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFF00,
        0xFF000000, 0xFF000000,
        0xFF000000, 0xFF000000,
        0xFF000000, 0xFF000000,
        0xFFFFFFFF, 0x60000000, 0xFFFFFF00,
        0xFFFFFFFF, 0xFFFFFF00, 0xFFFFFF00, 0xCCFFFFFF
    );

    public final int panelTop, panelBottom;
    public final int frameOuter, frameMid, frameAccent;
    public final int headerTop, headerBottom;
    public final int sidebarTop, sidebarBottom;
    public final int footerTop, footerBottom;
    public final int divider, shadow, tabActiveBg;
    public final int text, accent, accentBright, credit;

    Theme(int panelTop, int panelBottom,
          int frameOuter, int frameMid, int frameAccent,
          int headerTop, int headerBottom,
          int sidebarTop, int sidebarBottom,
          int footerTop, int footerBottom,
          int divider, int shadow, int tabActiveBg,
          int text, int accent, int accentBright, int credit) {
        this.panelTop = panelTop;
        this.panelBottom = panelBottom;
        this.frameOuter = frameOuter;
        this.frameMid = frameMid;
        this.frameAccent = frameAccent;
        this.headerTop = headerTop;
        this.headerBottom = headerBottom;
        this.sidebarTop = sidebarTop;
        this.sidebarBottom = sidebarBottom;
        this.footerTop = footerTop;
        this.footerBottom = footerBottom;
        this.divider = divider;
        this.shadow = shadow;
        this.tabActiveBg = tabActiveBg;
        this.text = text;
        this.accent = accent;
        this.accentBright = accentBright;
        this.credit = credit;
    }

    public static Theme current() {
        return switch (AcquiredUtilsConfig.get().guiTheme) {
            case DEFAULT -> DEFAULT;
            case HIGH_CONTRAST -> HIGH_CONTRAST;
            default -> DARK;
        };
    }
}