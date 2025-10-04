package com.bmt.convenient_malum;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ConvenientMalumMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ConvenientMalum.MOD_ID);

    public static final RegistryObject<MenuType<com.bmt.convenient_malum.world.inventory.SoulGuiMenu>> SOUL_GUI = REGISTRY.register("soul_gui",
            () -> IForgeMenuType.create(com.bmt.convenient_malum.world.inventory.SoulGuiMenu::new));
}