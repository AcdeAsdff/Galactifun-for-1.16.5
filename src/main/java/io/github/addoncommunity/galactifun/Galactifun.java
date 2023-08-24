package io.github.addoncommunity.galactifun;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.addoncommunity.galactifun.base.universe.denendencies.BiomeProvider;

import io.github.addoncommunity.galactifun.base.universe.denendencies.WorldInfo;

import io.github.addoncommunity.galactifun.base.universe.saturn.TitanBiomeProvider;
import io.github.addoncommunity.galactifun.util.GenUtils;

import lombok.Getter;

import org.bukkit.Bukkit;
//import org.bukkit.World;
//import org.bukkit.generator.ChunkGenerator;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.addoncommunity.galactifun.api.worlds.PlanetaryWorld;
import io.github.addoncommunity.galactifun.base.BaseAlien;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.base.BaseMats;
import io.github.addoncommunity.galactifun.base.BaseUniverse;
import io.github.addoncommunity.galactifun.core.CoreItemGroup;
import io.github.addoncommunity.galactifun.core.commands.AlienRemoveCommand;
import io.github.addoncommunity.galactifun.core.commands.AlienSpawnCommand;
import io.github.addoncommunity.galactifun.core.commands.EffectsCommand;
import io.github.addoncommunity.galactifun.core.commands.GalactiportCommand;
import io.github.addoncommunity.galactifun.core.commands.SealedCommand;
import io.github.addoncommunity.galactifun.core.commands.StructureCommand;
import io.github.addoncommunity.galactifun.core.managers.AlienManager;
import io.github.addoncommunity.galactifun.core.managers.ProtectionManager;
import io.github.addoncommunity.galactifun.core.managers.WorldManager;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.core.AbstractAddon;
import io.github.mooy1.infinitylib.metrics.bukkit.Metrics;
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;


public final class Galactifun extends AbstractAddon {

//    private volatile TitanBiomeProvider titan_provider = ;
    @Getter
    private static Galactifun instance;

    private boolean isTest = false;

    private AlienManager alienManager;
    private WorldManager worldManager;
    private ProtectionManager protectionManager;

    private boolean shouldDisable = false;

    public static long  worldSeed;
    public static Map<String,World> AlienWorldList = new HashMap<>();
    public static Map<String, BiomeProvider> AlienBiomeProviderList = new HashMap<>();

    public static Map<String, WorldInfo> AlienWorldInfoList = new HashMap<String, WorldInfo>();
    public Galactifun() {
        super("Slimefun-Addon-Community", "Galactifun", "master", "auto-update");
    }

    public Galactifun(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file, "Slimefun-Addon-Community", "Galactifun", "master", "auto-update");
        isTest = true;
    }

    public static AlienManager alienManager() {
        return instance.alienManager;
    }

    public static WorldManager worldManager() {
        return instance.worldManager;
    }

    public static ProtectionManager protectionManager() {
        return instance.protectionManager;
    }

    public static String spawningWorld = "";

    @Override
    protected void enable() {

        AlienBiomeProviderList.put("MARS", new GenUtils.SingleBiomeProvider(Biome.DESERT));
        AlienBiomeProviderList.put("VENUS", new GenUtils.SingleBiomeProvider(Biome.BASALT_DELTAS));
        AlienBiomeProviderList.put("EARTH_ORBIT",new GenUtils.SingleBiomeProvider(Biome.THE_VOID));
        AlienBiomeProviderList.put("THE_MOON", new GenUtils.SingleBiomeProvider(Biome.BADLANDS));
        AlienBiomeProviderList.put("EUROPA", new GenUtils.SingleBiomeProvider(Biome.FROZEN_OCEAN));
        AlienBiomeProviderList.put("IO", new GenUtils.SingleBiomeProvider(Biome.DESERT));
        AlienBiomeProviderList.put("ENCELADUS", new GenUtils.SingleBiomeProvider(Biome.FROZEN_OCEAN));
        AlienBiomeProviderList.put("TITAN", new TitanBiomeProvider());

        instance = this;

        worldSeed = Bukkit.getWorld("world").getSeed();
        if (!isTest) {
            if (!PaperLib.isPaper()) {
                log(Level.SEVERE, "Galactifun only supports Paper and its forks (i.e. Airplane and Purpur)");
                log(Level.SEVERE, "Please use Paper or a fork of Paper");
                shouldDisable = true;
            }
//            if (Slimefun.getMinecraftVersion().isBefore(MinecraftVersion.MINECRAFT_1_17)) {
//                log(Level.SEVERE, "Galactifun only supports Minecraft 1.17 and above");
//                log(Level.SEVERE, "Please use Minecraft 1.17 or above");
//                shouldDisable = true;
//            }
            if (Bukkit.getPluginManager().isPluginEnabled("ClayTech")) {
                log(Level.SEVERE, "Galactifun 不能和 ClayTech 同时运行");
                shouldDisable = true;
            }

            if (Bukkit.getPluginManager().isPluginEnabled("ChatColor2")) {
                log(Level.SEVERE, "Galactifun will not work properly with ChatColor2");
                log(Level.SEVERE, "Please disable ChatColor2");
                shouldDisable = true;
            }

//            if (shouldDisable) {
//                Bukkit.getPluginManager().disablePlugin(this);
//                return;
//            }
        }

        new Metrics(this, 11613);

        this.alienManager = new AlienManager(this);
        this.worldManager = new WorldManager(this);
        this.protectionManager = new ProtectionManager();
        try {
            BaseAlien.setup(this.alienManager);
            if (!isTest) {
                BaseUniverse.setup(this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        CoreItemGroup.setup(this);
        BaseMats.setup();
        BaseItems.setup(this);

        // log after startup
        Scheduler.run(() -> log(Level.INFO,
                "################# Galactifun " + getPluginVersion() + " #################",
                "",
                "Galactifun is open source, you can contribute or report bugs at: ",
                getBugTrackerURL(),
                "Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU",
                "",
                "###################################################"
        ));

        getAddonCommand()
                .addSub(new GalactiportCommand())
                .addSub(new AlienSpawnCommand())
                .addSub(new AlienRemoveCommand())
                .addSub(new StructureCommand(this))
                .addSub(new SealedCommand())
                .addSub(new EffectsCommand());
    }

    @Override
    protected void disable() {
        if (shouldDisable) return;

        this.alienManager.onDisable();

        // Do this last
        instance = null;
    }

    @Override
    public void load() {
        if (!isTest) {
            // Default to not logging world settings
            Bukkit.spigot().getConfig().set("world-settings.default.verbose", false);
        }
    }

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@Nonnull String worldName, @Nullable String id) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        PlanetaryWorld planetaryWorld = this.worldManager.getWorld(world);
        if (planetaryWorld instanceof AlienWorld) {
            return planetaryWorld.world().getGenerator();
        }

        return null;
    }

}
