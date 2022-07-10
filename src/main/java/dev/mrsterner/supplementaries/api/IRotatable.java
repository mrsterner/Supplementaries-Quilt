package dev.mrsterner.supplementaries.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IRotatable {
    /**
     * returns the block state rotated over an axis. Do not use this to actually rotate the block. use rotateOverAxis or onRotated instead
     * @param state original block state
     * @param world world
     * @param pos position
     * @param rotation rotation
     * @param axis rotation axis
     * @return rotated state. Optional.empty() if block hasn't been rotated. you can return the same blockstate for tile entities that have othe rmeaning of rotation
     */
    Optional<BlockState> getRotatedState(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation,
										 Direction axis, @Nullable Vec3d hit);
    /**
     * actually rotates the block. Overrides if you need more control over what rotating actually means
     * @param state original block state
     * @param world world
     * @param pos position
     * @param rotation rotation
     * @param axis rotation axis
     * @return the direction onto which the block was actually rotated. Optional.empty() if it wasn't rotated at all
     */
    default Optional<Direction> rotateOverAxis(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation,
                                   Direction axis, @Nullable Vec3d hit){

        Optional<BlockState> optional = this.getRotatedState(state, world, pos,rotation,axis, hit);
        if (optional.isPresent()) {
            BlockState rotated = optional.get();

            if (rotated.canPlaceAt(world, pos)) {
                rotated = Block.postProcessState(rotated, world, pos);

                if (world instanceof ServerWorld) {
                    world.setBlockState(pos, rotated, 11);
                    //level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                }
                this.onRotated(rotated, state, world, pos, rotation, axis, hit);
                return Optional.of(axis);
            }
        }
        return Optional.empty();
    };

    /**
     * performs extra actions after it has been rotated
     * @param newState blockstate
     * @param oldState old blockstate
     * @param axis axis of rotation
     */
    default void onRotated(BlockState newState, BlockState oldState, WorldAccess world, BlockPos pos, BlockRotation rotation,
                                         Direction axis, @Nullable Vec3d hit){
    }




}
