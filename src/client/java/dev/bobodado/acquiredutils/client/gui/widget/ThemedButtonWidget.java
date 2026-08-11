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

    public ThemedButtonWidget(
        int x,
        int y,
        int width,
        int height,
        Component label,
        Runnable clickHandler
    ) {
        this(x, y, width, height, label, clickHandler, false);
    }

    public ThemedButtonWidget(
        int x,
        int y,
        int width,
        int height,
        Component label,
        Runnable clickHandler,
        boolean bold
    ) {
        super(x, y, width, height, label);
        this.clickHandler = clickHandler;
        this.bold = bold;
    }

    @Override
    protected void renderWidget(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        Theme theme = Theme.current();
        boolean hovered = isHovered();

        int topColor = hovered ? theme.headerTop : theme.headerBottom;
        int bottomColor = hovered ? theme.panelTop : theme.panelBottom;
        int borderColor = hovered ? theme.accentBright : theme.frameMid;

        graphics.fillGradient(
            getX(),
            getY(),
            getX() + width,
            getY() + height,
            topColor,
            bottomColor
        );

        graphics.renderOutline(
            getX(),
            getY(),
            width,
            height,
            borderColor
        );

        var font = Minecraft.getInstance().font;

        Component text = bold
            ? getMessage().copy().withStyle(Style.EMPTY.withBold(true))
            : getMessage();

        int textWidth = font.width(text);
        int textX = getX() + (width - textWidth) / 2;
        int textY = getY() + (height - 8) / 2;

        graphics.drawString(
            font,
            text,
            textX,
            textY,
            theme.text,
            false
        );
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
