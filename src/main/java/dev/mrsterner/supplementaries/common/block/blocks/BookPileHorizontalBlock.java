package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BookPileHorizontalBlock extends BookPileBlock {

    private static final VoxelShape SHAPE_1_Z = Block.createCuboidShape(6D, 0D, 4D, 10D, 10D, 12D);
    private static final VoxelShape SHAPE_1_X = Block.createCuboidShape(4D, 0D, 6D, 12D, 10D, 10D);

    private static final VoxelShape SHAPE_2_Z = Block.createCuboidShape(3D, 0D, 4D, 13D, 10D, 12D);
    private static final VoxelShape SHAPE_2_X = Block.createCuboidShape(4D, 0D, 3D, 12D, 10D, 13D);


    private static final VoxelShape SHAPE_3_Z = Block.createCuboidShape(1D, 0D, 4D, 15D, 10D, 12D);
    private static final VoxelShape SHAPE_3_X = Block.createCuboidShape(4D, 0D, 1D, 12D, 10D, 15D);


    private static final VoxelShape SHAPE_4_Z = Block.createCuboidShape(0D, 0D, 4D, 16D, 10D, 12D);
    private static final VoxelShape SHAPE_4_X = Block.createCuboidShape(4D, 0D, 0D, 12D, 10D, 16D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BookPileHorizontalBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false).with(BOOKS, 1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getBlockPos());
        if (blockstate.getBlock() instanceof BookPileBlock) {
            return blockstate.with(BOOKS, blockstate.get(BOOKS) + 1);
        }
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        boolean flag = fluidState.getType() == Fluids.WATER && fluidState.getAmount() == 8;
        return this.getDefaultState ().with(WATERLOGGED, flag).with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new BookPileBlockTile(pPos, pState, true);
    }

    public boolean isAcceptedItem(Item i) {
        return isNormalBook(i) || (ServerConfigs.cached.MIXED_BOOKS && isEnchantedBook(i));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        boolean x = state.get(FACING).getAxis() == Direction.Axis.X;

        return switch (state.get(BOOKS)) {
            default -> x ? SHAPE_1_X : SHAPE_1_Z;
            case 2 -> x ? SHAPE_2_X : SHAPE_2_Z;
            case 3 -> x ? SHAPE_3_X : SHAPE_3_Z;
            case 4 -> x ? SHAPE_4_X : SHAPE_4_Z;
        };
    }
}
