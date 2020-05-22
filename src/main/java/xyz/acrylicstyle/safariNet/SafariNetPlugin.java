package xyz.acrylicstyle.safariNet;

import org.bukkit.Bukkit;
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
import util.reflect.Ref;
import xyz.acrylicstyle.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.acrylicstyle.minecraft.v1_15_R1.NBTTagCompound;
import xyz.acrylicstyle.minecraft.v1_15_R1.NBTTagList;
import xyz.acrylicstyle.safariNet.utils.SafariNetType;
import xyz.acrylicstyle.safariNet.utils.SafariNetUtils;
import xyz.acrylicstyle.shared.NMSAPI;
import xyz.acrylicstyle.tomeito_api.utils.Log;

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

    //@EventHandler
    //public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
    //    Log.info("Fired #AtEntity");
    //    if (e.getHand() != EquipmentSlot.HAND) return;
    //    interact(e.getRightClicked(), e.getPlayer());
    //}

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (lock.contains(e.getPlayer().getUniqueId())) return;
        interact(e.getRightClicked(), e.getPlayer());
    }

    public static CollectionList<UUID> lock = new CollectionList<>();

    private void interact(Entity clickedEntity, Player player) {
        if (!clickedEntity.getType().isSpawnable() || !clickedEntity.getType().isAlive()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SafariNetUtils.isSafariNet(item)) return;
        if (SafariNetUtils.isEmpty(item)) {
            lock.add(player.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    lock.remove(player.getUniqueId());
                }
            }.runTaskLater(this, 2); // wait 0.1 second
            xyz.acrylicstyle.minecraft.v1_15_R1.Entity handle = new xyz.acrylicstyle.minecraft.v1_15_R1.Entity(
                    Ref.getMethod(CraftEntity.CLASS, "getHandle").invokeObj(clickedEntity)
            );
            NBTTagCompound tag = new NBTTagCompound();
            Ref.getClass(xyz.acrylicstyle.minecraft.v1_15_R1.Entity.CLASS)
                    .getMethod("save", NBTTagCompound.CLASS)
                    .invokeObj(Ref.getDeclaredField(NMSAPI.class, "o").accessible(true).get(handle), tag.getHandle());
            player.getInventory().setItemInMainHand(SafariNetUtils.updateSafariNet(SafariNetUtils.store(item, clickedEntity.getType(), tag)));
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
        Location location = e.getClickedBlock().getLocation().clone().add(0.5, 1, 0.5);
        Entity entity = location.getWorld().spawnEntity(location, type);
        xyz.acrylicstyle.minecraft.v1_15_R1.Entity handle = new xyz.acrylicstyle.minecraft.v1_15_R1.Entity(
                Ref.getMethod(CraftEntity.CLASS, "getHandle").invokeObj(entity)
        );
        NBTTagCompound tag = SafariNetUtils.getData(item);
        NBTTagList list = (NBTTagList) tag.get("Pos");
        tag.set("Pos", SafariNetUtils.createList(list, location.getX(), location.getY(), location.getZ()));
        Ref.getMethod(xyz.acrylicstyle.minecraft.v1_15_R1.Entity.CLASS, "f", NBTTagCompound.CLASS)
                .invokeObj(Ref.getDeclaredField(NMSAPI.class, "o").accessible(true).get(handle), tag.getHandle());
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
    }
}