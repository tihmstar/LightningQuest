package net.tihmstar.LightningQuest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.world.NoteBlockEvent;

import java.util.*;

public class Squad {
    private ArrayList<PlayerEntity> players = new ArrayList<>();
    private ArrayList<UUID> invites = new ArrayList<>();
    public String squadName;

    public Squad(String squadName) {
        this.squadName = squadName;
    }

    public void join(PlayerEntity player) {
        if (players.isEmpty() || invites.contains(player.getUniqueID())) {
            players.add(player);
            if (invites.contains(player.getUniqueID())) {
                // delete invitation
                invites.remove(player.getUniqueID());
            }
        }

    }

    public void leave(PlayerEntity player) {
        players.remove(player);
    }

    public void invite(PlayerEntity player) {
        if (!players.contains(player) && !invites.contains(player)){
            invites.add(player.getUniqueID());
        }
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public void killAllPlayers() {
        for (PlayerEntity player: players) {
            // kill player in squad
            player.onKillCommand();
        }
    }

    public double getDamageMultiplier() {
        // TODO: implement logic to change damage by looking at squad size
        return 1;
    }
}
