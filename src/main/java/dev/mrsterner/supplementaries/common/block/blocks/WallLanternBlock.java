package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SwayingBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WallLanternBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class WallLanternBlock extends WaterBlock implements EntityBlock {
    public static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(5, 2, 6, 11, 15.99, 16);
    public static final VoxelShape SHAPE_SOUTH = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.SOUTH);
    public static final VoxelShape SHAPE_WEST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.WEST);
    public static final VoxelShape SHAPE_EAST = Utils.rotateVoxelShape(SHAPE_NORTH, Direction.EAST);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BlockProperties.BlockAttachment> ATTACHMENT = BlockProperties.BLOCK_ATTACHMENT;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public WallLanternBlock(Properties properties) {
        super(properties.lightLevel(s -> s.get(LIT) ? s.get(LIGHT_LEVEL) : 0));
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH)
                .with(LIGHT_LEVEL, 0).with(WATERLOGGED, false).with(LIT, true));
    }

    @Override
    public ActionResult use(BlockState pState, World pLevel, BlockPos pPos, Player pPlayer, Hand pHand, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof WallLanternBlockTile te && te.isAccessibleBy(pPlayer)) {
            BlockState lantern = te.getHeldBlock();
            if (lantern.getBlock() instanceof LightableLanternBlock) {
                var opt = LightableLanternBlock.toggleLight(lantern, pLevel, pPos, pPlayer, pHand);
                if (opt.isPresent()) {
                    te.setHeldBlock(opt.get());
                    int light = opt.get().getLightEmission();
                    pLevel.setBlockStateAndUpdate(pPos, pState.with(LIGHT_LEVEL, light));
                    pLevel.sendBlockUpdated(pPos, pState, pState, Block.UPDATE_CLIENTS);
                    return ActionResult.success(pLevel.isClient());
                }
            }
        }
        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getClickedFace().getAxis() == Direction.Axis.Y) return null;
        BlockState state = super.getPlacementState(context);

        BlockPos blockpos = context.getBlockPos();
        World world = context.getWorld();
        Direction dir = context.getClickedFace();
        BlockPos relative = blockpos.relative(dir.getOpposite());
        BlockState facingState = world.getBlockState(relative);

        return getConnectedState(state, facingState, world, relative, dir).with(FACING, context.getClickedFace());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        Item i = stack.getItem();
        if (te instanceof IBlockHolder blockHolder && i instanceof BlockItem blockItem) {
            blockHolder.setHeldBlock(blockItem.getBlock().getDefaultState ());
        }
        BlockUtils.addOptionalOwnership(entity, world, pos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos,
                                  BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        return facing == stateIn.get(FACING).getOpposite() ? !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState ()
                : getConnectedState(stateIn, facingState, worldIn, facingPos, facing) : stateIn;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);
        return BlockProperties.BlockAttachment.get(blockstate, blockpos, level, direction) != null;
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
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }


    public static BlockState getConnectedState(BlockState state, BlockState facingState, WorldAccess world, BlockPos pos, Direction dir) {
        BlockProperties.BlockAttachment attachment = BlockProperties.BlockAttachment.get(facingState, pos, world, dir);
        if (attachment == null) {
            return state;
        }
        return state.with(ATTACHMENT, attachment);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return switch (state.get(FACING)) {
            default -> SHAPE_SOUTH;
            case NORTH -> SHAPE_NORTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof WallLanternBlockTile te) {
            return new ItemStack(te.getHeldBlock().getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIGHT_LEVEL, LIT, FACING, ATTACHMENT);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        if (worldIn.getBlockEntity(pos) instanceof WallLanternBlockTile te && te.isRedstoneLantern) {
            if (state.get(LIT) && !worldIn.hasNeighborSignal(pos)) {
                worldIn.setBlockState(pos, state.cycle(LIT), 2);
                if (te.getHeldBlock().hasProperty(LIT))
                    te.setHeldBlock(te.getHeldBlock().cycle(LIT));
            }
        }
    }

    //i could reference held lantern block directly but maybe it's more efficient this way idk
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient()) {
            if (world.getBlockEntity(pos) instanceof WallLanternBlockTile tile && tile.isRedstoneLantern) {
                boolean flag = state.get(LIT);
                if (flag != world.hasNeighborSignal(pos)) {
                    if (flag) {
                        world.scheduleTick(pos, this, 4);
                    } else {
                        world.setBlockState(pos, state.cycle(LIT), 2);
                        if (tile.getHeldBlock().hasProperty(LIT))
                            tile.setHeldBlock(tile.getHeldBlock().cycle(LIT));
                    }
                }
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof WallLanternBlockTile tile) {
            return tile.getHeldBlock().getDrops(builder);
        }
        return super.getDrops(state, builder);
    }


    @Override
    public void animateTick(BlockState state, World level, BlockPos pos, Random random) {
        if (level.getBlockEntity(pos) instanceof WallLanternBlockTile tile) {
            BlockState s = tile.getHeldBlock();
            s.getBlock().animateTick(s, level, pos, random);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new WallLanternBlockTile(pPos, pState);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (world.getBlockEntity(pos) instanceof SwayingBlockTile tile) {
            tile.hitByEntity(entity, state, pos);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.WALL_LANTERN_TILE.get(), pLevel.isClient() ? SwayingBlockTile::clientTick : null);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        if (world.getBlockEntity(pos) instanceof WallLanternBlockTile te) {
            return te.getHeldBlock().getSoundType();
        }
        return super.getSoundType(state, world, pos, entity);
    }

    public void placeOn(BlockState lantern, BlockPos onPos, Direction face, World world) {
        BlockState state = getConnectedState(this.getDefaultState (), world.getBlockState(onPos), world, onPos, face)
                .with(FACING, face);
        BlockPos newPos = onPos.relative(face);
        world.setBlockState(newPos, state, 3);
        if (world.getBlockEntity(newPos) instanceof IBlockHolder tile) {
            tile.setHeldBlock(lantern);
        }
    }

}
