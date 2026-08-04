package com.acquiredutils.mixin;

import com.acquiredutils.notification.AcquiredUtilsNotifier;
import com.acquiredutils.rarity.RarityDetector;
import com.acquiredutils.rarity.RarityType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Unique
    private Map<Item, Map<RarityType, Integer>> acquiredutils$previousTotals = new HashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        Inventory inventory = player.getInventory();
        Map<Item, Map<RarityType, Integer>> currentTotals = new HashMap<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                RarityType rarity = RarityDetector.detectRarity(stack);
                currentTotals.computeIfAbsent(stack.getItem(), k -> new HashMap<>())
                        .merge(rarity, stack.getCount(), Integer::sum);
            }
        }

        if (!acquiredutils$previousTotals.isEmpty()) {
            for (Map.Entry<Item, Map<RarityType, Integer>> itemEntry : currentTotals.entrySet()) {
                Item item = itemEntry.getKey();
                Map<RarityType, Integer> currentRarities = itemEntry.getValue();
                Map<RarityType, Integer> prevRarities = acquiredutils$previousTotals.getOrDefault(item, Collections.emptyMap());

                for (Map.Entry<RarityType, Integer> rarityEntry : currentRarities.entrySet()) {
                    RarityType rarity = rarityEntry.getKey();
                    int currentCount = rarityEntry.getValue();
                    int prevCount = prevRarities.getOrDefault(rarity, 0);

                    if (currentCount > prevCount) {
                        ItemStack gained = new ItemStack(item, currentCount - prevCount);
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            ItemStack invStack = inventory.getItem(i);
                            if (!invStack.isEmpty() && invStack.getItem() == item && RarityDetector.detectRarity(invStack) == rarity) {
                                gained = invStack.copy();
                                gained.setCount(currentCount - prevCount);
                                break;
                            }
                        }
                        AcquiredUtilsNotifier.onItemAcquired(gained);
                    }
                }
            }
        }

        acquiredutils$previousTotals = currentTotals;
        AcquiredUtilsNotifier.tick();
    }
}
