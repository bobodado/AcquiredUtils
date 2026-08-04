package com.acquiredutils.notification;

import com.acquiredutils.rarity.RarityType;
import net.minecraft.world.item.ItemStack;

public class Notification {
    private final ItemStack itemStack;
    private final RarityType rarity;
    private int count;
    private long lastUpdateTime;
    public Notification(ItemStack stack, RarityType rarity, int count) {
        this.itemStack = stack.copy();
        this.rarity = rarity;
        this.count = count;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    public void merge(int add) { this.count += add; this.lastUpdateTime = System.currentTimeMillis(); }
    public boolean shouldAggregateWith(ItemStack other, RarityType otherRarity) {
        return this.rarity == otherRarity && ItemStack.isSameItemSameComponents(this.itemStack, other);
    }
    public boolean isExpired() { return System.currentTimeMillis() - lastUpdateTime > 3000; }
    public float getFadeAlpha() {
        long e = System.currentTimeMillis() - lastUpdateTime;
        if (e < 2500) return 1.0f;
        return Math.max(0.0f, 1.0f - (e - 2500) / 500.0f);
    }
    public long getLastInternalUpdateTime() { return lastUpdateTime; }
    public ItemStack getItemStack() { return itemStack; }
    public RarityType getRarity() { return rarity; }
    public int getCount() { return count; }
    public String getDisplayText() { return count + "x " + itemStack.getHoverName().getString(); }
}