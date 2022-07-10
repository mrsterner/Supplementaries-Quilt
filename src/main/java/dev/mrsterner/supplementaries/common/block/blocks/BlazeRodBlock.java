package dev.mrsterner.supplementaries.common.block.blocks;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class BlazeRodBlock extends StickBlock {

    public BlazeRodBlock(AbstractBlock.Settings properties) {
        super(properties, 0);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, Boolean.FALSE).with(AXIS_Y, true).with(AXIS_X, false).with(AXIS_Z, false));
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!entity.isFireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity) entity)) {
            if (!(entity instanceof PlayerEntity p && p.isCreative()))
                entity.setOnFireFor(2);
        }
        super.onSteppedOn(world, pos, state, entity);
    }


	@Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, RandomGenerator random) {
        if (random.nextFloat() > 0.3) return;
        ArrayList<Integer> list = new ArrayList<>();
        if (state.get(AXIS_Y)) list.add(0);
        if (state.get(AXIS_X)) list.add(1);
        if (state.get(AXIS_Z)) list.add(2);
        int s = list.size();
        if (s > 0) {
			ParticleEffect particle = state.get(WATERLOGGED) ? ParticleTypes.BUBBLE : ParticleTypes.SMOKE;
            int c = list.get(random.nextInt(s));
            double x, y, z = x = y =0;
            switch (c) {
                case 0 -> {
                    x = (double) pos.getX() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                    y = (double) pos.getY() + random.nextFloat();
                    z = (double) pos.getZ() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                }
                case 1 -> {
                    y = (double) pos.getY() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                    x = (double) pos.getX() + random.nextFloat();
                    z = (double) pos.getZ() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                }
                case 2 -> {
                    y = (double) pos.getY() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                    z = (double) pos.getZ() + random.nextFloat();
                    x = (double) pos.getX() + 0.5D - 0.125 + random.nextFloat() * 0.25;
                }
            }
            world.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
