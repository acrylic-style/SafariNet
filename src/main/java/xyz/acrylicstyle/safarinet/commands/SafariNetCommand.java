package xyz.acrylicstyle.safarinet.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.acrylicstyle.safarinet.utils.SafariNetType;
import xyz.acrylicstyle.safarinet.utils.SafariNetUtils;

import java.util.Arrays;
import java.util.List;

public class SafariNetCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /safarinet args...");
            return true;
        }
        if (args[0].equals("give")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /safarinet give <player> <type>");
                return true;
            }
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("Player not found.");
                return true;
            }
            SafariNetType type;
            try {
                type = SafariNetType.valueOf(args[2]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid type.");
                return true;
            }
            player.getInventory().addItem(SafariNetUtils.getSafariNet(type));
            sender.sendMessage("Gave " + player.getName() + " a " + type.name() + " safari net.");
            return true;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("give");
        }
        if (args.length == 2) {
            if (args[0].equals("give")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (args.length == 3) {
            if (args[0].equals("give")) {
                return Arrays.stream(SafariNetType.values()).map(Enum::name).toList();
            }
        }
        return List.of();
    }
}
