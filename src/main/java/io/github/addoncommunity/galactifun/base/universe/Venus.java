package io.github.addoncommunity.galactifun.base.universe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import io.github.addoncommunity.galactifun.base.universe.denendencies.BiomeProvider;

import io.github.addoncommunity.galactifun.base.universe.denendencies.WorldInfo;

import io.github.addoncommunity.galactifun.util.GenUtils;

import org.bukkit.Material;
import org.bukkit.block.Biome;
//import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.ItemStack;

import io.github.addoncommunity.galactifun.api.universe.StarSystem;
import io.github.addoncommunity.galactifun.api.universe.attributes.DayCycle;
import io.github.addoncommunity.galactifun.api.universe.attributes.Gravity;
import io.github.addoncommunity.galactifun.api.universe.attributes.Orbit;
import io.github.addoncommunity.galactifun.api.universe.attributes.atmosphere.Atmosphere;
import io.github.addoncommunity.galactifun.api.universe.types.PlanetaryType;
import io.github.addoncommunity.galactifun.api.worlds.SimpleAlienWorld;
import io.github.addoncommunity.galactifun.api.worlds.populators.LakePopulator;
import io.github.addoncommunity.galactifun.api.worlds.populators.VolcanoPopulator;

import org.jetbrains.annotations.NotNull;

import static io.github.addoncommunity.galactifun.Galactifun.AlienBiomeProviderList;

/**
 * Class for Venus
 *
 * @author Seggan
 */
public final class Venus extends SimpleAlienWorld {

    List<Biome> biomes = new ArrayList<>();
    public Venus(String name, PlanetaryType type, Orbit orbit, StarSystem orbiting, ItemStack baseItem,
                 DayCycle dayCycle, Atmosphere atmosphere, Gravity gravity, String CN_name) {
        super(name, type, orbit, orbiting, baseItem, dayCycle, atmosphere, gravity, CN_name);
        this.biomes.add(Biome.BASALT_DELTAS);
    }

    @Override
    public void getPopulators(@Nonnull List<BlockPopulator> populators) {
        populators.add(new VolcanoPopulator(117, Material.MAGMA_BLOCK, Material.LAVA, 150));
        populators.add(new LakePopulator(75, Material.LAVA, 50));
    }

    @Nonnull
    @Override
    protected Material generateMaterial(@Nonnull Random random, int x, int y, int z, int top) {
        if (y > 75) {
            return Material.BLACKSTONE;
        } else if (y > 9) {
            return Material.BASALT;
        } else if (y > 8) {
            return Material.YELLOW_TERRACOTTA;
        } else {
            return Material.BASALT;
        }
    }

    @Nonnull
    @Override
    protected Biome getBiome() {
        return Biome.BASALT_DELTAS;
    }

    @Override
    protected int getAverageHeight() {
        return 100;
    }

    @Override
    protected int getMaxDeviation() {
        return 50;
    }

    @Override
    protected double getScale() {
        return .02;
    }

    @Override
    protected double getFrequency() {
        return .3;
    }

}
