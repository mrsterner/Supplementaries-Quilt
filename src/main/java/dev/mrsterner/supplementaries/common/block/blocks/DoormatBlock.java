package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoormatBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DoormatBlock extends WaterBlock implements EntityBlock{

    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0.0D, 0.0D, 2.0D, 16.0D, 1.0D, 14.0D);
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(2.0D, 0.0D, 0.0D, 14.0D, 1.0D, 16.0D);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public DoormatBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    @Override
    public ActionResult use(BlockState state, World level, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if(!level.isClient()) {
            if (level.getBlockEntity(pos) instanceof DoormatBlockTile tile && tile.isAccessibleBy(player)) {

                ItemStack itemstack = player.getItemInHand(handIn);

                boolean sideHit = hit.getDirection() != Direction.UP;
                boolean canExtract = itemstack.isEmpty() && (player.isShiftKeyDown() || sideHit);
                boolean canInsert = tile.isEmpty() && sideHit;
                if (canExtract ^ canInsert) {
                    if (canExtract) {
                        ItemStack dropStack = tile.removeItemNoUpdate(0);
                        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.125, pos.getZ() + 0.5, dropStack);
                        drop.setDefaultPickUpDelay();
                        level.addFreshEntity(drop);
                    } else {
                        ItemStack newStack = itemstack.copy();
                        newStack.setCount(1);
                        tile.setItems(NonNullList.withSize(1, newStack));
                        if (!player.isCreative()) {
                            itemstack.decrement(1);
                        }
                    }
                    tile.setChanged();
                    level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F,
                            1.2f);
                    return ActionResult.CONSUME;
                }
                //color
                ActionResult result = tile.textHolder.playerInteract(level, pos, player, handIn, tile);
                if (result != ActionResult.PASS) {
                    return result;
                }
                // open gui (edit sign with empty hand)
                tile.sendOpenGuiPacket(level, pos, player);
                return ActionResult.CONSUME;
            }
            return ActionResult.PASS;
        }
        else{
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !worldIn.isEmptyBlock(pos.below());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.getDefaultState () : stateIn;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return state.get(FACING).getAxis() == Direction.Axis.X ? SHAPE_WEST : SHAPE_NORTH;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return super.getPlacementState(context).with(FACING, context.getPlayerFacing());
    }

    //for player bed spawn
    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new DoormatBlockTile(pPos, pState);
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
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof Container tile) {
                Containers.dropContents(world, pos, tile);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }

}
