package net.tihmstar.LightningQuest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.UUID;

public class SaveLQData extends WorldSavedData {
    private static final String MODID = "lightningquest";
    private static final String DATA_NAME = MODID + "_SquadMemberships";
    private static final String SQUADS_KEY  = DATA_NAME + "_squads" ;

    private String jsonData;

    // Required constructors
    public SaveLQData() {
        super(DATA_NAME);
    }
    public SaveLQData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        // TODO: deserialize JSON data
        jsonData = nbt.getString(SQUADS_KEY);
    }
    
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putString(SQUADS_KEY, jsonData);
        return nbt;
    }

    public void updateSquadMemberships(HashMap<UUID, Squad> uuidSquadHashMap) {
        //jsonData =
        // TODO: serialize JSON data
        markDirty();
    }

    // WorldSavedData methods
}