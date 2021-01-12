package net.tihmstar.lightningquest;

import javafx.scene.control.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

public class SquadManager {
    final private HashMap<String, Squad> playerToSquad = new HashMap<String, Squad>();
    final private HashMap<String, Squad> squads = new HashMap<String,Squad>();
    final private HashMap<String, HashSet<String>> invitesForPlayer = new HashMap<String, HashSet<String>>();

    void playerCreateSquad(Player player, String name){
        if (playerToSquad.containsKey(player.getName())){
            String s = "You need to leave your old squad before you can join a new one.\n";
            s += ChatColor.GREEN + name + ChatColor.RESET;
            s+= " won't forget your betrayal!";

            player.sendMessage(s);
            return;
        }

        if (squads.containsKey(name)){
            String s = "Squad ";
            s+= ChatColor.GREEN + name + ChatColor.RESET;
            s+= " already exists, but they don't want you to be part of it!";

            player.sendMessage(s);
            return;
        }

        Squad newSquad = new Squad(name);
        newSquad.addPlayerToSquad(player.getName());
        squads.put(name,newSquad);
        playerToSquad.put(player.getName(), newSquad);

        String s = "You successfully created squad ";
        s += ChatColor.GREEN + name + ChatColor.RESET;
        s += "!\nHold the burden of carrying a bunch of idiots";
        player.sendMessage(s);
    }

    public void playerInviteToSquad(Player player, String playerName){
        Squad squad = playerToSquad.get(player.getName());
        if (squad == null){
            String s = "You don't belong to any Squad, maybe you should fix that?";

            player.sendMessage(s);
            return;
        }
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null){
            String s = "Seems like the player you are trying to invite doesn't exist\n";
            s += "Inviting imaginary people won't help you here";

            player.sendMessage(s);
            return;
        }

        if (playerToSquad.containsKey(playerName)){
            String s = "Player " + ChatColor.RED + playerName + ChatColor.RESET +" already has friends, go find some elsewhere\n";

            player.sendMessage(s);
            return;
        }

        HashSet<String> targetPlayerInvites = invitesForPlayer.get(playerName);
        if (targetPlayerInvites == null){
            targetPlayerInvites = new HashSet<String>();
        }
        targetPlayerInvites.add(squad.getName());
        invitesForPlayer.put(playerName,targetPlayerInvites);

        {
            String s = ChatColor.RED + player.getName() + ChatColor.RESET;
            s += " invited you to the squad ";
            s += ChatColor.GREEN + squad.getName() + ChatColor.RESET;
            s += "\nIs it even worth joining?";
            targetPlayer.sendMessage(s);
        }

        {
            String s = "You successfully invited ";
            s += ChatColor.RED + playerName + ChatColor.RESET;
            s += " to your squad.\nThis wasn't your brightest idea";
            player.sendMessage(s);
        }
    }

    public void playerJoinSquadByName(Player player, String squadname){
        Squad oldSquad = playerToSquad.get(player.getName());
        if (oldSquad != null) {
            String s = "You need to leave your old squad before you can join a new one.\n";
            s += ChatColor.GREEN + oldSquad.getName() + ChatColor.RESET;
            s+= " won't forget your betrayal!";
            player.sendMessage(s);
            return;
        }

        Squad squad = squads.get(squadname);
        if (squad == null){
            String s = "Squad ";
            s += ChatColor.GREEN + squadname + ChatColor.RESET;
            s += " does not exist";

            player.sendMessage(s);
            return;
        }

        HashSet<String> playerInvites = invitesForPlayer.get(player.getName());
        if (playerInvites == null){
            playerInvites = new HashSet<String>();
        }

        if (!playerInvites.contains(squadname)){
            String s = "Squad ";
            s += ChatColor.GREEN + squadname + ChatColor.RESET;
            s += " doesn't want you to be part of it";

            player.sendMessage(s);
            return;
        }

        invitesForPlayer.remove(player.getName()); //drop all other invites for this player, since he already made a decision!
        squad.addPlayerToSquad(player.getName());
        playerToSquad.put(player.getName(),squad);

        {
            String s = "An idiot called ";
            s += ChatColor.RED + player.getName() + ChatColor.RESET;
            s += " joined your squad";
            squad.sendMessage(s);
        }

        {
            String s = "You successfully joined ";
            s += ChatColor.GREEN + squadname + ChatColor.RESET;
            s += "!\nLet's hope you aren't just ballast for them";
            player.sendMessage(s);
        }
    }

    public void playerLeaveSquad(Player player){
        Squad squad = playerToSquad.get(player.getName());
        if (squad == null){
            String s = "You do not belong to a squad :(\nYou are already alone";
            player.sendMessage(s);
            return;
        }

        squad.removePlayerFromSquad(player.getName());
        squad.onlineSquadPlayers--;
        playerToSquad.remove(player.getName());

        String s = "You left the squad ";
        s += ChatColor.GREEN + squad.getName() + ChatColor.RESET;
        s += "\nGood luck on your own";

        player.sendMessage(s);

        if (squad.getNumberOfMembers() == 0) {
            // delete empty squad
            squads.remove(squad.getName());
        }
    }

    public void playerListinvites(Player player){
        HashSet<String> invites = invitesForPlayer.get(player.getName());
        if (invites == null){
            String s = "There are no pending squad invites. Well, sucks to be you";
            player.sendMessage(s);
            return;
        }

        {
            String s = "";
            for (String i : invites){
                if (s.length() > 0){
                    s += ", ";
                }
                s += ChatColor.GREEN + i + ChatColor.RESET;
            }
            s = "You can join the following squads: " + s;
            player.sendMessage(s);
        }
    }

    public void playerSquadInfo(Player player){
        Squad squad = playerToSquad.get(player.getName());
        if (squad == null) {
            String s = "You do not belong to a squad :(\nGo find some friends";
            player.sendMessage(s);
            return;
        }

        String reply = "You are in squad ";
        reply += ChatColor.GREEN + squad.getName() + ChatColor.RESET;
        reply += " which has ";
        reply += ChatColor.BLUE + String.format("%d",squad.getNumberOfMembers()) + ChatColor.RESET;
        reply += " members:";

        for (String playername : squad.getSquadMembers()){
            reply += "\n" + ChatColor.RED + playername + ChatColor.RESET;
        }
        reply += "\nYour ";
        reply += ChatColor.GOLD + "Damage" + ChatColor.RESET;
        reply += " is currently multiplied by ";
        reply += ChatColor.BLUE + String.format("%.2f",squad.getDamageMultiplier()) + ChatColor.RESET;

        reply += "\nAdvancementPoints: " + String.format("%d",squad.advancementsPool);

        player.sendMessage(reply);
    }

    public void killPlayerSquad(Player player, boolean isInstantKill){
        Squad squad = playerToSquad.get(player.getName());
        if (squad != null){
            squad.killAllMembers(isInstantKill);
        }
    }

    public Squad squadForPlayer(Player player){
        return playerToSquad.get(player.getName());
    }

    public HashMap<String, HashSet<String>> getInvitesForPlayer() {
        return invitesForPlayer;
    }
}
