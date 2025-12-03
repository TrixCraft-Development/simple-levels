package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class LevelSystem extends JavaPlugin {

    private LevelSystemManager manager;

    @Override
    public void onEnable() {
        // ensure data folder
        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        // manager & load systems
        manager = new LevelSystemManager(this);
        manager.loadSystems();

        // register placeholder if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlaceholder(this).register();
            getLogger().info("PlaceholderAPI found — LevelPlaceholder registered.");
        } else {
            getLogger().info("PlaceholderAPI not found — placeholders unavailable.");
        }

        // commands
        PluginCommand add = getCommand("addxp");
        if (add != null) add.setExecutor(new AddXPCommand(this));

        PluginCommand rem = getCommand("removexp");
        if (rem != null) rem.setExecutor(new RemoveXPCommand(this));

        PluginCommand reload = getCommand("reloadlevelsystem");
        if (reload != null) reload.setExecutor(new ReloadCommand(this));

        PluginCommand levelstats = getCommand("levelstats");
        if (levelstats != null) levelstats.setExecutor(new LevelStatsCommand(this));

        CommandTabCompleter completer = new CommandTabCompleter(this);

        Objects.requireNonNull(getCommand("addxp")).setTabCompleter(completer);
        Objects.requireNonNull(getCommand("removexp")).setTabCompleter(completer);
        Objects.requireNonNull(getCommand("levelstats")).setTabCompleter(completer);

        // listener
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("LevelSystem enabled — loaded " + manager.getAll().size() + " system(s).");
    }

    @Override
    public void onDisable() {
        // save all instances
        manager.getAll().values().forEach(LevelSystemInstance::save);
        getLogger().info("LevelSystem disabled.");
    }

    public LevelSystemManager getManager() {
        return manager;
    }
}