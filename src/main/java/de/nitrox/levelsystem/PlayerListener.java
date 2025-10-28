package de.nitrox.levelsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.entity.Player;

public class PlayerListener implements Listener {

    private final LevelSystem plugin;

    public PlayerListener(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int xp = plugin.getLevelConfig().getInt("players." + player.getUniqueId() + ".xp", 0);
        player.setLevel(calculateLevel(xp));
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int xp = plugin.getLevelConfig().getInt("players." + player.getUniqueId() + ".xp", 0) + event.getAmount();
        plugin.getLevelConfig().set("players." + player.getUniqueId() + ".xp", xp);
        plugin.saveLevelConfig();
        player.setLevel(calculateLevel(xp));
    }

    private int calculateLevel(int xp) {
        int level = 1;
        while (xp >= plugin.getRequiredXPForLevel(level)) {
            level++;
        }
        return level - 1;
    }
}
