package de.nitrox.levelsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final LevelSystem plugin;

    public PlayerListener(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // plugin.getManager().getAll().values().forEach(inst -> inst.getPlayerXP(e.getPlayer().getUniqueId()));
    }
}