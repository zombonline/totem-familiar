package com.zombonline.totemfamiliar.enchantment;

import com.zombonline.totemfamiliar.TotemFamiliar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class TotemFamiliarEnchantments {
    public static final ResourceKey<Enchantment> LUMINANCE = of("luminance");
    public static final ResourceKey<Enchantment> SENTRY = of("sentry");

    private static ResourceKey<Enchantment> of(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(TotemFamiliar.MOD_ID, name));
    }
}