package com.acquiredutils.rarity;

public enum RarityType {
    COMMON(0xFFFFFF, "Common"), UNCOMMON(0x55FF55, "Uncommon"), RARE(0x5555FF, "Rare"),
    EPIC(0xAA00AA, "Epic"), LEGENDARY(0xFFAA00, "Legendary"), MYTHIC(0xFF55FF, "Mythic");
    private final int color;
    private final String displayName;
    RarityType(int color, String displayName) { this.color = color; this.displayName = displayName; }
    public int getColor() { return color; }
    public String getDisplayName() { return displayName; }
}