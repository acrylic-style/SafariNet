package xyz.acrylicstyle.safarinet;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
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
import xyz.acrylicstyle.safarinet.commands.SafariNetCommand;
import xyz.acrylicstyle.safarinet.utils.SafariNetType;
import xyz.acrylicstyle.safarinet.utils.SafariNetUtils;

import java.util.*;

public class SafariNetPlugin extends JavaPlugin implements Listener {
    private static final Set<EntityType> DISALLOWED_ENTITIES = new HashSet<>(Arrays.asList(EntityType.ENDER_DRAGON, EntityType.WITHER));
    public static NamespacedKey safariNet_singleUse = null;
    public static NamespacedKey safariNet = null;
    public static SafariNetPlugin instance = null;
    public static int singleUseModel = 1;
    public static int reUsableModel = 2;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        singleUseModel = getConfig().getInt("custom-model-data.single-use", singleUseModel);
        reUsableModel = getConfig().getInt("custom-model-data.re-usable", reUsableModel);
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
        if (!getConfig().getBoolean("disable-recipes", false)) {
            Bukkit.addRecipe(r1);
            Bukkit.addRecipe(r2);
        }
        Objects.requireNonNull(getCommand("safarinet")).setExecutor(new SafariNetCommand());
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

    public static Set<UUID> lock = new HashSet<>();

    public static final List<EntityType> excludedEntities = new ArrayList<>();

    static {
        excludedEntities.add(EntityType.ENDER_DRAGON);
        excludedEntities.add(EntityType.WITHER);
    }

    private void interact(Entity clickedEntity, Player player) {
        if (!clickedEntity.getType().isSpawnable() || !clickedEntity.getType().isAlive()) return;
        if (DISALLOWED_ENTITIES.contains(clickedEntity.getType())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cこの種類のエンティティはキャプチャーできません。"));
            return;
        }
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
            CompoundTag tag = new CompoundTag();
            ((CraftEntity) clickedEntity).getHandle().save(tag);
            player.getInventory().setItemInMainHand(
                    SafariNetUtils.updateSafariNet(
                            SafariNetUtils.store(
                                    item,
                                    clickedEntity.getType(),
                                    tag
                            )
                    )
            );
            getLogger().info(player.getName() + " stored " + clickedEntity.getType() + " (" + clickedEntity.getEntityId() + ") into the safari net at " + player.getLocation());
            // there was 1 tick delay before removing the entity
                    clickedEntity.remove();
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
        if (DISALLOWED_ENTITIES.contains(type)) {
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "この種類のエンティティは出せません。"));
            return;
        }
        Location location = e.getClickedBlock().getLocation().clone().add(0.5, 1, 0.5);
        Entity entity = Objects.requireNonNull(location.getWorld()).spawnEntity(location, type);
        CompoundTag tag = SafariNetUtils.getData(item);
        tag.put("Pos", SafariNetUtils.createList(location.getX(), location.getY(), location.getZ()));
        ((CraftEntity) entity).getHandle().load(tag);
        getLogger().info(e.getPlayer().getName() + " released " + entity.getType() + " (" + entity.getEntityId() + "): " + location);
        if (SafariNetUtils.getSafariNetType(item) == SafariNetType.SINGLE_USE) {
            e.getPlayer().getInventory().setItemInMainHand(null);
        } else {
            e.getPlayer().getInventory().setItemInMainHand(SafariNetUtils.updateSafariNet(SafariNetUtils.store(item, null, new CompoundTag())));
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        ItemStack item = e.getInventory().getResult();
        if (SafariNetUtils.isSafariNet(item)) {
            e.getInventory().setResult(SafariNetUtils.getSafariNet(SafariNetUtils.getSafariNetType(item)));
            return;
        }
        SafariNetType type = null;
        int count = 0;
        for (ItemStack stack : e.getInventory().getMatrix()) {
            if (SafariNetUtils.isSafariNet(stack)) {
                type = SafariNetUtils.getSafariNetType(stack);
                count++;
            }
        }
        if (count == 1) {
            e.getInventory().setResult(SafariNetUtils.getSafariNet(type));
        }
    }
}
