package de.nitrox.levelsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveXPCommand implements CommandExecutor {

    private final LevelSystem plugin;

    public RemoveXPCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage("This command cannot be used in this world.");
            return true;
        }

        if (!player.hasPermission("levelsystem.removexp")) {
            player.sendMessage("You do not have permission to execute this command.");
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            player.sendMessage("Usage: /removexp <amount> [player]");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("Please enter a valid number.");
            return true;
        }

        Player targetPlayer = player;
        if (args.length == 2) {
            targetPlayer = plugin.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage("The specified player is not online.");
                return true;
            }
        }

        int currentXP = plugin.getLevelConfig().getInt("players." + targetPlayer.getUniqueId() + ".xp", 0);
        int newXP = Math.max(currentXP - amount, 0);  // Ensure XP does not go below 0
        plugin.getLevelConfig().set("players." + targetPlayer.getUniqueId() + ".xp", newXP);
        plugin.saveLevelConfig();

        int newLevel = calculateLevel(newXP);
        targetPlayer.setLevel(newLevel);

        targetPlayer.sendMessage("You have lost " + amount + " XP. Your new level is " + newLevel + ".");
        return true;
    }

    private int calculateLevel(int xp) {
        int level = 1;
        while (xp >= plugin.getRequiredXPForLevel(level)) {
            level++;
        }
        return level - 1;
    }
}
