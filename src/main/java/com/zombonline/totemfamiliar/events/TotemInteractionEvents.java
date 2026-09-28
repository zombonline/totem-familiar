package com.zombonline.totemfamiliar.events;

import com.zombonline.totemfamiliar.entity.TotemInteraction;
import com.zombonline.totemfamiliar.totem.TotemManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class TotemInteractionEvents  {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if(!(entity instanceof TotemInteraction))
                return null;
            var totem = TotemManager.getTotemByInteraction((TotemInteraction) entity);
            assert totem != null;
            totem.processInteraction(player, world, hand);
            return null;
        });
    }

}
