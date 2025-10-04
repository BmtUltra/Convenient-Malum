package com.bmt.convenient_malum.event;

import com.bmt.convenient_malum.item.MalumBagItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SoulConversionHandler {
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;

        checkAndUpdateBag(player.getMainHandItem(), player.level(), player, true);
        checkAndUpdateBag(player.getOffhandItem(), player.level(), player, false);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            checkAndUpdateBag(stack, player.level(), player, false);
        }
    }
    
    private static void checkAndUpdateBag(ItemStack stack, net.minecraft.world.level.Level level, Player player, boolean isSelected) {
        if (stack.getItem() instanceof MalumBagItem malumBag) {
            for (int slot = 0; slot < 9; slot++) {
                malumBag.onUpdate(stack, level, player, slot, isSelected);
            }
        }
    }
}