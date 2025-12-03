package de.nitrox.levelsystem;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LevelSystemManager {

    private final LevelSystem plugin;
    private final Map<String, LevelSystemInstance> systems = new HashMap<>();

    public LevelSystemManager(LevelSystem plugin) {
        this.plugin = plugin;
    }

    public void loadSystems() {
        systems.clear();

        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();

        // --- NEW FEATURE: auto-create default.yml if missing ---
        File defaultFile = new File(folder, "default.yml");
        if (!defaultFile.exists()) {
            try {
                plugin.saveResource("default.yml", false);
                plugin.getLogger().info("Generated missing default.yml");
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("default.yml is NOT inside the plugin jar! Cannot auto-generate.");
            }
        }
        // -------------------------------------------------------

        // load all system files (*.yml)
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) return;

        for (File f : files) {
            String id = f.getName().substring(0, f.getName().length() - 4);

            try {
                LevelSystemInstance inst = new LevelSystemInstance(plugin, id, f);
                systems.put(id.toLowerCase(), inst);
                plugin.getLogger().info("Loaded LevelSystem: " + id);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load LevelSystem from file: " + f.getName());
                e.printStackTrace();
            }
        }
    }

    public LevelSystemInstance get(String identifier) {
        if (identifier == null) return null;
        return systems.get(identifier.toLowerCase());
    }

    public Map<String, LevelSystemInstance> getAll() {
        return systems;
    }
}