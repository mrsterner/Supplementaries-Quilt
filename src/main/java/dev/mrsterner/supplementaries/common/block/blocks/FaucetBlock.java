package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class FaucetBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(5, 5, 5, 11, 15, 16);
    protected static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(5, 5, 0, 11, 15, 11);
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(5, 5, 5, 16, 15, 11);
    protected static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0, 5, 5, 11, 15, 11);
    protected static final VoxelShape SHAPE_NORTH_JAR = Block.createCuboidShape(5, 0, 5, 11, 10, 16);
    protected static final VoxelShape SHAPE_SOUTH_JAR = Block.createCuboidShape(5, 0, 0, 11, 10, 11);
    protected static final VoxelShape SHAPE_WEST_JAR = Block.createCuboidShape(5, 0, 5, 16, 10, 11);
    protected static final VoxelShape SHAPE_EAST_JAR = Block.createCuboidShape(0, 0, 5, 11, 10, 11);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_WATER = BlockProperties.HAS_WATER;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_7;
    public static final BooleanProperty HAS_JAR = BlockProperties.HAS_JAR;

    public FaucetBlock(Properties properties) {
        super(properties.lightLevel(s->s.get(LIGHT_LEVEL)));
        this.setDefaultState(this.stateManager.getDefaultState().with(HAS_JAR, false).with(FACING, Direction.NORTH)
                .with(ENABLED, false).with(POWERED, false)
                .with(HAS_WATER, false).with(WATERLOGGED, false).with(LIGHT_LEVEL, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        if (state.get(HAS_JAR)) {
            return switch (state.get(FACING)) {
                default -> SHAPE_NORTH_JAR;
                case SOUTH -> SHAPE_SOUTH_JAR;
                case EAST -> SHAPE_EAST_JAR;
                case WEST -> SHAPE_WEST_JAR;
            };
        } else {
            return switch (state.get(FACING)) {
                default -> SHAPE_NORTH;
                case SOUTH -> SHAPE_SOUTH;
                case EAST -> SHAPE_EAST;
                case WEST -> SHAPE_WEST;
            };
        }
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        boolean enabled = state.get(ENABLED);

        float f = enabled ? 1F : 1.2F;
        worldIn.playSound(null, pos, ModSounds.FAUCET.get(), SoundSource.BLOCKS, 1F, f);
        worldIn.gameEvent(player, enabled ? GameEvent.BLOCK_SWITCH : GameEvent.BLOCK_UNSWITCH, pos);
        this.updateBlock(state, worldIn, pos, true);
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        boolean hasWater = updateTileFluid(state, pos, worldIn);
        if (hasWater != state.get(HAS_WATER)) worldIn.setBlockStateAndUpdate(pos, state.with(HAS_WATER, hasWater));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if (facing == Direction.DOWN) {
            boolean canConnectDown = canConnect(facingState, worldIn, facingPos, facing.getOpposite());
            //boolean water = canConnectDown?stateIn.get(HAS_WATER)&&this.isSpecialTankBelow(facingState): updateTileFluid(stateIn,currentPos,worldIn);
            return stateIn.with(HAS_JAR, canConnectDown);
        }
        if (facing == stateIn.get(FACING).getOpposite()) {
            boolean hasWater = updateTileFluid(stateIn, currentPos, worldIn);
            return stateIn.with(HAS_WATER, hasWater);
        }
        return stateIn;
    }

    //returns false if no color (water)
    public boolean updateTileFluid(BlockState state, BlockPos pos, WorldAccess world) {
        if (world.getBlockEntity(pos) instanceof FaucetBlockTile tile && world instanceof World level) {
            return tile.updateContainedFluidVisuals(level, pos, state);
        }
        return false;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (world.getBlockEntity(pos) instanceof FaucetBlockTile tile && world instanceof World level) {
            boolean water = tile.updateContainedFluidVisuals(level, pos, state);
            if (state.get(HAS_WATER) != water) {
                level.setBlockState(pos, state.with(HAS_WATER, water), 2);
            }
        }
    }

    //TODO: redo
    private boolean canConnect(BlockState downState, WorldAccess world, BlockPos pos, Direction dir) {
        if (downState.getBlock() instanceof JarBlock) return true;
        else if (downState.is(ModTags.POURING_TANK)) return false;
        else if (downState.hasProperty(BlockStateProperties.LEVEL_HONEY)) return true;
        return world instanceof World && FluidUtil.getFluidHandler((Level) world, pos, dir).isPresent();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        this.updateBlock(state, world, pos, false);
    }

    public void updateBlock(BlockState state, World world, BlockPos pos, boolean toggle) {
        boolean isPowered = world.hasNeighborSignal(pos);
        if (isPowered != state.get(POWERED) || toggle) {
            world.setBlockState(pos, state.with(POWERED, isPowered).with(ENABLED, toggle ^ state.get(ENABLED)), 2);
        }

        boolean hasWater = updateTileFluid(state, pos, world);
        if (hasWater != state.get(HAS_WATER)) world.setBlockStateAndUpdate(pos, state.with(HAS_WATER, hasWater));


        //handles concrete
        if (state.get(ENABLED) ^ toggle ^ isPowered && state.get(HAS_WATER)) {
            trySolidifyConcrete(pos.below(), world);
        }
    }

    public void trySolidifyConcrete(BlockPos pos, World world) {
        Block b = world.getBlockState(pos).getBlock();
        if (b instanceof ConcretePowderBlock concretePowderBlock)
            world.setBlockState(pos, concretePowderBlock.concrete, 2 | 16);
    }


    public boolean isOpen(BlockState state) {
        return (state.get(BlockStateProperties.POWERED) ^ state.get(BlockStateProperties.ENABLED));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED, POWERED, HAS_WATER, HAS_JAR, WATERLOGGED, LIGHT_LEVEL);
    }

    //TODO: fix water faucet connecting on rotation

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
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction dir = context.getClickedFace().getAxis() == Direction.Axis.Y ? Direction.NORTH : context.getClickedFace();

        boolean water = world.getFluidState(pos).getType() == Fluids.WATER;
        boolean hasJar = canConnect(world.getBlockState(pos.below()), world, pos.below(), Direction.UP);

        boolean powered = world.hasNeighborSignal(pos);

        return this.getDefaultState ().with(FACING, dir)
                .with(HAS_JAR, hasJar).with(WATERLOGGED, water).with(POWERED, powered);
    }

    //TODO: maybe remove haswater state
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        boolean flag = this.isOpen(state);
        if (state.get(HAS_WATER) && !state.get(HAS_JAR)) {
            if (random.nextFloat() > (flag ? 0 : 0.06)) return;
            float d = 0.125f;
            double x = (pos.getX() + 0.5 + d * (random.nextFloat() - 0.5));
            double y = (pos.getY() + 0.25);
            double z = (pos.getZ() + 0.5 + d * (random.nextFloat() - 0.5));
            int color = getTileParticleColor(pos, world);
            //get texture color if color is white
            float r = FastColor.ARGB32.red(color) / 255f;
            float g = FastColor.ARGB32.green(color) / 255f;
            float b = FastColor.ARGB32.blue(color) / 255f;
            world.addParticle(ModRegistry.DRIPPING_LIQUID.get(), x, y, z, r, g, b);
        }
    }

    //only client
    public int getTileParticleColor(BlockPos pos, World world) {
        if (world.getBlockEntity(pos) instanceof FaucetBlockTile te)
            return te.tempFluidHolder.getParticleColor(world, pos);
        return 0x423cf7;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new FaucetBlockTile(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.FAUCET_TILE.get(), pLevel.isClient() ? null : FaucetBlockTile::tick);
    }
}

