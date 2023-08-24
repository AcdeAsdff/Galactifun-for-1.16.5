package io.github.addoncommunity.galactifun.base.universe.denendencies;

import java.util.Random;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Creates perlin noise through unbiased octaves
 */
public class Paper1181PerlinOctaveGenerator extends Paper1181OctaveGenerator {

    /**
     * Creates a perlin octave generator for the given world
     *
     * @param world World to construct this generator for
     * @param octaves Amount of octaves to create
     */
    public Paper1181PerlinOctaveGenerator(@NotNull World world, int octaves) {
        this(new Random(world.getSeed()), octaves);
    }

    /**
     * Creates a perlin octave generator for the given world
     *
     * @param seed Seed to construct this generator for
     * @param octaves Amount of octaves to create
     */
    public Paper1181PerlinOctaveGenerator(long seed, int octaves) {
        this(new Random(seed), octaves);
    }

    /**
     * Creates a perlin octave generator for the given {@link Random}
     *
     * @param rand Random object to construct this generator for
     * @param octaves Amount of octaves to create
     */
    public Paper1181PerlinOctaveGenerator(@NotNull Random rand, int octaves) {
        super(createOctaves(rand, octaves));
    }

    @NotNull
    private static Paper1181NoiseGenerator[] createOctaves(@NotNull Random rand, int octaves) {
        Paper1181NoiseGenerator[] result = new Paper1181NoiseGenerator[octaves];

        for (int i = 0; i < octaves; i++) {
            result[i] = new Paper1181PerlinNoiseGenerator(rand);
        }

        return result;
    }
}
