package net.tihmstar.LightningQuest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.world.NoteBlockEvent;

import java.time.Instant;
import java.util.*;

public class Squad {
    private ArrayList<UUID> players = new ArrayList<>();
    private ArrayList<UUID> invites = new ArrayList<>();
    public boolean massKillingInProgress = false;
    public String squadName;
    public int onlineSquadPlayers = 0;
    public HashMap<UUID, Long> playersLastTeleport = new HashMap<UUID, Long>();

    private const long teleportTimeout = 300;//seconds

    public Squad(String squadName) {
        this.squadName = squadName;
    }

    public void join(UUID player) {
        if (players.isEmpty() || invites.contains(player)) {
            ++onlineSquadPlayers;
            players.add(player);
            if (invites.contains(player)){
                // delete invitation
                invites.remove(player);
            }
        }

    }

    public void leave(UUID player) {
        --onlineSquadPlayers;
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

    public float getDamageMultiplier() {
        switch (onlineSquadPlayers){
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

    public boolean playerCanDoTeleport(UUID playerUUID){
        Long curTP = Instant.now().getEpochSecond();
        if (playersLastTeleport.containsKey(playerUUID)){
            Long lastTP = playersLastTeleport.get(playerUUID);
            if (curTP - lastTP < teleportTimeout){
                return false; //we are not yet ready to tp
            }
        }
        playersLastTeleport.put(playerUUID, curTP);
        return true;
    }

}
