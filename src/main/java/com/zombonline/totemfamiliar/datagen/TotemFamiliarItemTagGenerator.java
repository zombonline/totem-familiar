package com.zombonline.totemfamiliar.datagen;

import com.zombonline.totemfamiliar.TotemFamiliar;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class TotemFamiliarItemTagGenerator extends FabricTagsProvider.ItemTagsProvider {

    public TotemFamiliarItemTagGenerator(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        TagKey<Item> totemTag = TagKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(
                        TotemFamiliar.MOD_ID,
                        "enchantable/totem"
                )
        );

        builder(totemTag)
                .add(Items.TOTEM_OF_UNDYING.builtInRegistryHolder().key());
    }
}