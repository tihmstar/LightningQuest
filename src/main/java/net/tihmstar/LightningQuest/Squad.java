package net.tihmstar.LightningQuest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.world.NoteBlockEvent;

import java.time.Instant;
import java.util.*;

public class Squad {
    public String squadName;
    private ArrayList<UUID> players = new ArrayList<>();
    private ArrayList<UUID> invites = new ArrayList<>();
    private HashMap<UUID, Long> playersLastTeleport = new HashMap<UUID, Long>();

    public boolean massKillingInProgress = false;
    public int onlineSquadPlayers = 0;

    final private long teleportTimeout = 300;//seconds

    public Squad(String squadName) {
        this.squadName = squadName;
    }

    public Squad(JsonObject obj){
        //unserialize from json obj

        squadName = obj.get("squadName").getAsString();

        {
            ArrayList<UUID> lplayers = new ArrayList<>();
            JsonArray playersObj = obj.get("players").getAsJsonArray();
            for (JsonElement pe : playersObj){
                String ps = pe.getAsString();
                lplayers.add(UUID.fromString(ps));
            }
            players = lplayers;
        }

        {
            ArrayList<UUID> linvites = new ArrayList<>();
            JsonArray invitesObj = obj.get("invites").getAsJsonArray();
            for (JsonElement ie : invitesObj){
                String is = ie.getAsString();
                linvites.add(UUID.fromString(is));
            }
            invites = linvites;
        }

        {
            HashMap<UUID, Long> lplayersLastTeleport = new HashMap<UUID, Long>();
            JsonObject playersLastTeleportObj = obj.get("playersLastTeleport").getAsJsonObject();
            for (Map.Entry<String, JsonElement> plt : playersLastTeleportObj.entrySet()){
                UUID playeruuid = UUID.fromString(plt.getKey());
                Long tpdate = plt.getValue().getAsLong();
                lplayersLastTeleport.put(playeruuid,tpdate);
            }
            playersLastTeleport = lplayersLastTeleport;
        }

    }

    public JsonObject serializeToJson(){
        JsonObject ret = new JsonObject();

        ret.add("squadName", new JsonPrimitive(squadName));

        {
            JsonArray playersObj = new JsonArray();
            for (UUID p : players) playersObj.add(p.toString());
            ret.add("players",playersObj);
        }

        {
            JsonArray invitesObj = new JsonArray();
            for (UUID i : invites) invitesObj.add(i.toString());
            ret.add("invites",invitesObj);
        }

        {
            JsonObject playersLastTeleportObj = new JsonObject();
            for (UUID p : playersLastTeleport.keySet()) playersLastTeleportObj.add(p.toString(),new JsonPrimitive(playersLastTeleport.get(p).toString()));
            ret.add("playersLastTeleport",playersLastTeleportObj);
        }

        return ret;
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
