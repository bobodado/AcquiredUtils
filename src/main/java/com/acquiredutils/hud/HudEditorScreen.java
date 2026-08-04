package com.acquiredutils.hud;

import com.acquiredutils.config.ConfigManager;
import com.acquiredutils.notification.Notification;
import com.acquiredutils.rarity.RarityType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HudEditorScreen extends Screen {
    private static final int PW = 160, PH = 32;
    private static final Component TITLE = Component.literal("AcquiredUtils HUD Editor");
    private boolean dragging = false;
    private int offX, offY, px, py;
    public HudEditorScreen() { super(TITLE); }
    @Override protected void init() {
        super.init();
        px = ConfigManager.get().notificationX >= 0 ? ConfigManager.get().notificationX : width - PW - 10;
        py = ConfigManager.get().notificationY >= 0 ? ConfigManager.get().notificationY : height / 2 - PH / 2;
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(0, 0, width, height, 0xCC000000, 0xCC000000);
        int gc = 0x33FFFFFF;
        g.hLine(0, width, height / 4, gc); g.hLine(0, width, height / 2, gc); g.hLine(0, width, height * 3 / 4, gc);
        g.vLine(width / 4, 0, height, gc); g.vLine(width / 2, 0, height, gc); g.vLine(width * 3 / 4, 0, height, gc);
        g.fill(px, py, px + PW, py + PH, 0xCC141414);
        g.fill(px, py, px + 3, py + PH, 0xFFAA00AA);
        g.drawString(Minecraft.getInstance().font, "5x Diamond", px + 11, py + 12, 0xFFFFFFFF, false);
        if (over(mx, my)) {
            int o = 0xFFFFFFFF;
            g.hLine(px - 1, px + PW + 1, py - 1, o); g.hLine(px - 1, px + PW + 1, py + PH + 1, o);
            g.vLine(px - 1, py - 1, py + PH + 1, o); g.vLine(px + PW + 1, py - 1, py + PH + 1, o);
        }
        String ct = "X: " + px + " | Y: " + py;
        g.drawString(Minecraft.getInstance().font, ct, (width - Minecraft.getInstance().font.width(ct)) / 2, height - 30, 0xFFFFFFFF, true);
        String ht = "Drag to move | ESC to save";
        g.drawString(Minecraft.getInstance().font, ht, (width - Minecraft.getInstance().font.width(ht)) / 2, height - 18, 0xFFAAAAAA, true);
        super.render(g, mx, my, pt);
    }
    private boolean over(int mx, int my) { return mx >= px && mx <= px + PW && my >= py && my <= py + PH; }
    @Override public boolean mouseClicked(double mx, double my, int b) {
        if (b == 0 && over((int)mx, (int)my)) { dragging = true; offX = (int)mx - px; offY = (int)my - py; return true; }
        return super.mouseClicked(mx, my, b);
    }
    @Override public boolean mouseDragged(double mx, double my, int b, double dx, double dy) {
        if (dragging && b == 0) {
            px = Math.max(0, Math.min(width - PW, (int)mx - offX));
            py = Math.max(0, Math.min(height - PH, (int)my - offY));
            return true;
        }
        return super.mouseDragged(mx, my, b, dx, dy);
    }
    @Override public boolean mouseReleased(double mx, double my, int b) { if (b == 0) dragging = false; return super.mouseReleased(mx, my, b); }
    @Override public void onClose() {
        ConfigManager.get().notificationX = px;
        ConfigManager.get().notificationY = py;
        ConfigManager.save();
        super.onClose();
    }
    @Override public boolean isPauseScreen() { return false; }
}