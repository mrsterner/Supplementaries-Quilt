package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Arrays;

public class FodderBlock extends WaterBlock {
    private static final int MAX_LAYERS = 8;
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[MAX_LAYERS];

    static {
        Arrays.setAll(SHAPE_BY_LAYER, l -> Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, l * 2 + 2, 16.0D));
    }

    public FodderBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(LAYERS, 8).with(WATERLOGGED, false));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView blockGetter, BlockPos pos, NavigationType pathType) {
        if (pathType == NavigationType.LAND) {
            return state.get(LAYERS) <= MAX_LAYERS / 2;
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView blockGetter, BlockPos pos,  ShapeContext  p_220053_4_) {
        return SHAPE_BY_LAYER[state.get(LAYERS) - 1];
    }


    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    //ugly but works
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
        if (facingState.is(this)) {
            if (direction == Direction.UP) {
                int layers = state.get(LAYERS);
                int missing = MAX_LAYERS - layers;
                if (missing > 0) {
                    int otherLayers = facingState.get(LAYERS);
                    int newOtherLayers = otherLayers - missing;
                    BlockState newOtherState;
                    if (newOtherLayers <= 0) {
                        newOtherState = Blocks.AIR.getDefaultState ();
                    } else {
                        newOtherState = facingState.with(LAYERS, newOtherLayers);
                    }
                    BlockState newState = state.with(LAYERS, layers + otherLayers - Math.max(0, newOtherLayers));
                    world.setBlockState(currentPos, newState, 0);
                    world.setBlockState(otherPos, newOtherState, 0);
                    return newState;
                }
            } else if (direction == Direction.DOWN) {
                int layers = facingState.get(LAYERS);
                int missing = MAX_LAYERS - layers;
                if (missing > 0) {
                    int myLayers = state.get(LAYERS);
                    int myNewLayers = myLayers - missing;
                    BlockState myNewState;
                    if (myNewLayers <= 0) {
                        myNewState = Blocks.AIR.getDefaultState ();
                    } else {
                        myNewState = state.with(LAYERS, myNewLayers);
                    }
                    world.setBlockState(otherPos, state.with(LAYERS, layers + myLayers - Math.max(0, myNewLayers)), 0);
                    return myNewState;
                }
            }
        }
        return super.updateShape(state, direction, facingState, world, currentPos, otherPos);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getBlockPos());
        if (blockstate.is(this)) {
            int i = blockstate.get(LAYERS);
            return blockstate.with(LAYERS, Math.min(MAX_LAYERS, i + 1));
        } else {
            return super.getPlacementState(context);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LAYERS);
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof HoeItem) {
            world.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClient()) {

                int layers = state.get(FodderBlock.LAYERS);
                if (layers > 1) {
                    world.syncWorldEvent(2001, pos, Block.getId(state));
                    world.setBlockState(pos, state.with(FodderBlock.LAYERS, layers - 1), 11);
                } else {
                    world.destroyBlock(pos, false);
                }
                stack.hurtAndBreak(1, player, (e) -> {
                    e.broadcastBreakEvent(hand);
                });
            }
            return ActionResult.success(world.isClient());
        }
        return ActionResult.PASS;
    }
}
