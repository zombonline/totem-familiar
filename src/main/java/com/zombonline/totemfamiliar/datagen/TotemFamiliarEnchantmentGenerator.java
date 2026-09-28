package com.zombonline.totemfamiliar.datagen;

import com.zombonline.totemfamiliar.TotemFamiliar;
import com.zombonline.totemfamiliar.enchantment.TotemFamiliarEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.concurrent.CompletableFuture;

public class TotemFamiliarEnchantmentGenerator extends FabricDynamicRegistryProvider {

    public TotemFamiliarEnchantmentGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT));
    }

    @Override
    public String getName() {
        return "Enchantments";
    }

    private static void register(BootstrapContext<Enchantment> context, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        context.register(key, builder.build(key.identifier()));
    }

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        TagKey<Item> totemTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(TotemFamiliar.MOD_ID, "enchantable/totem")
        );

        register(context, TotemFamiliarEnchantments.LUMINANCE,
                Enchantment.enchantment(
                        Enchantment.definition(
                                context.lookup(Registries.ITEM).getOrThrow(totemTag),
                                1,
                                1,
                                Enchantment.dynamicCost(0, 0),
                                Enchantment.dynamicCost(0, 0),
                                0,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
        register(context, TotemFamiliarEnchantments.SENTRY,
                Enchantment.enchantment(
                        Enchantment.definition(
                                context.lookup(Registries.ITEM).getOrThrow(totemTag),
                                1,
                                1,
                                Enchantment.dynamicCost(0, 0),
                                Enchantment.dynamicCost(0, 0),
                                0,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
    }
}