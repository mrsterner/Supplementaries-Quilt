package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public class GoldTrapdoorBlock extends TrapDoorBlock {
    public GoldTrapdoorBlock(Properties properties) {
        super(properties);
    }


    public boolean canBeOpened(BlockState state) {
        return !state.get(POWERED);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (this.canBeOpened(state)) {
            state = state.cycle(OPEN);
            worldIn.setBlockState(pos, state, 2);
            if (state.get(WATERLOGGED)) {
                worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
            }

            this.playSound(player, worldIn, pos, state.get(OPEN));
            return ActionResult.success(worldIn.isClient());
        }
        return ActionResult.PASS;
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClient()) {
            boolean hasPower = worldIn.hasNeighborSignal(pos);
            if (hasPower != state.get(POWERED)) {

                worldIn.setBlockState(pos, state.with(POWERED, hasPower), 2);
                if (state.get(WATERLOGGED)) {
                    worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
                }
            }

        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = this.getDefaultState ();
        FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
        Direction direction = context.getClickedFace();
        if (!context.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
            blockstate = blockstate.with(FACING, direction).with(HALF, context.getClickLocation().y - (double) context.getBlockPos().getY() > 0.5D ? Half.TOP : Half.BOTTOM);
        } else {
            blockstate = blockstate.with(FACING, context.getPlayerFacing().getOpposite()).with(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
        }

        if (context.getWorld().hasNeighborSignal(context.getBlockPos())) {
            blockstate = blockstate.with(POWERED, Boolean.TRUE);
        }

        return blockstate.with(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }
}
