package net.tihmstar.lightningquest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SquadTabCompleter implements TabCompleter {
    private final SquadManager squads;

    public SquadTabCompleter(SquadManager squads){
        this.squads = squads;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if (args.length <= 1){
            //main commands
            return Arrays.asList("create", "invite", "join", "leave", "listinvites", "info");
        }
        //args has at least 2 elements at this point
        switch (args[0]){
            case "invite":
                //return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).filter(p -> p.startsWith(args[1])).collect(Collectors.toList());
                return null; //apparently default for getting players close by?
            case "join":
                return new ArrayList<>(squads.getInvitesForPlayer().get(sender.getName())); //empty list indicates not autocompletion from this point on
            default:
                break;
        }
        return new ArrayList<>(); //empty list indicates no autocompletion from this point on
    }
}
