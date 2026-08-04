package com.acquiredutils.rarity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;

public class RarityDetector {

    public static RarityType detectRarity(ItemStack stack) {
        if (stack.isEmpty()) {
            return RarityType.COMMON;
        }

        RarityType customRarity = detectCustomRarity(stack);
        if (customRarity != null) {
            return customRarity;
        }

        Rarity vanillaRarity = stack.get(DataComponents.RARITY);
        if (vanillaRarity != null) {
            return switch (vanillaRarity) {
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
        if (customName != null) {
            RarityType fromName = parseComponentForRarity(customName);
            if (fromName != null) return fromName;
        }

        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore != null) {
            for (Component line : lore.lines()) {
                RarityType fromLine = parseComponentForRarity(line);
                if (fromLine != null) return fromLine;
            }
        }

        return null;
    }

    private static RarityType parseComponentForRarity(Component component) {
        String plain = component.getString().toLowerCase();

        if (containsRarityKeyword(plain, "mythic")) return RarityType.MYTHIC;
        if (containsRarityKeyword(plain, "legendary")) return RarityType.LEGENDARY;
        if (containsRarityKeyword(plain, "epic")) return RarityType.EPIC;
        if (containsRarityKeyword(plain, "rare")) return RarityType.RARE;
        if (containsRarityKeyword(plain, "uncommon")) return RarityType.UNCOMMON;

        RarityType fromColor = detectRarityFromStyle(component.getStyle());
        if (fromColor != null) return fromColor;

        for (Component sibling : component.getSiblings()) {
            RarityType fromSibling = parseComponentForRarity(sibling);
            if (fromSibling != null) return fromSibling;
        }

        return null;
    }

    private static boolean containsRarityKeyword(String text, String keyword) {
        return text.contains(keyword) || text.contains("[" + keyword + "]") || text.contains("(" + keyword + ")");
    }

    private static RarityType detectRarityFromStyle(Style style) {
        if (style == null || style.getColor() == null) return null;

        int colorValue = style.getColor().getValue();

        if (isColorMatch(colorValue, 0xFF55FF)) return RarityType.MYTHIC;
        if (isColorMatch(colorValue, 0xFFAA00)) return RarityType.LEGENDARY;
        if (isColorMatch(colorValue, 0xAA00AA)) return RarityType.EPIC;
        if (isColorMatch(colorValue, 0x5555FF)) return RarityType.RARE;
        if (isColorMatch(colorValue, 0x55FF55)) return RarityType.UNCOMMON;

        return null;
    }

    private static boolean isColorMatch(int color1, int color2) {
        return color1 == color2;
    }
}
