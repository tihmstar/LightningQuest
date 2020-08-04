package net.tihmstar.LightningQuest;

import net.minecraft.entity.Entity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public class EnemyCompass extends CompassItem {
    private BlockPos pointpos = null;
    public EnemyCompass(Item.Properties builder, BlockPos pointpos) {
        super(builder);
        this.pointpos = pointpos;
    }

    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isRemote) {
            ((ServerWorld)worldIn).getPointOfInterestManager().func_234135_a_(PointOfInterestType.field_234166_w_, pointpos);
            if (func_234670_d_(stack)) {
                CompoundNBT compoundnbt = stack.getOrCreateTag();
                if (compoundnbt.contains("LodestoneTracked") && !compoundnbt.getBoolean("LodestoneTracked")) {
                    return;
                }

                Optional<RegistryKey<World>> optional = func_234667_a_(compoundnbt);
                if (optional.isPresent() && optional.get() == worldIn.func_234923_W_() && compoundnbt.contains("LodestonePos") && !((ServerWorld)worldIn).getPointOfInterestManager().func_234135_a_(PointOfInterestType.field_234166_w_, NBTUtil.readBlockPos(compoundnbt.getCompound("LodestonePos")))) {
                    compoundnbt.remove("LodestonePos");
                }
            }

        }
    }
}
