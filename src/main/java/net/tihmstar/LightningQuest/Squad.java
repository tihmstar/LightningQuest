package net.tihmstar.LightningQuest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.world.NoteBlockEvent;

import java.util.*;

public class Squad {
    private ArrayList<UUID> players = new ArrayList<>();
    private ArrayList<UUID> invites = new ArrayList<>();
    public boolean massKillingInProgress = false;
    public String squadName;

    public Squad(String squadName) {
        this.squadName = squadName;
    }

    public void join(PlayerEntity player) {
        if (players.isEmpty() || invites.contains(player.getUniqueID())) {
            players.add(player.getUniqueID());
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
        if (!players.contains(player.getUniqueID()) && !invites.contains(player.getUniqueID())){
            invites.add(player.getUniqueID());
        }
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public final List<UUID> getSquadMembers() {
        return players;
    }

    public final List<UUID> getInvites() {
        return invites;
    }

    public boolean startMassKilling() {
        /*
        returns true if this is the first member to start a mass killing
         */
        if (massKillingInProgress) {
            return false;
        }
        massKillingInProgress = true;
        return true;
    }

    public void stopMassKilling() {
        massKillingInProgress = false;
    }

    /*
    // TODO: move to ligthningquestmod
    public void killAllPlayers() {
        // do not allow recursion to prevent infinite loop
        // players in the same squad might kill each other indefinitely otherwise
        if (massKillingInProgress) {
            return;
        }
        massKillingInProgress = true;
        for (PlayerEntity player: players) {
            // kill player in squad
            player.onKillCommand();
        }
        massKillingInProgress = false;
    }
    *
     */

    public double getDamageMultiplier() {
        // TODO: implement logic to change damage by looking at squad size
        return 1;
    }
}
