package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class NoticeBoardBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;

    public NoticeBoardBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(HAS_BOOK, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOK);
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile && tile.isAccessibleBy(player)) {
            return tile.interact(player, handIn, pos, state);
        }
        return ActionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new NoticeBoardBlockTile(pPos, pState);
    }


    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile) {
            //only needed if you are not using block entity tag
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            BlockUtils.addOptionalOwnership(placer, tile);
        }
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == stateIn.get(FACING)) {
            if (worldIn.getBlockEntity(currentPos) instanceof NoticeBoardBlockTile tile) {
                //((NoticeBoardBlockTile)te).textVisible = this.skipRendering(stateIn,facingState,facing);
                boolean culled = facingState.isSolidRender(worldIn, currentPos) &&
                        facingState.isFaceSturdy(worldIn, facingPos, facing.getOpposite());
                tile.setTextVisible(!culled);
            }
        }
        return stateIn;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof Container tile)
            return tile.isEmpty() ? 0 : 15;
        else
            return 0;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        super.neighborUpdate(state, world, pos, pBlock, pFromPos, pIsMoving);
        boolean powered = world.getReceivedRedstonePower(pos) > 0;
        if (world.getBlockEntity(pos) instanceof NoticeBoardBlockTile tile) {
            tile.updatePower(powered);
        }
    }


}
