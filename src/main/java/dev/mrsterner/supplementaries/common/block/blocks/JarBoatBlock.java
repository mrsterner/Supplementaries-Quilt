package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBoatTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class JarBoatBlock extends HorizontalDirectionalBlock implements EntityBlock {
    protected static final VoxelShape SHAPE_X = Block.createCuboidShape(3, 0, 0, 13, 12, 16);
    protected static final VoxelShape SHAPE_Z = Block.createCuboidShape(0, 0, 3, 16, 12, 13);

    public JarBoatBlock(Properties builder) {
        super(builder);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return state.get(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new JarBoatTile(pPos, pState);
    }
}
