package com.zombonline.totemfamiliar.events;

import com.zombonline.totemfamiliar.attachment.TotemFamiliarAttachments;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.zombonline.totemfamiliar.totem.TotemManager.createTotem;

public class TotemFamiliarItemEvents {
    public void register() {
        ItemEvents.USE.register((level, player, interactionHand) -> {
            var heldStack = player.getItemInHand(interactionHand);
            if (!heldStack.getItem().equals(Items.TOTEM_OF_UNDYING))
                return null; //player is not holding a totem.
            if(!player.getAttachedOrElse(TotemFamiliarAttachments.FLOATING_TOTEM, ItemStack.EMPTY).equals(ItemStack.EMPTY))
                return null; //player has an active totem;
            ItemStack storedTotem = heldStack.copy();
            player.setAttached(
                    TotemFamiliarAttachments.FLOATING_TOTEM,
                    storedTotem
            );
            createTotem(player, storedTotem);
            heldStack.shrink(1);

            return null;
        });
    }
}
