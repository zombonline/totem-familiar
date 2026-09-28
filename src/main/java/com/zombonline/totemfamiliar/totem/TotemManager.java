package com.zombonline.totemfamiliar.totem;

import com.mojang.math.Transformation;
import com.zombonline.totemfamiliar.entity.Totem;
import com.zombonline.totemfamiliar.entity.TotemDisplay;
import com.zombonline.totemfamiliar.entity.TotemInteraction;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class TotemManager {

    private static final Map<Player, Totem> ACTIVE_TOTEMS = new HashMap<>();
    public static void createTotem(Player owner, ItemStack storedTotem) {
        var display = createDisplay(owner, storedTotem);
        var interaction = createInteraction(owner);
        var newTotem = new Totem(owner,display,interaction);
        ACTIVE_TOTEMS.put(owner,newTotem);
    }
    public static Totem getTotemByOwner(Player owner) {
        return ACTIVE_TOTEMS.get(owner);
    }
    public static Totem getTotemByInteraction(TotemInteraction interaction) {
        for(Totem totem : ACTIVE_TOTEMS.values()) {
            if(totem.getInteraction().equals(interaction))
                return totem;
        }
        return null;
    }
    public static void tickTotems() {
        for(Totem totem : ACTIVE_TOTEMS.values()) {
            if(totem.isDestroyed()) {
                ACTIVE_TOTEMS.remove(totem.getOwner());
                continue;
            }
            totem.tick();
        }
    }
    private static TotemDisplay createDisplay(Player player, ItemStack storedTotem){
        var level = player.level();
        TotemDisplay display = new TotemDisplay(EntityTypes.ITEM_DISPLAY, level);
        display.setPos(
                player.getX(),
                player.getY() + 2,
                player.getZ()
        );
        display.setInvisible(true);
        display.setNoGravity(true);
        display.setSilent(true);

        display.setTransformation(
                new Transformation(
                        new Vector3f(0, 0, 0),       // translation
                        new Quaternionf(),            // left rotation
                        new Vector3f(0.5f, 0.5f, 0.5f), // scale
                        new Quaternionf()             // right rotation
                )
        );


        display.setItemStack(storedTotem.copy());
        level.addFreshEntity(display);
        return display;
    }
    private static TotemInteraction createInteraction(Player player) {
        var level = player.level();
        TotemInteraction interaction= new TotemInteraction(EntityTypes.INTERACTION, level);
        interaction.setPos(
                player.getX(),
                player.getY(),
                player.getZ()
        );
        interaction.setWidth(0.25f);
        interaction.setHeight(0.5f);
        level.addFreshEntity(interaction);
        return interaction;
    }
}