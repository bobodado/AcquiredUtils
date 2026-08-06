package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.function.Consumer;

public abstract class Setting<T> {
    protected final String name, description;
    protected T value;
    protected final Consumer<T> onChange;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChange) {
        this.name = name; this.description = description; this.value = defaultValue; this.onChange = onChange;
    }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }
    protected void updateValue(T newValue) {
        this.value = newValue;
        if (onChange != null) onChange.accept(newValue);
    }
    public int getHeight() { return 45; }
    public abstract void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
    public boolean charTyped(char chr, int modifiers) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
}
