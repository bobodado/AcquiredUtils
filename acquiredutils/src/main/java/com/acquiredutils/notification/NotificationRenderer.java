package com.acquiredutils.notification;

import com.acquiredutils.config.AcquiredUtilsConfig;
import com.acquiredutils.config.ConfigManager;
import com.acquiredutils.hud.HudEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class NotificationRenderer {
    private static final int NOTIFICATION_WIDTH = 160;
    private static final int NOTIFICATION_HEIGHT = 32;
    private static final int BORDER_WIDTH = 3;

    public static void init() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null && !(mc.screen instanceof HudEditorScreen)) {
            return;
        }

        AcquiredUtilsConfig config = ConfigManager.get();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        List<Notification> active = AcquiredUtilsNotifier.getNotifications();
        if (active.isEmpty() && !(mc.screen instanceof HudEditorScreen)) {
            return;
        }

        int x = config.notificationX;
        int y = config.notificationY;

        if (x < 0) {
            x = screenWidth - NOTIFICATION_WIDTH - 10;
        }
        if (y < 0) {
            y = screenHeight / 2 - (active.size() * (NOTIFICATION_HEIGHT + 4)) / 2;
        }

        int currentY = y;
        for (Notification notification : active) {
            renderNotification(graphics, notification, x, currentY);
            currentY += NOTIFICATION_HEIGHT + 4;
        }
    }

    private static void renderNotification(GuiGraphics graphics, Notification notification, int x, int y) {
        float alpha = notification.getFadeAlpha();
        if (alpha <= 0.01f) return;

        int aBg = (int) (alpha * 200);
        int aFg = (int) (alpha * 255);
        int bgColor = (aBg << 24) | 0x141414;
        int borderColor = (aFg << 24) | (notification.getRarity().getColor() & 0xFFFFFF);
        int textColor = (aFg << 24) | 0xFFFFFF;

        graphics.pose().pushPose();

        graphics.fill(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, bgColor);
        graphics.fill(x, y, x + BORDER_WIDTH, y + NOTIFICATION_HEIGHT, borderColor);

        int textX = x + BORDER_WIDTH + 8;
        int textY = y + (NOTIFICATION_HEIGHT - 8) / 2;

        graphics.drawString(Minecraft.getInstance().font, notification.getDisplayText(), textX, textY, textColor, false);

        graphics.pose().popPose();
    }
}
