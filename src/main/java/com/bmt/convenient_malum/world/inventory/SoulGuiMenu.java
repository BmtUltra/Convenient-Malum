package com.bmt.convenient_malum.world.inventory;

import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class SoulGuiMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    public final static HashMap<String, Object> guistate = new HashMap<>();
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private IItemHandler internal;
    private final Map<Integer, Slot> customSlots = new HashMap<>();
    private boolean bound = false;

    public SoulGuiMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player);
    }

    public SoulGuiMenu(int id, Inventory inv, Player player) {
        super(com.bmt.convenient_malum.ConvenientMalumMenus.SOUL_GUI.get(), id);
        this.entity = player;
        this.world = player.level();
        this.internal = new ItemStackHandler(9);


        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof com.bmt.convenient_malum.item.MalumBagItem) {
            var capability = heldItem.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (capability.isPresent()) {
                this.internal = capability.resolve().get();
                this.bound = true;
            }
        }


        this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 7, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:sacred_spirit");
            }
        }));
        this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 25, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:wicked_spirit");
            }
        }));
        this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 43, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:arcane_spirit");
            }
        }));
        this.customSlots.put(3, this.addSlot(new SlotItemHandler(internal, 3, 61, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:eldritch_spirit");
            }
        }));
        this.customSlots.put(4, this.addSlot(new SlotItemHandler(internal, 4, 79, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:earthen_spirit");
            }
        }));
        this.customSlots.put(5, this.addSlot(new SlotItemHandler(internal, 5, 97, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:infernal_spirit");
            }
        }));
        this.customSlots.put(6, this.addSlot(new SlotItemHandler(internal, 6, 115, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:aerial_spirit");
            }
        }));
        this.customSlots.put(7, this.addSlot(new SlotItemHandler(internal, 7, 133, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:aqueous_spirit");
            }
        }));
        this.customSlots.put(8, this.addSlot(new SlotItemHandler(internal, 8, 151, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isSpiritItem(stack, "malum:umbral_spirit");
            }
        }));


        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));
    }

    private boolean isSpiritItem(ItemStack stack, String expectedItemId) {
        var expectedLocation = net.minecraft.resources.ResourceLocation.tryParse(expectedItemId);
        var actualLocation = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return expectedLocation.equals(actualLocation);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, this.slots.size(), true))
                    return ItemStack.EMPTY;
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
                if (index < 9 + 27) {
                    if (!this.moveItemStackTo(itemstack1, 9 + 27, this.slots.size(), true))
                        return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(itemstack1, 9, 9 + 27, false))
                        return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
            if (itemstack1.getCount() == itemstack.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.set(itemstack);
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.set(itemstack);
                        flag = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }
            while (true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }
                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(stack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(stack.split(stack.getCount()));
                    }
                    slot1.setChanged();
                    flag = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }
        return flag;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                for (int j = 0; j < internal.getSlots(); ++j) {
                    if (internal instanceof ItemStackHandler handler) {
                        playerIn.drop(handler.extractItem(j, handler.getStackInSlot(j).getCount(), false), false);
                    }
                }
            } else {
                for (int i = 0; i < internal.getSlots(); ++i) {
                    if (internal instanceof ItemStackHandler handler) {
                        playerIn.getInventory().placeItemBackInInventory(handler.extractItem(i, handler.getStackInSlot(i).getCount(), false));
                    }
                }
            }
        }
    }

    public Map<Integer, Slot> get() {
        return customSlots;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player entity = event.player;
        if (event.phase == TickEvent.Phase.END && entity.containerMenu instanceof SoulGuiMenu) {

        }
    }
}
