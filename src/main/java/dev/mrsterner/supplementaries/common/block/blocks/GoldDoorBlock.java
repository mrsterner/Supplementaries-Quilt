package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public class GoldDoorBlock extends DoorBlock {

    public GoldDoorBlock(Properties builder) {
        super(builder);
    }

    public boolean canBeOpened(BlockState state) {
        return !state.get(POWERED);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (this.canBeOpened(state)) {
            tryOpenDoubleDoor(worldIn, state, pos);

            state = state.cycle(OPEN);
            worldIn.setBlockState(pos, state, 10);
            worldIn.syncWorldEvent(player, state.get(OPEN) ? this.getOpenSound() : this.getCloseSound(), pos, 0);
            return ActionResult.success(worldIn.isClient());
        }
        return ActionResult.PASS;
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        boolean hasPower = worldIn.hasNeighborSignal(pos) || worldIn.hasNeighborSignal(pos.relative(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (blockIn != this && hasPower != state.get(POWERED)) {
            worldIn.setBlockState(pos, state.with(POWERED, hasPower), 2);
        }

    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) state.with(OPEN, false);
        return state;
    }

    private int getCloseSound() {
        return 1011;
    }

    private int getOpenSound() {
        return 1005;
    }


    //double door stuff

    public static void tryOpenDoubleDoor(World world, BlockState state, BlockPos pos) {
        if ((CompatHandler.quark && QuarkPlugin.isDoubleDoorEnabled() || CompatHandler.doubledoors)) {
            Direction direction = state.get(DoorBlock.FACING);
            boolean isOpen = state.get(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.get(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
            BlockPos doorPos = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.get(DoorBlock.FACING) == direction && !other.get(DoorBlock.POWERED) &&
                    other.get(DoorBlock.OPEN) == isOpen && other.get(DoorBlock.HINGE) != isMirrored) {
                BlockState newState = other.cycle(DoorBlock.OPEN);
                world.setBlockState(doorPos, newState, 10);
            }
        }
    }

    public static void tryOpenDoubleDoorKey(World world, BlockState state, BlockPos pos, Player player, Hand hand) {
        if ((CompatHandler.quark && QuarkPlugin.isDoubleDoorEnabled() || CompatHandler.doubledoors)) {
            Direction direction = state.get(DoorBlock.FACING);
            boolean isOpen = state.get(DoorBlock.OPEN);
            DoorHingeSide isMirrored = state.get(DoorBlock.HINGE);
            BlockPos mirrorPos = pos.relative(isMirrored == DoorHingeSide.RIGHT ? direction.getCounterClockWise() : direction.getClockWise());
            BlockPos doorPos = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? mirrorPos : mirrorPos.below();
            BlockState other = world.getBlockState(doorPos);
            if (other.getBlock() == state.getBlock() && other.get(DoorBlock.FACING) == direction && other.get(DoorBlock.OPEN) == isOpen && other.get(DoorBlock.HINGE) != isMirrored) {
                if (world.getBlockEntity(doorPos) instanceof KeyLockableTile keyLockableTile && (keyLockableTile.handleAction(player, hand, "door"))) {
                    BlockState newState = other.cycle(DoorBlock.OPEN);
                    world.setBlockState(doorPos, newState, 10);
                }
            }
        }
    }
}
