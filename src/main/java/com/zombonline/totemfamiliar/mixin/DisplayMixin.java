package com.zombonline.totemfamiliar.mixin;


import com.zombonline.totemfamiliar.entity.TotemDisplay;
import net.minecraft.world.entity.Display;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.class)
public class DisplayMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void totemfamiliar$onTick(CallbackInfo ci) {
        var self = (Display) (Object) this;

        if (!(self instanceof TotemDisplay)) {
            return;
        }

    }
}