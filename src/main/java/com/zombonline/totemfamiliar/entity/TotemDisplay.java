package com.zombonline.totemfamiliar.entity;

import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TotemDisplay extends Display.ItemDisplay {
    public TotemDisplay(EntityType<?> type, Level level) {
        super(type, level);
    }

}
