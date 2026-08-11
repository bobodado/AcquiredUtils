package dev.bobodado.acquiredutils.client.gui.widget;

import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class DropdownWidget extends AbstractWidget {

    private static final int COLOR_FIELD_BG = 0xFF1F1611;
    private static final int COLOR_BORDER = 0xFF8B5A2B;
    private static final int COLOR_OPEN_BG = 0xFF241A14;
    private static final int COLOR_TEXT = 0xFFF2F2F2;
    private static final int COLOR_SELECTION_BG = 0xFF6B4A2E;
    private static final int ROW_HEIGHT = 12;

    private final List<Component> options;
    private final Consumer<Integer> onSelect;
    private int selectedIndex;
    private boolean open = false;

    public DropdownWidget(
        int x,
        int y,
        int width,
        int height,
        List<Component> options,
        int initialSelectedIndex,
        Consumer<Integer> onSelect
    ) {
        super(x, y, width, height, options.get(initialSelectedIndex));
        this.options = options;
        this.selectedIndex = initialSelectedIndex;
        this.onSelect = onSelect;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    private int s(int base) {
        return (int) (base * AcquiredUtilsConfig.get().menuScale);
    }

    @Override
    protected void renderWidget(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        graphics.fill(
            getX(),
            getY(),
            getX() + width,
            getY() + height,
            COLOR_FIELD_BG
        );

        graphics.renderOutline(
            getX(),
            getY(),
            width,
            height,
            COLOR_BORDER
        );

        graphics.drawString(
            Minecraft.getInstance().font,
            options.get(selectedIndex),
            getX() + 4,
            getY() + (height - 8) / 2,
            COLOR_TEXT,
            false
        );

        graphics.drawString(
            Minecraft.getInstance().font,
            "\u25BE",
            getX() + width - 10,
            getY() + (height - 8) / 2,
            COLOR_TEXT,
            false
        );
    }

    public void renderOverlay(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        if (!open) return;

        int listY = getY() + height;
        int listHeight = options.size() * s(ROW_HEIGHT);

        graphics.fill(
            getX(),
            listY,
            getX() + width,
            listY + listHeight,
            COLOR_OPEN_BG
        );

        graphics.renderOutline(
            getX(),
            listY,
            width,
            listHeight,
            COLOR_BORDER
        );

        for (int i = 0; i < options.size(); i++) {
            int rowY = listY + i * s(ROW_HEIGHT);

            if (i == selectedIndex) {
                graphics.fill(
                    getX() + 1,
                    rowY,
                    getX() + width - 1,
                    rowY + s(ROW_HEIGHT),
                    COLOR_SELECTION_BG
                );
            }

            graphics.drawString(
                Minecraft.getInstance().font,
                options.get(i),
                getX() + 4,
                rowY + 2,
                COLOR_TEXT,
                false
            );
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (open) {
            int listY = getY() + height;
            int rowHeight = s(ROW_HEIGHT);
            int listHeight = options.size() * rowHeight;

            if (mouseX >= getX()
                && mouseX < getX() + width
                && mouseY >= listY
                && mouseY < listY + listHeight) {

                int relativeY = (int) mouseY - listY;
                int index = relativeY / rowHeight;

                if (index >= 0 && index < options.size()) {
                    selectedIndex = index;
                    setMessage(options.get(index));
                    onSelect.accept(index);
                }

                open = false;
                return;
            }

            open = false;
            return;
        }

        if (isMouseOver(mouseX, mouseY)) {
            open = true;
        }
    }

    public boolean isOverExpandedArea(double mouseX, double mouseY) {
        if (!open) return isMouseOver(mouseX, mouseY);

        int listY = getY() + height;
        int listHeight = options.size() * s(ROW_HEIGHT);

        return mouseX >= getX()
            && mouseX < getX() + width
            && mouseY >= listY
            && mouseY < listY + listHeight;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, options.get(selectedIndex));
    }
}