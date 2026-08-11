package dev.bobodado.acquiredutils.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.client.gui.theme.Theme;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class LockedKeybindWidget extends AbstractWidget implements KeyListenerSlot.Listener {

    private static final int CHECKBOX_SIZE = 10;
    private static final int KEY_BOX_WIDTH = 70;
    private static final float LABEL_SCALE = 1.25f;

    private final KeyListenerSlot slot;
    private final BooleanSupplier enabledGetter;
    private final Consumer<Boolean> enabledSetter;
    private final IntSupplier keyGetter;
    private final Consumer<Integer> keySetter;

    public LockedKeybindWidget(
        int x,
        int y,
        int width,
        int height,
        Component label,
        KeyListenerSlot slot,
        BooleanSupplier enabledGetter,
        Consumer<Boolean> enabledSetter,
        IntSupplier keyGetter,
        Consumer<Integer> keySetter
    ) {
        super(x, y, width, height, label);
        this.slot = slot;
        this.enabledGetter = enabledGetter;
        this.enabledSetter = enabledSetter;
        this.keyGetter = keyGetter;
        this.keySetter = keySetter;
    }

    private int s(int base) {
        return (int) (base * AcquiredUtilsConfig.get().menuScale);
    }

    @Override
    public void applyKeyCode(int keyCode) {
        keySetter.accept(keyCode);

        if (slot.isListening(this)) {
            slot.clear();
        }
    }

    @Override
    protected void renderWidget(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        Theme theme = Theme.current();
        var font = Minecraft.getInstance().font;

        boolean enabled = enabledGetter.getAsBoolean();
        boolean listening = slot.isListening(this);

        int cbSize = s(CHECKBOX_SIZE);
        int cbY = getY() + (height - cbSize) / 2;

        graphics.fill(
            getX(),
            cbY,
            getX() + cbSize,
            cbY + cbSize,
            theme.footerBottom
        );

        graphics.renderOutline(
            getX(),
            cbY,
            cbSize,
            cbSize,
            enabled ? theme.accentBright : theme.frameMid
        );

        if (enabled) {
            graphics.fill(
                getX() + 2,
                cbY + 2,
                getX() + cbSize - 2,
                cbY + cbSize - 2,
                theme.accent
            );
        }

        int labelX = getX() + cbSize + s(6);
        int labelY = getY() + (int) ((height - 8 * LABEL_SCALE) / 2);

        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX, labelY);
        graphics.pose().scale(LABEL_SCALE, LABEL_SCALE);

        graphics.drawString(
            font,
            getMessage(),
            0,
            0,
            theme.text,
            false
        );

        graphics.pose().popMatrix();

        int keyBoxW = s(KEY_BOX_WIDTH);
        int keyBoxX = getX() + width - keyBoxW;
        int keyCode = keyGetter.getAsInt();

        String keyText = listening
            ? "..."
            : (keyCode < 0
                ? "[NONE]"
                : InputConstants.Type.KEYSYM
                    .getOrCreate(keyCode)
                    .getDisplayName()
                    .getString());

        int keyColor = listening
            ? theme.accentBright
            : (keyCode < 0 ? theme.credit : theme.text);

        graphics.fill(
            keyBoxX,
            getY(),
            keyBoxX + keyBoxW,
            getY() + height,
            theme.footerBottom
        );

        graphics.renderOutline(
            keyBoxX,
            getY(),
            keyBoxW,
            height,
            listening ? theme.accentBright : theme.frameMid
        );

        int textWidth = font.width(keyText);

        graphics.drawString(
            font,
            keyText,
            keyBoxX + (keyBoxW - textWidth) / 2,
            getY() + (height - 8) / 2,
            keyColor,
            false
        );
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        int cbSize = s(CHECKBOX_SIZE);
        int cbY = getY() + (height - cbSize) / 2;

        boolean onCheckbox =
            mouseX >= getX()
            && mouseX < getX() + cbSize
            && mouseY >= cbY
            && mouseY < cbY + cbSize;

        if (onCheckbox) {
            enabledSetter.accept(!enabledGetter.getAsBoolean());
            return;
        }

        int keyBoxW = s(KEY_BOX_WIDTH);
        int keyBoxX = getX() + width - keyBoxW;

        boolean onKeyBox =
            mouseX >= keyBoxX
            && mouseX < keyBoxX + keyBoxW
            && mouseY >= getY()
            && mouseY < getY() + height;

        if (onKeyBox) {
            slot.current = this;
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
