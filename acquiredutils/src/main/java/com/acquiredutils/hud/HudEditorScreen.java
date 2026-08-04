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
    private static final int PREVIEW_WIDTH = 160;
    private static final int PREVIEW_HEIGHT = 32;
    private static final Component TITLE = Component.literal("AcquiredUtils HUD Editor");

    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int previewX = 0;
    private int previewY = 0;

    public HudEditorScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        super.init();
        if (ConfigManager.get().notificationX >= 0) {
            previewX = ConfigManager.get().notificationX;
        } else {
            previewX = width - PREVIEW_WIDTH - 10;
        }
        if (ConfigManager.get().notificationY >= 0) {
            previewY = ConfigManager.get().notificationY;
        } else {
            previewY = height / 2 - PREVIEW_HEIGHT / 2;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0xCC000000, 0xCC000000);

        drawAlignmentGrid(graphics);

        renderPreviewBox(graphics, mouseX, mouseY);

        renderCoordinates(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawAlignmentGrid(GuiGraphics graphics) {
        int gridColor = 0x33FFFFFF;

        graphics.hLine(0, width, height / 4, gridColor);
        graphics.hLine(0, width, height / 2, gridColor);
        graphics.hLine(0, width, height * 3 / 4, gridColor);

        graphics.vLine(width / 4, 0, height, gridColor);
        graphics.vLine(width / 2, 0, height, gridColor);
        graphics.vLine(width * 3 / 4, 0, height, gridColor);
    }

    private void renderPreviewBox(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack previewStack = new ItemStack(Items.DIAMOND);
        Notification preview = new Notification(previewStack, RarityType.EPIC, 5);

        int bgColor = 0xCC141414;
        int borderColor = 0xFFAA00AA;
        int textColor = 0xFFFFFFFF;

        graphics.fill(previewX, previewY, previewX + PREVIEW_WIDTH, previewY + PREVIEW_HEIGHT, bgColor);
        graphics.fill(previewX, previewY, previewX + 3, previewY + PREVIEW_HEIGHT, borderColor);

        String text = "5x Diamond";
        graphics.drawString(Minecraft.getInstance().font, text, previewX + 11, previewY + 12, textColor, false);

        if (isMouseOverPreview(mouseX, mouseY)) {
            int outline = 0xFFFFFFFF;
            graphics.hLine(previewX - 1, previewX + PREVIEW_WIDTH + 1, previewY - 1, outline);
            graphics.hLine(previewX - 1, previewX + PREVIEW_WIDTH + 1, previewY + PREVIEW_HEIGHT + 1, outline);
            graphics.vLine(previewX - 1, previewY - 1, previewY + PREVIEW_HEIGHT + 1, outline);
            graphics.vLine(previewX + PREVIEW_WIDTH + 1, previewY - 1, previewY + PREVIEW_HEIGHT + 1, outline);
        }
    }

    private void renderCoordinates(GuiGraphics graphics) {
        String coordText = "X: " + previewX + " | Y: " + previewY;
        int textWidth = Minecraft.getInstance().font.width(coordText);
        graphics.drawString(Minecraft.getInstance().font, coordText, (width - textWidth) / 2, height - 30, 0xFFFFFFFF, true);

        String hintText = "Drag to move | ESC to save";
        int hintWidth = Minecraft.getInstance().font.width(hintText);
        graphics.drawString(Minecraft.getInstance().font, hintText, (width - hintWidth) / 2, height - 18, 0xFFAAAAAA, true);
    }

    private boolean isMouseOverPreview(int mouseX, int mouseY) {
        return mouseX >= previewX && mouseX <= previewX + PREVIEW_WIDTH &&
                mouseY >= previewY && mouseY <= previewY + PREVIEW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverPreview((int) mouseX, (int) mouseY)) {
            dragging = true;
            dragOffsetX = (int) mouseX - previewX;
            dragOffsetY = (int) mouseY - previewY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            previewX = (int) mouseX - dragOffsetX;
            previewY = (int) mouseY - dragOffsetY;

            previewX = Math.max(0, Math.min(width - PREVIEW_WIDTH, previewX));
            previewY = Math.max(0, Math.min(height - PREVIEW_HEIGHT, previewY));

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        ConfigManager.get().notificationX = previewX;
        ConfigManager.get().notificationY = previewY;
        ConfigManager.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
