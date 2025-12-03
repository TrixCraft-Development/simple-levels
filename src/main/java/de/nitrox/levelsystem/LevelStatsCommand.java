package de.nitrox.levelsystem;

import de.nitrox.levelsystem.LevelSystem;
import de.nitrox.levelsystem.LevelSystemInstance;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelStatsCommand implements CommandExecutor {

    private final LevelSystem plugin;

    public LevelStatsCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /levelstats <identifier>");
            return true;
        }

        String id = args[0].toLowerCase();
        LevelSystemInstance system = plugin.getManager().get(id);

        if (system == null) {
            player.sendMessage(ChatColor.RED + "Unknown LevelSystem: " + id);
            return true;
        }

        int xp = system.getXP(player);
        int level = system.getLevelFromXP(xp);
        String design = ChatColor.translateAlternateColorCodes('&', system.getLevelDesign(level));

        int nextLevelXp = system.getRequiredXPForLevel(level + 1);
        int needed = nextLevelXp - xp;

        player.sendMessage(ChatColor.GOLD + "=== Level Stats (" + id + ") ===");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level);
        player.sendMessage(ChatColor.YELLOW + "Design: " + ChatColor.WHITE + design);
        player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.WHITE + xp);
        player.sendMessage(ChatColor.YELLOW + "XP needed for next level: " + ChatColor.WHITE + needed);

        return true;
    }
}