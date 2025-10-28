package de.nitrox.levelsystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LevelPlaceholder extends PlaceholderExpansion {

    private final LevelSystem plugin;

    public LevelPlaceholder(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() { return true; }

    @Override
    public boolean canRegister() { return true; }

    @Override
    public String getAuthor() { return plugin.getDescription().getAuthors().toString(); }

    @Override
    public String getIdentifier() { return "levelsystem"; }

    @Override
    public String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        if (identifier.equalsIgnoreCase("level")) {
            int xp = plugin.getLevelConfig().getInt("players." + player.getUniqueId() + ".xp", 0);
            int level = calculateLevel(xp);

            // get display for level from leveldesign.yml under "levels.<level>.display"
            String path = "levels." + level + ".display";
            String raw = plugin.getLevelDesignConfig().getString(path);

            if (raw != null && !raw.isEmpty()) {
                // Replace %level% placeholder and translate color codes (&)
                String replaced = raw.replace("%level%", String.valueOf(level));
                return ChatColor.translateAlternateColorCodes('&', replaced);
            }

            // fallback: just return colored "Level X"
            return ChatColor.translateAlternateColorCodes('&', "&eLevel " + level);
        }

        return null;
    }

    private int calculateLevel(int xp) {
        int level = 1;
        while (xp >= plugin.getRequiredXPForLevel(level)) level++;
        return level - 1;
    }
}