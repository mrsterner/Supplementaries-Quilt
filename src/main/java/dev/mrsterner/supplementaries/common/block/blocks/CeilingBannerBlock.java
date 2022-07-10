package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CeilingBannerBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CeilingBannerBlock extends AbstractBannerBlock {
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE_X = Block.createCuboidShape(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_Z = Block.createCuboidShape(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);

    public CeilingBannerBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(ATTACHED, false));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        if (state.get(ATTACHED)) {
            return this.canAttach(state, above);
        }
        return above.getMaterial().isSolid();
    }

    private boolean canAttach(BlockState state, BlockState above) {
        Block b = above.getBlock();
        if (b instanceof RopeBlock) {
            if (!above.get(RopeBlock.DOWN)) {
                Direction dir = state.get(FACING);
                return above.get(RopeBlock.FACING_TO_PROPERTY_MAP.get(dir.getClockWise())) &&
                        above.get(RopeBlock.FACING_TO_PROPERTY_MAP.get(dir.getCounterClockWise()));
            }
            return false;
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState myState, Direction direction, BlockState otherState, WorldAccess world, BlockPos myPos, BlockPos otherPos) {
        return direction == Direction.UP && !myState.canSurvive(world, myPos) ? Blocks.AIR.getDefaultState () : super.updateShape(myState, direction, myState, world, myPos, otherPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView p_220053_2_, BlockPos p_220053_3_,  ShapeContext  p_220053_4_) {
        return state.get(FACING).getAxis() == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    public BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getClickedFace() == Direction.DOWN) {
            BlockState blockstate = this.getDefaultState ();
            LevelReader world = context.getWorld();
            BlockPos blockpos = context.getBlockPos();
            blockstate = blockstate.with(FACING, context.getPlayerFacing().getOpposite());
            boolean attached = this.canAttach(blockstate, world.getBlockState(blockpos.above()));
            blockstate = blockstate.with(ATTACHED, attached);
            if (blockstate.canSurvive(world, blockpos)) {
                return blockstate;
            }
        }
        return null;
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new CeilingBannerBlockTile(pPos, pState, this.getColor());
    }

    @Override
    public void onPlaced(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.hasCustomHoverName()) {
            if (pLevel.getBlockEntity(pPos) instanceof CeilingBannerBlockTile tile) {
                tile.setCustomName(pStack.getHoverName());
            }
        }
    }

    @Override
    public ActionResult use(BlockState pState, World pLevel, BlockPos pPos, Player pPlayer, Hand pHand, BlockHitResult pHit) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Item item = itemstack.getItem();

        //put post on map
        if (item instanceof MapItem) {
            if (!pLevel.isClient()) {
                if (MapItem.getSavedData(itemstack, pLevel) instanceof ExpandedMapData data) {
                    data.toggleCustomDecoration(pLevel, pPos);
                }
            }
            return ActionResult.success(pLevel.isClient());
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
