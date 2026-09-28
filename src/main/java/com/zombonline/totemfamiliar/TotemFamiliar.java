package com.zombonline.totemfamiliar;

import com.zombonline.totemfamiliar.attachment.TotemFamiliarAttachments;
import com.zombonline.totemfamiliar.events.TotemFamiliarServerEvents;
import com.zombonline.totemfamiliar.events.TotemInteractionEvents;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.server.MinecraftServer;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import static com.zombonline.totemfamiliar.totem.TotemManager.createTotem;

public class TotemFamiliar implements ModInitializer {
	public static final String MOD_ID = "totem-familiar";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;
	public static final RandomSource RANDOM = RandomSource.create();
	@Override
	public void onInitialize() {

		LOGGER.info("Initialized {}",MOD_ID);
		TotemFamiliarAttachments.register();
		TotemInteractionEvents.register();
		TotemFamiliarServerEvents.register();


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


	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
