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

    public void join(UUID player) {
        if (players.isEmpty() || invites.contains(player)) {
            players.add(player);
            if (invites.contains(player)){
                // delete invitation
                invites.remove(player);
            }
        }

    }

    public void leave(UUID player) {
        players.remove(player);
    }

    public void invite(UUID player) {
        if (!players.contains(player) && !invites.contains(player)){
            invites.add(player);
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

    public float getDamageMultiplier() {
        switch (players.size()){
            case 1:
                return 0;
            case 2:
                return (float) 0.5;
            case 3:
                return (float) 0.75;
            default:
                return 1;
        }
    }
}
