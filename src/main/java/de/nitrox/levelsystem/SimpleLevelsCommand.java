package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class SimpleLevelsCommand implements CommandExecutor {

    private final LevelSystem plugin;

    public SimpleLevelsCommand(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (sub) {

            case "addxp":
                return plugin.getAddXPCommand().execute(sender, subArgs);

            case "removexp":
                return plugin.getRemoveXPCommand().execute(sender, subArgs);

            case "levelstats":
                return plugin.getLevelStatsCommand().execute(sender, subArgs);

            case "reload":
                return plugin.getReloadCommand().execute(sender, subArgs);

            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== SimpleLevels Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/simplelevels addxp <id> <amount> <player>");
        sender.sendMessage(ChatColor.YELLOW + "/simplelevels removexp <id> <amount> <player>");
        sender.sendMessage(ChatColor.YELLOW + "/simplelevels levelstats <id> [player]");
        sender.sendMessage(ChatColor.YELLOW + "/simplelevels reload");
    }
}