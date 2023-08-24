package io.github.addoncommunity.galactifun.base.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.migrator.BlockStorageMigrator;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

import io.github.thebusybiscuit.slimefun4.core.services.BlockDataService;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.destroystokyo.paper.event.player.PlayerTeleportEndGatewayEvent;
import io.github.addoncommunity.galactifun.Galactifun;
import io.github.addoncommunity.galactifun.api.worlds.AlienWorld;
import io.github.addoncommunity.galactifun.base.BaseItems;
import io.github.addoncommunity.galactifun.util.BSUtils;
import io.github.mooy1.infinitylib.common.Events;
import io.github.mooy1.infinitylib.common.Scheduler;
import io.github.mooy1.infinitylib.machines.MenuBlock;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

// TODO clean up if possible
public final class StargateController extends SlimefunItem implements Listener {

    private static final int[] BACKGROUND = new int[] { 1, 2, 6, 7, 8 };
    private static final int ADDRESS_SLOT = 3;
    private static final int DESTINATION_SLOT = 4;
    private static final int DEACTIVATE_SLOT = 5;
    private static final int SET_ADDRESS_SLOT = 0;

    private static String address = "";

    private static final ComponentPosition[] RING_POSITIONS = new ComponentPosition[] {
            // bottom
            new ComponentPosition(0, 1),
            new ComponentPosition(0, -1),

            // corners
            new ComponentPosition(1, -2),
            new ComponentPosition(1, 2),
            new ComponentPosition(5, -2),
            new ComponentPosition(5, 2),

            // left side
            new ComponentPosition(2, 3),
            new ComponentPosition(3, 3),
            new ComponentPosition(4, 3),

            // right side
            new ComponentPosition(2, -3),
            new ComponentPosition(3, -3),
            new ComponentPosition(4, -3),

            // top
            new ComponentPosition(6, -1),
            new ComponentPosition(6, 0),
            new ComponentPosition(6, 1),
    };

    private static final ComponentPosition[] PORTAL_POSITIONS;
    private static final int GATEWAY_TICKS = 201;

    static {
        List<ComponentPosition> portalPositions = new LinkedList<>(Arrays.asList(
                new ComponentPosition(1, -1),
                new ComponentPosition(1, 0),
                new ComponentPosition(1, 1)
        ));
        for (int y = 2; y <= 4; y++) {
            for (int z = -2; z <= 2; z++) {
                portalPositions.add(new ComponentPosition(y, z));
            }
        }
        portalPositions.add(new ComponentPosition(5, -1));
        portalPositions.add(new ComponentPosition(5, 0));
        portalPositions.add(new ComponentPosition(5, 1));

        PORTAL_POSITIONS = portalPositions.toArray(new ComponentPosition[0]);
    }

    public StargateController(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {

        super(category, item, recipeType, recipe);

        Events.registerListener(this);

        addItemHandler((BlockUseHandler) e -> e.getClickedBlock().ifPresent(b -> onUse(e, e.getPlayer(), b)));

        addItemHandler(new BlockBreakHandler(true, true) {
            @Override
            @ParametersAreNonnullByDefault
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                if (Boolean.parseBoolean(
                        StorageCacheUtils.getData(e.getBlock().getLocation(), "locked")
//                        BlockStorage.getLocationInfo(e.getBlock().getLocation(), "locked")
                )) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "在摧毁星门之前先关闭它");
                }
            }
        });
    }

    public static boolean isPartOfStargate(@Nonnull Block b) {
        for (ComponentPosition position : RING_POSITIONS) {
            if (!position.isInSameRing(b)) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    public static Optional<List<Block>> getRingBlocks(@Nonnull Block b) {
        List<Block> rings = new ArrayList<>();
        for (ComponentPosition position : RING_POSITIONS) {
            if (position.isInSameRing(b)) {
                rings.add(position.getBlock(b));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(rings);
    }

    @Nonnull
    public static Optional<List<Block>> getPortalBlocks(@Nonnull Block b) {
        List<Block> portals = new ArrayList<>();
        for (ComponentPosition position : PORTAL_POSITIONS) {
            if (position.isPortal(b)) {
                portals.add(position.getBlock(b));
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(portals);
    }

    public static void lockBlocks(Block controller, boolean lock) {
        String data = Boolean.toString(lock);
        getRingBlocks(controller).ifPresent(l -> l.forEach(b ->
//                BlockStorage.addBlockInfo(b, "locked", data)
                StorageCacheUtils.setData(b.getLocation(), "locked", data)
        ));
        getPortalBlocks(controller).ifPresent(l -> l.forEach(b ->
//                BlockStorage.addBlockInfo(b, "locked", data)
                StorageCacheUtils.setData(b.getLocation(), "locked", data)
        ));
    }

    private void onUse(PlayerRightClickEvent event, Player p, Block b) {
        if (!isPartOfStargate(b)) {
            p.sendMessage(ChatColor.RED + "未形成星门!");
            return;
        }
        event.cancel();
        if (getPortalBlocks(b).isEmpty()) {
            for (ComponentPosition position : PORTAL_POSITIONS) {
                Block portal = position.getBlock(b);
                portal.setType(Material.END_GATEWAY);
                EndGateway gateway = (EndGateway) portal.getState();
                gateway.setAge(GATEWAY_TICKS);
                gateway.setExitLocation(b.getLocation());
                gateway.update(false, false);
                Slimefun.getDatabaseManager().getBlockDataController().createBlock(gateway.getLocation(), "GFSGPORTAL");//应付数据库的面子工程
                BlockStorage.addBlockInfo(portal, "StarGate", "T");
//                StorageCacheUtils.getBlock(portal.getLocation()).setData("StarGate", "T");
//                StorageCacheUtils.setData(portal.getLocation(), "StarGate", "T");
//                Galactifun.instance().getLogger().log(Level.WARNING, "addingPortalInfo");
//                StorageCacheUtils.requestLoad(StorageCacheUtils.getBlock(gateway.getLocation()));
            }

            String destAddress =

                    BlockStorage.getLocationInfo(b.getLocation(), "destination")
//                    StorageCacheUtils.getData(b.getLocation(), "destination")
                    ;
            if (destAddress != null) {
                setDestination(destAddress, b, p);
            }

            lockBlocks(b, true);
            p.sendMessage(ChatColor.YELLOW + "星门已激活!");
            return;
        }

        ChestMenu menu = getMenu(b);
        menu.open(p);
    }

    @Nonnull
    private ChestMenu getMenu(@Nonnull Block b) {
        ChestMenu menu = new ChestMenu(this.getItemName());
        for (int i : BACKGROUND) {
            menu.addItem(i, MenuBlock.BACKGROUND_ITEM, ChestMenuUtils.getEmptyClickHandler());
        }

        String address = BlockStorage.getLocationInfo(b.getLocation(), "gfsgAddress");
//        address = temp;
        if (address == null || address.equals("")){
            address = String.valueOf((new Random().nextInt(10000)));
//            StorageCacheUtils.setData(b.getLocation(), "gfsgAddress", address);
            BlockStorage.addBlockInfo(b.getLocation(), "gfsgAddress", address);
        }
        String temp = address;
        Location l = b.getLocation();

//        String address =
////                BlockStorage.getLocationInfo(l, "gfsgAddress");
//        StorageCacheUtils.getData(l, "gfsgAddress");
//        if (address == null) {
//            String lString = String.format(
//                    "%s-%d-%d-%d",
//                    b.getWorld().getName(),
//                    l.getBlockX(),
//                    l.getBlockY(),
//                    l.getBlockZ()
//            );
//            address = Integer.toHexString(lString.hashCode());
////            BlockStorage.addBlockInfo(b, "gfsgAddress", address);
//            StorageCacheUtils.setData(b.getLocation(), "gfsgAddress", address);
//        }

        String destination =
                BlockStorage.getLocationInfo(l, "destination");
//            StorageCacheUtils.getData(l, "destination");
        destination = destination == null ? "" : destination;

        menu.addItem(ADDRESS_SLOT, new CustomItemStack(
                Material.BOOK,
                "&f密码: " + address,
                "&7点击以获取星门密码及坐标"
        ), (p, i, s, c) -> {
            p.sendMessage(ChatColor.YELLOW + "世界: " + b.getWorld().getName() + "坐标: [X: " + b.getLocation().getBlockX() + "|Y: " + b.getLocation().getBlockY() + "|Z: "+ b.getLocation().getBlockZ() + "] 密码: " + temp);
            p.closeInventory();
            return false;
        });

        menu.addItem(SET_ADDRESS_SLOT, new CustomItemStack(
                Material.CHEST,
                "&f密码: " + address,
                "&7点击以设置星门密码"
                ),(p, i, s, c) -> {
            p.sendMessage(ChatColor.YELLOW + "输入星门密码");
            ChatUtils.awaitInput(p, st -> setPassword(st, b, p));
            p.closeInventory();
            return false;
        });
        menu.addItem(DEACTIVATE_SLOT, new CustomItemStack(
                Material.BARRIER,
                "&f点击以关闭星门"
        ), (p, i, s, c) -> {
            getPortalBlocks(b).ifPresent(li -> {
                for (Block block : li) {
                    block.setType(Material.AIR);
                    BlockStorage.clearBlockInfo(block);
//                    Slimefun.getDatabaseManager().getBlockDataController().removeBlock(block.getLocation());
                }
            });
            lockBlocks(b, false);
            p.closeInventory();
            return false;
        });

        menu.addItem(DESTINATION_SLOT, new CustomItemStack(
                Material.RAIL,
                "&f点击以设置目标星门密码",
                "&7当前目标星门密码: " + destination
        ), (p, i, s, c) -> {
            p.sendMessage(ChatColor.YELLOW + "输入星门密码, 格式: 世界|X|Y|Z|密码");
            ChatUtils.awaitInput(p, st -> setDestination(st, b, p));
            p.closeInventory();
            return false;
        });

        return menu;
    }

    private static void setDestination(String destination, Block b, Player p) {

        String[] DestInfo = destination.split("\\|");
        if(DestInfo.length != 5){
            p.sendMessage("由 | 分割的字符串数量小于5, 目的地信息不符合格式,请重新输入");
            return;
        }

        String DestWorld = DestInfo[0];
        List<String> worldnames = new ArrayList<>();
        for(World world:Bukkit.getWorlds()){
            worldnames.add(world.getName());
        }
        if(!worldnames.contains(DestWorld)){
            p.sendMessage("未找到目的地世界,请重新输入");
            return;
        }

        String DestXCoordsStr = DestInfo[1];
        int DestXCoords;
        String DestYCoordsStr = DestInfo[2];
        int DestYCoords;
        String DestZCoordsStr = DestInfo[3];
        int DestZCoords;
        try {
            DestXCoords = Integer.parseInt(DestXCoordsStr);
            DestYCoords = Integer.parseInt(DestYCoordsStr);
            DestZCoords = Integer.parseInt(DestZCoordsStr);
        }
        catch (NumberFormatException e) {p.sendMessage("坐标格式错误,请重新输入");return;}
        String address = DestInfo[4];

        Location Dest = new Location(Bukkit.getWorld(DestWorld), DestXCoords, DestYCoords, DestZCoords);

//        Config destBlockData =
////                StorageCacheUtils.getBlock(Dest);
//        BlockStorage.getLocationInfo(Dest);
        if (BlockStorage.getLocationInfo(Dest) == null)
        {
            p.sendMessage("未找到目标星门");return;
        }
        if (BlockStorage.getLocationInfo(Dest, "gfsgAddress") == null){p.sendMessage("未找到目标星门");return;}
        if (!Objects.equals(String.valueOf(BlockStorage.getLocationInfo(Dest, "gfsgAddress")), address)){p.sendMessage("密码错误,请重新输入");return;}

//        Location dest;
//        worldLoop:
//        {
//            for (
//                    BlockStorage storage :
//                    Slimefun.getRegistry().getWorlds().values()
//            ) {
//                for (
//                        Map.Entry<Location, Config> configEntry : storage.getRawStorage().entrySet()
//                ) {
//                    String bAddress = configEntry.getValue().getString("gfsgAddress");
//                    if (bAddress != null && bAddress.equals(destination)) {
//                        dest = configEntry.getKey();
//                        break worldLoop;
//                    }
//                }
//            }
//            p.sendMessage(ChatColor.RED + "未找到密码对应星门!");
//            return;
//        }

        Optional<List<Block>> portalOptional = getPortalBlocks(b);
        if (portalOptional.isEmpty()) {
            p.sendMessage(ChatColor.RED + "星门因为某种原因没有启动(框架不在X方向上或框架不完整)");
            return;
        }

        BSUtils.setStoredLocation(b.getLocation(), "dest",Dest);

        p.sendMessage(ChatColor.YELLOW + String.format(
                "目标星门已设置为:%s (世界) %d %d %d ",
//                dest.getBlockX(),
//                dest.getBlockY(),
//                dest.getBlockZ(),
//                dest.getWorld().getName()
                DestWorld,
                DestXCoords,
                DestYCoords,
                DestZCoords
        ));

        BlockStorage.addBlockInfo(b, "destination", destination);
//        StorageCacheUtils.setData(b.getLocation(), "destination", destination);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onGateBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getType() == Material.END_GATEWAY &&
                Boolean.parseBoolean(
//                        BlockStorage.getLocationInfo(b.getLocation(), "locked")
                        StorageCacheUtils.getData(b.getLocation(), "locked")
                )) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "在摧毁星门之前先关闭它");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onUsePortal(PlayerTeleportEndGatewayEvent e) {

//        Galactifun.instance().getLogger().log(Level.WARNING, "teleporting");

//        Galactifun.instance().getLogger().log(Level.WARNING, "StarGate:" + BlockStorage.getLocationInfo(e.getGateway().getLocation(), "StarGate"));
        if (
                !(Objects.equals(BlockStorage.getLocationInfo(e.getGateway().getLocation(), "StarGate"), "T"))
        ) {

            return;
        }

        Location dest = BSUtils.getStoredLocation(e.getGateway().getExitLocation(), "dest");

//        Galactifun.instance().getLogger().log(Level.WARNING, "teleporting" + dest);

        if (dest == null) return;

        e.setCancelled(true);

        Player p = e.getPlayer();
        if (p.hasMetadata("disableStargate")) return;

        Block b = dest.getBlock();
        if (BlockStorage.check(b, BaseItems.STARGATE_CONTROLLER.getItemId()) &&
                StargateController.getPortalBlocks(b).isEmpty()) {
            e.getPlayer().sendMessage(ChatColor.RED + "目标星门没有启动");
            return;
        }

        Block destBlock = b.getRelative(1, 0, 0);
        if (destBlock.getType().isEmpty()) {
            // Check if the player is teleporting to an alien world, and if so, allow them to
            AlienWorld world = Galactifun.worldManager().getAlienWorld(destBlock.getWorld());
            if (world != null) {
                e.getPlayer().setMetadata("CanTpAlienWorld", new FixedMetadataValue(Galactifun.instance(), true));
            }
            PaperLib.teleportAsync(e.getPlayer(), destBlock.getLocation());
//            Galactifun.instance().getLogger().log(Level.WARNING, "teleporting" + dest);
            p.setMetadata("disableStargate", new FixedMetadataValue(Galactifun.instance(), true));
            Scheduler.run(10, () -> p.removeMetadata("disableStargate", Galactifun.instance()));
        } else {
            e.getPlayer().sendMessage(ChatColor.RED + "目的地被阻挡");
        }
    }

    private static final record ComponentPosition(int y, int z) {

        public boolean isInSameRing(@Nonnull Block b) {
            return BlockStorage.check(b.getRelative(0, this.y, this.z)) instanceof StargateRing;
        }

        @Nonnull
        public Block getBlock(@Nonnull Block b) {
            return b.getRelative(0, this.y, this.z);
        }

        public boolean isPortal(@Nonnull Block b) {
            return b.getRelative(0, this.y, this.z).getType() == Material.END_GATEWAY;
        }

    }

    private void setPassword(String password, Block block, Player player){
        if (password.contains("|")){player.sendMessage("密码不应含有 | 符号" );return;}
        if (password.equals("")){player.sendMessage("密码不能为空");return;}
        if (password.endsWith(" ")){player.sendMessage("警告:不推荐的密码(末尾为空格)");}
//        StorageCacheUtils.setData(block.getLocation(), "gfsgAddress", password);
        BlockStorage.addBlockInfo(block.getLocation(), "gfsgAddress", password);
        address = password;
    }
}
