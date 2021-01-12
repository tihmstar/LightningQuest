package net.tihmstar.lightningquest;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Material.COMPASS;

public class LightningQuest extends JavaPlugin {

    @Override
    public void onEnable(){
        //Fired when the server enables the plugin
        Config config = new Config(this);
        SquadManager squads = new SquadManager();


        PluginCommand squadcmd = this.getCommand("squad");
        squadcmd.setExecutor(new SquadCommand(squads));
        squadcmd.setTabCompleter(new SquadTabCompleter(squads));

        if (config.isSquadTp()){
            PluginCommand squadtpcmd = this.getCommand("squadtp");
            squadtpcmd.setExecutor(new SquadTpCommand(squads));
            squadtpcmd.setTabCompleter(new SquadTpTabCompleter(squads));
        }

        Worker wrkr = null;
        if (config.isCompassTracking()){
            wrkr = new Worker(squads);
            getLogger().info("CompassTracking Enabled");
            getServer().getScheduler().scheduleSyncRepeatingTask(this, wrkr, 1, 10);//20 ticks = 1 second
        }else{
            getLogger().info("CompassTracking Disabled");
        }

        getServer().getPluginManager().registerEvents(new EventListener(squads, wrkr), this);
    }

    @Override
    public void onDisable(){
        //Fired when the server stops and disables all plugins
        //TODO: save stuff
    }
}
