package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronGateBlock extends FenceGateBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final boolean gold;

    public IronGateBlock(Properties properties, boolean gold) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, Boolean.FALSE));
        this.gold = gold;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos blockpos = context.getBlockPos();
        boolean flag = world.hasNeighborSignal(blockpos);
        Direction direction = context.getPlayerFacing();
        FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
        BlockState state = this.getDefaultState ().with(WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);

        return state.with(FACING, direction).with(OPEN, flag)
                .with(POWERED, flag).with(IN_WALL, canConnect(world, blockpos, direction));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_,  ShapeContext  p_220053_4_) {
        return state.get(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    //better done here cause of side + up
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        if (!world.isClient()) {
            boolean flag = world.hasNeighborSignal(pos);
            if (state.get(POWERED) != flag) {
                state = state.with(POWERED, flag);
                if (!gold || !ServerConfigs.cached.CONSISTENT_GATE) {
                    if (state.get(OPEN) != flag) {
                        world.syncWorldEvent(null, flag ? 1036 : 1037, pos, 0);
                        world.gameEvent(flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                    }
                    state = state.with(OPEN, flag);
                }
            }
            boolean connect = canConnect(world, pos, state.get(FACING));
            world.setBlockState(pos, state.with(IN_WALL, connect), 2);
        }
    }



    private boolean canConnect(WorldAccess world, BlockPos pos, Direction dir) {
        return canConnectUp(world.getBlockState(pos.above()), world, pos.above()) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getClockWise()))) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getCounterClockWise())));
    }

    private boolean canConnectSide(BlockState state) {
        return state.getBlock() instanceof IronBarsBlock;
    }

    private boolean canConnectUp(BlockState state, WorldAccess world, BlockPos pos) {
        return state.isFaceSturdy(world, pos, Direction.DOWN) || state.is(this) || state.getBlock() instanceof IronBarsBlock;
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult result) {

        if (!state.get(POWERED) && gold || !ServerConfigs.cached.CONSISTENT_GATE) {
            Direction dir = player.getDirection();


            if (ServerConfigs.cached.DOUBLE_IRON_GATE) {
                BlockPos up = pos.above();
                BlockState stateUp = world.getBlockState(up);
                if (stateUp.is(this) && stateUp.with(IN_WALL, false) == state.with(IN_WALL, false))
                    openGate(stateUp, world, up, dir);
                BlockPos down = pos.below();
                BlockState stateDown = world.getBlockState(down);
                if (stateDown.is(this) && stateDown.with(IN_WALL, false) == state.with(IN_WALL, false))
                    openGate(stateDown, world, down, dir);
            }

            openGate(state, world, pos, dir);
            boolean open = state.get(OPEN);
            world.syncWorldEvent(player, open ? 1036 : 1037, pos, 0);
            world.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            return ActionResult.success(world.isClient());
        }

        return ActionResult.PASS;

    }

    private void openGate(BlockState state, World world, BlockPos pos, Direction dir) {
        if (state.get(OPEN)) {
            state = state.with(OPEN, Boolean.FALSE);
        } else {
            if (state.get(FACING) == dir.getOpposite()) {
                state = state.with(FACING, dir);
            }
            state = state.with(OPEN, Boolean.TRUE);
        }
        world.setBlockState(pos, state, 10);
    }

}
