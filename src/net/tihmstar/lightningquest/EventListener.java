package net.tihmstar.lightningquest;

import org.bukkit.ChatColor;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.COMPASS;

public class EventListener implements Listener {
    final private SquadManager squads;
    final private Worker wrkr;
    final private Config config;

    EventListener(SquadManager squads, Worker wrkr, Config config){
        this.squads = squads;
        this.wrkr = wrkr;
        this.config = config;
    }

    @EventHandler
    public void entityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player){
            Player player = (Player)entity;
            squads.killPlayerSquad(player,config.isInstantKill());
        }
    }

    @EventHandler
    public void onEntityDame(EntityDamageByEntityEvent e){
        Entity entity = e.getDamager();
        if (!(entity instanceof Player)) return;
        Player damager = (Player)entity;

        Squad squad = squads.squadForPlayer(damager);
        double damage = 0; //if you are not part of a squad, you don't deal damage. Deal with it :P
        if (squad != null){
            damage = e.getDamage() * squad.getDamageMultiplier();
        }
        e.setDamage(damage);
    }

    @EventHandler
    public void onPlayerConnect(PlayerJoinEvent e){
        Player player = e.getPlayer();
        Squad squad = squads.squadForPlayer(player);
        if (squad != null){
            squad.onlineSquadPlayers++;
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent e){
        Player player = e.getPlayer();
        Squad squad = squads.squadForPlayer(player);
        if (squad != null){
            squad.onlineSquadPlayers--;
        }
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent e) {
        Player player = e.getPlayer();
        Squad squad = squads.squadForPlayer(player);

        if (squad != null) {
            squad.advancementsPool++;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (wrkr != null){
            switch (e.getAction()){
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                    break;
                default:
                    return;
            }
            ItemStack is = e.getItem();
            if (is == null || is.getType() != COMPASS) return;
            Player player = e.getPlayer();
            Player enemy = wrkr.updateNearestEnemy(player);
            String tracking = null;
            if (enemy != null){
                Squad enemySquad = squads.squadForPlayer(enemy);
                if (enemySquad != null){
                    tracking = "Now tracking: " + ChatColor.GREEN + enemySquad.getName() + ChatColor.RESET;
                }else{
                    tracking = "Now tracking: <a player>";
                }
            }else{
                tracking = "Nobody to track";
            }
            player.sendMessage(tracking);
        }
    }
}
