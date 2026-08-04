package com.acquiredutils.mixin;

import com.acquiredutils.notification.AcquiredUtilsNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
    private void onPickup(ClientboundTakeItemEntityPacket p, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Entity item = mc.level.getEntity(p.getItemId());
        Entity player = mc.level.getEntity(p.getPlayerId());
        if (item instanceof ItemEntity ie && player == mc.player) {
            ItemStack s = ie.getItem().copy();
            if (p.getAmount() > 0) s.setCount(Math.min(p.getAmount(), s.getCount()));
            AcquiredUtilsNotifier.onItemAcquired(s);
        }
    }
}