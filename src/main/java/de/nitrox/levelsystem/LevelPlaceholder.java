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
        return "simplelevels";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null || params == null || params.isEmpty()) return "";

        if (params.endsWith("_level_raw")) {
            String systemId = params.substring(0, params.length() - "_level_raw".length());
            return handleRaw(systemId, player);
        }
        if (params.endsWith("_level")) {
            String systemId = params.substring(0, params.length() - "_level".length());
            return handleFormatted(systemId, player);
        }

        return null;
    }

    private String handleFormatted(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        int lvl = inst.getPlayerLevel(player.getUniqueId());
        return inst.getAnimatedDisplay(lvl);
    }

    private String handleRaw(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        int lvl = inst.getPlayerLevel(player.getUniqueId());
        return String.valueOf(lvl);
    }
}