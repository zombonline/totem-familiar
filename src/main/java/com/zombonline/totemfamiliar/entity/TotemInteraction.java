package com.zombonline.totemfamiliar.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.Level;

public class TotemInteraction extends Interaction {

    public TotemInteraction(EntityType<?> type, Level level) {
        super(type, level);
    }
}
