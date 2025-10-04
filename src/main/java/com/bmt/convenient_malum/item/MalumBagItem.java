package com.bmt.convenient_malum.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MalumBagItem extends Item {

    private final Map<Integer, Integer> conversionTimers = new HashMap<>();

    public MalumBagItem() {
        super(new Item.Properties()
                .stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {

            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) ->
                            new com.bmt.convenient_malum.world.inventory.SoulGuiMenu(containerId, playerInventory, player),
                    stack.getHoverName()
            ));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }


    public void onUpdate(ItemStack stack, Level level, Player player, int slot, boolean isSelected) {
        if (level.isClientSide()) return;


        var capability = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (!capability.isPresent()) return;

        ItemStackHandler itemHandler = (ItemStackHandler) capability.resolve().get();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack soulItem = itemHandler.getStackInSlot(i);
            if (!soulItem.isEmpty() && isSpiritItem(soulItem)) {

                int currentTimer = conversionTimers.getOrDefault(i, 0);
                currentTimer++;

                if (currentTimer >= 200) {

                    convertSoulToExperience(level, player, soulItem, i, itemHandler);
                    conversionTimers.remove(i);
                } else {
                    conversionTimers.put(i, currentTimer);
                }
            } else {
                conversionTimers.remove(i);
            }
        }
    }

    private boolean isSpiritItem(ItemStack stack) {
        String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
        return itemId.contains("malum:") && itemId.contains("_spirit");
    }

    private void convertSoulToExperience(Level level, Player player, ItemStack soulItem, int slot, ItemStackHandler itemHandler) {
        int experienceAmount = getExperienceForSoul(soulItem);

        if (experienceAmount > 0) {
            ExperienceOrb expOrb = new ExperienceOrb(level, player.getX(), player.getY() + 0.5, player.getZ(), experienceAmount);
            level.addFreshEntity(expOrb);


            itemHandler.extractItem(slot, 1, false);

        }
    }

    private int getExperienceForSoul(ItemStack soulItem) {
        String itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(soulItem.getItem()).toString();

        return switch (itemId) {
            case "malum:sacred_spirit" -> 4;
            case "malum:wicked_spirit" -> 5;
            case "malum:arcane_spirit" -> 5;
            case "malum:eldritch_spirit" ->6;
            case "malum:earthen_spirit" -> 6;
            case "malum:infernal_spirit" -> 6;
            case "malum:aerial_spirit" -> 5;
            case "malum:aqueous_spirit" -> 8;
            case "malum:umbral_spirit" -> 22;
            default -> 5;
        };
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable net.minecraft.nbt.CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
                @Override
                protected void onContentsChanged(int slot) {

                    if (stack.getTag() == null) {
                        stack.setTag(new net.minecraft.nbt.CompoundTag());
                    }
                    stack.getTag().put("Inventory", serializeNBT());


                    conversionTimers.remove(slot);
                }
            };

            {

                if (stack.getTag() != null && stack.getTag().contains("Inventory")) {
                    itemHandler.deserializeNBT(stack.getTag().getCompound("Inventory"));
                }
            }

            @Override
            public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
                if (cap == ForgeCapabilities.ITEM_HANDLER) {
                    return LazyOptional.of(() -> (T) itemHandler);
                }
                return LazyOptional.empty();
            }
        };
    }
}