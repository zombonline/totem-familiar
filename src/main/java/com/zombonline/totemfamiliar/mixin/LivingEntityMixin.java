package com.zombonline.totemfamiliar.mixin;

import com.zombonline.totemfamiliar.TotemFamiliar;
import com.zombonline.totemfamiliar.attachment.TotemFamiliarAttachments;

import com.zombonline.totemfamiliar.totem.TotemManager;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
    private void totemfamiliar$onCheckTotemDeathProtection(DamageSource killingDamage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (killingDamage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return; // let vanilla return false as normal
        }

        // Only override behavior for players with an active floating totem
        var floatingTotem = self.getAttached(TotemFamiliarAttachments.FLOATING_TOTEM);

        if (self instanceof ServerPlayer player && floatingTotem!=ItemStack.EMPTY) {
            TotemFamiliar.LOGGER.info("Consuming floating totem instead of held item.");

            DeathProtection protection = floatingTotem.get(DataComponents.DEATH_PROTECTION);

            if (protection != null) {
                player.awardStat(Stats.ITEM_USED.get(floatingTotem.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger(player, floatingTotem);
                floatingTotem.causeUseVibration(self, GameEvent.ITEM_INTERACT_FINISH);

                self.setHealth(1.0F);
                protection.applyEffects(floatingTotem, self);
                self.level().broadcastEntityEvent(self, (byte) 35);

                // TODO: despawn/consume your floating totem entity here
                TotemManager.getTotemByOwner(player).destroy();
                self.setAttached(TotemFamiliarAttachments.FLOATING_TOTEM, ItemStack.EMPTY);

                cir.setReturnValue(true);
                cir.cancel();
            }
            return;
        }
    }
}

//private boolean checkTotemDeathProtection(final DamageSource killingDamage) {
//    if (killingDamage.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
//        return false;
//    } else {
//        ItemStack protectionItem = null;
//        DeathProtection protection = null;
//
//        for(InteractionHand hand : InteractionHand.values()) {
//            ItemStack itemStack = this.getItemInHand(hand);
//            protection = (DeathProtection)itemStack.get(DataComponents.DEATH_PROTECTION);
//            if (protection != null) {
//                protectionItem = itemStack.copy();
//                itemStack.shrink(1);
//                break;
//            }
//        }
//
//        if (protectionItem != null) {
//            if (this instanceof ServerPlayer) {
//                ServerPlayer player = (ServerPlayer)this;
//                player.awardStat(Stats.ITEM_USED.get(protectionItem.getItem()));
//                CriteriaTriggers.USED_TOTEM.trigger(player, protectionItem);
//                protectionItem.causeUseVibration(this, GameEvent.ITEM_INTERACT_FINISH);
//            }
//
//            this.setHealth(1.0F);
//            protection.applyEffects(protectionItem, this);
//            this.level().broadcastEntityEvent(this, (byte)35);
//        }
//
//        return protection != null;
//    }
//}