package com.acquiredutils.notification;

import com.acquiredutils.config.AcquiredUtilsConfig;
import com.acquiredutils.config.ConfigManager;
import com.acquiredutils.hud.HudEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.util.List;

public class NotificationRenderer {
    private static final int W = 160, H = 32, BORDER = 3;
    public static void init() {}
    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null && !(mc.screen instanceof HudEditorScreen)) return;
        AcquiredUtilsConfig cfg = ConfigManager.get();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        List<Notification> active = AcquiredUtilsNotifier.getNotifications();
        if (active.isEmpty() && !(mc.screen instanceof HudEditorScreen)) return;
        int x = cfg.notificationX < 0 ? sw - W - 10 : cfg.notificationX;
        int y = cfg.notificationY < 0 ? sh / 2 - (active.size() * (H + 4)) / 2 : cfg.notificationY;
        for (Notification n : active) { renderOne(g, n, x, y); y += H + 4; }
    }
    private static void renderOne(GuiGraphics g, Notification n, int x, int y) {
        float a = n.getFadeAlpha();
        if (a <= 0.01f) return;
        int abg = (int)(a * 200);
        int afg = (int)(a * 255);
        int bg = (abg << 24) | 0x141414;
        int border = (afg << 24) | (n.getRarity().getColor() & 0xFFFFFF);
        int tc = (afg << 24) | 0xFFFFFF;
        g.pose().pushPose();
        g.fill(x, y, x + W, y + H, bg);
        g.fill(x, y, x + BORDER, y + H, border);
        g.drawString(Minecraft.getInstance().font, n.getDisplayText(), x + BORDER + 8, y + (H - 8) / 2, tc, false);
        g.pose().popPose();
    }
}