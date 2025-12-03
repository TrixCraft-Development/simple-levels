package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LevelSystemInstance {

    private final LevelSystem plugin;
    private final String id;
    private final File file;
    private FileConfiguration cfg;

    // cached lists/maps
    private final Map<Integer, Integer> explicitXpPerLevel = new HashMap<>();
    private List<String> disabledWorlds = new ArrayList<>();

    public LevelSystemInstance(LevelSystem plugin, String id, File file) {
        this.plugin = plugin;
        this.id = id;
        this.file = file;
        load();
    }

    public void load() {
        try {
            if (!file.exists()) {
                plugin.saveResource(file.getName(), false);
            }
        } catch (IllegalArgumentException ignored) {}

        cfg = YamlConfiguration.loadConfiguration(file);

        disabledWorlds = cfg.getStringList("disabled-worlds");
        if (disabledWorlds == null) disabledWorlds = new ArrayList<>();

        explicitXpPerLevel.clear();
        if (cfg.isConfigurationSection("levels")) {
            for (String key : cfg.getConfigurationSection("levels").getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(key);
                    if (cfg.contains("levels." + key + ".xp")) {
                        int xp = cfg.getInt("levels." + key + ".xp", -1);
                        if (xp >= 0) explicitXpPerLevel.put(lvl, xp);
                    }
                } catch (NumberFormatException ignore) {}
            }
        }
    }

    public void save() {
        try {
            if (cfg != null) cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + file.getName());
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public int getXP(UUID uuid) {
        return getPlayerXP(uuid);
    }

    public int getXP(org.bukkit.entity.Player player) {
        return getPlayerXP(player.getUniqueId());
    }

    public FileConfiguration getConfig() {
        return cfg;
    }

    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName);
    }

    public int getRequiredXPForLevel(int level) {
        if (explicitXpPerLevel.containsKey(level)) return explicitXpPerLevel.get(level);

        double base = cfg.getDouble("formula.base", 100.0);
        double multiplier = cfg.getDouble("formula.multiplier", 2.0);

        double value = base * Math.pow(level, multiplier);
        return Math.max(0, (int) Math.round(value));
    }

    public int getPlayerXP(UUID uuid) {
        return cfg.getInt("players." + uuid.toString() + ".xp", 0);
    }

    public void setPlayerXP(UUID uuid, int xp) {
        cfg.set("players." + uuid.toString() + ".xp", xp);
        save();
    }

    public int getPlayerLevel(UUID uuid) {
        int xp = getPlayerXP(uuid);
        return getLevelFromXP(xp);
    }

    public int getLevelFromXP(int xp) {
        int lvl = 1;
        while (xp >= getRequiredXPForLevel(lvl)) {
            lvl++;
        }
        return Math.max(0, lvl - 1);
    }

    // Existing display method
    public String getDisplayForLevel(int level) {
        String path = "levels." + level + ".display";
        String raw = cfg.getString(path);

        if (raw == null)
            return ChatColor.translateAlternateColorCodes('&', "&eLevel " + level);

        raw = raw.replace("%level%", String.valueOf(level));
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    // NEW: alias for backwards compatibility
    public String getLevelDesign(int level) {
        return getDisplayForLevel(level);
    }
}
