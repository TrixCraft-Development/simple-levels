package de.nitrox.levelsystem;

import de.nitrox.levelsystem.LevelSystem;
import de.nitrox.levelsystem.LevelSystemInstance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LevelStatsCommand implements CommandExecutor {

    private final LevelSystem plugin;

    public LevelStatsCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /levelstats <identifier> <player>

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage("§cUsage: /levelstats <identifier> [player]");
            return true;
        }

        String systemId = args[0];
        LevelSystemInstance inst = plugin.getManager().get(systemId);

        if (inst == null) {
            sender.sendMessage("§cLevelsystem '" + systemId + "' does not exist.");
            return true;
        }

        UUID targetUUID;
        String targetName;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cConsole must specify a player: /levelstats <id> <player>");
                return true;
            }

            Player p = (Player) sender;
            targetUUID = p.getUniqueId();
            targetName = p.getName();
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);

            if (offline == null || (!offline.hasPlayedBefore() && !offline.isOnline())) {
                sender.sendMessage("§cPlayer '" + args[1] + "' not found.");
                return true;
            }

            targetUUID = offline.getUniqueId();
            targetName = offline.getName();
        }

        int xp = inst.getPlayerXP(targetUUID);
        int level = inst.getPlayerLevel(targetUUID);
        int nextLevel = level + 1;

        int neededXP = inst.getRequiredXPForLevel(nextLevel);
        int difference = Math.max(0, neededXP - xp);

        String display = inst.getDisplayForLevel(level);

        sender.sendMessage("§8---------------------------------");
        sender.sendMessage("§6Level Stats for: §e" + targetName);
        sender.sendMessage("§7System: §b" + systemId);
        sender.sendMessage("§7Level: §a" + level);
        sender.sendMessage("§7Design: §f" + display);
        sender.sendMessage("§7XP: §d" + xp);
        sender.sendMessage("§7XP needed for next level (§e" + nextLevel + "§7): §d" + difference);
        sender.sendMessage("§8---------------------------------");

        return true;
    }
}