package dev.mrsterner.supplementaries.common.block.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.api.ILightable;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.world.explosion.GunpowderExplosion;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.decorativeblocks.DecoBlocksCompatRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

/**
 * The main gunpowder block. Very similar to the RedstoneWireBlock
 * block.
 *
 * @author Tmtravlr (Rebeca Rey), updated by MehVahdJukaar
 * @Date December 2015, 2021
 */
public class GunpowderBlock extends LightUpBlock {


    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty BURNING = BlockProperties.BURNING;


    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
    private static final VoxelShape SHAPE_DOT = Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.createCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.createCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.createCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.createCuboidShape(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.createCuboidShape(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.createCuboidShape(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));

    private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
    private final BlockState crossState;

    private static int getDelay() {
        return ServerConfigs.cached.GUNPOWDER_BURN_SPEED;
    }

    private static int getSpreadAge() {
        return ServerConfigs.cached.GUNPOWDER_SPREAD_AGE;
    }

    public GunpowderBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(NORTH, RedstoneSide.NONE)
                .with(EAST, RedstoneSide.NONE).with(SOUTH, RedstoneSide.NONE)
                .with(WEST, RedstoneSide.NONE).with(BURNING, 0));
        this.crossState = this.getDefaultState ().with(NORTH, RedstoneSide.SIDE)
                .with(EAST, RedstoneSide.SIDE).with(SOUTH, RedstoneSide.SIDE)
                .with(WEST, RedstoneSide.SIDE).with(BURNING, 0);

        for (BlockState blockstate : this.getStateManager().getPossibleStates()) {
            if (blockstate.get(BURNING) == 0) {
                this.SHAPES_CACHE.put(blockstate, this.calculateVoxelShape(blockstate));
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, BURNING);
    }

    private VoxelShape calculateVoxelShape(BlockState state) {
        VoxelShape voxelshape = SHAPE_DOT;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.get(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneside == RedstoneSide.SIDE) {
                voxelshape = Shapes.or(voxelshape, SHAPES_FLOOR.get(direction));
            } else if (redstoneside == RedstoneSide.UP) {
                voxelshape = Shapes.or(voxelshape, SHAPES_UP.get(direction));
            }
        }
        return voxelshape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return this.SHAPES_CACHE.get(state.with(BURNING, 0));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getConnectionState(context.getWorld(), this.crossState, context.getBlockPos());
    }

    @Deprecated
    public boolean canReplace(BlockState p_225541_1_, Fluid p_225541_2_) {
        return this.material.isReplaceable() || !this.material.isSolid();
    }

    //-----connection logic------

    private BlockState getConnectionState(BlockGetter world, BlockState state, BlockPos pos) {
        boolean flag = isDot(state);
        state = this.getMissingConnections(world, this.getDefaultState ().with(BURNING, state.get(BURNING)), pos);
        if (!flag || !isDot(state)) {
            boolean flag1 = state.get(NORTH).isConnected();
            boolean flag2 = state.get(SOUTH).isConnected();
            boolean flag3 = state.get(EAST).isConnected();
            boolean flag4 = state.get(WEST).isConnected();
            boolean flag5 = !flag1 && !flag2;
            boolean flag6 = !flag3 && !flag4;
            if (!flag4 && flag5) {
                state = state.with(WEST, RedstoneSide.SIDE);
            }

            if (!flag3 && flag5) {
                state = state.with(EAST, RedstoneSide.SIDE);
            }

            if (!flag1 && flag6) {
                state = state.with(NORTH, RedstoneSide.SIDE);
            }

            if (!flag2 && flag6) {
                state = state.with(SOUTH, RedstoneSide.SIDE);
            }
        }
        return state;
    }

    private BlockState getMissingConnections(BlockGetter world, BlockState state, BlockPos pos) {
        boolean flag = !world.getBlockState(pos.above()).isRedstoneConductor(world, pos);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!state.get(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
                RedstoneSide redstoneside = this.getConnectingSide(world, pos, direction, flag);
                state = state.with(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, WorldAccess world, BlockPos pos, BlockPos otherPos) {
        //should be server only
        BlockState newState;
        if (direction == Direction.DOWN) {
            newState = this.canSurvive(state, world, pos) ? state : Blocks.AIR.getDefaultState ();
        } else if (direction == Direction.UP) {
            newState = this.getConnectionState(world, state, pos);
        } else {
            RedstoneSide redstoneside = this.getConnectingSide(world, pos, direction);
            newState = redstoneside.isConnected() == state.get(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && !isCross(state) ?
                    state.with(PROPERTY_BY_DIRECTION.get(direction), redstoneside) :
                    this.getConnectionState(world, this.crossState.with(BURNING, state.get(BURNING)).with(PROPERTY_BY_DIRECTION.get(direction), redstoneside), pos);
        }
        return newState;
    }

    private static boolean isCross(BlockState state) {
        return state.get(NORTH).isConnected() && state.get(SOUTH).isConnected() && state.get(EAST).isConnected() && state.get(WEST).isConnected();
    }

    private static boolean isDot(BlockState state) {
        return !state.get(NORTH).isConnected() && !state.get(SOUTH).isConnected() && !state.get(EAST).isConnected() && !state.get(WEST).isConnected();
    }

    //used to connect diagonally
    @Override
    public void updateIndirectNeighbourShapes(BlockState state, WorldAccess world, BlockPos pos, int var1, int var2) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.get(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneside != RedstoneSide.NONE && !world.getBlockState(mutable.setWithOffset(pos, direction)).is(this)) {
                mutable.move(Direction.DOWN);
                BlockState blockstate = world.getBlockState(mutable);
                if (!blockstate.is(Blocks.OBSERVER)) {
                    BlockPos blockpos = mutable.relative(direction.getOpposite());
                    BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), world.getBlockState(blockpos), world, mutable, blockpos);
                    updateOrDestroy(blockstate, blockstate1, world, mutable, var1, var2);
                }

                mutable.setWithOffset(pos, direction).move(Direction.UP);
                BlockState blockstate3 = world.getBlockState(mutable);
                if (!blockstate3.is(Blocks.OBSERVER)) {
                    BlockPos blockpos1 = mutable.relative(direction.getOpposite());
                    BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(), world.getBlockState(blockpos1), world, mutable, blockpos1);
                    updateOrDestroy(blockstate3, blockstate2, world, mutable, var1, var2);
                }
            }
        }

    }

    //gets connection to blocks diagonally above
    private RedstoneSide getConnectingSide(BlockGetter world, BlockPos pos, Direction dir) {
        return this.getConnectingSide(world, pos, dir, !world.getBlockState(pos.above()).isRedstoneConductor(world, pos));
    }

    private RedstoneSide getConnectingSide(BlockGetter world, BlockPos pos, Direction dir, boolean canClimbUp) {
        BlockPos blockpos = pos.relative(dir);
        BlockState blockstate = world.getBlockState(blockpos);
        if (canClimbUp) {
            boolean flag = this.canSurviveOn(world, blockpos, blockstate);
            if (flag && canConnectTo(world.getBlockState(blockpos.above()), world, blockpos.above(), null)) {
                if (blockstate.isFaceSturdy(world, blockpos, dir.getOpposite())) {
                    return RedstoneSide.UP;
                }
                return RedstoneSide.SIDE;
            }
        }
        return !canConnectTo(blockstate, world, blockpos, dir) && (blockstate.isRedstoneConductor(world, blockpos) || !canConnectTo(world.getBlockState(blockpos.below()), world, blockpos.below(), null)) ? RedstoneSide.NONE : RedstoneSide.SIDE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = world.getBlockState(blockpos);
        return this.canSurviveOn(world, blockpos, blockstate);
    }

    private boolean canSurviveOn(BlockGetter world, BlockPos pos, BlockState state) {
        return state.isFaceSturdy(world, pos, Direction.UP) || state.is(Blocks.HOPPER);
    }

    protected boolean canConnectTo(BlockState state, BlockView world, BlockPos pos, @Nullable Direction dir) {
        Block b = state.getBlock();
        return b instanceof ILightable || b instanceof TntBlock || b instanceof CampfireBlock || b instanceof AbstractCandleBlock ||
                (CompatHandler.deco_blocks && DecoBlocksCompatRegistry.isBrazier(b));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90 -> state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
            case CLOCKWISE_90 -> state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
            default -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
            case FRONT_BACK -> state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        };
    }


    //-----redstone------

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moving) {
        if (!oldState.is(state.getBlock()) && !world.isClient()) {
            //doesn't ignite immediately
            world.scheduleTick(pos, this, getDelay());

            for (Direction direction : Direction.Plane.VERTICAL) {
                world.updateNeighborsAt(pos.relative(direction), this);
            }

            this.updateNeighborsOfNeighboringWires(world, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            super.onRemove(state, world, pos, newState, isMoving);
            if (!world.isClient()) {
                for (Direction direction : Direction.values()) {
                    world.updateNeighborsAt(pos.relative(direction), this);
                }
                this.updateNeighborsOfNeighboringWires(world, pos);
            }
        }
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which
     * neighbor changed (coordinates passed are their own) Args: x, y, z,
     * neighbor Block
     */
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, neighborPos, moving);
        if (!world.isClient()) {
            world.scheduleTick(pos, this, getDelay());
        }
    }

    private void updateNeighborsOfNeighboringWires(World world, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(world, pos.relative(direction));
        }

        for (Direction direction1 : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction1);
            if (world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
                this.checkCornerChangeAt(world, blockpos.above());
            } else {
                this.checkCornerChangeAt(world, blockpos.below());
            }
        }
    }

    private void checkCornerChangeAt(World world, BlockPos pos) {
        if (world.getBlockState(pos).is(this)) {
            world.updateNeighborsAt(pos, this);

            for (Direction direction : Direction.values()) {
                world.updateNeighborsAt(pos.relative(direction), this);
            }
        }
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult hit) {
        ActionResult lightUp = super.use(state, world, pos, player, hand, hit);
        if (lightUp.consumesAction()) return lightUp;
        if (player.getAbilities().mayBuild) {
            if (isCross(state) || isDot(state)) {
                BlockState blockstate = isCross(state) ? this.getDefaultState () : this.crossState;
                blockstate = blockstate.with(BURNING, state.get(BURNING));
                blockstate = this.getConnectionState(world, blockstate, pos);
                if (blockstate != state) {
                    world.setBlockState(pos, blockstate, 3);
                    this.updatesOnShapeChange(world, pos, state, blockstate);
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }

    private void updatesOnShapeChange(World world, BlockPos pos, BlockState state, BlockState newState) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            if (state.get(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != newState.get(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
                world.updateNeighborsAtExceptFromFacing(blockpos, newState.getBlock(), direction.getOpposite());
            }
        }

    }


    //-----explosion-stuff------

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int burning = state.get(BURNING);

        if (!world.isClient()) {

            if (burning == 8) {
                world.removeBlock(pos, false);
                createMiniExplosion(world, pos, false);
            } else if (burning > 0) {
                if (burning >= getSpreadAge()) {
                    this.lightUpNeighbouringWires(pos, state, world);
                }
                world.setBlockStateAndUpdate(pos, state.with(BURNING, burning + 1));
                world.scheduleTick(pos, this, getDelay());
            }
            //not burning. check if it should
            else {
                for (Direction dir : Direction.values()) {
                    BlockPos p = pos.relative(dir);
                    if (this.isFireSource(world, p)) {
                        this.lightUp(null, state, pos, world, FireSound.FLAMING_ARROW);
                        world.scheduleTick(pos, this, getDelay());
                        break;
                    }
                }
            }
        }
    }


    public static void createMiniExplosion(World world, BlockPos pos, boolean alwaysFire) {
        GunpowderExplosion explosion = new GunpowderExplosion(world, null, pos.getX(), pos.getY(), pos.getZ(), 0.5f);
        if (ForgeEventFactory.onExplosionStart(world, explosion)) return;
        explosion.explode();
        explosion.finalizeExplosion(alwaysFire);
    }

    @Override
    public boolean lightUp(Entity entity, BlockState state, BlockPos pos, WorldAccess world, FireSound sound) {
        boolean ret = super.lightUp(entity, state, pos, world, sound);
        if (ret) {
            //spawn particles when first lit
            if (!world.isClient()()) {
                ((Level) world).blockEvent(pos, this, 0, 0);
            }
            world.scheduleTick(pos, this, getDelay());
        }
        return ret;
    }



    //for gunpowder -> gunpowder
    private void lightUpByWire(BlockState state, BlockPos pos, WorldAccess world) {
        if (!isLit(state)) {
            //spawn particles when first lit
            if (!world.isClient()()) {
                ((Level) world).blockEvent(pos, this, 0, 0);
            }
            world.setBlockState(pos, toggleLitState(state, true), 11);
            world.playSound(null, pos, ModSounds.GUNPOWDER_IGNITE.get(), SoundSource.BLOCKS, 2.0f,
                    1.9f + world.getRandom().nextFloat() * 0.1f);
        }
    }

    protected void lightUpNeighbouringWires(BlockPos pos, BlockState state, World world) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            RedstoneSide side = state.get(PROPERTY_BY_DIRECTION.get(dir));
            BlockState neighbourState;
            BlockPos p;
            if (side == RedstoneSide.UP) {
                p = pos.relative(dir).above();
                neighbourState = world.getBlockState(p);

            } else if (side == RedstoneSide.SIDE) {
                p = pos.relative(dir);
                neighbourState = world.getBlockState(p);
                if (!neighbourState.is(this) && !neighbourState.isRedstoneConductor(world, pos)) {
                    p = p.below();
                    neighbourState = world.getBlockState(p);
                }
            } else continue;
            if (neighbourState.is(this)) {
                world.scheduleTick(p, this, Math.max(getDelay() - 1, 1));
                this.lightUpByWire(neighbourState, p, world);
            }
        }
    }

    private boolean isFireSource(WorldAccess world, BlockPos pos) {
        //wires handled separately
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        //TODO: add tag
        if (b instanceof FireBlock || b instanceof MagmaBlock || (b instanceof TorchBlock && !(b instanceof RedstoneTorchBlock)) ||
                b == ModRegistry.BLAZE_ROD_BLOCK.get())
            return true;
        if (b instanceof CampfireBlock || (CompatHandler.deco_blocks && DecoBlocksCompatRegistry.isBrazier(b))) {
            return state.get(CampfireBlock.LIT);
        }
        return world.getFluidState(pos).getType() == Fluids.LAVA;
    }

    /**
     * Called upon the block being destroyed by an explosion
     */
    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if (!world.isClient() && this.canSurvive(state, world, pos)) {
            this.lightUp(null, state, pos, world, FireSound.FLAMING_ARROW);
        } else {
            super.onBlockExploded(state, world, pos, explosion);
        }
    }


    //TODO: this is not working
    @Override
    public void fallOn(World world, BlockState state, BlockPos pos, Entity entity, float height) {
        super.fallOn(world, state, pos, entity, height);
        if (height > 1) {
            this.extinguish(entity, world.getBlockState(pos), pos, world);
        }
    }

    //----- light up block ------


    @Override
    public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return 300;
    }

    @Override
    public void onCaughtFire(BlockState state, World world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
    }

    @Override
    public boolean isLit(BlockState state) {
        return state.get(BURNING) != 0;
    }

    @Override
    public BlockState toggleLitState(BlockState state, boolean lit) {
        return state.with(BURNING, lit ? 1 : 0);
    }

   // @Override
   // public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
   //     return new ItemStack(Items.GUNPOWDER);
   // }

    //client

    //called when first lit
    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        if (eventID == 0) {
            this.animateTick(state.with(BURNING, 1), world, pos, world.random);
            return true;
        }
        return super.triggerEvent(state, world, pos, eventID, eventParam);
    }

    /**
     * A randomly called display refreshTextures to be able to add particles or other
     * items for display
     */
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        int i = state.get(BURNING);
        if (i != 0) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                RedstoneSide redstoneside = state.get(PROPERTY_BY_DIRECTION.get(direction));
                switch (redstoneside) {
                    case UP:
                        this.spawnParticlesAlongLine(world, random, pos, i, direction, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        this.spawnParticlesAlongLine(world, random, pos, i, Direction.DOWN, direction, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        this.spawnParticlesAlongLine(world, random, pos, i, Direction.DOWN, direction, 0.0F, 0.3F);
                }
            }
        }
    }

    private void spawnParticlesAlongLine(World world, Random rand, BlockPos pos, int burning, Direction dir1, Direction dir2, float from, float to) {
        float f = to - from;
        float in = (7.5f - (burning - 1)) / 7.5f;
        if ((rand.nextFloat() < 1 * f * in)) {
            float f2 = from + f * rand.nextFloat();
            double x = pos.getX() + 0.5D + (double) (0.4375F * (float) dir1.getStepX()) + (double) (f2 * (float) dir2.getStepX());
            double y = pos.getY() + 0.5D + (double) (0.4375F * (float) dir1.getStepY()) + (double) (f2 * (float) dir2.getStepY());
            double z = pos.getZ() + 0.5D + (double) (0.4375F * (float) dir1.getStepZ()) + (double) (f2 * (float) dir2.getStepZ());

            float velY = (burning / 15.0F) * 0.03F;
            float velX = rand.nextFloat() * 0.02f - 0.01f;
            float velZ = rand.nextFloat() * 0.02f - 0.01f;

            world.addParticle(ParticleTypes.FLAME, x, y, z, velX, velY, velZ);
            world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, velX, velY, velZ);
        }
    }
}
