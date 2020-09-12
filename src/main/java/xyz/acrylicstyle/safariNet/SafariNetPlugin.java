package xyz.acrylicstyle.safariNet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import util.CollectionList;
import util.ICollectionList;
import xyz.acrylicstyle.paper.nbt.NBTTagCompound;
import xyz.acrylicstyle.safariNet.utils.SafariNetType;
import xyz.acrylicstyle.safariNet.utils.SafariNetUtils;
import xyz.acrylicstyle.tomeito_api.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SafariNetPlugin extends JavaPlugin implements Listener {
    public static NamespacedKey safariNet_singleUse = null;
    public static NamespacedKey safariNet = null;
    public static SafariNetPlugin instance = null;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        safariNet_singleUse = new NamespacedKey(this, "safari_net_single_use");
        safariNet = new NamespacedKey(this, "safari_net");
        ShapedRecipe r1 = new ShapedRecipe(safariNet_singleUse, SafariNetUtils.getSafariNet(SafariNetType.SINGLE_USE));
        r1.shape("SLS", " J ", "S S");
        r1.setIngredient('S', Material.STRING);
        r1.setIngredient('L', Material.LEATHER);
        r1.setIngredient('J', Material.SLIME_BALL);
        ShapedRecipe r2 = new ShapedRecipe(safariNet, SafariNetUtils.getSafariNet(SafariNetType.RE_USABLE));
        r2.shape(" E ", "EGE", " E ");
        r2.setIngredient('E', Material.ENDER_PEARL);
        r2.setIngredient('G', Material.GHAST_TEAR);
        Bukkit.addRecipe(r1);
        Bukkit.addRecipe(r2);
    }

    @Override
    public void onDisable() {
        Bukkit.removeRecipe(safariNet); // r2
        Bukkit.removeRecipe(safariNet_singleUse); // r1
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (lock.contains(e.getPlayer().getUniqueId())) return;
        interact(e.getRightClicked(), e.getPlayer());
    }

    public static CollectionList<UUID> lock = new CollectionList<>();

    public static final List<EntityType> excludedEntities = new ArrayList<>();

    static {
        excludedEntities.add(EntityType.ENDER_DRAGON);
        excludedEntities.add(EntityType.WITHER);
    }

    private void interact(Entity clickedEntity, Player player) {
        if (!clickedEntity.getType().isSpawnable() || !clickedEntity.getType().isAlive()) return;
        if (excludedEntities.contains(clickedEntity.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SafariNetUtils.isSafariNet(item)) return;
        if (false && clickedEntity.getWorld().getName().equalsIgnoreCase("world")) {
            player.sendActionBar(ChatColor.RED + "このワールドではエンティティを捕まえられません。");
            return;
        }
        if (SafariNetUtils.isEmpty(item)) {
            lock.add(player.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    lock.remove(player.getUniqueId());
                }
            }.runTaskLater(this, 2); // wait 0.1 second
            player.getInventory().setItemInMainHand(SafariNetUtils.updateSafariNet(SafariNetUtils.store(item, clickedEntity.getType(), (NBTTagCompound) clickedEntity.getTag().clone())));
            Log.info(player.getName() + " stored " + clickedEntity.getType() + " (" + clickedEntity.getEntityId() + ") into the safari net");
            new BukkitRunnable() {
                @Override
                public void run() {
                    clickedEntity.remove();
                }
            }.runTaskLater(this, 1);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (lock.contains(e.getPlayer().getUniqueId())) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (!SafariNetUtils.isSafariNet(e.getPlayer().getInventory().getItemInMainHand())) return;
        if (e.getClickedBlock() == null) return;
        if (SafariNetUtils.isEmpty(item)) return;
        EntityType type = SafariNetUtils.getEntityType(item);
        if (type == null) return;
        if (e.getPlayer().getWorld().getName().equalsIgnoreCase("world")
                && (type != EntityType.VILLAGER
                && type != EntityType.SHEEP
                && type != EntityType.PIG
                && type != EntityType.CAT
                && type != EntityType.COW
                && type != EntityType.COD
                && type != EntityType.BAT
                && type != EntityType.FOX
                && type != EntityType.CHICKEN
                && type != EntityType.HORSE
                && type != EntityType.LLAMA
                && type != EntityType.WANDERING_TRADER
                && type != EntityType.TRADER_LLAMA
                && type != EntityType.IRON_GOLEM
                && type != EntityType.RABBIT
                && type != EntityType.OCELOT
                && type != EntityType.MULE
                && type != EntityType.MUSHROOM_COW
                && type != EntityType.SQUID
                && type != EntityType.ZOMBIE_HORSE
                && type != EntityType.SKELETON_HORSE
                && type != EntityType.PARROT
                && type != EntityType.PANDA
                && type != EntityType.DONKEY
                && type != EntityType.TURTLE
                && type != EntityType.DOLPHIN
                && type != EntityType.STRIDER
                && type != EntityType.POLAR_BEAR)) {
            e.getPlayer().sendActionBar(ChatColor.RED + "この種類のMobはこのワールドは出せません。");
            return;
        }
        Location location = e.getClickedBlock().getLocation().clone().add(0.5, 1, 0.5);
        Entity entity = location.getWorld().spawnEntity(location, type);
        NBTTagCompound tag = SafariNetUtils.getData(item);
        tag.set("Pos", SafariNetUtils.createList(location.getX(), location.getY(), location.getZ()));
        entity.load(tag);
        Log.info(e.getPlayer().getName() + " released " + entity.getType() + " (" + entity.getEntityId() + ") into the world");
        if (SafariNetUtils.getSafariNetType(item) == SafariNetType.SINGLE_USE) {
            e.getPlayer().getInventory().setItemInMainHand(null);
        } else {
            e.getPlayer().getInventory().setItemInMainHand(SafariNetUtils.updateSafariNet(SafariNetUtils.store(item, null, new NBTTagCompound())));
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        ItemStack item = e.getInventory().getResult();
        if (!SafariNetUtils.isSafariNet(item)) return;
        e.getInventory().setResult(SafariNetUtils.getSafariNet(SafariNetUtils.getSafariNetType(item)));
        if (e.getPlayer() == null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                ICollectionList.asList(e.getPlayer().getInventory().getContents()).foreach((item, index) -> {
                    if (SafariNetUtils.isSafariNet(item)) {
                        e.getPlayer().getInventory().setItem(index, SafariNetUtils.resetUniqueId(item));
                    }
                });
            }
        }.runTaskLater(this, 2);
    }
}
