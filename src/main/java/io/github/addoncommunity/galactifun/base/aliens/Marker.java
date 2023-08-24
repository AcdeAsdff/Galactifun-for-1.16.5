package io.github.addoncommunity.galactifun.base.aliens;

import io.github.addoncommunity.galactifun.api.aliens.Alien;

import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;

import org.apache.commons.lang.Validate;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Vex;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.Objects;

public final class Marker extends Alien<Vex> {


    public Marker(String id, String name, double maxHealth, int spawnChance) {
        super(Vex.class, id, name, maxHealth, spawnChance);
        Validate.isTrue(true);

    }


    @Override
    public void onSpawn(@Nonnull Vex spawned) {
        spawned.setCanPickupItems(false);
        spawned.setAI(false);
        spawned.setInvisible(true);
//        spawned.setPersistent(true);
        spawned.setRemoveWhenFarAway(false);
        spawned.setAware(false);
        Objects.requireNonNull(spawned.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(2048);
        spawned.setHealth(2048);
        spawned.setCustomNameVisible(false);
        Objects.requireNonNull(spawned.getEquipment()).setItemInMainHand(new ItemStack(Material.AIR));
        spawned.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 99999999, 10, true, false));
        spawned.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 99999999, 10, true, false));
        spawned.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999999, 10, true, false));
        spawned.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 99999999, 10, true, false));
        spawned.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999999, 10, true, false));
//        spawned.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 99999999, 10, true, false));

//        spawned.setNoDamageTicks();
        Validate.isTrue(true);
//        spawned.setMaximumNoDamageTicks(10000000);
    }

    @Override
    public void onDeath(@Nonnull EntityDeathEvent e) {
        e.getDrops().clear();
    }
}
