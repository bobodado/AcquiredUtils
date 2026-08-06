package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface GuiElement {
    int getHeight();
    void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick);

    /** 
     * Called AFTER the scissor is disabled so tooltips/dropdowns draw on top.
     * Return true if something was drawn that needs to stay on top.
     */
    default void renderOverlay(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick) {}

    boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);
    default boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    default boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
}