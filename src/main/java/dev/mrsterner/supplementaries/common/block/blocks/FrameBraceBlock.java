package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class FrameBraceBlock extends FrameBlock { //implements IRotationLockable
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;

    public FrameBraceBlock(Properties properties, Supplier<Block> daub) {
        super(properties, daub);
        this.setDefaultState(this.stateManager.getDefaultState().with(FLIPPED, false)
                .with(LIGHT_LEVEL, 0).with(HAS_BLOCK, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FLIPPED);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockPos blockpos = context.getBlockPos();
        Direction direction = context.getClickedFace();
        return this.getDefaultState ().with(FLIPPED, direction != Direction.DOWN && (direction == Direction.UP || !(context.getClickLocation().y - (double) blockpos.getY() > 0.5D)));
    }

    //quark rot lock
    //@Override
    public BlockState applyRotationLock(World world, BlockPos blockPos, BlockState state, Direction direction, int half) {
        return state.with(FLIPPED, half == 1);
    }

}
