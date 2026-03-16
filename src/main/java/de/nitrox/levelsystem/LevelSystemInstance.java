package de.nitrox.levelsystem;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LevelSystemInstance {

    private final LevelSystem plugin;
    private final String id;
    private final File file;
    private FileConfiguration cfg;

    // XP & worlds
    private final Map<Integer, Integer> explicitXpPerLevel = new HashMap<>();
    private final Map<Integer, String> levelPermissions = new HashMap<>();
    private List<String> disabledWorlds = new ArrayList<>();

    // Animated displays
    private final Map<Integer, List<String>> animatedFrames = new HashMap<>();
    private final Map<Integer, Integer> frameIndex = new HashMap<>();

    private int tickSpeed = 20;
    private int internalTickCounter = 0;
    private int maxLevelDefault = 0;
    private int maxLevelGlobal = 0;
    private List<String> defaultFrames = new ArrayList<>();
    private final Map<String, Integer> permissionMaxLevels = new HashMap<>();

    public LevelSystemInstance(LevelSystem plugin, String id, File file) {
        this.plugin = plugin;
        this.id = id;
        this.file = file;
        load();
    }

    /* ---------------------------------------------------
       LOAD / SAVE
     --------------------------------------------------- */

    public void load() {
        try {
            if (!file.exists()) {
                plugin.saveResource(file.getName(), false);
            }
        } catch (IllegalArgumentException ignored) {}

        cfg = YamlConfiguration.loadConfiguration(file);

        tickSpeed = Math.max(1, cfg.getInt("tick-speed", 20));

        disabledWorlds = cfg.getStringList("disabled-worlds");
        if (disabledWorlds == null) disabledWorlds = new ArrayList<>();

        explicitXpPerLevel.clear();
        animatedFrames.clear();
        frameIndex.clear();
        levelPermissions.clear();
        permissionMaxLevels.clear();

        defaultFrames = readDisplayFrames("default-design.display");
        if (defaultFrames.isEmpty()) {
            defaultFrames.add("&eLevel %level%");
        }

        int maxFromLevels = 0;
        if (cfg.isConfigurationSection("levels")) {
            for (String key : cfg.getConfigurationSection("levels").getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    maxFromLevels = Math.max(maxFromLevels, level);
                } catch (NumberFormatException ignored) {}
            }
        }

        int configuredDefaultMax = cfg.getInt("max-levels.default", cfg.getInt("max_level", -1));
        if (configuredDefaultMax > 0) {
            maxLevelDefault = configuredDefaultMax;
        } else {
            maxLevelDefault = Math.max(1, maxFromLevels);
        }

        int maxFromPerms = 0;
        if (cfg.isConfigurationSection("max-levels.permissions")) {
            for (String perm : cfg.getConfigurationSection("max-levels.permissions").getKeys(false)) {
                int value = cfg.getInt("max-levels.permissions." + perm, -1);
                if (value > 0) {
                    permissionMaxLevels.put(perm, value);
                    maxFromPerms = Math.max(maxFromPerms, value);
                }
            }
        }

        maxLevelGlobal = Math.max(maxLevelDefault, Math.max(maxFromLevels, maxFromPerms));

        for (int level = 1; level <= maxLevelGlobal; level++) {
            String key = String.valueOf(level);
            String basePath = "levels." + key;

            // XP override
            if (cfg.contains(basePath + ".xp")) {
                explicitXpPerLevel.put(level, cfg.getInt(basePath + ".xp"));
            }

            // Permission (optional)
            if (cfg.isString(basePath + ".permission")) {
                levelPermissions.put(level, cfg.getString(basePath + ".permission"));
            }

            // Display (default fallback)
            List<String> frames = readDisplayFrames(basePath + ".display");
            if (frames.isEmpty()) {
                frames = new ArrayList<>(defaultFrames);
            }
            if (frames.isEmpty()) {
                frames.add("&eLevel %level%");
            }

            animatedFrames.put(level, frames);
            frameIndex.put(level, 0);
        }
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save LevelSystem: " + id);
            e.printStackTrace();
        }
    }

    /* ---------------------------------------------------
       BASIC GETTERS
     --------------------------------------------------- */

    public String getId() {
        return id;
    }

    public FileConfiguration getConfig() {
        return cfg;
    }

    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName);
    }

    /* ---------------------------------------------------
       XP & LEVEL LOGIC (PERMISSION AWARE)
     --------------------------------------------------- */

    public int getRequiredXPForLevel(int level) {
        if (explicitXpPerLevel.containsKey(level)) {
            return explicitXpPerLevel.get(level);
        }

        double base = cfg.getDouble("formula.base", 100.0);
        double multiplier = cfg.getDouble("formula.multiplier", 2.0);
        return (int) Math.round(base * Math.pow(level, multiplier));
    }

    public int getPlayerXP(UUID uuid) {
        return cfg.getInt("players." + uuid + ".xp", 0);
    }

    public void setPlayerXP(UUID uuid, int xp) {
        cfg.set("players." + uuid + ".xp", Math.max(0, xp));
        save();
    }

    /**
     * RAW level from XP only (no permissions)
     */
    public int getLevelFromXP(int xp) {
        return getLevelFromXP(xp, maxLevelDefault);
    }

    public int getLevelFromXP(int xp, int maxLevel) {
        if (maxLevel <= 0) return 0;
        int level = 1;
        while (level <= maxLevel && xp >= getRequiredXPForLevel(level)) {
            level++;
        }
        return Math.max(0, Math.min(maxLevel, level - 1));
    }

    /**
     * EFFECTIVE level (permission capped)
     */
    public int getPlayerLevel(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        int maxLevelForPlayer = maxLevelDefault;
        if (player == null) {
            return getLevelFromXP(getPlayerXP(uuid), maxLevelForPlayer);
        }

        maxLevelForPlayer = getMaxLevelForPlayer(player);
        int rawLevel = getLevelFromXP(getPlayerXP(uuid), maxLevelForPlayer);
        int allowedLevel = 0;

        for (int lvl : animatedFrames.keySet()) {
            if (lvl > rawLevel || lvl > maxLevelForPlayer) continue;

            String perm = levelPermissions.get(lvl);
            if (perm == null || player.hasPermission(perm)) {
                allowedLevel = Math.max(allowedLevel, lvl);
            }
        }
        return allowedLevel;
    }

    public int getMaxLevel() {
        return maxLevelDefault;
    }

    public int getMaxLevelForPlayer(Player player) {
        int max = maxLevelDefault;
        if (player == null) return max;
        for (Map.Entry<String, Integer> entry : permissionMaxLevels.entrySet()) {
            if (player.hasPermission(entry.getKey())) {
                max = Math.max(max, entry.getValue());
            }
        }
        return max;
    }

    private List<String> readDisplayFrames(String path) {
        List<String> frames = new ArrayList<>();
        if (cfg.isList(path)) {
            for (String s : cfg.getStringList(path)) {
                if (s != null) frames.add(s);
            }
        } else if (cfg.isString(path)) {
            String value = cfg.getString(path);
            if (value != null) frames.add(value);
        }
        return frames;
    }

    /* ---------------------------------------------------
       ANIMATION TICK
     --------------------------------------------------- */

    public void tick() {
        internalTickCounter++;
        if (internalTickCounter % tickSpeed != 0) return;

        for (Map.Entry<Integer, List<String>> entry : animatedFrames.entrySet()) {
            if (entry.getValue().size() <= 1) continue;

            int lvl = entry.getKey();
            int idx = frameIndex.getOrDefault(lvl, 0);
            frameIndex.put(lvl, (idx + 1) % entry.getValue().size());
        }
    }

    /* ---------------------------------------------------
       DISPLAY
     --------------------------------------------------- */

    public String getDisplayForLevel(int level) {
        List<String> frames = animatedFrames.get(level);
        if (frames == null || frames.isEmpty()) {
            return ChatColor.YELLOW + "Level " + level;
        }

        int idx = frameIndex.getOrDefault(level, 0);
        String raw = frames.get(Math.min(idx, frames.size() - 1));

        return ChatColor.translateAlternateColorCodes(
                '&',
                raw.replace("%level%", String.valueOf(level))
        );
    }

    /* ---------------------------------------------------
       REWARDS (PERMISSION SAFE)
     --------------------------------------------------- */

    public boolean hasReward(UUID uuid, int level) {
        return cfg.getIntegerList("players." + uuid + ".rewarded-levels").contains(level);
    }

    private void markRewarded(UUID uuid, int level) {
        String path = "players." + uuid + ".rewarded-levels";
        List<Integer> list = cfg.getIntegerList(path);
        if (!list.contains(level)) {
            list.add(level);
            cfg.set(path, list);
            save();
        }
    }

    public void giveRewardIfPresent(Player player, int level) {

        String perm = levelPermissions.get(level);
        if (perm != null && !player.hasPermission(perm)) return;

        String base = getRewardBasePath(level);
        if (base == null) return;
        if (hasReward(player.getUniqueId(), level)) return;

        String type = cfg.getString(base + ".type", "").toLowerCase();

        if (type.equals("command")) {
            for (String cmd : cfg.getStringList(base + ".commands")) {
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(),
                        cmd.replace("%player_name%", player.getName())
                                .replace("%level%", String.valueOf(level))
                );
            }
        }

        if (type.equals("message")) {
            for (String msg : cfg.getStringList(base + ".messages")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes(
                        '&',
                        msg.replace("%level%", String.valueOf(level))
                ));
            }
        }

        markRewarded(player.getUniqueId(), level);
    }

    private String getRewardBasePath(int level) {
        String perLevel = "levels." + level + ".reward";
        if (cfg.isConfigurationSection(perLevel)) {
            return perLevel;
        }
        if (cfg.isConfigurationSection("default-reward")) {
            return "default-reward";
        }
        return null;
    }
}
