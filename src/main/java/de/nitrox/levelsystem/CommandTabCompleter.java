package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import de.nitrox.levelsystem.LevelPlaceholder;

import java.util.ArrayList;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    private final LevelSystem plugin;

    public CommandTabCompleter(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        List<String> list = new ArrayList<>();

        List<String> ids = new ArrayList<>(plugin.getManager().getIdentifiers());

        switch (cmd.getName().toLowerCase()) {

            /* --------------------------
               /addxp <identifier> <amount> <player>
               -------------------------- */
            case "addxp":
            case "removexp":

                if (args.length == 1) {
                    return ids;
                }

                if (args.length == 2) {
                    list.add("100");
                    list.add("500");
                    list.add("1000");
                    return list;
                }

                if (args.length == 3) {
                    Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
                    return list;
                }

                return list;


            /* --------------------------
               /levelstats <identifier> <player?>
               -------------------------- */
            case "levelstats":

                if (args.length == 1) {
                    return ids;
                }

                if (args.length == 2) {
                    Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
                    return list;
                }

                return list;
        }

        return list;
    }
}

