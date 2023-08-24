package io.github.addoncommunity.galactifun.base.universe.denendencies;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.addoncommunity.galactifun.Galactifun;

import io.github.addoncommunity.galactifun.api.worlds.SimpleAlienWorld;
import io.github.addoncommunity.galactifun.api.worlds.populators.LakePopulator;
import io.github.addoncommunity.galactifun.api.worlds.populators.OrePopulator;
import io.github.addoncommunity.galactifun.api.worlds.populators.VolcanoPopulator;
import io.github.addoncommunity.galactifun.api.worlds.populators.relics.FallenSatellitePopulator;

import io.github.addoncommunity.galactifun.base.BaseMats;
import io.github.addoncommunity.galactifun.base.universe.earth.EarthOrbit;
import io.github.addoncommunity.galactifun.base.universe.saturn.TitanBiome;
import io.github.addoncommunity.galactifun.base.universe.saturn.TitanBiomeProvider;

import io.github.addoncommunity.galactifun.util.GenUtils;
import io.github.addoncommunity.galactifun.util.Sphere;
import io.github.addoncommunity.galactifun.util.Util;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import me.mrCookieSlime.Slimefun.api.BlockStorage;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static io.github.addoncommunity.galactifun.Galactifun.AlienBiomeProviderList;
import static io.github.addoncommunity.galactifun.Galactifun.AlienWorldInfoList;
import static io.github.addoncommunity.galactifun.Galactifun.spawningWorld;
import static io.github.addoncommunity.galactifun.Galactifun.worldSeed;

public class ownChunkGenerator extends ChunkGenerator {

    Sphere ORBIT_comet = new Sphere(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE);
    Sphere ORBIT_asteroid = new Sphere(Material.STONE, Material.COBBLESTONE, Material.ANDESITE);

    private volatile Int2ObjectMap<Material> FlatWorld_top = null;
    private volatile Int2ObjectMap<Material> FlatWorld_bottom = null;
    private String name;

    private volatile Paper1181SimplexOctaveGenerator SimpleAlienWorld_generator;
    private volatile Paper1181SimplexOctaveGenerator TITAN_generator;

    private volatile Paper1181SimplexOctaveGenerator craterGenerator;

    private final AtomicDouble craterDepthNoise = new AtomicDouble(0);

    private volatile CraterSettings craterSettings;

    TitanBiomeProvider Titan_provider = new TitanBiomeProvider();

    Double gainFactor = (double) (1);
    String PlanetName_;

    public ownChunkGenerator(){
        this.name = spawningWorld;
        this.PlanetName_ = name;
//        System.out.println("Init generator " + name);
    }

    public ownChunkGenerator setName(String newName){
        this.name = newName;
        return this;
    }


    public Biome getAlienBiome(int x, int z) {
        if (!Objects.equals(name, "TITAN")){
            return AlienBiomeProviderList.get(name).getBiome(AlienWorldInfoList.get(name), x, 60, z);
        }else {
            return Titan_provider.getBiome(AlienWorldInfoList.get(name), x, 60, z);
        }
    }

    public Biome getAlienBiome(int x, int y, int z) {
        return AlienBiomeProviderList.get(name).getBiome(AlienWorldInfoList.get(name), x, y, z);
    }
    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        PlanetName_ = name;
        ChunkData chunkData = null;

//        System.out.println(world.getName().replace("world_galactifun_", ""));
//        System.out.println(name.replace("world_galactifun_", ""));
//        System.out.println("generating Chunk Data1");
        try{
            chunkData = createChunkData(world);
        }catch (Exception e){e.printStackTrace();}

        int bedrock = 0;

        //bedrock(>6) shuold not be in ORBIT area
        try{
            if (!Objects.equals(name, "EARTH_ORBIT")) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunkData.setBlock(x, bedrock, z, Material.BEDROCK);
                        biome.setBiome(x,z,getAlienBiome(x,z));
                    }
                }
            } else {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        biome.setBiome(x,z,getAlienBiome(x,z));
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();}


        try{
            generateChunk(chunkData, random, AlienWorldInfoList.get(name), chunkX, chunkZ, PlanetName_);
//            System.out.println("generating Chunk Data3");
//            this.generateSurface(chunkData, random, AlienWorldInfoList.get(name), chunkX, chunkZ, PlanetName_);
            generateSurface(chunkData, random, AlienWorldInfoList.get(name), chunkX, chunkZ, PlanetName_);

        }catch (Exception e){
            System.out.println("GenerateChunkDataFailed");
            e.printStackTrace();
        }
        return chunkData;
    }


    private void Titan_init(@Nonnull WorldInfo info) {
        try{
            if (this.TITAN_generator == null) {
                this.TITAN_generator = new Paper1181SimplexOctaveGenerator(new Random(worldSeed), 8);
                this.TITAN_generator.setScale(0.04);
            }
            if (AlienBiomeProviderList.get("TITAN") == null) {
                this.Titan_provider = new TitanBiomeProvider();
            }
        }catch (Exception e){e.printStackTrace();}
    }


    int getHeight(int x, int z) {
        // find max height
        double startHeight = TITAN_generator.noise(x, z, 0.5, 0.5, true);
//        startHeight = compressPlanB(startHeight);
        return (int) (55 + 30 * (startHeight * startHeight));
    }

    int SimpleAlienWorld_getHeight(int x, int z) {

        if (this.SimpleAlienWorld_generator == null) {
            this.SimpleAlienWorld_generator = new Paper1181SimplexOctaveGenerator(new Random(worldSeed), getOctaves());
            this.SimpleAlienWorld_generator.setScale(getScale());
        }

        if (this.craterDepthNoise.get() != -1 && this.craterSettings == null) {
            craterSettings = getCraterSettings();
            if (this.craterSettings != null) {
                this.craterDepthNoise.set(1 - this.craterSettings.noiseDepth());
            } else {
                this.craterDepthNoise.set(-1);
            }
        }

        if (this.craterGenerator == null && this.craterSettings != null) {
            this.craterGenerator = new Paper1181SimplexOctaveGenerator(worldSeed, this.craterSettings.octaves());
            this.craterGenerator.setScale(this.craterSettings.scale());
        }
        double noise = SimpleAlienWorld_generator.noise(x, z, getFrequency(), getAmplitude(), true);
//        noise += 1;
//        noise = compressMethod(noise);

        if (this.craterDepthNoise.get() != -1) {
            double craterNoise = craterGenerator.noise(x, z, this.craterSettings.frequency(), this.craterSettings.amplitude(), true);
            craterNoise += this.craterDepthNoise.get();
            if (craterNoise < 0) {
                if(noise > 0){
                    noise += craterNoise;
                }
            }

        }


        if (smoothenTerrain()) {
            noise *= noise;
        }


        // find max height
        double temp = getAverageHeight() + getMaxDeviation() * noise *gainFactor;
        return temp >= 0 ? (int) temp : (int) temp - 1;

    }

    protected double getFrequency() {
        return switch (PlanetName_){

            case "IO" , "VENUS" -> .3;
            default -> 0.5;
        };
    }
    protected double getAmplitude() {
        return 0.5;
    }
    protected boolean smoothenTerrain() {
        return false;
    }
    protected int getAverageHeight() {

        return switch (PlanetName_){
            case "THE_MOON" -> 50;
            case "IO" -> 80;
            case "VENUS" -> 100;
            default -> 75;
        };
    }
    protected int getMaxDeviation() {
        return switch (PlanetName_){
            case "IO" -> 45;
            case "VENUS" -> 50;
            default -> 35;
        };
    }
    @Nullable
    protected CraterSettings getCraterSettings() {
        if (this.PlanetName_.equals("THE_MOON")){
            return CraterSettings.DEFAULT;
        }
        else {
            return null;
        }
    }
    protected double getScale() {
        return switch(PlanetName_) {
            case "IO", "VENUS" -> .01;
            default -> 0.005;
        };
    }
    protected int getOctaves() {
        return 8;
    }

    protected void generateSurface(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Random random, @Nonnull WorldInfo world, int chunkX, int chunkZ, String worldName) {
//        chunkX %= 16;
//        chunkZ %= 16;
        try{
            if (worldName.equals("TITAN")) {
                Titan_init(world);
                int realX = chunkX << 4;
                int realZ = chunkZ << 4;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {

                        int height = getHeight(realX + x, realZ + z);
                        Biome biome = Titan_provider.getBiome(world, realX + x, 5, realZ + z);

                        Material material = height < 57 ? Material.BLUE_ICE : switch (biome) {
                            case FOREST -> random.nextBoolean() ? Material.WARPED_NYLIUM : Material.CRIMSON_NYLIUM;
                            case SNOWY_TAIGA -> Material.WARPED_NYLIUM;
                            case DESERT -> Material.SAND;
                            case ICE_SPIKES -> Material.PACKED_ICE;
                            case DARK_FOREST, BIRCH_FOREST -> Material.COAL_BLOCK;
                            default -> Material.AIR;
                        };

                        chunk.setBlock(x, height, z, material);

                        // carbon forest/frozen carbon forest
                        if (height > 56) {
                            if (biome == TitanBiome.CARBON_FOREST.biome()) {
                                if (random.nextDouble() < 0.1) {
                                    for (int y = height + random.nextInt(4); y > height; y--) {
                                        chunk.setBlock(x, y, z, Material.COAL_BLOCK);
                                    }
                                }
                            } else if (biome == TitanBiome.FROZEN_CARBON_FOREST.biome()) {
                                if (random.nextDouble() < 0.1) {
                                    for (int y = height + random.nextInt(4); y > height; y--) {
                                        if (random.nextBoolean()) {
                                            chunk.setBlock(x, y, z, Material.PACKED_ICE);
                                        } else {
                                            chunk.setBlock(x, y, z, Material.COAL_BLOCK);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (worldName.equals("Enceladus".toUpperCase()) || worldName.equals("Europa".toUpperCase())) {
                apportionLayers(name);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int y = 1;//Bedrock Layer + 1
                        IntIterator heights = this.FlatWorld_bottom.keySet().intIterator();
                        while (heights.hasNext() && y < world.getMaxHeight()) {
                            int height = heights.nextInt();
                            Material material = this.FlatWorld_bottom.get(height);
                            while (y <= height) {
                                chunk.setBlock(x, y, z, material);
                                y++;
                            }
                        }
                    }
                }
            }
            else {
                ObjectIntPair<Material> top = getTop(worldName);
                if (top != null) {
                    Material material = top.left();
                    int height = top.rightInt();
                    int realX = chunkX << 4;
                    int realZ = chunkZ << 4;
                    for (int x = 0;  x < 16; x++) {
                        for (int z = 0; z < 16; z++) {

                            int topY = SimpleAlienWorld_getHeight(realX + x, realZ + z);

                            for (int y = topY; y > topY - height; y--) {
                                chunk.setBlock(x, y, z, material);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();};
    }

    @Nullable
    protected ObjectIntPair<Material> getTop(String PlanetName) {
        if (Objects.equals(PlanetName, "MARS")){
            return new ObjectIntImmutablePair<>(Material.RED_SAND, 4);
        } else if (Objects.equals(PlanetName, "THE_MOON")) {
            return new ObjectIntImmutablePair<>(Material.LIGHT_GRAY_CONCRETE_POWDER, 2);
        } else{return null;}
    }


    protected static record CraterSettings(int octaves, double scale, double frequency, double amplitude, double noiseDepth) {

        public static CraterSettings DEFAULT = new CraterSettings(3, 0.01, 0.5,
                0.1, 0.35);

        // sadly we have to do this as protected will not give accessibility to subclasses
        public CraterSettings {}
    }

//    @Deprecated
//    protected void generateChunk(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull ChunkGenerator.BiomeGrid grid,
//                                 @Nonnull Random random, @Nonnull World world, int chunkX, int chunkZ) {
//    }

    protected void generateChunk(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Random random,
                                 @Nonnull WorldInfo world, int chunkX, int chunkZ,String worldName) {

        try{
            switch (worldName) {
                case "TITAN" -> TITAN_generateChunk(chunk, random, world, chunkX, chunkZ);
                case "ENCELADUS", "EUROPA" ->
                        FLATWORLD_generateChunk(chunk, random, world, chunkX, chunkZ, PlanetName_);
                case "EARTH_ORBIT" -> EARTH_ORBIT_generateChunk(chunk, random, world, chunkX, chunkZ);
                default -> SIMPLE_ALIEN_WORLD_generateChunk(chunk, random, chunkX, chunkZ, PlanetName_);
            }
        }catch (Exception e){e.printStackTrace();}
    }

    @Nonnull
    @Override
    public List<BlockPopulator> getDefaultPopulators(@Nonnull World world) {
        List<BlockPopulator> list = new ArrayList<>(1);
        list = getPopulators(list);
//        if (getSetting("generate-fallen-satellites", Boolean.class, true)) {
//            list.add(new FallenSatellitePopulator(0.5));
//        }
        list.add(new FallenSatellitePopulator(0.5));
        return list;
    }


    protected List<BlockPopulator> getPopulators(@Nonnull List<BlockPopulator> populators) {
        switch (PlanetName_){
            case "VENUS" -> {
                populators.add(new VolcanoPopulator(117, Material.MAGMA_BLOCK, Material.LAVA, 130));//may cause StackOverFlow
                populators.add(new LakePopulator(75, Material.LAVA, 50));//may cause StackOverFlow
            }
            case "MARS" -> {
                populators.add(new BlockPopulator() {
                    @Override
                    public void populate(@Nonnull World worldInfo, @Nonnull Random random, @Nonnull Chunk region) {
                        if (random.nextInt(100) < 1) {
                            int x = random.nextInt(16);
                            int z = random.nextInt(16);
//getMaxHeight -> 255
                            for (int y = 0; y < 255; y++) {
                                if (region.getBlock(x, y, z).getType() == Material.AIR) {
                                    region.getBlock(x, y, z).setType(Material.ANCIENT_DEBRIS);
//                                    setSlimefunBlock(region,new Location(null, x, y, z), );
                                    break;
                                }
                            }
                        }
                    }
                });
                populators.add(new BlockPopulator() {
                    @Override
                    public void populate(@Nonnull World worldInfo, @Nonnull Random random,@Nonnull Chunk region) {
                        if (random.nextDouble() < 0.5) {

                            int x = random.nextInt(16) + (region.getX() << 4);
                            int z = random.nextInt(16) + (region.getZ() << 4);
                            int y = random.nextInt(30) + 1;

                            GenUtils.generateSquare(
                                    region,
                                    new Location(null, x, y, z),
                                    Material.PACKED_ICE,
                                    random.nextInt(4) + 1
                            );
                        }
                    }
                });
            }
            case "TITAN" ->{populators.add(new OrePopulator(
                    1,
                    50,
                    1,
                    40,
                    2,
                    6,
                    BaseMats.LASERITE_ORE,
                    Material.STONE, Material.COAL_ORE
            ));
//                populators.add(new LakePopulator(58, Material.WATER, 20));
                populators.add(new LakePopulator(40, Material.WATER, 20));
                populators.add(new BlockPopulator() {

                    @Override
                    public void populate(@Nonnull World worldInfo, @Nonnull Random random,@Nonnull Chunk region) {
                        int cx = region.getX();
                        int cz = region.getZ();
                        int xrand = random.nextInt(16);
                        int zrand = random.nextInt(16);
                        if (random.nextDouble() < 0.25) {
                            int x = (cx << 4) + xrand;
                            int z = (cz << 4) + zrand;

                            if (Titan_provider.getBiome(null,x, z).biome() == TitanBiome.FROZEN_FOREST.biome() && region.getBlock(xrand, 1, zrand).getType() == Material.WARPED_NYLIUM) {
                                Location l = Util.getHighestBlockAt(region, x, z).add(0, 1, 0);
                                worldInfo.generateTree(l, TreeType.WARPED_FUNGUS);
                            }
                        }
                    }
                });
                populators.add(new BlockPopulator() {
                    @Override
                    public void populate(@Nonnull World worldInfo, @Nonnull Random random, @Nonnull Chunk region) {
                        int amount = random.nextInt(2) + 1;
                        int cx = region.getX();
                        int cz = region.getZ();
                        for (int i = 0; i < amount; i++) {
                            int x = (cx << 4) + random.nextInt(16);
                            int z = (cz << 4) + random.nextInt(16);

                    if (Titan_provider.getBiome(null, x, 1, z) == TitanBiome.FOREST.biome()) {
                        Location l = Util.getHighestBlockAt(region, x, z);
                        int bx = x % 16;
                        int bz = z % 16;
                        if (bx < 0){bx += 16;}
                        if (bz < 0){bz += 16;}
                        if (region.getBlock(bx, l.getBlockY(), bz).getType() == Material.WARPED_NYLIUM) {
                            worldInfo.generateTree(l.add(0, 1, 0),TreeType.WARPED_FUNGUS);
                        } else if (region.getBlock(bx, l.getBlockY(), bz).getType() == Material.CRIMSON_NYLIUM) {
                            worldInfo.generateTree(l.add(0, 1, 0),TreeType.CRIMSON_FUNGUS);
                        }
                    }

                            if (Titan_provider.getBiome(null,1, z).biome() == TitanBiome.FOREST.biome()) {
                                Location l = Util.getHighestBlockAt(region, x, z);
                                int bx = x % 16;
                                int bz = z % 16;
                                if (bx < 0){bx += 16;}
                                if (bz < 0){bz += 16;}
                                if (region.getBlock(bx, l.getBlockY(), bz).getType() == Material.WARPED_NYLIUM) {
                                    worldInfo.generateTree(l.add(0, 1, 0), TreeType.WARPED_FUNGUS);
//                            region.generateTree(l.add(0, 1, 0), random, TreeType.WARPED_FUNGUS);
                                } else if (region.getBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ()).getType() == Material.CRIMSON_NYLIUM) {
                                    worldInfo.generateTree(l.add(0, 1, 0), TreeType.CRIMSON_FUNGUS);
                                }
                            }
                        }
                    }
                });
            }
            case "IO" -> {
                populators.add(new VolcanoPopulator(115, Material.MAGMA_BLOCK, Material.LAVA, 125));//may cause StackOverFlow
                populators.add(new LakePopulator(60, Material.LAVA, 35));//may cause StackOverFlow
            }
            default -> {}
        }
        return populators;
    }


    public List<BlockPopulator> EARTH_ORBIT_getPopulators(@Nonnull List<BlockPopulator> populators) {
        populators.add(new BlockPopulator() {
            @Override
            public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk region)
            {

                if (random.nextInt(10) == 0) {
                    int x = random.nextInt(2) + 7;
                    int y = random.nextInt(224) + 16;
                    int z = random.nextInt(2) + 7;

                    int regionX = region.getX();
                    int regionZ = region.getZ();
//                    regionX = regionX % 16;
//                    regionZ = regionZ % 16;
//                    if (regionX < 0){
//                        regionX += 16;
//                    }
//                    if (regionZ < 0) {
//                        regionZ += 16;
//                    }
                    x = (regionX << 4) + x;
                    z = (regionZ << 4) + z;

                    x = x % 16;
                    z = z % 16;
                    if (x < 0){
                        x += 16;
                    }
                    if (z < 0) {
                        z += 16;
                    }
                    Location l = new Location(region.getWorld(),x,y,z);
                    switch (random.nextInt(3)) {
                        case 0 -> ORBIT_asteroid.generate(l, region, 5, 2);
                        case 1 -> ORBIT_comet.generate(l, region, 5, 2);
                        case 2 -> region.getBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ()).setType(Material.IRON_BLOCK);
                        case 3 -> region.getBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ()).setType(Material.IRON_BLOCK);
                    }
                }
            }
        });
        return populators;
    }
//    public <T> T getSetting(@Nonnull String path, @Nonnull Class<T> clazz, T defaultValue) {
//        return Galactifun.worldManager().getSetting(this, path, clazz, defaultValue);
//    }


    private void apportionLayers(String name) {
        try{
            if (FlatWorld_top == null) {
                Int2ObjectSortedMap<Material> layers = switch (name) {
                    case "ENCELADUS" -> Enceladus_getLayers();
                    case "EUROPA" -> Europa_getLayers();
                    default -> Default_getLayers();
                };

                Int2ObjectSortedMap<Material> newTop = new Int2ObjectLinkedOpenHashMap<>();
                IntIterator iter = layers.keySet().intIterator();

                iter.skip(layers.size() / 2 + 1);
                iter.forEachRemaining(i -> newTop.put(i, layers.get(i)));

                this.FlatWorld_top = Int2ObjectSortedMaps.unmodifiable(newTop);
            }
            if (FlatWorld_bottom == null) {
                Int2ObjectSortedMap<Material> layers = switch (name) {
                    case "ENCELADUS" -> Enceladus_getLayers();
                    case "EUROPA" -> Europa_getLayers();
                    default -> Default_getLayers();
                };

                Int2ObjectSortedMap<Material> newBottom = new Int2ObjectLinkedOpenHashMap<>();
                IntIterator iter = layers.keySet().intIterator();

                int amount = layers.size() / 2 + 1;
                for (int i = 0; i < amount; i++) {
                    int layer = iter.nextInt();
                    newBottom.put(layer, layers.get(layer));
                }

                this.FlatWorld_bottom = Int2ObjectSortedMaps.unmodifiable(newBottom);
            }
        }catch (Exception e){e.printStackTrace();}
    }


    @Nonnull
    protected Int2ObjectSortedMap<Material> Enceladus_getLayers() {
        return new Int2ObjectLinkedOpenHashMap<>() {{
            put(30, Material.PACKED_ICE);
            put(60, Material.BLUE_ICE);
        }};
    }

    protected Int2ObjectSortedMap<Material> Europa_getLayers() {
        // double brace init go brr
        return new Int2ObjectLinkedOpenHashMap<>() {{
            put(30, Material.PACKED_ICE);
            put(60, Material.ICE);
        }};
    }

    protected Int2ObjectSortedMap<Material> Default_getLayers() {
        // double brace init go brr
        return new Int2ObjectLinkedOpenHashMap<>() {{
            put(30, Material.AIR);
            put(60, Material.AIR);
        }};
    }


    protected void TITAN_generateChunk(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Random random, @Nonnull WorldInfo world, int chunkX, int chunkZ) {
        try{
            Titan_init(world);
            for (int x = 0, realX = chunkX << 4; x < 16; x++, realX++) {
                for (int z = 0, realZ = chunkZ << 4; z < 16; z++, realZ++) {

                    int height = getHeight(realX, realZ);

                    for (int y = 1; y < height; y++) {
                        if (random.nextBoolean()) {
                            chunk.setBlock(x, y, z, Material.STONE);
                        } else {
                            chunk.setBlock(x, y, z, Material.COAL_ORE);
                        }
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }

    protected final void FLATWORLD_generateChunk(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Random random, @Nonnull WorldInfo world, int chunkX, int chunkZ, String worldName) {
        try{
            apportionLayers(worldName);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int y = 1;
                    IntIterator heights = this.FlatWorld_top.keySet().intIterator();
                    while (heights.hasNext() && y < world.getMaxHeight()) {
                        int height = heights.nextInt();
                        Material material = this.FlatWorld_top.get(height);
                        while (y <= height) {
                            chunk.setBlock(x, y, z, material);
                            y++;
                        }
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }

    void EARTH_ORBIT_generateChunk(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Random random, @Nonnull WorldInfo world, int chunkX, int chunkZ) {
        // nop
    }

    protected final void SIMPLE_ALIEN_WORLD_generateChunk(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Random random, int chunkX, int chunkZ, String worldName) {

        try{
            ObjectIntPair<Material> top = getTop(worldName);
            int heightSub = top == null ? 0 : top.rightInt();

            for (int x = 0, realX = chunkX << 4; x < 16; x++, realX++) {
                for (int z = 0, realZ = chunkZ << 4; z < 16; z++, realZ++) {
                    int height = SimpleAlienWorld_getHeight(realX, realZ) - heightSub;
                    // y = 1 to height, generate
                    int y = 1;
                    while (y <= height) {
                        chunk.setBlock(x, y++, z, SIMPLE_ALIEN_WORLD_generateMaterial(random, x, y, z, height, worldName));
                    }
                    // more
                    SIMPLE_ALIEN_WORLD_generateMore(chunk, SimpleAlienWorld_generator, random, realX, realZ, x, z, height, worldName);
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }

    @Nonnull
    protected Material SIMPLE_ALIEN_WORLD_generateMaterial(@Nonnull Random random, int x, int y, int z, int top,String PlanetName){
        switch (PlanetName){
            case "IO" -> {
                return IO_generateMaterial(random, x, y, z, top);
            }
            case "MARS" -> {
                return MARS_generateMaterial(random, x, y, z, top);
            }
            case "THE_MOON" -> {
                return THEMOON_generateMaterial(random, x, y, z, top);
            }
            case "VENUS" -> {
                return VENUS_generateMaterial(random, x, y, z, top);
            }
            default -> {
                return Material.AIR;
            }
        }
    };

    protected void SIMPLE_ALIEN_WORLD_generateMore(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Paper1181SimplexOctaveGenerator generator,
                                @Nonnull Random random, int realX, int realZ, int x, int z, int height, String PlanetName) {
            if (Objects.equals(PlanetName, "MARS")){
                MARS_generateMore(chunk, generator, random, realX, realZ, x, z, height);
            }
    }

    protected void MARS_generateMore(@Nonnull ChunkGenerator.ChunkData chunk, @Nonnull Paper1181SimplexOctaveGenerator generator,
                                @Nonnull Random random, int realX, int realZ, int x, int z, int height) {
        // generate caves
        for (int y = 1; y <= height - 16; y++) {
            double density = generator.noise(realX, y, realZ, getFrequency(), getAmplitude(), true);

            // Choose a narrow selection of blocks
            if (Math.abs(density) < 0.03) {
                chunk.setBlock(x, y, z, Material.CAVE_AIR);
            }
        }
    }

    protected Material IO_generateMaterial(@Nonnull Random random, int x, int y, int z, int top) {
        if (y > 75) {
            return Material.BLACKSTONE;
        } else {
            return Material.YELLOW_TERRACOTTA;
        }
    }

    protected Material MARS_generateMaterial(@Nonnull Random random, int x, int y, int z, int top) {
        if (random.nextDouble() <= 0.1 && y <= 15) {
            // 10% of blocks under y 15 are iron ore
            return Material.IRON_ORE;
        }
        // 90% of blocks are terracotta
        return Material.TERRACOTTA;
    }

    protected Material THEMOON_generateMaterial(@Nonnull Random random, int x, int y, int z, int top) {
        if (random.nextFloat() > .02) {
            return Material.ANDESITE;
        } else {
            return Material.GOLD_ORE;
        }
    }

    protected Material VENUS_generateMaterial(@Nonnull Random random, int x, int y, int z, int top) {
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


//    protected void setSlimefunBlock(Chunk region, Location l, SlimefunItemStack item) {
//
//        int x = l.getBlockX();
//        int z = l.getBlockZ();
//        x = x%16;
//        z = z%16;
//        if (x < 0){
//            x += 16;
//        }
//        if (z < 0){
//            z += 16;
//        }
//        region.getBlock(x, l.getBlockY(), z).setType(item.getType());
//        Location copy = l.clone();
//        copy.setWorld(region.getWorld());
//        Scheduler.run(() -> BlockStorage.addBlockInfo(copy, "id", item.getItemId()));
//    }
}
