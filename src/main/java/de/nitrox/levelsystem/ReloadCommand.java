package de.nitrox.levelsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final LevelSystem plugin;

    public ReloadCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // No player-only restriction: allow console too
        if (!sender.hasPermission("levelsystem.reload")) {
            sender.sendMessage("§cYou do not have permission to execute this command.");
            return true; // handled
        }

        // Call reload on plugin (safe)
        try {
            plugin.reloadConfigs();
            sender.sendMessage("§aLevelSystem: Configs reloaded.");
        } catch (Exception ex) {
            sender.sendMessage("§cLevelSystem: An error occurred while reloading. See console.");
            plugin.getLogger().severe("Error reloading LevelSystem configs:");
            ex.printStackTrace();
        }

        return true; // IMPORTANT: return true so Bukkit does NOT send the usage message
    }
}