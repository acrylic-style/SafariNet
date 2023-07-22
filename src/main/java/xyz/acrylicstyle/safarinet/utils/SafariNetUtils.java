package xyz.acrylicstyle.safarinet.utils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.safarinet.SafariNetPlugin;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SafariNetUtils {
    public static @NotNull ItemStack modifySafariNetData(@NotNull ItemStack item, @NotNull Consumer<NBTTagCompound> action) {
        net.minecraft.world.item.ItemStack i = CraftItemStack.asNMSCopy(item);
        NBTTagCompound itemTag = i.w();
        NBTTagCompound tag = i.a("SafariNetData");
        action.accept(tag);
        if (tag.f() == 0) { // if tag is empty
            itemTag.r("SafariNetData");
        } else {
            itemTag.a("SafariNetData", tag);
        }
        if (itemTag.f() == 0) {
            i.c((NBTTagCompound) null);
        }
        return CraftItemStack.asBukkitCopy(i);
    }

    public static @NotNull NBTTagCompound getSafariNetTag(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item).a("SafariNetData");
    }

    public static @NotNull ItemStack getSafariNet(@NotNull SafariNetType type) {
        return updateSafariNet(modifySafariNetData(new ItemStack(Material.CLOCK), tag -> {
            tag.a("Single", type == SafariNetType.SINGLE_USE);
            tag.a("Type", "null");
            tag.a("Tag", new NBTTagCompound());
            tag.a("Unique", UUID.randomUUID().toString());
        }));
    }

    public static boolean isSafariNet(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getType() != Material.CLOCK) return false;
        return CraftItemStack.asNMSCopy(itemStack).w().e("SafariNetData");
    }

    public static ItemStack resetUniqueId(@Nullable ItemStack itemStack) {
        if (!isSafariNet(itemStack)) return itemStack;
        return modifySafariNetData(itemStack, tag -> tag.a("Unique", UUID.randomUUID().toString()));
    }

    @NotNull
    public static SafariNetType getSafariNetType(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        itemStack = addMissingTags(itemStack);
        return getSafariNetTag(itemStack).q("Single") ? SafariNetType.SINGLE_USE : SafariNetType.RE_USABLE;
    }

    @NotNull
    public static ItemStack saveEntityType(@NotNull ItemStack itemStack, @Nullable EntityType entityType) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        itemStack = addMissingTags(itemStack);
        return modifySafariNetData(itemStack, tag -> tag.a("Type", entityType == null ? "null" : entityType.name()));
    }

    @NotNull
    public static ItemStack saveEntityData(@NotNull ItemStack itemStack, @NotNull NBTTagCompound nbtTagCompound) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        itemStack = addMissingTags(itemStack);
        return modifySafariNetData(itemStack, tag -> tag.a("Tag", nbtTagCompound));
    }

    @NotNull
    public static ItemStack store(@NotNull ItemStack itemStack, @Nullable EntityType entityType, @NotNull NBTTagCompound nbtTagCompound) {
        itemStack = saveEntityType(itemStack, entityType);
        return saveEntityData(itemStack, nbtTagCompound);
    }

    @NotNull
    public static NBTTagCompound getData(ItemStack itemStack) {
        return getSafariNetTag(itemStack).p("Tag");
    }

    @NotNull
    public static ItemStack addMissingTags(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) return itemStack;
        return modifySafariNetData(itemStack, tag -> {
            if (!tag.e("Type")) {
                tag.a("Type", "null");
            }
            if (!tag.e("Tag")) {
                tag.a("Tag", new NBTTagCompound());
            }
            if (!tag.e("Unique")) {
                tag.a("Unique", UUID.randomUUID().toString());
            }
        });
    }

    @Nullable
    public static EntityType getEntityType(ItemStack itemStack) {
        String type = getSafariNetTag(itemStack).l("Type");
        return type.equals("null") || type.equals("") ? null : EntityType.valueOf(type);
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return getEntityType(itemStack) == null;
    }

    @NotNull
    public static ItemStack updateSafariNet(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        NBTTagCompound tag = getSafariNetTag(itemStack);
        ItemMeta meta = itemStack.getItemMeta();
        boolean singleUse = tag.q("Single");
        assert meta != null;
        if (isEmpty(itemStack)) {
            meta.removeEnchant(Enchantment.DURABILITY);
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setDisplayName(ChatColor.WHITE + "Safari Net" + (singleUse ? " (使い捨て) (再生可能資源使用)" : ""));
        meta.setLore(List.of(ChatColor.GRAY + "エンティティ: " + tag.l("Type")));
        if (singleUse) {
            meta.setCustomModelData(SafariNetPlugin.singleUseModel);
        } else {
            meta.setCustomModelData(SafariNetPlugin.reUsableModel);
        }
        itemStack.setItemMeta(meta);
        return addMissingTags(itemStack);
    }

    @NotNull
    public static NBTTagList createList(double... doubles) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < doubles.length; i++) list.b(0, NBTTagDouble.a(doubles[doubles.length - 1 - i]));
        return list;
    }
}
