package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FlowerBoxBlock extends WaterBlock implements EntityBlock {

    protected static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 6.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0.0D, 0.0D, 10.0D, 16.0D, 6.0D, 16.0D);

    protected static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 6.0D, 6.0D, 16.0D);
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(10.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);


    protected static final VoxelShape SHAPE_NORTH_FLOOR = Block.createCuboidShape(0.0D, 0.0D, 5.0D, 16.0D, 6.0D, 11.0D);

    protected static final VoxelShape SHAPE_WEST_FLOOR = Block.createCuboidShape(5.0D, 0.0D, 0.0D, 11.0D, 6.0D, 16.0D);


    public static final BooleanProperty FLOOR = BlockProperties.FLOOR;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FlowerBoxBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false).with(FLOOR, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, FLOOR);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        return this.getDefaultState ().with(WATERLOGGED, flag).with(FLOOR, context.getClickedFace() == Direction.UP)
                .with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof FlowerBoxBlockTile tile && tile.isAccessibleBy(player)) {
            int ind;

            Direction dir = state.get(FACING);
            Vec3 v = hit.getLocation();
            v = v.add(Math.round(Math.abs(v.x) + 1), 0, Math.round(Math.abs(v.z) + 1));

            if (dir.getAxis() == Direction.Axis.X) {

                ind = (int) ((v.z % 1d) / (1 / 3d));
                if (dir.getStepX() < 0) ind = 2 - ind;
            } else {
                ind = (int) ((v.x % 1d) / (1 / 3d));
                if (dir.getStepZ() > 0) ind = 2 - ind;

            }
            return tile.interact(player, handIn, ind);
        }
        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new FlowerBoxBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof FlowerBoxBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        boolean wall = !state.get(FLOOR);
        return switch (state.get(FACING)) {
            default -> wall ? SHAPE_NORTH : SHAPE_NORTH_FLOOR;
            case SOUTH -> wall ? SHAPE_SOUTH : SHAPE_NORTH_FLOOR;
            case EAST -> wall ? SHAPE_EAST : SHAPE_WEST_FLOOR;
            case WEST -> wall ? SHAPE_WEST : SHAPE_WEST_FLOOR;
        };
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }
}
