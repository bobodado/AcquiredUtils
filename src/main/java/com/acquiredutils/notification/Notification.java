package com.acquiredutils.notification;

import com.acquiredutils.rarity.RarityType;
import net.minecraft.world.item.ItemStack;

public class Notification {
    private final ItemStack itemStack;
    private final RarityType rarity;
    private int count;
    private final long startTime;
    private long lastUpdateTime;

    public Notification(ItemStack itemStack, RarityType rarity, int count) {
        this.itemStack = itemStack.copy();
        this.rarity = rarity;
        this.count = count;
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = this.startTime;
    }

    public void merge(int additionalCount) {
        this.count += additionalCount;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public boolean shouldAggregateWith(ItemStack other, RarityType otherRarity) {
        if (this.rarity != otherRarity) return false;
        return ItemStack.isSameItemSameComponents(this.itemStack, other);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.lastUpdateTime > 3000;
    }

    public float getFadeAlpha() {
        long elapsed = System.currentTimeMillis() - this.lastUpdateTime;
        if (elapsed < 2500) return 1.0f;
        float fadeProgress = (elapsed - 2500) / 500.0f;
        return Math.max(0.0f, 1.0f - fadeProgress);
    }

    public long getLastInternalUpdateTime() {
        return lastUpdateTime;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public RarityType getRarity() {
        return rarity;
    }

    public int getCount() {
        return count;
    }

    public String getDisplayText() {
        return count + "x " + itemStack.getHoverName().getString();
    }
}
