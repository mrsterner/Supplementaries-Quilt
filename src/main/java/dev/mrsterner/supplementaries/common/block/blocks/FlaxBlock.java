package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.api.IBeeGrowable;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.Random;

public class FlaxBlock extends CropBlock implements IBeeGrowable {
    public static final int DOUBLE_AGE = 4; //age at which it grows in block above
    private static final VoxelShape FULL_BOTTOM = Block.createCuboidShape(1, 0, 1, 15, 16, 15);
    private static final VoxelShape[] SHAPES_BOTTOM = new VoxelShape[]{
            Block.createCuboidShape(4, 0, 4, 12, 6, 12),
            Block.createCuboidShape(3, 0, 3, 13, 10, 13),
            Block.createCuboidShape(3, 0, 3, 13, 13, 13),
            Block.createCuboidShape(3, 0, 3, 13, 16, 13),
            Block.createCuboidShape(2, 0, 2, 14, 16, 14),
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM};
    private static final VoxelShape[] SHAPES_TOP = new VoxelShape[]{
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            Block.createCuboidShape(2, 0, 2, 14, 3, 14),
            Block.createCuboidShape(1, 0, 1, 15, 7, 15),
            Block.createCuboidShape(1, 0, 1, 15, 11, 15),
            Block.createCuboidShape(1, 0, 1, 15, 16, 15),};

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public FlaxBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(this.getAgeProperty(), 0)
                .with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return SHAPES_BOTTOM[state.get(AGE)];
        }
        return SHAPES_TOP[state.get(AGE)];
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return OffsetType.NONE;
    }

    //double plant code
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        DoubleBlockHalf half = stateIn.get(HALF);

        if (facing.getAxis() != Direction.Axis.Y || (half == DoubleBlockHalf.LOWER != (facing == Direction.UP) || this.isSingle(stateIn)) || (facingState.is(this) && facingState.get(HALF) != half)) {
            return half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos)
                    ? Blocks.AIR.getDefaultState () : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        } else {
            return Blocks.AIR.getDefaultState ();
        }
    }

    public boolean isSingle(BlockState state) {
        return this.getAge(state) < DOUBLE_AGE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            //no more mods messing with my upper stage smh
            BlockState above = worldIn.getBlockState(pos.above());
            if (above.getBlock() == this && this.isSingle(above)) return false;
            return super.canSurvive(state, worldIn, pos);
        } else {
            if (this.isSingle(state)) return false;
            BlockState blockstate = worldIn.getBlockState(pos.below());
            if (state.getBlock() != this)
                return super.canSurvive(state, worldIn, pos); //Forge: This function is called during world gen and placement, before this block is set, so if we are not 'here' then assume it's the pre-check.
            return blockstate.is(this) && blockstate.get(HALF) == DoubleBlockHalf.LOWER && this.getAge(state) == this.getAge(blockstate);
        }
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClient()) {
            if (player.isCreative()) {
                removeBottomHalf(worldIn, pos, state, player);
            } else {
                dropResources(state, worldIn, pos, null, player, player.getMainHandItem());
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public void playerDestroy(World worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, Blocks.AIR.getDefaultState (), te, stack);
    }

    protected static void removeBottomHalf(World world, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf doubleblockhalf = state.get(HALF);
        if (doubleblockhalf == DoubleBlockHalf.UPPER) {
            BlockPos blockpos = pos.below();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.getBlock() == state.getBlock() && blockstate.get(HALF) == DoubleBlockHalf.LOWER) {
                world.setBlockState(blockpos, Blocks.AIR.getDefaultState (), 35);
                world.syncWorldEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HALF);
    }

    public void placeAt(WorldAccess worldIn, BlockPos pos, int flags) {
        worldIn.setBlockState(pos, this.getDefaultState ().with(HALF, DoubleBlockHalf.LOWER), flags);
        worldIn.setBlockState(pos.above(), this.getDefaultState ().with(HALF, DoubleBlockHalf.UPPER), flags);
    }

    // Tick function
    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (!worldIn.isAreaLoaded(pos, 1))
            return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (state.get(HALF) == DoubleBlockHalf.UPPER) return; //only bottom one handles ticking
        if (worldIn.getRawBrightness(pos, 0) >= 9) {
            int age = this.getAge(state);
            if (this.isValidBonemealTarget(worldIn, pos, state, worldIn.isClient())) {
                float f = getGrowthSpeed(this, worldIn, pos);
                if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt((int) (25.0F / f) + 1) == 0)) {
                    if (age + 1 >= DOUBLE_AGE) {
                        worldIn.setBlockState(pos.above(), this.getStateForAge(age + 1).with(HALF, DoubleBlockHalf.UPPER), 3);
                    }
                    worldIn.setBlockState(pos, this.getStateForAge(age + 1), 2);
                    ForgeHooks.onCropsGrowPost(worldIn, pos, state);
                }
            }
        }
    }

    public boolean canGrowUp(BlockGetter worldIn, BlockPos downPos) {
        BlockState state = worldIn.getBlockState(downPos.above());
        return state.getBlock() instanceof FlaxBlock || state.getMaterial().isReplaceable();
    }


    //for bonemeal
    @Override
    public boolean isValidBonemealTarget(BlockGetter worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(HALF) == DoubleBlockHalf.LOWER && (!this.isMaxAge(state) && (this.canGrowUp(worldIn, pos) || this.getAge(state) < DOUBLE_AGE - 1));
    }

    //here I'm assuming canGrow has already been called
    @Override
    public void growCrops(World level, BlockPos pos, BlockState state) {
        growCropBy(level, pos, state, this.getBonemealAgeIncrease(level));
    }

    public void growCropBy(World level, BlockPos pos, BlockState state, int increment){
        if(state.get(HALF) == DoubleBlockHalf.UPPER){
            //as if it was called on lower
            pos = pos.below();
        }
        int newAge = this.getAge(state) +increment;
        newAge = Math.min(newAge, this.getMaxAge());

        if (newAge >= DOUBLE_AGE) {
            if (!this.canGrowUp(level, pos)) return;
            level.setBlockState(pos.above(), getStateForAge(newAge).with(HALF, DoubleBlockHalf.UPPER), 2);
        }
        level.setBlockState(pos, getStateForAge(newAge), 2);

    }

    @Override
    public boolean canReplace(BlockState pState, ItemPlacementContext pUseContext) {
        return false;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModRegistry.FLAX_SEEDS_ITEM.get();
    }

    @Override
    public boolean getPollinated(World level, BlockPos pos, BlockState state) {
        growCropBy(level, pos, state, 1);
        return true;
    }

    @Override
    public boolean isMaxAge(BlockState pState) {
        return super.isMaxAge(pState);
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult rayTraceResult) {
        ActionResult old = super.use(state, world, pos, player, hand, rayTraceResult);
        if (!old.consumesAction() && !this.isSingle(state) && state.get(HALF) == DoubleBlockHalf.UPPER) {
            PlayerInteractEvent.RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, pos.below(), rayTraceResult);
            if (event.isCanceled()) return event.getCancellationResult();
        }
        return old;
    }
}
