package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelSystem extends JavaPlugin {

    private LevelSystemManager manager;

    private AddXPCommand addXPCommand;
    private RemoveXPCommand removeXPCommand;
    private LevelStatsCommand levelStatsCommand;
    private ReloadCommand reloadCommand;

    @Override
    public void onEnable() {

        // ensure data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // manager FIRST
        manager = new LevelSystemManager(this);
        manager.loadSystems();

        // register main command
        PluginCommand main = getCommand("simplelevels");
        if (main != null) {
            main.setExecutor(new SimpleLevelsCommand(this));
            main.setTabCompleter(new CommandTabCompleter(this));
        }

        // create command instances
        addXPCommand = new AddXPCommand(this);
        removeXPCommand = new RemoveXPCommand(this);
        levelStatsCommand = new LevelStatsCommand(this);
        reloadCommand = new ReloadCommand(this);

        // placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlaceholder(this).register();
            getLogger().info("PlaceholderAPI found — LevelPlaceholder registered.");
        } else {
            getLogger().info("PlaceholderAPI not found — placeholders unavailable.");
        }

        // listeners
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

    /* --------------------
       Getters
     -------------------- */

    public LevelSystemManager getManager() {
        return manager;
    }

    public AddXPCommand getAddXPCommand() {
        return addXPCommand;
    }

    public RemoveXPCommand getRemoveXPCommand() {
        return removeXPCommand;
    }

    public LevelStatsCommand getLevelStatsCommand() {
        return levelStatsCommand;
    }

    public ReloadCommand getReloadCommand() {
        return reloadCommand;
    }
}