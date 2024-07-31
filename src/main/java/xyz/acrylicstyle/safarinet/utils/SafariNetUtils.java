package xyz.acrylicstyle.safarinet.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
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
    public static @NotNull ItemStack modifySafariNetData(@NotNull ItemStack item, @NotNull Consumer<CompoundTag> action) {
        net.minecraft.world.item.ItemStack i = CraftItemStack.asNMSCopy(item);
        CompoundTag itemTag = i.getOrCreateTag();
        CompoundTag tag = i.getOrCreateTagElement("SafariNetData");
        action.accept(tag);
        if (tag.isEmpty()) { // if tag is empty
            itemTag.remove("SafariNetData");
        } else {
            itemTag.put("SafariNetData", tag);
        }
        if (itemTag.isEmpty()) {
            i.setTag(null);
        }
        return CraftItemStack.asBukkitCopy(i);
    }

    public static @NotNull CompoundTag getSafariNetTag(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getOrCreateTagElement("SafariNetData");
    }

    public static @NotNull ItemStack getSafariNet(@NotNull SafariNetType type) {
        return updateSafariNet(modifySafariNetData(new ItemStack(Material.CLOCK), tag -> {
            tag.putBoolean("Single", type == SafariNetType.SINGLE_USE);
            tag.putString("Type", "null");
            tag.put("Tag", new CompoundTag());
            tag.putString("Unique", UUID.randomUUID().toString());
        }));
    }

    public static boolean isSafariNet(@Nullable ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getType() != Material.CLOCK) return false;
        return CraftItemStack.asNMSCopy(itemStack).getOrCreateTag().contains("SafariNetData");
    }

    @NotNull
    public static SafariNetType getSafariNetType(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        itemStack = addMissingTags(itemStack);
        return getSafariNetTag(itemStack).getBoolean("Single") ? SafariNetType.SINGLE_USE : SafariNetType.RE_USABLE;
    }

    @NotNull
    public static ItemStack saveEntityType(@NotNull ItemStack itemStack, @Nullable EntityType entityType) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        itemStack = addMissingTags(itemStack);
        return modifySafariNetData(itemStack, tag -> tag.putString("Type", entityType == null ? "null" : entityType.name()));
    }

    @NotNull
    public static ItemStack saveEntityData(@NotNull ItemStack itemStack, @NotNull CompoundTag compoundTag) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        itemStack = addMissingTags(itemStack);
        return modifySafariNetData(itemStack, tag -> tag.put("Tag", compoundTag));
    }

    @NotNull
    public static ItemStack store(@NotNull ItemStack itemStack, @Nullable EntityType entityType, @NotNull CompoundTag compoundTag) {
        itemStack = saveEntityType(itemStack, entityType);
        return saveEntityData(itemStack, compoundTag);
    }

    @NotNull
    public static CompoundTag getData(ItemStack itemStack) {
        return getSafariNetTag(itemStack).getCompound("Tag");
    }

    @NotNull
    public static ItemStack addMissingTags(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) return itemStack;
        return modifySafariNetData(itemStack, tag -> {
            if (!tag.contains("Type")) {
                tag.putString("Type", "null");
            }
            if (!tag.contains("Tag")) {
                tag.put("Tag", new CompoundTag());
            }
            if (!tag.contains("Unique")) {
                tag.putString("Unique", UUID.randomUUID().toString());
            }
        });
    }

    @Nullable
    public static EntityType getEntityType(ItemStack itemStack) {
        String type = getSafariNetTag(itemStack).getString("Type");
        return type.equals("null") || type.isEmpty() ? null : EntityType.valueOf(type);
    }

    public static boolean isEmpty(ItemStack itemStack) {
        return getEntityType(itemStack) == null;
    }

    @NotNull
    public static ItemStack updateSafariNet(ItemStack itemStack) {
        if (!isSafariNet(itemStack)) throw new IllegalStateException("wrong item");
        CompoundTag tag = getSafariNetTag(itemStack);
        ItemMeta meta = itemStack.getItemMeta();
        boolean singleUse = tag.getBoolean("Single");
        assert meta != null;
        if (isEmpty(itemStack)) {
            meta.removeEnchant(Enchantment.DURABILITY);
        } else {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setDisplayName(ChatColor.WHITE + "Safari Net" + (singleUse ? " (使い捨て) (再生可能資源使用)" : ""));
        meta.setLore(List.of(ChatColor.GRAY + "エンティティ: " + tag.getString("Type")));
        if (singleUse) {
            meta.setCustomModelData(SafariNetPlugin.singleUseModel);
        } else {
            meta.setCustomModelData(SafariNetPlugin.reUsableModel);
        }
        itemStack.setItemMeta(meta);
        return addMissingTags(itemStack);
    }

    @NotNull
    public static ListTag createList(double... doubles) {
        ListTag list = new ListTag();
        for (int i = 0; i < doubles.length; i++) list.add(0, DoubleTag.valueOf(doubles[doubles.length - 1 - i]));
        return list;
    }
}
