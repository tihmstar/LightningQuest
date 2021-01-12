package net.tihmstar.lightningquest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SquadCommand implements CommandExecutor {
    private final SquadManager squads;

    public SquadCommand(SquadManager squads){
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
            player.sendMessage("Missing argument!");
            //show helpscreen
            return false;
        }
        switch (args[0]){
            case "create":
                handleCreate(player, Arrays.copyOfRange(args,1,args.length));
                return true;
            case "invite":
                handleInvite(player, Arrays.copyOfRange(args,1,args.length));
                return true;
            case "join":
                handleJoin(player, Arrays.copyOfRange(args,1,args.length));
                return true;
            case "leave":
                handleLeave(player, Arrays.copyOfRange(args,1,args.length));
                return true;
            case "listinvites":
                handleListinvites(player, Arrays.copyOfRange(args,1,args.length));
                return true;
            case "info":
                handleInfo(player, Arrays.copyOfRange(args,1,args.length));
                return true;

            default:
                break;
        }
        player.sendMessage("Invalid argument");
        return false;
    }

    private void handleCreate(Player sender, String[] args){
        if (args.length < 1){
            sender.sendMessage(ChatColor.RED + "Squadname missing!" + ChatColor.RESET);
        }else{
            squads.playerCreateSquad(sender,args[0]);
        }
    }

    private void handleInvite(Player sender, String[] args){
        if (args.length < 1){
            sender.sendMessage(ChatColor.RED + "Please specify a player you want to invite!" + ChatColor.RESET);
        }else{
            squads.playerInviteToSquad(sender,args[0]);
        }
    }

    private void handleJoin(Player sender, String[] args){
        if (args.length < 1){
            sender.sendMessage(ChatColor.RED + "Please specify the squad you want to join!" + ChatColor.RESET);
        }else{
            squads.playerJoinSquadByName(sender,args[0]);
        }
    }

    private void handleLeave(Player sender, String[] args){
        squads.playerLeaveSquad(sender);
    }

    private void handleListinvites(Player sender, String[] args){
        squads.playerListinvites(sender);
    }

    private void handleInfo(Player sender, String[] args){
        squads.playerSquadInfo(sender);
    }
}
