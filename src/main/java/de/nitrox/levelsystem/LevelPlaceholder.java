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

        if (params.endsWith("_max_level")) {
            String systemId = params.substring(0, params.length() - "_max_level".length());
            return handleMaxLevel(systemId, player);
        }
        if (params.endsWith("_level_raw")) {
            String systemId = params.substring(0, params.length() - "_level_raw".length());
            return handleRaw(systemId, player);
        }
        if (params.endsWith("_level")) {
            String systemId = params.substring(0, params.length() - "_level".length());
            return handleFormatted(systemId, player);
        }
        if (params.endsWith("_xp_needed_next")) {
            String systemId = params.substring(0, params.length() - "_xp_needed_next".length());
            return handleXPNeededNext(systemId, player);
        }
        if (params.endsWith("_current_xp")) {
            String systemId = params.substring(0, params.length() - "_current_xp".length());
            return handlePlayerXP(systemId, player);
        }

        return null;
    }

    private String handleFormatted(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        int lvl = inst.getPlayerLevel(player.getUniqueId());
        return inst.getDisplayForLevel(lvl);
    }

    private String handleRaw(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        int lvl = inst.getPlayerLevel(player.getUniqueId());
        return String.valueOf(lvl);
    }

    private String handleXPNeededNext(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        int lvl = inst.getPlayerLevel(player.getUniqueId());
        return String.valueOf(inst.getRequiredXPForLevel(lvl + 1));
    }

    private String handlePlayerXP(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        int xp = inst.getPlayerXP(player.getUniqueId());
        return String.valueOf(xp);
    }

    private String handleMaxLevel(String systemId, Player player) {
        LevelSystemInstance inst = plugin.getManager().get(systemId);
        if (inst == null) return null;
        return String.valueOf(inst.getMaxLevel());
    }
}