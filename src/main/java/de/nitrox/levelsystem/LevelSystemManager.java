package de.nitrox.levelsystem;

import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;

public class LevelSystemManager {

    private final LevelSystem plugin;
    private final Map<String, LevelSystemInstance> systems = new HashMap<>();
    private int taskId = -1;

    public LevelSystemManager(LevelSystem plugin) {
        this.plugin = plugin;
    }

    public void loadSystems() {
        systems.clear();

        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();

        File defaultFile = new File(folder, "default.yml");
        if (!defaultFile.exists()) {
            try {
                plugin.saveResource("default.yml", false);
                plugin.getLogger().info("Generated missing default.yml");
            } catch (IllegalArgumentException e) {
                try {
                    plugin.saveResource("default.yml", false);
                    plugin.getLogger().info("Generated missing default.yml (fallback)");
                } catch (IllegalArgumentException ignored) {
                    plugin.getLogger().severe("Default levels template missing in JAR default.yml");
                }
            }
        }

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

        startAnimationTask();
    }

    public LevelSystemInstance get(String identifier) {
        if (identifier == null) return null;
        return systems.get(identifier.toLowerCase());
    }

    public Map<String, LevelSystemInstance> getAll() {
        return systems;
    }

    public Set<String> getIdentifiers() {
        return new HashSet<>(systems.keySet());
    }

    public void startAnimationTask() {
        // If already running, cancel first
        stopAnimationTask();

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                for (LevelSystemInstance inst : systems.values()) {
                    inst.tick();
                }
            } catch (Throwable t) {
                plugin.getLogger().severe("Error while advancing animation frames:");
                t.printStackTrace();
            }
        }, 1L, 1L).getTaskId();
    }

    public void stopAnimationTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}