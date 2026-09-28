package com.zombonline.totemfamiliar.attachment;

import com.mojang.serialization.Codec;
import com.zombonline.totemfamiliar.TotemFamiliar;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;


public class TotemFamiliarAttachments {

    public static final AttachmentType<ItemStack> FLOATING_TOTEM = AttachmentRegistry.<ItemStack>create(
            Identifier.fromNamespaceAndPath(TotemFamiliar.MOD_ID, "floating_totem"),
            builder -> builder
                    .persistent(ItemStack.CODEC)
                    .initializer(() -> ItemStack.EMPTY)
    );
    public static final AttachmentType<Integer> TOTEM_GLOWSTONE = AttachmentRegistry.<Integer>create(
            Identifier.fromNamespaceAndPath(TotemFamiliar.MOD_ID, "totem_glowstone"),
            builder -> builder
                    .persistent(Codec.INT)
                    .initializer(() -> 0)
    );

    public static void register() {

    }

}
