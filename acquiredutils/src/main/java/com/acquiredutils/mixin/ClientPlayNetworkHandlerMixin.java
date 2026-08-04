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
    private void onItemPickup(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Entity itemEntity = mc.level.getEntity(packet.getItemId());
        Entity playerEntity = mc.level.getEntity(packet.getPlayerId());

        if (itemEntity instanceof ItemEntity item && playerEntity == mc.player) {
            ItemStack stack = item.getItem().copy();
            int amount = packet.getAmount();
            if (amount > 0) {
                stack.setCount(Math.min(amount, stack.getCount()));
            }
            AcquiredUtilsNotifier.onItemAcquired(stack);
        }
    }
}
