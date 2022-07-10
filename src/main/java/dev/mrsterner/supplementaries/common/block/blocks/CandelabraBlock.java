package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class CandelabraBlock extends LightUpWaterBlock {
    protected static final VoxelShape SHAPE_FLOOR = Block.createCuboidShape(5D, 0D, 5D, 11D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_NORTH = Block.createCuboidShape(5D, 0D, 11D, 11D, 14D, 16D);
    protected static final VoxelShape SHAPE_WALL_SOUTH = Block.createCuboidShape(5D, 0D, 0D, 11D, 14D, 5D);
    protected static final VoxelShape SHAPE_WALL_WEST = Block.createCuboidShape(11D, 0D, 5D, 16D, 14D, 11D);
    protected static final VoxelShape SHAPE_WALL_EAST = Block.createCuboidShape(0D, 0D, 5D, 5D, 14D, 11D);
    protected static final VoxelShape SHAPE_CEILING = Block.createCuboidShape(5D, 3D, 5D, 11D, 16D, 11D);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public CandelabraBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(LIT, true)
                .with(FACE, AttachFace.FLOOR).with(FACING, Direction.NORTH));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        for (Direction direction : context.getPlayerLookDirection s()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.getDefaultState ().with(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).with(FACING, context.getPlayerFacing());
            } else {
                blockstate = this.getDefaultState ().with(FACE, AttachFace.WALL).with(FACING, direction.getOpposite());
            }

            if (blockstate.canSurvive(context.getWorld(), context.getBlockPos())) {
                return blockstate.with(WATERLOGGED, flag).with(LIT, !flag);
            }
        }
        return null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACE, FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return switch (state.get(FACE)) {
            case FLOOR -> SHAPE_FLOOR;
            case WALL -> switch (state.get(FACING)) {
                default -> SHAPE_WALL_NORTH;
                case SOUTH -> SHAPE_WALL_SOUTH;
                case WEST -> SHAPE_WALL_WEST;
                case EAST -> SHAPE_WALL_EAST;
            };
            case CEILING -> SHAPE_CEILING;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if (state.get(FACE) == AttachFace.FLOOR) {
            return canSupportCenter(worldIn, pos.below(), Direction.UP);
        } else if (state.get(FACE) == AttachFace.CEILING) {
            return RopeBlock.isSupportingCeiling(pos.above(), worldIn);
        }
        return isSideSolidForDirection(worldIn, pos, state.get(FACING).getOpposite());
    }


    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!stateIn.get(LIT)) return;
        Direction dir1 = stateIn.get(FACING);
        double xm, ym, zm, xl, yl, zl, xr, zr;
        Direction dir = dir1.getClockWise();
        double xOff = dir.getStepX() * 0.3125D;
        double zOff = dir.getStepZ() * 0.3125D;
        switch (stateIn.get(FACE)) {
            default -> {
                xm = pos.getX() + 0.5D;
                ym = pos.getY() + 1D;
                zm = pos.getZ() + 0.5D;
                xl = pos.getX() + 0.5D - xOff;
                yl = pos.getY() + 0.9375D;
                zl = pos.getZ() + 0.5D - zOff;
                xr = pos.getX() + 0.5D + xOff;
                zr = pos.getZ() + 0.5D + zOff;
            }
            case WALL -> {
                double xo1 = -dir1.getStepX() * 0.3125;
                double zo2 = -dir1.getStepZ() * 0.3125;
                xm = pos.getX() + 0.5D + xo1;
                ym = pos.getY() + 1;
                zm = pos.getZ() + 0.5D + zo2;
                xl = pos.getX() + 0.5D + xo1 - xOff;
                yl = pos.getY() + 0.9375;
                zl = pos.getZ() + 0.5D + zo2 - zOff;
                xr = pos.getX() + 0.5D + xo1 + xOff;
                zr = pos.getZ() + 0.5D + zo2 + zOff;
            }
            case CEILING -> {
                //high
                xm = pos.getX() + 0.5D + zOff;
                zm = pos.getZ() + 0.5D - xOff;
                ym = pos.getY() + 0.875;//0.9375D;

                //2 medium
                xl = pos.getX() + 0.5D + xOff;
                zl = pos.getZ() + 0.5D + zOff;
                xr = pos.getX() + 0.5D - zOff;
                zr = pos.getZ() + 0.5D + xOff;
                yl = pos.getY() + 0.8125;
                double xs = pos.getX() + 0.5D - xOff;
                double zs = pos.getZ() + 0.5D - zOff;
                double ys = pos.getY() + 0.75;
                worldIn.addParticle(ParticleTypes.FLAME, xs, ys, zs, 0, 0, 0);
            }
        }
        worldIn.addParticle(ParticleTypes.FLAME, xm, ym, zm, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xl, yl, zl, 0, 0, 0);
        worldIn.addParticle(ParticleTypes.FLAME, xr, yl, zr, 0, 0, 0);

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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        return getFacing(stateIn).getOpposite() == facing && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.getDefaultState () : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }


    protected static Direction getFacing(BlockState state) {
        return switch (state.get(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> state.get(FACING);
        };
    }

    public static boolean isSideSolidForDirection(LevelReader reader, BlockPos pos, Direction direction) {
        BlockPos blockpos = pos.relative(direction);
        return reader.getBlockState(blockpos).isFaceSturdy(reader, blockpos, direction.getOpposite());
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
