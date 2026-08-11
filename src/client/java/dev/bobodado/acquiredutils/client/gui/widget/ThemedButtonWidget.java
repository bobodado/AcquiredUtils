package dev.bobodado.acquiredutils.client.gui.widget;

import dev.bobodado.acquiredutils.client.gui.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ThemedButtonWidget extends AbstractWidget {

    private final Runnable clickHandler;
    private final boolean bold;

    public ThemedButtonWidget(int x, int y, int width, int height, Component label, Runnable clickHandler) {
        this(x, y, width, height, label, clickHandler, false);
    }

    public ThemedButtonWidget(int x, int y, int width, int height, Component label, Runnable clickHandler, boolean bold) {
        super(x, y, width, height, label);
        this.clickHandler = clickHandler;
        this.bold = bold;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Theme theme = Theme.current();
        boolean hovered = isHovered();

        int topColor = hovered ? 0xFF4A3626 : 0xFF3A2A1E;
        int bottomColor = hovered ? 0xFF2E2119 : 0xFF241A14;
        int borderColor = hovered ? theme.accentBright : 0xFF8B5A2B;

        graphics.fillGradient(getX(), getY(), getX() + width, getY() + height, topColor, bottomColor);
        graphics.renderOutline(getX(), getY(), width, height, borderColor);

        var font = Minecraft.getInstance().font;
        Component text = bold ? getMessage().copy().withStyle(Style.EMPTY.withBold(true)) : getMessage();
        int tw = font.width(text);
        int tx = getX() + (width - tw) / 2;
        int ty = getY() + (height - 8) / 2;
        graphics.drawString(font, text, tx, ty, theme.text, false);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        clickHandler.run();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}