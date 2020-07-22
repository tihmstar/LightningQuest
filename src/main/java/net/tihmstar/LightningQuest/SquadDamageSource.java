package net.tihmstar.LightningQuest;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SquadDamageSource extends DamageSource {
    public SquadDamageSource() {
        super("SquadKill");
        this.setDamageBypassesArmor().setDamageAllowedInCreativeMode();
    }

    public ITextComponent getDeathMessage(LivingEntity entityLivingBaseIn) {
        PlayerEntity player = (PlayerEntity)entityLivingBaseIn;
        StringTextComponent msg = new StringTextComponent(String.format("%s died because his squad's incompetence",player.getName().getString()));
        return msg;
    }
}
