package de.nitrox.levelsystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class LevelPlaceholder extends PlaceholderExpansion {

    private final LevelSystem plugin;

    public LevelPlaceholder(LevelSystem plugin) {
        this.plugin = plugin;
    }

    @Override public boolean persist() { return true; }
    @Override public boolean canRegister() { return true; }

    @Override
    public String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "levelsystem";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {

        if (player == null || params == null || params.isEmpty()) {
            return "";
        }

        // format: <systemid>_<type>
        // example: default_level_raw
        int lastUnd = params.lastIndexOf('_');
        if (lastUnd <= 0) return null;

        String systemId = params.substring(0, lastUnd);
        String type = params.substring(lastUnd + 1).toLowerCase();

        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;

        int level = inst.getPlayerLevel(player.getUniqueId());

        // FORMATTED LEVEL
        if (type.equals("level")) {
            return inst.getDisplayForLevel(level);
        }

        // RAW LEVEL (supports both versions)
        if (type.equals("level_raw") || type.equals("levelraw")) {
            return String.valueOf(level);
        }

        return null;
    }
}