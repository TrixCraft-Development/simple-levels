package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    private final LevelSystem plugin;

    public CommandTabCompleter(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            result.add("addxp");
            result.add("removexp");
            result.add("levelstats");
            result.add("reload");
            return result;
        }

        String sub = args[0].toLowerCase();
        List<String> ids = new ArrayList<>(plugin.getManager().getIdentifiers());

        switch (sub) {

            case "addxp":
            case "removexp":
                if (args.length == 2) return ids;
                if (args.length == 3) return List.of("10", "100", "1000");
                if (args.length == 4) {
                    Bukkit.getOnlinePlayers().forEach(p -> result.add(p.getName()));
                    return result;
                }
                break;

            case "levelstats":
                if (args.length == 2) return ids;
                if (args.length == 3) {
                    Bukkit.getOnlinePlayers().forEach(p -> result.add(p.getName()));
                    return result;
                }
                break;
        }

        return result;
    }
}