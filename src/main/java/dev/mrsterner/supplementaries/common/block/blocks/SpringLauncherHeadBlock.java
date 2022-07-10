package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.tiles.SpringLauncherArmBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class SpringLauncherHeadBlock extends DirectionalBlock {
    protected static final VoxelShape PISTON_EXTENSION_EAST_AABB = Block.createCuboidShape(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_WEST_AABB = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_SOUTH_AABB = Block.createCuboidShape(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_NORTH_AABB = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
    protected static final VoxelShape PISTON_EXTENSION_UP_AABB = Block.createCuboidShape(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_EXTENSION_DOWN_AABB = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape UP_ARM_AABB = Block.createCuboidShape(1.0D, -4.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    protected static final VoxelShape DOWN_ARM_AABB = Block.createCuboidShape(1.0D, 4.0D, 1.0D, 15.0D, 20.0D, 15.0D);
    protected static final VoxelShape SOUTH_ARM_AABB = Block.createCuboidShape(1.0D, 1.0D, -4.0D, 15.0D, 15.0D, 12.0D);
    protected static final VoxelShape NORTH_ARM_AABB = Block.createCuboidShape(1.0D, 1.0D, 4.0D, 15.0D, 15.0D, 20.0D);
    protected static final VoxelShape EAST_ARM_AABB = Block.createCuboidShape(-4.0D, 1.0D, 1.0D, 12.0D, 15.0D, 15.0D);
    protected static final VoxelShape WEST_ARM_AABB = Block.createCuboidShape(4.0D, 1.0D, 1.0D, 20.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHORT_UP_ARM_AABB = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);
    protected static final VoxelShape SHORT_DOWN_ARM_AABB = Block.createCuboidShape(1.0D, 4.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    protected static final VoxelShape SHORT_SOUTH_ARM_AABB = Block.createCuboidShape(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 12.0D);
    protected static final VoxelShape SHORT_NORTH_ARM_AABB = Block.createCuboidShape(1.0D, 1.0D, 4.0D, 15.0D, 15.0D, 16.0D);
    protected static final VoxelShape SHORT_EAST_ARM_AABB = Block.createCuboidShape(0.0D, 1.0D, 1.0D, 12.0D, 15.0D, 15.0D);
    protected static final VoxelShape SHORT_WEST_ARM_AABB = Block.createCuboidShape(4.0D, 1.0D, 1.0D, 16.0D, 15.0D, 15.0D);
    private static final VoxelShape[] EXTENDED_SHAPES = getShapesForExtension(true);
    private static final VoxelShape[] UNEXTENDED_SHAPES = getShapesForExtension(false);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty SHORT = BlockStateProperties.SHORT; // is not small? (only used for
    // tile entity, leave true
    public SpringLauncherHeadBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(SHORT, false).with(FACING, Direction.NORTH));
    }

    private static VoxelShape[] getShapesForExtension(boolean extended) {
        return Arrays.stream(Direction.values()).map((direction) -> getShapeForDirection(direction, extended)).toArray(VoxelShape[]::new);
    }

    private static VoxelShape getShapeForDirection(Direction direction, boolean shortArm) {
        return switch (direction) {
            default -> Shapes.or(PISTON_EXTENSION_DOWN_AABB, shortArm ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
            case UP -> Shapes.or(PISTON_EXTENSION_UP_AABB, shortArm ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
            case NORTH -> Shapes.or(PISTON_EXTENSION_NORTH_AABB, shortArm ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
            case SOUTH -> Shapes.or(PISTON_EXTENSION_SOUTH_AABB, shortArm ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
            case WEST -> Shapes.or(PISTON_EXTENSION_WEST_AABB, shortArm ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
            case EAST -> Shapes.or(PISTON_EXTENSION_EAST_AABB, shortArm ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return (state.get(SHORT) ? EXTENDED_SHAPES : UNEXTENDED_SHAPES)[state.get(FACING).ordinal()];
    }

    @Override
    public void fallOn(World worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
        if (entityIn.isSuppressingBounce() || state.get(FACING)!=Direction.UP) {
            super.fallOn(worldIn, state, pos, entityIn, fallDistance);
        } else {
            entityIn.causeFallDamage(fallDistance, 0.0F, DamageSource.FALL);
            //TODO: add falling block entity support
            if((entityIn instanceof LivingEntity) && !worldIn.isClient() && fallDistance>(float)ServerConfigs.cached.LAUNCHER_HEIGHT){
                worldIn.setBlockState(pos, ModRegistry.SPRING_LAUNCHER_ARM.get().getDefaultState ()
                        .with(SpringLauncherArmBlock.EXTENDING, false).with(FACING, state.get(FACING)), 3);
                if(worldIn.getBlockEntity(pos) instanceof SpringLauncherArmBlockTile launcherArmBlockTile){
                    launcherArmBlockTile.age = 1;
                    launcherArmBlockTile.offset = -0.5;
                }
            }
            //this.bounceEntity(entityIn);
        }

    }

    /**
     * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
     * on its own
     */
    /*
    public void onLanded(IBlockReader worldIn, Entity entityIn) {
        if (entityIn.isSuppressingBounce()) {
            super.onLanded(worldIn, entityIn);
        } else {
            this.bounceEntity(entityIn);
        }

    }*/

    private void bounceEntity(Entity entity) {
        Vec3 vector3d = entity.getDeltaMovement();
        if (vector3d.y < 0.0D) {
            double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
            entity.setDeltaMovement(vector3d.x, -vector3d.y * d0, vector3d.z);
        }

    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SHORT);
    }

    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerLookDirection ().getOpposite());
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        return new ItemStack(ModRegistry.SPRING_LAUNCHER.get());
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    // piston code
    /**
     * Called before the Block is set to air in the world. Called regardless of if
     * the player's tool can actually collect this block
     */
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClient() && player.getAbilities().instabuild) {
            BlockPos blockpos = pos.relative(state.get(FACING).getOpposite());
            Block block = worldIn.getBlockState(blockpos).getBlock();
            if (block instanceof SpringLauncherBlock) {
                worldIn.removeBlock(blockpos, false);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockState comp = ModRegistry.SPRING_LAUNCHER_ARM.get().getDefaultState ().with(SpringLauncherArmBlock.EXTENDING, false).with(FACING, state.get(FACING));
        if ((state.getBlock() != newState.getBlock()) && (newState != comp)) {
            super.onRemove(state, worldIn, pos, newState, isMoving);
            Direction direction = state.get(FACING).getOpposite();
            pos = pos.relative(direction);
            BlockState blockstate = worldIn.getBlockState(pos);
            if ((blockstate.getBlock() instanceof SpringLauncherBlock) && blockstate.get(BlockStateProperties.EXTENDED)) {
                dropResources(blockstate, worldIn, pos);
                worldIn.removeBlock(pos, false);
            }
        }
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor
     * state, returning a new state. For example, fences make their connections to
     * the passed in state if possible, and wet concrete powder immediately returns
     * its solidified counterpart. Note that this method should ideally consider
     * only the specific face passed in.
     */
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        return facing.getOpposite() == stateIn.get(FACING) && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState ()
                : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState bs = worldIn.getBlockState(pos.relative(state.get(FACING).getOpposite()));
        return bs == ModRegistry.SPRING_LAUNCHER.get().getDefaultState ().with(BlockStateProperties.EXTENDED, true).with(FACING, state.get(FACING));
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.canSurvive(worldIn, pos)) {
            BlockPos blockpos = pos.relative(state.get(FACING).getOpposite());
            worldIn.getBlockState(blockpos).neighborUpdate(worldIn, blockpos, blockIn, fromPos, false);
        }
    }

}

