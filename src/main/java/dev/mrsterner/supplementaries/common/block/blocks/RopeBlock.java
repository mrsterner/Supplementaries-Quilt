package dev.mrsterner.supplementaries.common.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.math.MathHelperUtils;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils.PlayerLessContext;
import net.mehvahdjukaar.supplementaries.common.items.ItemsUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.decorativeblocks.RopeChandelierBlock;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPistonPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.Util;
import net.minecraft.advancements.Criteria;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Entity ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

public class RopeBlock extends WaterBlock {

    //TODO: make solid when player is not colliding
    public static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 13, 16);

    private final Map<BlockState, VoxelShape> SHAPES_MAP = new HashMap<>();

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final IntegerProperty DISTANCE = BlockStateProperties.STABILITY_DISTANCE;
    public static final BooleanProperty KNOT = BlockProperties.KNOT;

    public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
        directions.put(Direction.UP, UP);
        directions.put(Direction.DOWN, DOWN);
    });

    public RopeBlock(Properties properties) {
        super(properties);
        this.makeShapes();
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(UP, true).with(DOWN, true).with(KNOT, false).with(DISTANCE, 7).with(WATERLOGGED, false)
                .with(NORTH, false).with(SOUTH, false).with(EAST, false).with(WEST, false));
    }

    @Override
    public boolean canReplace(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return SHAPES_MAP.getOrDefault(state.with(DISTANCE, 0).with(WATERLOGGED, false), VoxelShapes.fullCube());
    }

    //oh boy 32k shapes. 2k by removing water and distance lol
    protected void makeShapes() {
        VoxelShape down = Block.createCuboidShape(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.createCuboidShape(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.createCuboidShape(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.createCuboidShape(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.createCuboidShape(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.createCuboidShape(6, 9, 6, 16, 13, 10);
        VoxelShape knot = Block.createCuboidShape(6, 9, 6, 10, 13, 10);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.get(WATERLOGGED) || state.get(DISTANCE) != 0) continue;
            VoxelShape v = Shapes.empty();
            if (state.get(KNOT)) v = Shapes.or(knot);
            if (state.get(DOWN)) v = Shapes.or(v, down);
            if (state.get(UP)) v = Shapes.or(v, up);
            if (state.get(NORTH)) v = Shapes.or(v, north);
            if (state.get(SOUTH)) v = Shapes.or(v, south);
            if (state.get(WEST)) v = Shapes.or(v, west);
            if (state.get(EAST)) v = Shapes.or(v, east);
            v = v.optimize();
            boolean flag = true;
            for (VoxelShape existing : this.SHAPES_MAP.values()) {
                if (existing.equals(v)) {
                    this.SHAPES_MAP.put(state, existing);
                    flag = false;
                    break;
                }
            }
            if (flag) this.SHAPES_MAP.put(state, v);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED, DISTANCE, KNOT);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
        return state.get(DOWN) && (state.get(UP) || entity.position().y() - pos.getY() < (13 / 16f));
    }


    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return ((!state.get(UP) && (context.isAbove(COLLISION_SHAPE, pos, true) || !state.get(DOWN)))
                || !(context instanceof Entity ShapeContext  ec && ec.getEntity() instanceof LivingEntity) ?
                getShape(state, worldIn, pos, context) : Shapes.empty());

    }

    public static boolean shouldConnectToDir(BlockState thisState, BlockPos currentPos, LevelReader world, Direction dir) {
        BlockPos facingPos = currentPos.relative(dir);
        return shouldConnectToFace(thisState, world.getBlockState(facingPos), facingPos, dir, world);
    }

    public static boolean shouldConnectToFace(BlockState thisState, BlockState facingState, BlockPos facingPos, Direction dir, LevelReader world) {
        Block thisBlock = thisState.getBlock();
        Block b = facingState.getBlock();
        boolean isKnot = thisBlock == ModRegistry.ROPE_KNOT.get();
        boolean isVerticalKnot = isKnot && thisState.get(RopeKnotBlock.AXIS) == Direction.Axis.Y;

        switch (dir) {
            case UP -> {
                if (isVerticalKnot) return false;
                return RopeBlock.isSupportingCeiling(facingState, facingPos, world);
            }
            case DOWN -> {
                if (isVerticalKnot) return false;
                return RopeBlock.isSupportingCeiling(facingPos.above(2), world) || RopeBlock.canConnectDown(facingState);
            }
            default -> {
                if (ServerConfigs.cached.ROPE_UNRESTRICTED && facingState.isFaceSturdy(world, facingPos, dir.getOpposite())) {
                    return true;
                }
                if (facingState.is(ModRegistry.ROPE_KNOT.get())) {
                    return thisBlock != b && (dir.getAxis() == Direction.Axis.Y || facingState.get(RopeKnotBlock.AXIS) == Direction.Axis.Y);
                } else if (isKnot && !isVerticalKnot) {
                    return false;
                }
                return b == ModRegistry.ROPE.get();
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (!worldIn.isClient()()) {
            worldIn.scheduleTick(currentPos, this, 1);
        }

        if (facing == Direction.UP) {
            stateIn = stateIn.with(DOWN, shouldConnectToDir(stateIn, currentPos, worldIn, Direction.DOWN));
        }
        stateIn = stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), shouldConnectToDir(stateIn, currentPos, worldIn, facing));


        if (facing == Direction.DOWN && !worldIn.isClient()() && CompatHandler.deco_blocks) {
            RopeChandelierBlock.tryConverting(facingState, worldIn, facingPos);
        }

        return stateIn.with(KNOT, hasMiddleKnot(stateIn));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        boolean hasWater = context.getWorld().getFluidState(pos).getType() == Fluids.WATER;
        BlockState state = this.getDefaultState ();
        for (Direction dir : Direction.values()) {
            state = state.with(FACING_TO_PROPERTY_MAP.get(dir), shouldConnectToDir(state, pos, world, dir));
        }

        state = state.with(WATERLOGGED, hasWater);
        state = state.with(KNOT, hasMiddleKnot(state)).with(DISTANCE, this.getDistance(world, pos));
        return state;
    }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isClient()) {
            worldIn.scheduleTick(pos, this, 1);
            if (CompatHandler.deco_blocks) {
                BlockPos down = pos.below();
                RopeChandelierBlock.tryConverting(worldIn.getBlockState(down), worldIn, down);
            }
        }
    }

    public static boolean hasMiddleKnot(BlockState state) {
        boolean up = state.get(UP);
        boolean down = state.get(DOWN);
        boolean north = state.get(NORTH);
        boolean east = state.get(EAST);
        boolean south = state.get(SOUTH);
        boolean west = state.get(WEST);
        //not inverse
        return !((up && down && !north && !south && !east && !west)
                || (!up && !down && north && south && !east && !west)
                || (!up && !down && !north && !south && east && west));

    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return (this.getDistance(worldIn, pos) < 7);
        //return!(!state.get(UP)&&state.get(NORTH).isNone()&&state.get(SOUTH).isNone()&&state.get(EAST).isNone()&&state.get(WEST).isNone());
    }

    public static boolean isSupportingCeiling(BlockState facingState, BlockPos pos, LevelReader world) {
        Block b = facingState.getBlock();
        return canSupportCenter(world, pos, Direction.DOWN) || facingState.is(ModTags.ROPE_SUPPORT_TAG) ||
                (facingState.is(ModRegistry.ROPE_KNOT.get()) && facingState.get(RopeKnotBlock.AXIS) != Direction.Axis.Y);
    }

    public static boolean isSupportingCeiling(BlockPos pos, LevelReader world) {
        return isSupportingCeiling(world.getBlockState(pos), pos, world);
    }

    public static boolean canConnectDown(BlockPos currentPos, LevelReader world) {
        BlockState state = world.getBlockState(currentPos.below());
        return canConnectDown(state);
    }

    public static boolean canConnectDown(BlockState downState) {
        Block b = downState.getBlock();
        return (downState.is(ModRegistry.ROPE.get()) || downState.is(ModTags.ROPE_HANG_TAG)
                || (downState.is(ModRegistry.ROPE_KNOT.get()) && downState.get(RopeKnotBlock.AXIS) != Direction.Axis.Y)
                || (downState.hasProperty(FaceAttachedHorizontalDirectionalBlock.FACE) && downState.get(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING)
                || (b instanceof ChainBlock && downState.get(BlockStateProperties.AXIS) == Direction.Axis.Y)
                || (downState.hasProperty(BlockStateProperties.HANGING) && downState.get(BlockStateProperties.HANGING)));
    }

    public int getDistance(LevelReader world, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable().move(Direction.UP);
        BlockState blockstate = world.getBlockState(mutable);
        int i = 7;
        if (blockstate.is(this)) {
            if (blockstate.get(DOWN) || !blockstate.get(UP)) {
                i = blockstate.get(DISTANCE);
            }
        } else if (isSupportingCeiling(mutable, world)) {
            return 0;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos facingPos = mutable.setWithOffset(pos, direction);
            BlockState sideState = world.getBlockState(facingPos);
            Block b = sideState.getBlock();
            if (b instanceof RopeBlock) {
                i = Math.min(i, sideState.get(DISTANCE) + 1);
                if (i == 1) {
                    break;
                }
            } else if (shouldConnectToFace(this.getDefaultState (), sideState, facingPos, direction, world)) i = 0;
        }

        return i;
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        int i = this.getDistance(worldIn, pos);
        BlockState blockstate = state.with(DISTANCE, i);
        if (i == 7) {
            worldIn.destroyBlock(pos, true);
        } else if (state != blockstate) {
            worldIn.setBlockState(pos, blockstate, 3);
        }
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return state.get(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    @Override
    public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return state.get(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    public static boolean findAndRingBell(World world, BlockPos pos, Player player, int it, Predicate<BlockState> predicate) {

        if (it > ServerConfigs.cached.BELL_CHAIN_LENGTH) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (predicate.test(state)) {
            return findAndRingBell(world, pos.above(), player, it + 1, predicate);
        } else if (b instanceof BellBlock && it != 0) {
            //boolean success = CommonUtil.tryRingBell(Block b, world, pos, state.get(BellBlock.FACING).getClockWise());
            BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    state.get(BellBlock.FACING).getClockWise(), pos, true);
            //if (success && player != null) {//player.awardStat(Stats.BELL_RING);}
            return ((BellBlock) b).onHit(world, state, hit, player, true);
        }
        return false;
    }

    private static boolean findConnectedPulley(World world, BlockPos pos, Player player, int it, BlockRotation rot) {
        if (it > 64) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof RopeBlock) {
            return findConnectedPulley(world, pos.above(), player, it + 1, rot);
        } else if (b instanceof PulleyBlock pulley && it != 0) {
            if (world.getBlockEntity(pos) instanceof PulleyBlockTile tile) {
                if (tile.isEmpty() && !player.isShiftKeyDown()) {
                    tile.setDisplayedItem(new ItemStack(ModRegistry.ROPE_ITEM.get()));
                    boolean ret = pulley.windPulley(state, pos, world, rot, null);
                    tile.getDisplayedItem().decrement(1);
                    return ret;
                } else {
                    return pulley.windPulley(state, pos, world, rot, null);
                }
            }
        }
        return false;
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item i = stack.getItem();

        if (i == this.asItem()) {
            if (hit.getDirection().getAxis() == Direction.Axis.Y || state.get(DOWN)) {
                //restores sheared
                if (state.get(UP) && !state.get(DOWN)) {
                    state = state.with(DOWN, true);
                    world.setBlockState(pos, state, 0);
                }
                if (addRope(pos.below(), world, player, handIn, this)) {
                    SoundType soundtype = state.getSoundType(world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (!player.getAbilities().instabuild) {
                        stack.decrement(1);
                    }
                    return ActionResult.success(world.isClient());
                }
            }
            return ActionResult.PASS;
        } else if (stack.isEmpty()) {
            if (state.get(UP)) {
                if (ServerConfigs.cached.BELL_CHAIN && findAndRingBell(world, pos, player, 0, s -> s.getBlock() == this))
                    return ActionResult.success(world.isClient());
                else if (findConnectedPulley(world, pos, player, 0, player.isShiftKeyDown() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90)) {
                    return ActionResult.success(world.isClient());
                }
            }
            if (!player.isShiftKeyDown() && handIn == Hand.MAIN_HAND) {
                if (world.getBlockState(pos.below()).getBlock() == this) {
                    if (removeRope(pos.below(), world, this)) {
                        world.playSound(player, pos, SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 1, 0.6f);
                        if (!player.getAbilities().instabuild) {
                            Utils.addStackToExisting(player, new ItemStack(this));
                        }
                        return ActionResult.success(world.isClient());
                    }
                }
            }
        } else if (i instanceof ShearsItem) {
            if (state.get(DOWN)) {
                if (!world.isClient()) {
                    //TODO: proper sound event here
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 0.8F, 1.3F);
                    BlockState newState = state.with(DOWN, false).with(KNOT, true);
                    world.setBlockState(pos, newState, 3);
                    //refreshTextures below
                    //world.updateNeighborsAt(pos, newState.getBlock());
                }
                return ActionResult.success(world.isClient());
            }
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    public static boolean removeRope(BlockPos pos, World world, Block ropeBlock) {
        BlockState state = world.getBlockState(pos);
        if (ropeBlock == state.getBlock()) {
            return removeRope(pos.below(), world, ropeBlock);
        } else {
            //if (dist == 0) return false;
            BlockPos up = pos.above();
            if (!(world.getBlockState(up).getBlock() == ropeBlock)) return false;
            FluidState fromFluid = world.getFluidState(up);
            boolean water = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource());
            world.setBlockStateAndUpdate(up, water ? Blocks.WATER.getDefaultState () : Blocks.AIR.getDefaultState ());
            tryMove(pos, up, world);
            return true;
        }
    }


    public static boolean addRope(BlockPos pos, World world, @Nullable Player player, Hand hand, Block ropeBlock) {
        BlockState state = world.getBlockState(pos);
        if (ropeBlock == state.getBlock()) {
            return addRope(pos.below(), world, player, hand, ropeBlock);
        } else {
            return tryPlaceAndMove(player, hand, world, pos, ropeBlock);
        }
    }

    public static boolean tryPlaceAndMove(@Nullable Player player, Hand hand, World world, BlockPos pos, Block ropeBlock) {
        ItemStack stack = new ItemStack(ropeBlock);

        ItemPlacementContext context = new PlayerLessContext(world, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        if (!context.canPlace()) {
            //checks if block below this is hollow
            BlockPos downPos = pos.below();
            //try move block down
            if (!(world.getBlockState(downPos).getMaterial().isReplaceable()
                    && tryMove(pos, downPos, world))) return false;
            context = new PlayerLessContext(world, player, hand, stack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        }

        BlockState state = ItemsUtil.getPlacementState(context, ropeBlock);
        if (state == null) return false;
        if (state == world.getBlockState(context.getBlockPos())) return false;
        if (world.setBlockState(context.getBlockPos(), state, 11)) {
            if (player != null) {
                BlockState placedState = world.getBlockState(context.getBlockPos());
                Block block = placedState.getBlock();
                if (block == state.getBlock()) {
                    block.onPlaced(world, context.getBlockPos(), placedState, player, stack);
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) player, context.getBlockPos(), stack);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isBlockMovable(BlockState state, World level, BlockPos pos) {
        return (!state.isAir() && !state.is(Blocks.OBSIDIAN) &&
                !state.is(Blocks.CRYING_OBSIDIAN) && !state.is(Blocks.RESPAWN_ANCHOR))
                && state.getDestroySpeed(level, pos) != -1;
    }

    //TODO: fix order of operations to allow pulling down lanterns
    private static boolean tryMove(BlockPos fromPos, BlockPos toPos, World world) {
        if (toPos.getY() < world.getMinBuildHeight() || toPos.getY() > world.getMaxBuildHeight()) return false;
        BlockState state = world.getBlockState(fromPos);

        PushReaction push = state.getPistonPushReaction();

        if (isBlockMovable(state, world, fromPos) &&
                (
                        ((push == PushReaction.NORMAL || (toPos.getY() < fromPos.getY() && push == PushReaction.PUSH_ONLY)) && state.canSurvive(world, toPos))
                                || (state.is(ModTags.ROPE_HANG_TAG))
                )
        ) {

            BlockEntity tile = world.getBlockEntity(fromPos);
            if (tile != null) {
                //moves everything if quark is not enabled. bad :/ install quark guys
                if (CompatHandler.quark && !QuarkPistonPlugin.canMoveTile(state)) {
                    return false;
                } else {
                    tile.setRemoved();
                }
            }

            //gets refreshTextures state for new position

            Fluid fluidState = world.getFluidState(toPos).getType();
            boolean waterFluid = fluidState == Fluids.WATER;
            boolean canHoldWater = false;
            if (state.hasProperty(WATERLOGGED)) {
                canHoldWater = state.is(ModTags.WATER_HOLDER);
                if (!canHoldWater) state = state.with(WATERLOGGED, waterFluid);
            } else if (state.getBlock() instanceof AbstractCauldronBlock) {
                if (waterFluid && state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
                    state = Blocks.WATER_CAULDRON.getDefaultState ().with(LayeredCauldronBlock.LEVEL, 3);
                }
                if (fluidState == Fluids.LAVA && state.is(Blocks.CAULDRON) || state.is(Blocks.LAVA_CAULDRON)) {
                    state = Blocks.LAVA_CAULDRON.getDefaultState ().with(LayeredCauldronBlock.LEVEL, 3);
                }
            }


            FluidState fromFluid = world.getFluidState(fromPos);
            boolean leaveWater = (fromFluid.getType() == Fluids.WATER && fromFluid.isSource()) && !canHoldWater;
            world.setBlockStateAndUpdate(fromPos, leaveWater ? Blocks.WATER.getDefaultState () : Blocks.AIR.getDefaultState ());

            //refreshTextures existing block block to new position
            BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            world.setBlockStateAndUpdate(toPos, newState);
            if (tile != null) {
                NbtCompound tag = tile.saveWithoutMetadata();
                BlockEntity te = world.getBlockEntity(toPos);
                if (te != null) {
                    te.load(tag);
                }
            }
            //world.notifyNeighborsOfStateChange(toPos, state.getBlock());
            world.neighborUpdate(toPos, state.getBlock(), toPos);
            return true;
        }
        return false;
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityCollision(state, worldIn, pos, entityIn);
        if (entityIn instanceof Arrow && !worldIn.isClient()) {
            worldIn.destroyBlock(pos, true, entityIn);
            //TODO: add proper sound event
            worldIn.playSound(null, pos, SoundEvents.LEASH_KNOT_BREAK, SoundSource.BLOCKS, 1, 1);
        }
    }

    //for culling
    @Override
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.is(this) || super.skipRendering(pState, pAdjacentBlockState, pSide);
    }

    public static boolean playEntitySlideSound(LivingEntity entity, int ropeTicks) {
        if (ropeTicks % 14 == 0) {
            if (!entity.isSilent()) {
                Player p = entity instanceof Player pl ? pl : null;
                entity.level.playSound(p, entity.getX(), entity.getY(), entity.getZ(), ModSounds.ROPE_SLIDE.get(),
                        entity.getSoundSource(), 0.1f, 1);
            }
            return true;
        }
        return false;
    }

}
