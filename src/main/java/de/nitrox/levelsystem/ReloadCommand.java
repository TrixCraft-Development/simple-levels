package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand {

    private final LevelSystem plugin;

    public ReloadCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * /simplelevels reload
     */
    public boolean execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("simplelevels.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        plugin.getManager().loadSystems();
        sender.sendMessage(ChatColor.GREEN + "SimpleLevels: all systems reloaded.");
        return true;
    }
}
