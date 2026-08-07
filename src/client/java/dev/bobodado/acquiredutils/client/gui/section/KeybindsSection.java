package dev.bobodado.acquiredutils.client.gui.section;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bobodado.acquiredutils.AcquiredUtils;
import dev.bobodado.acquiredutils.client.AcquiredUtilsClient;
import dev.bobodado.acquiredutils.config.AcquiredUtilsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class KeybindsSection extends ModSection {

    private static final int COLOR_LISTENING = 0xFFE38A2D;
    private static final int COLOR_NONE = 0xFF666666;
    private static final int COLOR_ROW_HOVER = 0x20FFFFFF;

    private enum ListenTarget { NONE, SLOT_LOCK, CUSTOM }
    private ListenTarget listening = ListenTarget.NONE;
    private int listeningCustomIndex = -1;
    private boolean addingNew = false;
    private EditBox nameField;
    private float scrollOffset = 0;

    private final List<RowHitbox> hitboxes = new ArrayList<>();

    private record RowHitbox(int x, int y, int w, int h, int type, int index) {}

    public KeybindsSection(dev.bobodado.acquiredutils.client.gui.AcquiredUtilsConfigScreen screen) {
        super(screen);
    }

    @Override
    public String getId() {
        return "keybinds";
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("acquiredutils.gui.tab.keybinds");
    }

    @Override
    public void buildContent(int contentX, int contentY, int contentWidth, int contentHeight) {
        int btnW = s(100), btnH = s(16);
        addWidget(Button.builder(Component.translatable("acquiredutils.gui.add_keybind"), b -> {
            addingNew = true;
            screen.rebuild();
        }).bounds(contentX, contentY, btnW, btnH).build());

        if (addingNew) {
            int fieldW = s(140), fieldH = s(16);
            int fieldY = contentY + btnH + s(6);
            nameField = new EditBox(screen.getFont(), contentX, fieldY, fieldW, fieldH,
                    Component.translatable("acquiredutils.gui.keybind_name_hint"));
            nameField.setMaxLength(32);
            nameField.setFocused(true);
            addWidget(nameField);

            addWidget(Button.builder(Component.translatable("acquiredutils.gui.confirm"), b -> {
                String name = nameField.getValue().trim();
                if (!name.isEmpty()) {
                    AcquiredUtilsConfig.get().customKeybinds.add(
                            new AcquiredUtilsConfig.CustomKeybindEntry(name, -1));
                    addingNew = false;
                    AcquiredUtilsClient.syncCustomKeybinds();
                    screen.rebuild();
                }
            }).bounds(contentX + fieldW + s(4), fieldY, s(60), fieldH).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                       int contentX, int contentY, int contentWidth, int contentHeight) {
        hitboxes.clear();

        int btnH = s(16);
        int listTop = contentY + btnH + s(6);
        if (addingNew) listTop += s(22);

        int clipY1 = listTop;
        int clipY2 = contentY + contentHeight;
        int rowH = s(24);
        AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();

        int totalRows = 1 + cfg.customKeybinds.size();
        int totalH = totalRows * rowH;
        int visibleH = clipY2 - clipY1;

        if (scrollOffset < 0) scrollOffset = 0;
        if (totalH > visibleH) {
            if (scrollOffset > totalH - visibleH) scrollOffset = totalH - visibleH;
        } else {
            scrollOffset = 0;
        }

        graphics.enableScissor(contentX, clipY1, contentX + contentWidth, clipY2);

        int drawY = listTop - (int) scrollOffset;

        drawY = renderRow(graphics, mouseX, mouseY,
                Component.translatable("acquiredutils.gui.keybind.slot_lock"),
                cfg.slotLockKey, contentX, drawY, contentWidth, rowH, true, -1);

        for (int i = 0; i < cfg.customKeybinds.size(); i++) {
            var entry = cfg.customKeybinds.get(i);
            drawY = renderRow(graphics, mouseX, mouseY,
                    Component.literal(entry.name), entry.keyCode,
                    contentX, drawY, contentWidth, rowH, false, i);
        }

        graphics.disableScissor();

        if (totalH > visibleH) {
            int sbW = s(4);
            int sbX = contentX + contentWidth - sbW;
            int sbH = (int) (visibleH * ((float) visibleH / totalH));
            int sbY = clipY1 + (int) ((visibleH - sbH) * (scrollOffset / (float) (totalH - visibleH)));
            graphics.fill(sbX, clipY1, sbX + sbW, clipY2, 0x30FFFFFF);
            graphics.fill(sbX, sbY, sbX + sbW, sbY + sbH, 0x80FFFFFF);
        }
    }

    private int renderRow(GuiGraphics g, int mx, int my, Component name, int keyCode,
                          int x, int y, int w, int h, boolean builtIn, int customIdx) {
        boolean isListening = (listening == ListenTarget.SLOT_LOCK && builtIn)
                || (listening == ListenTarget.CUSTOM && !builtIn && customIdx == listeningCustomIndex);
        boolean hovered = my >= y && my < y + h && mx >= x && mx < x + w;

        if (hovered && listening == ListenTarget.NONE) {
            g.fill(x, y, x + w, y + h, COLOR_ROW_HOVER);
        }

        g.drawString(screen.getFont(), name, x, y + (h - 8) / 2, 0xFFF2F2F2, false);

        int keyBtnW = s(80), keyBtnH = s(16);
        int keyBtnX = x + w - keyBtnW - (builtIn ? 0 : s(22));
        int keyBtnY = y + (h - keyBtnH) / 2;

        int keyColor = isListening ? COLOR_LISTENING : (keyCode < 0 ? COLOR_NONE : 0xFFF2F2F2);
        String keyText = isListening ? "..." : getKeyName(keyCode);

        g.fill(keyBtnX, keyBtnY, keyBtnX + keyBtnW, keyBtnY + keyBtnH, 0xFF1A1A1A);
        g.renderOutline(keyBtnX, keyBtnY, keyBtnW, keyBtnH, isListening ? COLOR_LISTENING : 0xFF5A5A5A);

        int tw = screen.getFont().width(keyText);
        g.drawString(screen.getFont(), keyText, keyBtnX + (keyBtnW - tw) / 2,
                keyBtnY + (keyBtnH - 8) / 2, keyColor, false);

        if (!builtIn) {
            int ds = s(14);
            int dx = x + w - ds;
            int dy = y + (h - ds) / 2;
            boolean dHover = mx >= dx && mx < dx + ds && my >= dy && my < dy + ds;
            int dc = dHover ? 0xFFFF5555 : 0xFFAA4444;
            g.fill(dx, dy, dx + ds, dy + ds, dc);
            g.renderOutline(dx, dy, ds, ds, 0xFFCC3333);
            int xw = screen.getFont().width("x");
            g.drawString(screen.getFont(), "x", dx + (ds - xw) / 2, dy + (ds - 8) / 2, 0xFFF2F2F2, false);
        }

        hitboxes.add(new RowHitbox(keyBtnX, keyBtnY, keyBtnW, keyBtnH, builtIn ? 0 : 1, customIdx));
        if (!builtIn) {
            int ds = s(14);
            hitboxes.add(new RowHitbox(x + w - ds, y + (h - ds) / 2, ds, ds, 2, customIdx));
        }

        return y + h;
    }

    private String getKeyName(int keyCode) {
        if (keyCode < 0) return "[NONE]";
        return InputConstants.Type.KEYSYM.getOrCreate(keyCode).getDisplayName().getString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listening != ListenTarget.NONE) {
            stopListening();
            return true;
        }

        for (RowHitbox box : hitboxes) {
            if (mouseX >= box.x && mouseX < box.x + box.w &&
                mouseY >= box.y && mouseY < box.y + box.h) {

                if (box.type == 0) {
                    startListening(ListenTarget.SLOT_LOCK, -1);
                    return true;
                } else if (box.type == 1) {
                    startListening(ListenTarget.CUSTOM, box.index);
                    return true;
                } else if (box.type == 2) {
                    AcquiredUtilsConfig.get().customKeybinds.remove(box.index);
                    AcquiredUtilsClient.syncCustomKeybinds();
                    screen.rebuild();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (float) (scrollY * 20 * scale());
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != ListenTarget.NONE) {
            AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();
            int keyCode = InputConstants.getKey(event).getValue();

            if (keyCode == 256) {
                keyCode = -1;
            }

            if (listening == ListenTarget.SLOT_LOCK) {
                cfg.slotLockKey = keyCode;
                AcquiredUtilsClient.syncSlotLockKeybind();
            } else if (listeningCustomIndex >= 0 && listeningCustomIndex < cfg.customKeybinds.size()) {
                cfg.customKeybinds.get(listeningCustomIndex).keyCode = keyCode;
                AcquiredUtilsClient.syncCustomKeybinds();
            }

            stopListening();
            return true;
        }

        if (addingNew && nameField != null && nameField.isFocused()) {
            int keyCode = InputConstants.getKey(event).getValue();
            if (keyCode == 257 || keyCode == 335) {
                String name = nameField.getValue().trim();
                if (!name.isEmpty()) {
                    AcquiredUtilsConfig cfg = AcquiredUtilsConfig.get();
                    cfg.customKeybinds.add(new AcquiredUtilsConfig.CustomKeybindEntry(name, -1));
                    addingNew = false;
                    AcquiredUtilsClient.syncCustomKeybinds();
                    screen.rebuild();
                }
                return true;
            }
        }

        return false;
    }

    private void startListening(ListenTarget target, int idx) {
        this.listening = target;
        this.listeningCustomIndex = idx;
    }

    private void stopListening() {
        this.listening = ListenTarget.NONE;
        this.listeningCustomIndex = -1;
    }

    @Override
    public void onClose() {
        stopListening();
        AcquiredUtilsClient.syncSlotLockKeybind();
        AcquiredUtilsClient.syncCustomKeybinds();
    }
}
