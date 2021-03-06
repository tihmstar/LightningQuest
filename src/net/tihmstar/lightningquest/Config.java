package net.tihmstar.lightningquest;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {
    private final Plugin plugin;

    private boolean compassTracking;
    private boolean squadTp;
    private boolean isInstantKill;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reload config from config file located in plugins directory
     */
    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        config.options().copyDefaults(true);
        plugin.saveConfig();

        compassTracking = config.getBoolean("compass-tracking");
        squadTp = config.getBoolean("squad-tp");
        isInstantKill = config.getBoolean("squad-instant-kill");
    }

    public boolean isCompassTracking() {
        return compassTracking;
    }

    public boolean isSquadTp() {
        return squadTp;
    }

    public boolean isInstantKill() {
        return isInstantKill;
    }
}
