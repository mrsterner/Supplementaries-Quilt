package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.util.MathHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Shadow
    @Nullable
    public abstract MobEffectInstance getEffect(MobEffect pPotion);

    @Inject(method = "getJumpBoostPower", at = @At("RETURN"), cancellable = true)
    private void getJumpBoostPower(CallbackInfoReturnable<Double> cir) {
        var effect = this.getEffect(ModRegistry.OVERENCUMBERED.get());
        if (effect != null && effect.getAmplifier() > 0) cir.setReturnValue(cir.getReturnValue() - 0.1);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
    private void handleOnClimbable(Vec3 motion, CallbackInfoReturnable<Vec3> info) {
        if (this.onClimbable() && ServerConfigs.cached.ROPE_SLIDE) {
            BlockState b = this.getFeetBlockState();
            if (b.is(ModRegistry.ROPE.get())) {
                this.fallDistance = 0;
                double x = MathHelper.clamp(motion.x, -0.15F, 0.15F);
                double z = MathHelper.clamp(motion.z, -0.15F, 0.15F);
                double y = motion.y();

                if (this.isSuppressingSlidingDownLadder()) {
                    if (y < 0 && ((Object) this) instanceof Player) y = 0;
                    if (ropeTicks > 0) ropeTicks--;
                } else {
                    if (this.level.isClient()) {
                        //cant use oldY since it has already been set
                        if (this.getY() < lastY && y < -0.05) {
                            if (RopeBlock.playEntitySlideSound(((LivingEntity) (Object) this), ropeTicks) && ropeTicks != 0) {
                                ropeTicks = 1;
                            } else ropeTicks++;
                        }
                    }
                }
                info.setReturnValue(new Vec3(x, y, z));
            }
        } else if (ropeTicks > 0) ropeTicks--;
        lastY = this.getY();
    }

    @Unique
    private int ropeTicks = 0;
    @Unique
    private double lastY = 0;

}
