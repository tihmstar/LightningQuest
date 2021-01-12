package net.tihmstar.lightningquest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Worker implements Runnable {
    final private SquadManager squads;
    final private ReentrantLock mutex = new ReentrantLock();
    private HashMap<String, Location> compassDst = new HashMap<String, Location>();

    Worker(SquadManager squads){
        this.squads = squads;
    }

    @Override
    public void run() {
        mutex.lock();
        HashMap<String, Location> dsts = compassDst;
        compassDst = new HashMap<String, Location>();
        mutex.unlock();

        for (String pname : dsts.keySet()){
            Player p = Bukkit.getPlayer(pname);
            Location dst = dsts.get(pname);
            if (p != null && dst != null){
                p.setCompassTarget(dst);
            }
        }
    }

    public Player updateNearestEnemy(Player player){
        Squad squad = squads.squadForPlayer(player);
        if (squad == null) return null;
        Player nearestEnemy = getNearestEnemy(player, squad);
        if (nearestEnemy == null) return null;
        String pname = player.getName();
        Location eloc = nearestEnemy.getLocation();
        mutex.lock();
        compassDst.put(pname,eloc);
        mutex.unlock();
        return nearestEnemy;
    }

    private Player getNearestEnemy(Player player, Squad squad) {
        Location playerLocation = player.getLocation();
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(player.getWorld()))
                .filter(p -> !squad.getSquadMembers().contains(p.getName()))
                .filter(p -> squads.squadForPlayer(p) != null) //tracked player has to be in a squad
                .min(Comparator.comparing(p -> p.getLocation().distance(playerLocation))).orElse(null);
    }
}
