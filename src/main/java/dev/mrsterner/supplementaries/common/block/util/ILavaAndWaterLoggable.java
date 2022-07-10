package net.mehvahdjukaar.supplementaries.common.block.util;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public interface ILavaAndWaterLoggable extends BucketPickup, LiquidBlockContainer {

    default boolean canPlaceLiquid(BlockGetter reader, BlockPos pos, BlockState state, Fluid fluid) {
        return (!state.get(BlockProperties.LAVALOGGED) && fluid == Fluids.LAVA)
                || (!state.get(BlockStateProperties.WATERLOGGED) && fluid == Fluids.WATER);
    }

    default boolean placeLiquid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(BlockProperties.LAVALOGGED) && fluidState.getType() == Fluids.LAVA) {
            if (!world.isClient()()) {
                world.setBlockState(pos, state.with(BlockProperties.LAVALOGGED, Boolean.TRUE), 3);
                world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        } else if (!state.get(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!world.isClient()()) {
                world.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, Boolean.TRUE), 3);
                world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
            }

            return true;
        }
        return false;
    }

    default Fluid takeLiquid(WorldAccess world, BlockPos pos, BlockState state) {
        if (state.get(BlockProperties.LAVALOGGED)) {
            world.setBlockState(pos, state.with(BlockProperties.LAVALOGGED, Boolean.FALSE), 3);
            return Fluids.LAVA;
        } else if (state.get(BlockStateProperties.WATERLOGGED)) {
            world.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, Boolean.FALSE), 3);
            return Fluids.WATER;
        }

        return Fluids.EMPTY;

    }


    default ItemStack pickupBlock(WorldAccess pLevel, BlockPos pPos, BlockState pState) {
        if (pState.get(BlockStateProperties.WATERLOGGED)) {
            pLevel.setBlockState(pPos, pState.with(BlockStateProperties.WATERLOGGED, Boolean.FALSE), 3);
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.destroyBlock(pPos, true);
            }

            return new ItemStack(Items.WATER_BUCKET);
        } else if (pState.get(BlockProperties.LAVALOGGED)) {
            pLevel.setBlockState(pPos, pState.with(BlockProperties.LAVALOGGED, Boolean.FALSE), 3);
            if (!pState.canSurvive(pLevel, pPos)) {
                pLevel.destroyBlock(pPos, true);
            }

            return new ItemStack(Items.LAVA_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    default Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }

}
