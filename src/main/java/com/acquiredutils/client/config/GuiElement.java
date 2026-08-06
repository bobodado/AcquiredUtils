package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface GuiElement {
    int getHeight();
    void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick);
    boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);
    default boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
}