package com.acquiredutils.rarity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;

public class RarityDetector {
    public static RarityType detectRarity(ItemStack stack) {
        if (stack.isEmpty()) return RarityType.COMMON;
        RarityType custom = detectCustomRarity(stack);
        if (custom != null) return custom;
        Rarity vanilla = stack.get(DataComponents.RARITY);
        if (vanilla != null) {
            return switch (vanilla) {
                case UNCOMMON -> RarityType.UNCOMMON;
                case RARE -> RarityType.RARE;
                case EPIC -> RarityType.EPIC;
                default -> RarityType.COMMON;
            };
        }
        return RarityType.COMMON;
    }
    private static RarityType detectCustomRarity(ItemStack stack) {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) { RarityType r = parseComponentForRarity(customName); if (r != null) return r; }
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) { for (Component line : lore.lines()) { RarityType r = parseComponentForRarity(line); if (r != null) return r; } }
        return null;
    }
    private static RarityType parseComponentForRarity(Component c) {
        String plain = c.getString().toLowerCase();
        if (contains(plain, "mythic")) return RarityType.MYTHIC;
        if (contains(plain, "legendary")) return RarityType.LEGENDARY;
        if (contains(plain, "epic")) return RarityType.EPIC;
        if (contains(plain, "rare")) return RarityType.RARE;
        if (contains(plain, "uncommon")) return RarityType.UNCOMMON;
        RarityType fromColor = detectRarityFromStyle(c.getStyle());
        if (fromColor != null) return fromColor;
        for (Component s : c.getSiblings()) { RarityType r = parseComponentForRarity(s); if (r != null) return r; }
        return null;
    }
    private static boolean contains(String text, String kw) {
        return text.contains(kw) || text.contains("["+kw+"]") || text.contains("("+kw+")");
    }
    private static RarityType detectRarityFromStyle(Style style) {
        if (style == null || style.getColor() == null) return null;
        int v = style.getColor().getValue();
        if (v == 0xFF55FF) return RarityType.MYTHIC;
        if (v == 0xFFAA00) return RarityType.LEGENDARY;
        if (v == 0xAA00AA) return RarityType.EPIC;
        if (v == 0x5555FF) return RarityType.RARE;
        if (v == 0x55FF55) return RarityType.UNCOMMON;
        return null;
    }
}