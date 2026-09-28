package com.zombonline.totemfamiliar.mixin;

import com.zombonline.totemfamiliar.entity.TotemInteraction;
import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Interaction.class)
public class InteractionMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void totemfamiliar$onTick(CallbackInfo ci) {
        var self = (Interaction) (Object) this;

        if (!(self instanceof TotemInteraction)) {
            return;
        }

//        InteractionHandler.tick((TotemInteraction)self);
    }
}
