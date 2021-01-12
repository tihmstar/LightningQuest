package net.tihmstar.lightningquest;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SquadTpTabCompleter implements TabCompleter {
    private final SquadManager squads;

    public SquadTpTabCompleter(SquadManager squads){
        this.squads = squads;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
        if (!(sender instanceof Player)) {
            return new ArrayList<>(); //empty list indicates no autocompletion from this point on
        }
        Player player = (Player)sender;

        Squad squad = squads.squadForPlayer(player);
        if (squad != null){
            return new ArrayList<>(squad.getSquadMembers());
        }
        return new ArrayList<>(); //empty list indicates no autocompletion from this point on
    }
}
