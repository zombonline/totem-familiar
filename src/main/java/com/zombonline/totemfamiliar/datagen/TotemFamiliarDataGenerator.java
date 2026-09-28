package com.zombonline.totemfamiliar.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.registries.Registries;

public class TotemFamiliarDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(TotemFamiliarEnchantmentGenerator::new);
        pack.addProvider(TotemFamiliarItemTagGenerator::new);
    }

    @Override
    public void buildRegistry(net.minecraft.core.RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, TotemFamiliarEnchantmentGenerator::bootstrap);
    }
}