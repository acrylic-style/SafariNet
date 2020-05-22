package xyz.acrylicstyle.safariNet.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import xyz.acrylicstyle.minecraft.v1_15_R1.NBTTagCompound;
import xyz.acrylicstyle.minecraft.v1_15_R1.NBTTagDouble;
import xyz.acrylicstyle.minecraft.v1_15_R1.NBTTagList;

import java.util.Arrays;
import java.util.UUID;

public class SafariNetUtils {
    public static ItemStack getSafariNet(SafariNetType type) {
        ItemStack item = new ItemStack(Material.CLOCK);
        xyz.acrylicstyle.minecraft.v1_15_R1.ItemStack is = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = is.getOrCreateTag();
        tag.setBoolean("safariNetSingleUse", type == SafariNetType.SINGLE_USE);
        tag.setString("safariNetType", "null");
        tag.set("safariNetData", new NBTTagCompound());
        tag.setString("uuid", UUID.randomUUID().toString());
        is.setTag(tag);
        item = CraftItemStack.asBukkitCopy(is);
        return updateSafariNet(item);
    }

    public static boolean isSafariNet(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getType() != Material.CLOCK) return false;
        return CraftItemStack.asNMSCopy(itemStack).getOrCreateTag().hasKey("safariNetData");
    }

    @NotNull
    public static SafariNetType getSafariNetType(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("This item isn't safari net!");
        itemStack = addMissingTags(itemStack);
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
        if (!tag.hasKey("safariNetSingleUse")) tag.setBoolean("safariNetSingleUse", true);
        return tag.getBoolean("safariNetSingleUse") ? SafariNetType.SINGLE_USE : SafariNetType.RE_USABLE;
    }

    @NotNull
    public static ItemStack saveEntityType(@NotNull ItemStack itemStack, @Nullable EntityType entityType) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("This item isn't safari net!");
        itemStack = addMissingTags(itemStack);
        xyz.acrylicstyle.minecraft.v1_15_R1.ItemStack handle = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = handle.getOrCreateTag();
        tag.setString("safariNetType", entityType == null ? "null" : entityType.name());
        handle.setTag(tag);
        return CraftItemStack.asBukkitCopy(handle);
    }

    @NotNull
    public static ItemStack saveEntityData(@NotNull ItemStack itemStack, @NotNull NBTTagCompound nbtTagCompound) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("This item isn't safari net!");
        itemStack = addMissingTags(itemStack);
        xyz.acrylicstyle.minecraft.v1_15_R1.ItemStack handle = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = handle.getOrCreateTag();
        tag.set("safariNetData", nbtTagCompound);
        handle.setTag(tag);
        return CraftItemStack.asBukkitCopy(handle);
    }

    @NotNull
    public static ItemStack store(@NotNull ItemStack itemStack, @Nullable EntityType entityType, @NotNull NBTTagCompound nbtTagCompound) {
        itemStack = saveEntityType(itemStack, entityType);
        return saveEntityData(itemStack, nbtTagCompound);
    }

    @NotNull
    public static NBTTagCompound getData(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("This item isn't safari net!");
        itemStack = addMissingTags(itemStack);
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
        if (!tag.hasKey("safariNetData")) tag.set("safariNetData", new NBTTagCompound());
        return tag.getCompound("safariNetData");
    }

    @NotNull
    public static ItemStack addMissingTags(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) return itemStack;
        xyz.acrylicstyle.minecraft.v1_15_R1.ItemStack handle = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = handle.getOrCreateTag();
        boolean modified = false;
        if (!tag.hasKey("safariNetType")) {
            tag.setString("safariNetType", "null");
            modified = true;
        }
        if (!tag.hasKey("safariNetData")) {
            tag.set("safariNetData", new NBTTagCompound());
            modified = true;
        }
        if (!tag.hasKey("uuid")) {
            tag.setString("uuid", UUID.randomUUID().toString());
            modified = true;
        }
        if (modified) handle.setTag(tag);
        return CraftItemStack.asBukkitCopy(handle);
    }

    @Nullable
    public static EntityType getEntityType(ItemStack itemStack) {
        ItemStack is = addMissingTags(itemStack);
        xyz.acrylicstyle.minecraft.v1_15_R1.ItemStack handle = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = handle.getOrCreateTag();
        String type = tag.getString("safariNetType");
        return type.equals("null") ? null : EntityType.valueOf(type);
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return getEntityType(itemStack) == null;
    }

    @NotNull
    public static ItemStack updateSafariNet(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("This item isn't safari net!");
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getOrCreateTag();
        ItemMeta meta = itemStack.getItemMeta();
        boolean singleUse = tag.getBoolean("safariNetSingleUse");
        if (isEmpty(itemStack)) {
            meta.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL);
        } else {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setDisplayName(ChatColor.WHITE + "Safari Net" + (singleUse ? " (Single Use)" : " (Re-Usable)"));
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Type: " + tag.getString("safariNetType"),
                ChatColor.GRAY + "Has data: " + tag.getCompound("safariNetData").isEmpty()
        ));
        itemStack.setItemMeta(meta);
        return addMissingTags(itemStack);
    }

    @NotNull
    public static NBTTagList createList(NBTTagList list, double... doubles) {
        for (int i = 0; i < 3; i++) list.set(i, NBTTagDouble.a(doubles[i]));
        return list;
    }
}
