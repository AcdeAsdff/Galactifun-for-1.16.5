package io.github.addoncommunity.galactifun.api.worlds.populators;

import java.util.Random;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
//import org.bukkit.generator.BlockPopulator;
//import org.bukkit.generator.BlockPopulator;
//import org.bukkit.generator.LimitedRegion;
//import org.bukkit.generator.WorldInfo;

/**
 * Lake populator
 *
 * @author Seggan
 * @author Mooy1
 */

public class LakePopulator extends BlockPopulator {

    private final int maxY;
    @Nonnull
    private final Material liquid;

    private final int minY;
    public LakePopulator(int maxY, Material liquid, int minY){
        this.maxY = maxY;
        this.liquid = liquid;
        this.minY = minY;
    }

    @Override
    public void populate(@Nonnull World worldInfo, @Nonnull Random random, @Nonnull Chunk region) {
        if (random.nextInt(10) == 5){
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minY; y < maxY; y++) {
                        if (region.getBlock(x, y, z).getType().isAir()) {
                            region.getBlock(x, y, z).setType(liquid);
                        }
                    }
                }
            }
        }
    }



}
