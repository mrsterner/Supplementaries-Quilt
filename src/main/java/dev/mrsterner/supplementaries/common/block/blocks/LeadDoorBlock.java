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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Random;

public class LeadDoorBlock extends DoorBlock {
    public static IntegerProperty OPENING_PROGRESS = BlockProperties.OPENING_PROGRESS;

    public LeadDoorBlock(Properties builder) {
        super(builder);
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
            GoldDoorBlock.tryOpenDoubleDoor(worldIn, state, pos);

            state = state.cycle(OPEN).with(OPENING_PROGRESS, 0);
            worldIn.setBlockState(pos, state, 10);
            worldIn.syncWorldEvent(player, state.get(OPEN) ? this.getOpenSound() : this.getCloseSound(), pos, 0);
        } else {
            //sound here
            int p = state.get(OPENING_PROGRESS) + 1;
            if (state.get(HALF) == DoubleBlockHalf.UPPER) {
                worldIn.setBlockState(pos.below(), worldIn.getBlockState(pos.below()).with(OPENING_PROGRESS, p), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
            } else {
                worldIn.setBlockState(pos.above(), worldIn.getBlockState(pos.above()).with(OPENING_PROGRESS, p), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
            }
            worldIn.setBlockState(pos, state.with(OPENING_PROGRESS, p), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);

            worldIn.playSound(player, pos, SoundEvents.NETHERITE_BLOCK_STEP, SoundSource.BLOCKS, 1, 1);
            worldIn.scheduleTick(pos, this, 20);
        }
        return ActionResult.success(worldIn.isClient());
    }

    @Override
    public void tick(BlockState state, ServerWorld level, BlockPos pos, Random pRandom) {
        level.setBlockState(pos, state.with(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        if (state.get(HALF) == DoubleBlockHalf.UPPER) {
            level.setBlockState(pos.below(), level.getBlockState(pos.below()).with(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        } else {
            level.setBlockState(pos.above(), level.getBlockState(pos.above()).with(OPENING_PROGRESS, 0), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(worldIn, pos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) state.with(OPEN, false).with(POWERED, false);
        return state;
    }

    private int getCloseSound() {
        return 1011;
    }

    private int getOpenSound() {
        return 1005;
    }

}
