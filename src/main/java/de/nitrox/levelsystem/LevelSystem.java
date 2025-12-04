package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelSystem extends JavaPlugin {

    private LevelSystemManager manager;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        manager = new LevelSystemManager(this);
        manager.loadSystems();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlaceholder(this).register();
            getLogger().info("PlaceholderAPI found — LevelPlaceholder registered.");
        } else {
            getLogger().info("PlaceholderAPI not found — placeholders unavailable.");
        }

        PluginCommand add = getCommand("addxp");
        PluginCommand rem = getCommand("removexp");
        PluginCommand reload = getCommand("reloadlevelsystem");
        PluginCommand stats = getCommand("levelstats");

        CommandTabCompleter completer = new CommandTabCompleter(this);

        if (add != null) {
            add.setExecutor(new AddXPCommand(this));
            add.setTabCompleter(completer);
        }

        if (rem != null) {
            rem.setExecutor(new RemoveXPCommand(this));
            rem.setTabCompleter(completer);
        }

        if (reload != null) {
            reload.setExecutor(new ReloadCommand(this));
        }

        if (stats != null) {
            stats.setExecutor(new LevelStatsCommand(this));
            stats.setTabCompleter(completer);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("LevelSystem enabled — loaded " + manager.getAll().size() + " system(s).");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.stopAnimationTask();
            manager.getAll().values().forEach(LevelSystemInstance::save);
        }
        getLogger().info("LevelSystem disabled.");
    }

    public LevelSystemManager getManager() {
        return manager;
    }
}