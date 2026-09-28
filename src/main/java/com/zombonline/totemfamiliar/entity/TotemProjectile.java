package com.zombonline.totemfamiliar.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.level.Level;

public class TotemProjectile extends SpectralArrow {
    private static final float BASE_DAMAGE = 1;
    public TotemProjectile(EntityType<? extends SpectralArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
    }

    @Override
    protected boolean canHitEntity(final Entity entity) {
        if (entity instanceof Player) {
            return false;
        }

        return super.canHitEntity(entity);
    }
    @Override
    protected float getAirDrag() {
        return 1.0F;
    }
}
