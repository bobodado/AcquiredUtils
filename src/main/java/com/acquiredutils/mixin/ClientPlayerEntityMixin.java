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
import java.util.*;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {
    @Unique private Map<Item, Map<RarityType, Integer>> prevTotals = new HashMap<>();
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        Inventory inv = player.getInventory();
        Map<Item, Map<RarityType, Integer>> cur = new HashMap<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) {
                RarityType r = RarityDetector.detectRarity(s);
                cur.computeIfAbsent(s.getItem(), k -> new HashMap<>()).merge(r, s.getCount(), Integer::sum);
            }
        }
        if (!prevTotals.isEmpty()) {
            for (var e : cur.entrySet()) {
                Item item = e.getKey();
                var curR = e.getValue();
                var prevR = prevTotals.getOrDefault(item, Collections.emptyMap());
                for (var re : curR.entrySet()) {
                    RarityType r = re.getKey();
                    int cc = re.getValue(), pc = prevR.getOrDefault(r, 0);
                    if (cc > pc) {
                        ItemStack gained = new ItemStack(item, cc - pc);
                        for (int i = 0; i < inv.getContainerSize(); i++) {
                            ItemStack is = inv.getItem(i);
                            if (!is.isEmpty() && is.getItem() == item && RarityDetector.detectRarity(is) == r) {
                                gained = is.copy(); gained.setCount(cc - pc); break;
                            }
                        }
                        AcquiredUtilsNotifier.onItemAcquired(gained);
                    }
                }
            }
        }
        prevTotals = cur;
        AcquiredUtilsNotifier.tick();
    }
}