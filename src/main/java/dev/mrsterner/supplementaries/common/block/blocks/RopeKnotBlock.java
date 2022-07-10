package dev.mrsterner.supplementaries.common.block.blocks;


import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.RopeKnotBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RopeKnotBlock extends MimicBlock implements SimpleWaterloggedBlock, EntityBlock {

    private final Map<BlockState, VoxelShape> SHAPES_MAP = new HashMap<>();
    private final Map<BlockState, VoxelShape> COLLISION_SHAPES_MAP = new HashMap<>();

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<PostType> POST_TYPE = BlockProperties.POST_TYPE;

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;


    protected static final Map<Direction, BooleanProperty> FENCE_PROPERTY = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((d) -> d.getKey().getAxis().isHorizontal()).collect(Util.toMap());
    protected static final Map<Direction, EnumProperty<WallSide>> WALL_PROPERTY = ImmutableMap.of(Direction.NORTH, WallBlock.NORTH_WALL, Direction.SOUTH, WallBlock.SOUTH_WALL, Direction.WEST, WallBlock.WEST_WALL, Direction.EAST, WallBlock.EAST_WALL);

    public RopeKnotBlock(Properties properties) {
        super(properties);
        this.makeShapes();

        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.Y)
                .with(WATERLOGGED, false).with(POST_TYPE, PostType.POST)
                .with(NORTH, false).with(SOUTH, false).with(WEST, false)
                .with(EAST, false).with(UP, false).with(DOWN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, POST_TYPE, AXIS, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new RopeKnotBlockTile(pPos, pState);
    }

    /*
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.with(WATERLOGGED, false), VoxelVoxelShapes.fullCube());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COLLISION_SHAPES_MAP.getOrDefault(state.with(WATERLOGGED, false), VoxelVoxelShapes.fullCube());
    }*/


    //this is madness

    /*
    @Override
    public VoxelShape getVisualShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.with(WATERLOGGED, false), VoxelVoxelShapes.fullCube());
    }*/


    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            return tile.getShape();
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView reader, BlockPos pos) {
        return SHAPES_MAP.getOrDefault(state.with(WATERLOGGED, false), VoxelShapes.fullCube());
    }


    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockView reader, BlockPos pos) {
        return SHAPES_MAP.getOrDefault(state.with(WATERLOGGED, false), VoxelShapes.fullCube());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            return tile.getCollisionShape();
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    protected void makeShapes() {
        VoxelShape down = Block.createCuboidShape(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.createCuboidShape(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.createCuboidShape(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.createCuboidShape(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.createCuboidShape(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.createCuboidShape(6, 9, 6, 16, 13, 10);
        //VoxelShape knot = Block.createCuboidShape(6, 9, 6, 10, 13, 10);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.get(WATERLOGGED)) continue;

            VoxelShape v;
            VoxelShape c;
            int w = state.get(POST_TYPE).getWidth();
            int o = (16 - w) / 2;
            switch (state.get(AXIS)) {
                default -> {
                    v = Block.createCuboidShape(o, 0D, o, o + w, 16D, o + w);
                    c = Block.createCuboidShape(o, 0D, o, o + w, 24, o + w);
                }
                case X -> {
                    v = Block.createCuboidShape(0D, o, o, 16D, o + w, o + w);
                    c = v;
                }
                case Z -> {
                    v = Block.createCuboidShape(o, o, 0, o + w, o + w, 16);
                    c = v;
                }
            }
            if (state.get(DOWN)) v = Shapes.or(v, down);
            if (state.get(UP)) v = Shapes.or(v, up);
            if (state.get(NORTH)) v = Shapes.or(v, north);
            if (state.get(SOUTH)) v = Shapes.or(v, south);
            if (state.get(WEST)) v = Shapes.or(v, west);
            if (state.get(EAST)) v = Shapes.or(v, east);
            c = Shapes.or(c, v);
            c = c.optimize();
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

            boolean flag2 = true;
            for (VoxelShape existing : this.COLLISION_SHAPES_MAP.values()) {
                if (existing.equals(c)) {
                    this.COLLISION_SHAPES_MAP.put(state, existing);
                    flag2 = false;
                    break;
                }
            }
            if (flag2) this.COLLISION_SHAPES_MAP.put(state, c);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, WorldAccess world, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        BlockState newState = state.with(RopeBlock.FACING_TO_PROPERTY_MAP.get(facing), RopeBlock.shouldConnectToFace(state, facingState, facingPos, facing, world));
        if (world.getBlockEntity(currentPos) instanceof RopeKnotBlockTile tile) {
            BlockState oldHeld = tile.getHeldBlock();

            RopeKnotBlockTile otherTile = null;
            if (facingState.is(ModRegistry.ROPE_KNOT.get())) {
                if (world.getBlockEntity(facingPos) instanceof RopeKnotBlockTile te2) {
                    otherTile = te2;
                    facingState = otherTile.getHeldBlock();
                }
            }

            BlockState newHeld = null;

            if (CompatHandler.quark) {
                newHeld = QuarkPlugin.updateWoodPostShape(oldHeld, facing, facingState);
            }
            if (newHeld == null) {
                newHeld = oldHeld.updateShape(facing, facingState, world, currentPos, facingPos);
            }

            //manually refreshTextures facing states
            //world.setBlockState(currentPos,newHeld,2);
            BlockState newFacing = facingState.updateShape(facing.getOpposite(), newHeld, world, facingPos, currentPos);

            if (newFacing != facingState) {
                if (otherTile != null) {
                    otherTile.setHeldBlock(newFacing);
                    otherTile.setChanged();
                } else {
                    world.setBlockState(facingPos, newFacing, 2);
                }
            }

            //BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            // world.setBlockStateAndUpdate(toPos, newState);

            PostType type = PostType.get(newHeld);

            if (newHeld != oldHeld) {
                tile.setHeldBlock(newHeld);
                tile.setChanged();
            }
            if (newState != state) {
                tile.recalculateShapes(newState);
            }
            if (type != null) {
                newState = newState.with(POST_TYPE, type);
            }
        }

        return newState;
    }

    //TODO: fix this not updating mimic block
    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        if (rotation == Rotation.CLOCKWISE_180) {
            return state;
        } else {
            return switch (state.get(AXIS)) {
                case X -> state.with(AXIS, Direction.Axis.Z);
                case Z -> state.with(AXIS, Direction.Axis.X);
                default -> state;
            };
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
        boolean flag = fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8;
        return this.getDefaultState ().with(WATERLOGGED, flag);
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult p_225533_6_) {
        if (player.getItemInHand(hand).getItem() instanceof ShearsItem) {
            if (!world.isClient()) {
                if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
                    popResource(world, pos, new ItemStack(ModRegistry.ROPE_ITEM.get()));
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.PLAYERS, 0.8F, 1.3F);
                    world.setBlockState(pos, tile.getHeldBlock(), 3);
                }
            }
            return ActionResult.success(world.isClient());
        }
        return ActionResult.PASS;
    }


    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getPickStack(state, target, world, pos, player);
        }
        return super.getPickStack(state, target, world, pos, player);
    }



    public static @Nullable BlockState convertToRopeKnot(BlockProperties.PostType type, BlockState state, World world, BlockPos pos) {
        Direction.Axis axis = Direction.Axis.Y;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            axis = state.get(BlockStateProperties.AXIS);
        }
        BlockState newState = ModRegistry.ROPE_KNOT.get().getDefaultState ()
                .with(AXIS, axis).with(POST_TYPE, type);
        newState = Block.updateFromNeighbourShapes(newState, world, pos);


        if (!world.setBlockState(pos, newState, 0)) {
            return null;
        }

        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            tile.setHeldBlock(state);
            tile.setChanged();
        }
        newState.updateNeighbourShapes(world, pos, UPDATE_CLIENTS | Block.UPDATE_INVISIBLE);
        return newState;
    }
}
