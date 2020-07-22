package net.tihmstar.LightningQuest;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class SaveLQData extends WorldSavedData {
    private static final String MODID = "lightningquest";
    private static final String DATA_NAME = MODID + "_SquadMemberships";

    // Required constructors
    public SaveLQData() {
        super(DATA_NAME);
    }
    public SaveLQData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {

    }
    
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        return nbt;
    }

    // WorldSavedData methods
}