package com.zombonline.totemfamiliar.events;

import com.zombonline.totemfamiliar.TotemFamiliar;
import com.zombonline.totemfamiliar.attachment.TotemFamiliarAttachments;
import com.zombonline.totemfamiliar.totem.TotemManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.item.ItemStack;

public class TotemFamiliarServerEvents {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TotemFamiliar.SERVER = server;
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TotemFamiliar.SERVER = null;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            TotemManager.tickTotems();
        });

        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> {
            var player = serverGamePacketListener.player;
            var storedTotem = player.getAttached(TotemFamiliarAttachments.FLOATING_TOTEM);
            if(storedTotem.equals(ItemStack.EMPTY))
                return; //player doesn't have an active floating totem
            TotemManager.createTotem(player, storedTotem.copy());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> {
            var player = serverGamePacketListener.player;
            TotemManager.getTotemByOwner(player).destroy();
        });

    }
}