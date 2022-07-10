package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.math.MathHelperUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Random;

public class CrankBlock extends WaterBlock {
    protected static final VoxelShape SHAPE_DOWN = Block.createCuboidShape(2, 11, 2, 14, 16, 14);
    protected static final VoxelShape SHAPE_UP = Block.createCuboidShape(2, 0, 2, 14, 5, 14);
    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(2, 2, 11, 14, 14, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(2, 2, 0, 14, 14, 5);
    protected static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0, 2, 2, 5, 14, 14);
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(11, 2, 2, 16, 14, 14);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public CrankBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(POWER, 0).with(FACING, Direction.NORTH));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing.getOpposite() == stateIn.get(FACING) && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState ()
                : stateIn;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return canSupportCenter(worldIn, blockpos, direction);
        } else {
            return blockstate.isFaceSturdy(worldIn, blockpos, direction);
        }
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (worldIn.isClient()) {
            Direction direction = state.get(FACING).getOpposite();
            // Direction direction1 = getFacing(state).getOpposite();
            double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getStepX() + 0.2D * (double) direction.getStepX();
            double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getStepY() + 0.2D * (double) direction.getStepY();
            double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getStepZ() + 0.2D * (double) direction.getStepZ();
            worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0, 0);
            return ActionResult.SUCCESS;
        } else {
            boolean ccw = player.isShiftKeyDown();
            this.activate(state, worldIn, pos, ccw);
            float f = 0.55f + state.get(POWER) * 0.04f ; //(ccw ? 0.6f : 0.7f)+ MathHelperUtils.nextWeighted(worldIn.random, 0.04f)
            worldIn.playSound(null, pos, ModSounds.CRANK.get(), SoundSource.BLOCKS, 0.5F, f);
            worldIn.gameEvent(player, GameEvent.BLOCK_SWITCH, pos);

            Direction dir = state.get(FACING).getOpposite();
            if (dir.getAxis() != Direction.Axis.Y) {
                BlockPos behind = pos.relative(dir);
                BlockState backState = worldIn.getBlockState(behind);
                if (backState.is(ModRegistry.PULLEY_BLOCK.get()) && dir.getAxis() == backState.get(PulleyBlock.AXIS)) {
                    ((PulleyBlock) backState.getBlock()).windPulley(backState, behind, worldIn, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, dir);
                }
            }
            return ActionResult.CONSUME;
        }
    }

    public void activate(BlockState state, World world, BlockPos pos, boolean ccw) {
        //cycle == cycle
        state = state.with(POWER, (16 + state.get(POWER) + (ccw ? -1 : 1)) % 16);
        world.setBlockState(pos, state, 3);
        this.updateNeighbors(state, world, pos);
    }

    @Override
    public int getSignal(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(FACING) == side ? blockState.get(POWER) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        world.updateNeighborsAt(pos.relative(state.get(FACING).getOpposite()), this);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            if (state.get(POWER) != 0) {
                this.updateNeighbors(state, worldIn, pos);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }


    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.get(POWER) > 0 && rand.nextFloat() < 0.25F) {
            Direction direction = stateIn.get(FACING).getOpposite();
            // Direction direction1 = getFacing(state).getOpposite();
            double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getStepX() + 0.2D * (double) direction.getStepX();
            double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getStepY() + 0.2D * (double) direction.getStepY();
            double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getStepZ() + 0.2D * (double) direction.getStepZ();
            worldIn.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 0.5f), d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockView world, BlockPos pos, Mob entity) {
        return BlockPathTypes.OPEN;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return switch (state.get(FACING)) {
            default -> SHAPE_SOUTH;
            case NORTH -> SHAPE_NORTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
        };
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView reader, BlockPos pos, NavigationType pathType) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, WATERLOGGED);
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        BlockState blockstate = this.getDefaultState ();
        LevelReader level = context.getWorld();
        BlockPos blockpos = context.getBlockPos();
        Direction[] directions = context.getPlayerLookDirection s();

        for (Direction direction : directions) {

            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.with(FACING, direction1);
            if (blockstate.canSurvive(level, blockpos)) {
                return blockstate.with(WATERLOGGED, flag);
            }

        }
        return null;
    }
}
