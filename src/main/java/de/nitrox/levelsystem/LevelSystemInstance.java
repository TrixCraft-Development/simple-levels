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

    private final Map<Integer, Integer> explicitXpPerLevel = new HashMap<>();
    private List<String> disabledWorlds = new ArrayList<>();

    private final Map<Integer, List<String>> animatedFrames = new HashMap<>();
    private final Map<Integer, Integer> frameIndex = new HashMap<>();

    private int tickSpeed = 20;
    private int internalTickCounter = 0;

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
        } catch (IllegalArgumentException ignored) {
        }

        cfg = YamlConfiguration.loadConfiguration(file);

        tickSpeed = Math.max(1, cfg.getInt("tick-speed", 20));

        disabledWorlds = cfg.getStringList("disabled-worlds");
        if (disabledWorlds == null) disabledWorlds = new ArrayList<>();

        explicitXpPerLevel.clear();
        animatedFrames.clear();
        frameIndex.clear();

        if (cfg.isConfigurationSection("levels")) {
            for (String key : cfg.getConfigurationSection("levels").getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(key);

                    if (cfg.contains("levels." + key + ".xp")) {
                        int xp = cfg.getInt("levels." + key + ".xp");
                        explicitXpPerLevel.put(lvl, xp);
                    }

                    if (cfg.isList("levels." + key + ".display")) {
                        List<String> frames = cfg.getStringList("levels." + key + ".display");
                        List<String> clean = new ArrayList<>();
                        for (String s : frames) if (s != null) clean.add(s);
                        if (clean.isEmpty()) clean.add("&eLevel " + lvl);
                        animatedFrames.put(lvl, clean);
                        frameIndex.put(lvl, 0);
                    } else if (cfg.isString("levels." + key + ".display")) {
                        String single = cfg.getString("levels." + key + ".display");
                        animatedFrames.put(lvl, Collections.singletonList(single != null ? single : "&eLevel " + lvl));
                        frameIndex.put(lvl, 0);
                    } else {
                        animatedFrames.put(lvl, Collections.singletonList("&eLevel " + lvl));
                        frameIndex.put(lvl, 0);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public void save() {
        try {
            if (cfg != null) cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("FAILED to save LevelSystem config: " + id);
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public FileConfiguration getConfig() {
        return cfg;
    }

    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName);
    }

    public int getRequiredXPForLevel(int level) {
        if (explicitXpPerLevel.containsKey(level)) {
            return explicitXpPerLevel.get(level);
        }

        double base = cfg.getDouble("formula.base", 100.0);
        double multiplier = cfg.getDouble("formula.multiplier", 2.0);

        double value = base * Math.pow(level, multiplier);
        return Math.max(0, (int) Math.round(value));
    }

    public int getPlayerXP(UUID uuid) {
        return cfg.getInt("players." + uuid + ".xp", 0);
    }

    public int getXP(Player player) {
        return getPlayerXP(player.getUniqueId());
    }

    public void setPlayerXP(UUID uuid, int xp) {
        cfg.set("players." + uuid + ".xp", xp);
        save();
    }

    public void setXP(Player player, int xp) {
        setPlayerXP(player.getUniqueId(), xp);
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

    public void tick() {
        if (animatedFrames.isEmpty()) return;

        internalTickCounter++;
        if (internalTickCounter < 0) internalTickCounter = 0; // safety

        if (tickSpeed <= 1) {
            // advance every tick
            advanceAllFrames();
            internalTickCounter = 0;
            return;
        }

        if (internalTickCounter % tickSpeed == 0) {
            advanceAllFrames();
        }
    }

    private void advanceAllFrames() {
        for (Map.Entry<Integer, List<String>> entry : animatedFrames.entrySet()) {
            int level = entry.getKey();
            List<String> frames = entry.getValue();
            if (frames == null || frames.size() <= 1) continue;

            int idx = frameIndex.getOrDefault(level, 0);
            idx = (idx + 1) % frames.size();
            frameIndex.put(level, idx);
        }
    }

    public String getAnimatedDisplay(int level) {
        List<String> frames = animatedFrames.get(level);
        if (frames == null || frames.isEmpty()) return ChatColor.translateAlternateColorCodes('&', "&eLevel " + level);

        int idx = frameIndex.getOrDefault(level, 0);
        if (idx < 0 || idx >= frames.size()) idx = 0;

        String raw = frames.get(idx);
        if (raw == null) raw = "&eLevel " + level;
        raw = raw.replace("%level%", String.valueOf(level));
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getDisplayForLevel(int level) {
        return getAnimatedDisplay(level);
    }

    public int getTickSpeed() {
        return tickSpeed;
    }

    public void setTickSpeed(int tickSpeed) {
        this.tickSpeed = Math.max(1, tickSpeed);
    }
}