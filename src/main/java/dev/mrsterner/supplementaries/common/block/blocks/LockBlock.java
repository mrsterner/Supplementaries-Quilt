package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class LockBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LockBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (state.get(POWERED)) return ActionResult.PASS;
        if (worldIn.getBlockEntity(pos) instanceof KeyLockableTile tile) {
            if (tile.handleAction(player, handIn, "lock_block")) {
                if (!worldIn.isClient()) {
                    worldIn.setBlockState(pos, state.with(POWERED, true), 2 | 4 | 3);
                    worldIn.scheduleTick(pos, this, 20);
                }

                worldIn.syncWorldEvent(player, 1037, pos, 0);
                //this.playSound(player, worldIn, pos, true);
            }
        }

        return ActionResult.success(worldIn.isClient());
    }

    protected void playSound(@Nullable Player player, World worldIn, BlockPos pos, boolean isOpened) {
        if (isOpened) {
            int i = this.material == Material.METAL ? 1037 : 1007;
            worldIn.syncWorldEvent(player, i, pos, 0);
        } else {
            int j = this.material == Material.METAL ? 1036 : 1013;
            worldIn.syncWorldEvent(player, j, pos, 0);
        }

    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        super.tick(state, worldIn, pos, rand);
        if (!worldIn.isClient() && state.get(POWERED)) {
            worldIn.setBlockState(pos, state.with(POWERED, false), 2 | 4 | 3);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new KeyLockableTile(pPos, pState);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockView worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.key.lockable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }
}
