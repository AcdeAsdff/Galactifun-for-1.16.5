package io.github.addoncommunity.galactifun.api.worlds.populators;

import java.util.Random;

import javax.annotation.Nonnull;

import io.github.addoncommunity.galactifun.util.Util;

import lombok.AllArgsConstructor;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
//import org.bukkit.generator.BlockPopulator;
//import org.bukkit.generator.LimitedRegion;
//import org.bukkit.generator.WorldInfo;

/**
 * Volcano populator
 *
 * @author Seggan
 * @author Mooy1
 */
public class VolcanoPopulator extends BlockPopulator {

    private final int minY;
    private final Material belowLiquid;
    private final Material liquid;
    private final int maxY;

    public VolcanoPopulator(int minY, Material belowLiquid, Material liquid, int maxY){
        this.minY = minY;
        this.belowLiquid = belowLiquid;
        this.liquid = liquid;
        this.maxY = maxY;
    }

    @Override
    public void populate(@Nonnull World worldInfo, @Nonnull Random random, @Nonnull Chunk region) {
        if (random.nextInt(7) == 5){
            int by = 0;
            int cx = 0;
            int cz = 0;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = minY; y<= maxY; y++) {
                        if (region.getBlock(x,y,z).getType().isAir()){
                            region.getBlock(x,y,z).setType(liquid);
                            by = y;
                            for (int ny = 7; ny > 0; ny--) {
                                region.getBlock(cx, by - ny, cz).setType(belowLiquid);
                            }
                            return;
                        }

                    }
                }
            }
        }
    }

}
