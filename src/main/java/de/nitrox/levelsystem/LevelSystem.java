package de.nitrox.levelsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LevelSystem extends JavaPlugin {

    private File levelFile;
    private FileConfiguration levelConfig;
    private File levelDesignFile;
    private FileConfiguration levelDesignConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        createLevelConfig();
        createLevelDesignConfig();
        saveDefaultConfig();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register Placeholder
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LevelPlaceholder(this).register();
        }

        // Register Commands
        PluginCommand addXpCommand = getCommand("addxp");
        if (addXpCommand != null) {
            addXpCommand.setExecutor(new AddXPCommand(this));
        }

        PluginCommand removeXpCommand = getCommand("removexp");
        if (removeXpCommand != null) {
            removeXpCommand.setExecutor(new RemoveXPCommand(this));
        }

        PluginCommand reloadCommand = getCommand("reloadlevelsystem");
        if (reloadCommand != null) {
            reloadCommand.setExecutor(new ReloadCommand(this));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createLevelConfig() {
        levelFile = new File(getDataFolder(), "levels.yml");

        if (!levelFile.exists()) {
            levelFile.getParentFile().mkdirs();
            saveResource("levels.yml", false);
        }

        levelConfig = new YamlConfiguration();
        try {
            levelConfig.load(levelFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLevelDesignConfig() {
        levelDesignFile = new File(getDataFolder(), "leveldesign.yml");

        if (!levelDesignFile.exists()) {
            levelDesignFile.getParentFile().mkdirs();
            saveResource("leveldesign.yml", false);
        }

        levelDesignConfig = new YamlConfiguration();
        try {
            levelDesignConfig.load(levelDesignFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getLevelConfig() {
        return levelConfig;
    }

    public void saveLevelConfig() {
        try {
            levelConfig.save(levelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getLevelDesignConfig() {
        return levelDesignConfig;
    }

    public void saveLevelDesignConfig() {
        try {
            levelDesignConfig.save(levelDesignFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRequiredXPForLevel(int level) {
        return (int) Math.pow(level, 2) * 100;
    }

    public boolean isWorldDisabled(String worldName) {
        List<String> disabledWorlds = getConfig().getStringList("disabled-worlds");
        return disabledWorlds.contains(worldName);
    }

    public void reloadConfigs() {
        // reload default config (config.yml)
        reloadConfig();

        // reload levels.yml
        try {
            levelFile = new File(getDataFolder(), "levels.yml");
            if (!levelFile.exists()) saveResource("levels.yml", false);
            levelConfig = new YamlConfiguration();
            levelConfig.load(levelFile);
        } catch (Exception e) {
            getLogger().severe("Failed to reload levels.yml");
            e.printStackTrace();
        }

        // reload leveldesign.yml
        try {
            levelDesignFile = new File(getDataFolder(), "leveldesign.yml");
            if (!levelDesignFile.exists()) saveResource("leveldesign.yml", false);
            levelDesignConfig = new YamlConfiguration();
            levelDesignConfig.load(levelDesignFile);
        } catch (Exception e) {
            getLogger().severe("Failed to reload leveldesign.yml");
            e.printStackTrace();
        }
    }
}
