package net.tihmstar.lightningquest;

import org.bukkit.Bukkit;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class Squad {
    private final String name;
    final private HashSet<String> squadMembers = new HashSet<String>();
    public int advancementsPool = 0;
    public int onlineSquadPlayers = 0;
    private boolean killingInProgress = false;

    Squad(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addPlayerToSquad(String player){
        squadMembers.add(player);
    }

    public void removePlayerFromSquad(String player){
        squadMembers.remove(player);
    }

    public void sendMessage(String message){
        for (String p : squadMembers){
            Player player = Bukkit.getPlayer(p);
            if (player != null){
                player.sendMessage("[Squad] "+message);
            }
        }
    }

    public int getNumberOfMembers(){
        return squadMembers.size();
    }

    public HashSet<String> getSquadMembers() {
        return squadMembers;
    }

    public float getDamageMultiplier(){
        switch (onlineSquadPlayers){
            case 1:
                return (float) 0.25;
            case 2:
                return (float) 0.5;
            case 3:
                return (float) 0.75;
            default:
                return 1;
        }
    }

    public void killAllMembers(){
        if (killingInProgress) return;
        killingInProgress = true;
        for (String pname : squadMembers){
            Player player = Bukkit.getPlayer(pname);
            if (player == null) continue;

            player.getWorld().strikeLightning(player.getLocation()); //might not kill?
        }
        killingInProgress = false;
    }

}
