package io.github.addoncommunity.galactifun.api.worlds.populators.relics;

import java.util.Random;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;

import org.bukkit.Chunk;
import org.bukkit.Location;
//import org.bukkit.generator.BlockPopulator;
//import org.bukkit.generator.LimitedRegion;
//import org.bukkit.generator.WorldInfo;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;

import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.util.Util;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

@AllArgsConstructor
public class FallenSatellitePopulator extends BlockPopulator {

    private final double chance;

    @Override
    public void populate(@Nonnull World worldInfo, @Nonnull Random random, @Nonnull Chunk region) {

        if (random.nextDouble() * 100 < chance) {
            int cx = region.getX();
            int cz = region.getZ();
            Vector v = random.nextBoolean() ? new Vector(1, 0, 0) : new Vector(0, 0, 1);
            int x = (cx << 4) + random.nextInt(16);
            int z = (cz << 4) + random.nextInt(16);
            Location l = Util.getHighestBlockAt(region, x, z).add(0, 1, 0);
            setSlimefunBlock(region, l, BaseItems.FALLEN_SATELLITE_RELIC);
            if (random.nextBoolean()) {
                setSlimefunBlock(region, l.add(v), BaseItems.BROKEN_SOLAR_PANEL_RELIC);
                v.multiply(-1);
                l.add(v);
            }
            if (random.nextBoolean()) {
                setSlimefunBlock(region, l.add(v), BaseItems.BROKEN_SOLAR_PANEL_RELIC);
            }
        }
    }

    protected void setSlimefunBlock(Chunk region, Location l, SlimefunItemStack item) {

        int x = l.getBlockX();
        int z = l.getBlockZ();
        x = x%16;
        z = z%16;
        if (x < 0){
            x += 16;
        }
        if (z < 0){
            z += 16;
        }
        region.getBlock(x, l.getBlockY(), z).setType(item.getType());
        Location copy = l.clone();
        copy.setWorld(region.getWorld());
        Scheduler.run(() -> BlockStorage.addBlockInfo(copy, "id", item.getItemId()));
    }

}
