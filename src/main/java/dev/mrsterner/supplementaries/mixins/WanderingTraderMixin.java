package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//why cant they open them already shm
@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {

    public WanderingTraderMixin(EntityType<? extends AbstractVillager> p_35267_, World p_35268_) {
        super(p_35267_, p_35268_);
    }

    @Inject(
            method = {"registerGoals"},
            at = {@At("RETURN")}
    )
    public void registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
    }
}
