package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;

public class LeadTrapdoorBlock extends TrapDoorBlock {

    public static IntegerProperty OPENING_PROGRESS = BlockProperties.OPENING_PROGRESS;

    public LeadTrapdoorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder);
        pBuilder.add(OPENING_PROGRESS);
    }

    public boolean canBeOpened(BlockState state) {
        return state.get(OPENING_PROGRESS) == 2;
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (this.canBeOpened(state)) {

            state = state.cycle(OPEN).with(OPENING_PROGRESS, 0);
            worldIn.setBlockState(pos, state, 2);
            if (state.get(WATERLOGGED)) {
                worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
            }
            this.playSound(player, worldIn, pos, state.get(OPEN));
        } else {
            //sound here
            worldIn.setBlockState(pos, state.with(OPENING_PROGRESS, state.get(OPENING_PROGRESS) + 1), Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_CLIENTS);
            worldIn.playSound(player, pos, SoundEvents.NETHERITE_BLOCK_STEP, SoundSource.BLOCKS, 1, 1);
            worldIn.scheduleTick(pos, this, 20);
        }
        return ActionResult.success(worldIn.isClient());
    }

    @Override
    public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
        pLevel.setBlockState(pPos, pState.with(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_CLIENTS);
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(worldIn, pos);
        if (state.get(WATERLOGGED)) {
            worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) state.with(OPEN, false).with(POWERED, false);
        return state;
    }

}
