package com.zombonline.totemfamiliar.entity;

import com.zombonline.totemfamiliar.TotemFamiliar;
import com.zombonline.totemfamiliar.attachment.TotemFamiliarAttachments;
import com.zombonline.totemfamiliar.enchantment.TotemFamiliarEnchantments;
import com.zombonline.totemfamiliar.totem.TotemBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;


public class Totem {
    private final Player owner;
    private final TotemDisplay display;
    private final TotemInteraction interaction;
//    private final Display.TextDisplay textDisplay;
    private boolean destroyed = false;

    private TotemBehavior currentBehavior = TotemBehavior.STOP;

    public long behaviorStartTick;
    public long behaviorDurationTicks = -1;
    public Vec3 posOffset = Vec3.ZERO;
    public long bobTick = 0L;
    public Vec3 ownerLastPos = Vec3.ZERO;
    public long timeSinceOwnerMove = 0L;
    public long timeOwnerSprinting = 0L;

    public float radius = 1.2f;
    public Runnable onCompleteBehavior;

    //LUMINANCE ENCHANT VARS
    public ResourceKey<Level> lastLightDimension = null;
    public BlockPos lastLightPos = null;
    private long lastGlowstoneConsumptionTick;

    //SENTRY ENCHANT VARS
    public long projectileCooldownTicks = 30L;
    public float detectionRange = 12.0F;



    public record TotemMovement(Vec3 posOffset, float yaw) {
    }

    public Totem(Player owner, TotemDisplay display, TotemInteraction interaction) {
        this.owner = owner;
        this.display = display;
        this.interaction = interaction;
//        this.textDisplay = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, owner.level());
//        this.textDisplay.setBillboardConstraints(Display.BillboardConstraints.CENTER);
//        owner.level().addFreshEntity(textDisplay);
    }

    public void destroy() {
        display.discard();
        interaction.discard();
        destroyLastLight();
        destroyed = true;
    }

    public void tick() {
        if(owner == null) {
            destroy();
        }
        debugChangeStatesTick();
        changeBehavior();
        tickOwnerMovement();
        tickDisplay();
        tickInteraction();
        tickLightSource();

        ItemStack totem = owner.getAttached(TotemFamiliarAttachments.FLOATING_TOTEM);
        Holder<Enchantment> sentry = owner.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(TotemFamiliarEnchantments.SENTRY);

        if (totem.getEnchantments().getLevel(sentry) > 0) {
            if(TotemFamiliar.SERVER.getTickCount()%projectileCooldownTicks!=0)
                return;
            AABB searchBox = display.getBoundingBox().inflate(detectionRange);

            List<Zombie> zombies = display.level().getEntitiesOfClass(
                    Zombie.class,
                    searchBox,
                    zombie -> zombie.isAlive() && !zombie.isRemoved()
            );

            if (!zombies.isEmpty()) {
                Zombie target = zombies.stream()
                        .min(Comparator.comparingDouble(display::distanceToSqr))
                        .orElse(null);

                if (target != null) {

                    Vec3 start = display.position();
                    Vec3 targetPos = target.getBoundingBox().getCenter();
                    Vec3 direction = targetPos.subtract(start).normalize();

                    TotemProjectile projectile = new TotemProjectile(EntityTypes.SPECTRAL_ARROW, display.level());
                    projectile.setPos(start.x, start.y, start.z);
                    projectile.setOwner(owner);

                    projectile.shoot(
                            direction.x,
                            direction.y,
                            direction.z,
                            4F,
                            0.0F
                    );
                    display.level().addFreshEntity(projectile);
                }
            }
        }
    }

    private void changeBehavior() {
        if(timeSinceOwnerMove > 5L && currentBehavior.equals(TotemBehavior.FOLLOW)) {
            setBehavior(TotemBehavior.STOP, -1L);
        }
        if(timeSinceOwnerMove < 5L && currentBehavior.equals(TotemBehavior.STOP)) {
            setBehavior(TotemBehavior.FOLLOW);
        }
        if(timeSinceOwnerMove > 5L && TotemFamiliar.RANDOM.nextFloat() < 0.001f && !currentBehavior.equals(TotemBehavior.SPIN)) {
            setBehavior(TotemBehavior.SPIN, 60L, () -> {
                setBehavior(TotemBehavior.FOLLOW, -1L);
            });
        }
        if(timeOwnerSprinting > 60L && TotemFamiliar.RANDOM.nextFloat() < 0.05f && currentBehavior.equals(TotemBehavior.FOLLOW))
            setBehavior(TotemBehavior.LEAD, -1L);
        if(timeOwnerSprinting < 60L && currentBehavior.equals(TotemBehavior.LEAD))
            setBehavior(TotemBehavior.FOLLOW, -1L);

    }

    private void debugChangeStatesTick() {
        if(owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.ARMADILLO_SCUTE && currentBehavior !=TotemBehavior.BOB) {
            setBehavior(TotemBehavior.BOB, 60L, () -> {
                TotemFamiliar.LOGGER.info("Completed a spin");
                setBehavior(TotemBehavior.FOLLOW);
            });
        }
        if(owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.TORCH && currentBehavior !=TotemBehavior.SPIN) {
            setBehavior(TotemBehavior.SPIN, 60L, () -> {
                TotemFamiliar.LOGGER.info("Completed a spin");
                setBehavior(TotemBehavior.FOLLOW);
            });
        }
        if(owner.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.STICK && currentBehavior!=TotemBehavior.STOP) {
            setBehavior(TotemBehavior.STOP, 60L, () -> {
                TotemFamiliar.LOGGER.info("Completed a spin");
                setBehavior(TotemBehavior.FOLLOW);
            });
        }
//        textDisplay.setText(Component.literal(currentBehavior.name()));
//        textDisplay.setPos(display.position());
    }

    private void tickInteraction() {
        interaction.setPos(display.position().subtract(0,interaction.getHeight()/2, 0));
    }

    public void processInteraction(Player player, Level world, InteractionHand hand) {
        player.sendOverlayMessage(Component.literal("Hello!"));
        ItemStack heldStack = player.getItemInHand(hand);
        if(heldStack.getItem().equals(Items.GLOWSTONE_DUST)) {
            var heldGlowstone = owner.getAttachedOrElse(TotemFamiliarAttachments.TOTEM_GLOWSTONE, 0);
            var newGlowstone = heldGlowstone+1;
            if(newGlowstone > 64)
                return;
            heldStack.shrink(1);
            owner.setAttached(TotemFamiliarAttachments.TOTEM_GLOWSTONE, newGlowstone);
        }
    }

    private void tickDisplay() {
        Vec3 basePos = owner.position().add(0,owner.getEyeHeight(),0);
        long now = TotemFamiliar.SERVER.getTickCount();
        long elapsed = now - behaviorStartTick;

        boolean complete = elapsed >= behaviorDurationTicks && behaviorDurationTicks != -1;
        if(!complete) {
            TotemMovement totemMovement = getPosOffset(elapsed);
            if(totemMovement.posOffset != null)
                posOffset = totemMovement.posOffset;
            if (!Float.isNaN(totemMovement.yaw)) {
                display.setYRot(totemMovement.yaw);
            }
        } else if(onCompleteBehavior!=null){
            onCompleteBehavior.run();
            onCompleteBehavior = null;
        }

        bobTick++;
        Vec3 bobOffset = getBobOffset();

        Vec3 finalPos = basePos.add(posOffset).add(bobOffset);
        Vec3 current = display.position();
        Vec3 smoothed = current.lerp(finalPos, getMovementLerpSpeed());
        display.setPos(smoothed);
        display.setPosRotInterpolationDuration(2);
    }
    private float getMovementLerpSpeed() {
        return switch (currentBehavior) {
            case STOP   -> 0.15f;
            case FOLLOW -> 0.20f;
            case LEAD   -> 0.08f;
            case BOB    -> 0.20f;
            case SPIN   -> 0.50f;
        };
    }
    private Vec3 getBobOffset() {
        double verticalFreq = 0.05;
        double horizontalFreq = 0.035;
        double verticalAmp = 0.08;
        double horizontalAmp = 0.06;

        double vertical = Math.sin(bobTick * verticalFreq) * verticalAmp;
        double sideways = Math.sin(bobTick * horizontalFreq) * horizontalAmp;

        float yawRad = (float) Math.toRadians(owner.getYRot());
        Vec3 rightDir = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

        return rightDir.scale(sideways).add(0, vertical, 0);
    }

    private TotemMovement getPosOffset(long elapsed) {
        return switch (currentBehavior) {
            case STOP -> {
                var pos = posOffset;
                Vec3 toPlayer = pos.scale(-1);
                float finalYaw = (float) Math.toDegrees(
                        Math.atan2(-toPlayer.x, toPlayer.z)
                );
                yield new TotemMovement(pos, finalYaw);
            }
            case FOLLOW -> {
                Vec3 lookDir = owner.getHeadLookAngle();
                Vec3 flatDir = new Vec3(lookDir.x, 0, lookDir.z).normalize();
                Vec3 behindDir = flatDir.scale(-1);
                Vec3 rightDir = new Vec3(-flatDir.z, 0, flatDir.x);
                var pos = behindDir.add(rightDir).normalize();
                yield new TotemMovement(pos, owner.getYRot());
            }
            case BOB -> new TotemMovement(Vec3.ZERO,0);
            case SPIN -> {
                double progress = (double) elapsed / behaviorDurationTicks;
                double angle = progress * Math.PI * 2;

                Vec3 pos = new Vec3(
                        Math.cos(angle) * radius,
                        0,
                        Math.sin(angle) * radius
                );

                Vec3 toPlayer = pos.scale(-1);

                float finalYaw = (float) Math.toDegrees(
                        Math.atan2(-toPlayer.x, toPlayer.z)
                );

                yield new TotemMovement(pos, finalYaw);
            }
            case LEAD -> {
                Vec3 lookDir = owner.getHeadLookAngle();
                Vec3 flatDir = new Vec3(lookDir.x, 0, lookDir.z).normalize();

                Vec3 pos = flatDir.scale(7.5);

                float finalYaw = owner.getYRot();

                yield new TotemMovement(pos, finalYaw);
            }
        };
    }


    private void tickOwnerMovement() {
        if(owner.position() != ownerLastPos) {
            timeSinceOwnerMove = 0L;
            ownerLastPos = owner.position();
        } else {
            timeSinceOwnerMove++;
        }
        if(owner.isSprinting())
            timeOwnerSprinting++;
        else
            timeOwnerSprinting = 0L;
    }

    public void tickLightSource() {
        BlockPos newPos = BlockPos.containing(display.position());

        var level = display.level();
        ResourceKey<Level> currentDimension = level.dimension();


        if (lastLightPos != null && lastLightDimension != null) {
            destroyLastLight();
        }
        var currentGlowstone = owner.getAttachedOrElse(TotemFamiliarAttachments.TOTEM_GLOWSTONE, 0);
        if(TotemFamiliar.SERVER.getTickCount()-lastGlowstoneConsumptionTick >= 20L * 18L) {
            lastGlowstoneConsumptionTick = TotemFamiliar.SERVER.getTickCount();
            currentGlowstone--;
            TotemFamiliar.LOGGER.info("Current glowstone {}", currentGlowstone);
            if(currentGlowstone >= 0)
                owner.setAttached(TotemFamiliarAttachments.TOTEM_GLOWSTONE, currentGlowstone);
        }
        if(currentGlowstone == -1)
            return;
        int lightLevel = (int) Math.ceil(15 * ((double) currentGlowstone /16));
        lightLevel = Math.min(15, lightLevel);
        if (level.getBlockState(newPos).isAir()) {
            BlockState lightState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel);
            level.setBlock(newPos, lightState, 3);
        }

        lastLightDimension = currentDimension;
        lastLightPos = newPos;
    }

    private void destroyLastLight() {
        ServerLevel oldLevel = TotemFamiliar.SERVER.getLevel(lastLightDimension);
        if (oldLevel != null && oldLevel.getBlockState(lastLightPos).is(Blocks.LIGHT)) {
            oldLevel.setBlock(lastLightPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    public void setBehavior(TotemBehavior behavior, long behaviorDurationTicks, Runnable onCompleteBehavior) {
        currentBehavior = behavior;
        behaviorStartTick = TotemFamiliar.SERVER.getTickCount();
        this.behaviorDurationTicks = behaviorDurationTicks;
        this.onCompleteBehavior = onCompleteBehavior;
    }
    public void setBehavior(TotemBehavior behavior, long behaviorDurationTicks) {
        setBehavior(behavior, behaviorDurationTicks, null);
    }
    public void setBehavior(TotemBehavior behavior) {
        setBehavior(behavior, -1L);
    }

    public Player getOwner() {
        return owner;
    }

    public TotemDisplay getDisplay() {
        return display;
    }

    public TotemInteraction getInteraction() {
        return interaction;
    }
    public boolean isDestroyed() {
        return destroyed;
    }
}
