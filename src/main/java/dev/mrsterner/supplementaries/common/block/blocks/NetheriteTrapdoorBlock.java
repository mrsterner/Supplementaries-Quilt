package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.util.ILavaAndWaterLoggable;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class NetheriteTrapdoorBlock extends TrapDoorBlock implements ILavaAndWaterLoggable, EntityBlock {
    public static final BooleanProperty LAVALOGGED = BlockProperties.LAVALOGGED;

    public NetheriteTrapdoorBlock(Properties properties) {
        super(properties.lightLevel(state->state.get(LAVALOGGED) ? 15 : 0));
        this.setDefaultState(this.getDefaultState ().with(FACING, Direction.NORTH)
                .with(OPEN, false).with(HALF, Half.BOTTOM).with(POWERED, false)
                .with(WATERLOGGED, false).with(LAVALOGGED, false));
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {

        if (worldIn.getBlockEntity(pos) instanceof KeyLockableTile tile) {
            if (tile.handleAction(player, handIn, "trapdoor")) {
                state = state.cycle(OPEN);
                worldIn.setBlockState(pos, state, 2);
                if (state.get(WATERLOGGED)) {
                    worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
                }

                //TODO: replace with proper sound event
                boolean open = state.get(OPEN);
                this.playSound(player, worldIn, pos, open);
                worldIn.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
        }

        return ActionResult.success(worldIn.isClient());
    }

    @Override
    public void neighborUpdate(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (state.get(WATERLOGGED)) {
            worldIn.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        } else if (state.get(LAVALOGGED)) {
            worldIn.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(worldIn));
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) return null;
        FluidState fluidstate = context.getWorld().getFluidState(context.getBlockPos());
        state = state.with(LAVALOGGED, fluidstate.getType() == Fluids.LAVA);
        return state.with(OPEN, false).with(POWERED, false);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new KeyLockableTile(pPos, pState);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction direction, BlockState pFacingState, WorldAccess pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.get(LAVALOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(pLevel));
        }
        return super.updateShape(pState, direction, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LAVALOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(LAVALOGGED) ? Fluids.LAVA.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter p_204510_1_, BlockPos p_204510_2_, BlockState p_204510_3_, Fluid p_204510_4_) {
        return ILavaAndWaterLoggable.super.canPlaceLiquid(p_204510_1_, p_204510_2_, p_204510_3_, p_204510_4_);
    }

    @Override
    public boolean placeLiquid(WorldAccess p_204509_1_, BlockPos p_204509_2_, BlockState p_204509_3_, FluidState p_204509_4_) {
        return ILavaAndWaterLoggable.super.placeLiquid(p_204509_1_, p_204509_2_, p_204509_3_, p_204509_4_);
    }

    @Override
    public Fluid takeLiquid(WorldAccess p_204508_1_, BlockPos p_204508_2_, BlockState p_204508_3_) {
        return ILavaAndWaterLoggable.super.takeLiquid(p_204508_1_, p_204508_2_, p_204508_3_);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockView worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add(new TranslatableComponent("message.supplementaries.key.lockable").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ItemStack pickupBlock(WorldAccess pLevel, BlockPos pPos, BlockState pState) {
        return ILavaAndWaterLoggable.super.pickupBlock(pLevel, pPos, pState);
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return super.getPickupSound();
    }
}
