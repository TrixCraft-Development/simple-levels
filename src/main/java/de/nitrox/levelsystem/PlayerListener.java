package de.nitrox.levelsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final LevelSystem plugin;

    public PlayerListener(LevelSystem plugin) {
        this.plugin = plugin;
    }

    // Example: set player's vanilla level to highest across systems (optional UX)
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // optional: compute highest level across systems and set player's exp level
        int highest = 0;
        for (LevelSystemInstance inst : plugin.getManager().getAll().values()) {
            int lvl = inst.getPlayerLevel(e.getPlayer().getUniqueId());
            if (lvl > highest) highest = lvl;
        }
        e.getPlayer().setLevel(highest);
    }
}