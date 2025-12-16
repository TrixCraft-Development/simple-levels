package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
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
        if (!sender.hasPermission("simplelevels.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }

        try {
            plugin.getManager().loadSystems();
            sender.sendMessage(ChatColor.GREEN + "LevelSystem: all systems reloaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("Error reloading systems:");
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "An error occurred. Check console.");
        }

        return true;
    }
}