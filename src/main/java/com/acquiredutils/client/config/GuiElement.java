package com.acquiredutils.client.config;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Interface for all GUI elements in the config system.
 * Every element must handle its own rendering, input, and focus state.
 */
public interface GuiElement {
    void render(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY, float delta);
    boolean mouseClicked(double mouseX, double mouseY, int button);
    boolean mouseReleased(double mouseX, double mouseY, int button);
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount);
    boolean keyPressed(int keyCode, int scanCode, int modifiers);
    boolean charTyped(char chr, int modifiers);
    void setFocused(boolean focused);
    boolean isFocused();
    int getHeight();
    void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY, float delta);
}