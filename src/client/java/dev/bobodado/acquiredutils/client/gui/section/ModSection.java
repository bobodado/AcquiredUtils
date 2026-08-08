package dev.bobodado.acquiredutils.client.gui.section;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public abstract class ModSection {

    protected final dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen screen;

    public ModSection(dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen screen) {
        this.screen = screen;
    }

    public abstract String getId();
    public abstract Component getDisplayName();

    protected int s(int base) {
        return screen.s(base);
    }

    protected float scale() {
        return screen.getMenuScale();
    }

    protected <T extends AbstractWidget> T addWidget(T widget) {
        screen.addSectionWidget(widget);
        return widget;
    }

    public abstract void buildContent(int contentX, int contentY, int contentWidth, int contentHeight);

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                       int contentX, int contentY, int contentWidth, int contentHeight) {}

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { return false; }
    public boolean keyPressed(KeyEvent event) { return false; }

    public void onClose() {}
}