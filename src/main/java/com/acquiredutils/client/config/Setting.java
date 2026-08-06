package com.acquiredutils.client.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

/**
 * Base class for every registrable config entry.
 *
 * This is the "one-line registration" backend the whole GUI is built around.
 * A concrete subclass (e.g. {@link com.acquiredutils.client.config.widget.BooleanSettingWidget})
 * knows how to render itself and handle its own input; {@link ScrollableListWidget}
 * never needs to know what kind of setting it's looking at - it just calls
 * {@link #render}, {@link #mouseClicked}, etc. and stacks them vertically.
 *
 * Row chrome (the dark floating panel background, per-row) is drawn by
 * {@link ScrollableListWidget}, matching how NEU's {@code GuiOptionEditor}
 * base class drew a shared "floating rect" behind every option type.
 */
public abstract class Setting<T> {

    protected final String name;
    protected final String description;
    protected T value;
    protected final Consumer<T> onChange;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChange) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.onChange = onChange;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T getValue() {
        return value;
    }

    /** Subclasses call this instead of writing to {@link #value} directly so onChange always fires. */
    protected void updateValue(T newValue) {
        this.value = newValue;
        if (onChange != null) {
            onChange.accept(newValue);
        }
    }

    /**
     * Height of just this row's control area, NOT including the inter-row gap.
     * NEU's {@code GuiOptionEditor.HEIGHT} was a flat 45px for every editor type;
     * we keep that as the default but let sliders/complex widgets override it.
     */
    public int getHeight() {
        return 45;
    }

    /**
     * Draws the name/description text plus the interactive control.
     * {@code x, y, width} describe the row's content box (chrome already drawn by the caller).
     */
    public abstract void render(GuiGraphics graphics, Font font, int x, int y, int width, int mouseX, int mouseY, float partialTick);

    public abstract boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}