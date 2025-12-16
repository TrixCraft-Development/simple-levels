package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AddXPCommand implements CommandExecutor {

    private final LevelSystem plugin;

    public AddXPCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Usage: /addxp <identifier> <amount> <player>
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simplelevels.addxp")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /addxp <identifier> <amount> <player>");
            return true;
        }

        String id = args[0];
        LevelSystemInstance inst = plugin.getManager().get(id);
        if (inst == null) {
            sender.sendMessage(ChatColor.RED + "Unknown LevelSystem: " + id);
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[1]);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not online: " + args[2]);
            return true;
        }

        // check disabled worlds for that instance
        if (inst.isWorldDisabled(target.getWorld().getName())) {
            sender.sendMessage(ChatColor.RED + "This LevelSystem is disabled in that world.");
            return true;
        }

        UUID uuid = target.getUniqueId();
        int currentXP = inst.getPlayerXP(uuid);
        int newXP = Math.max(0, currentXP + amount);
        inst.setPlayerXP(uuid, newXP);

        int newLevel = inst.getPlayerLevel(uuid);

        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " XP to " + target.getName() + " in system " + id + ". New level: " + newLevel);
        target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou received &e" + amount + " &aXP in system &b" + id + "&a. New level: &e" + newLevel));
        return true;
    }
}