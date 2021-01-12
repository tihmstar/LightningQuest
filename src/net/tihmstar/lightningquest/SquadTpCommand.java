package net.tihmstar.lightningquest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SquadTpCommand implements CommandExecutor {
    private final SquadManager squads;

    public SquadTpCommand(SquadManager squads){
        this.squads = squads;
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is meant for players only");
            return false;
        }
        Player player = (Player) sender;
        if (args.length < 1){
            player.sendMessage("Missing destination player!");
            return false;
        }

        Squad squad = squads.squadForPlayer(player);
        if (squad == null){
            player.sendMessage("Only Squad members can use this command!");
            return true;
        }

        if (!squad.getSquadMembers().contains(args[0])){
            player.sendMessage("Destination player is not member of your squad");
            return true;
        }

        Player dstPlayer = Bukkit.getPlayer(args[0]);
        if (dstPlayer == null){
            player.sendMessage("Failed to look up destination player. Is the player online?");
            return true;
        }

        if (squad.advancementsPool <= 0){
            player.sendMessage("Your squad can not afford any more teleportations at the moment");
            return true;
        }
        squad.advancementsPool--;
        player.teleport(dstPlayer);
        String s = ChatColor.RED + player.getName() + ChatColor.RESET + " teleported to " + ChatColor.RED + dstPlayer.getName() + ChatColor.RESET;
        squad.sendMessage(s);
        return true;
    }
}
