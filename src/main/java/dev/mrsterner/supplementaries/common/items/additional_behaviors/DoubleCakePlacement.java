package net.mehvahdjukaar.supplementaries.common.items.additional_behaviors;

import net.mehvahdjukaar.supplementaries.api.AdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.block.blocks.DoubleCakeBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleCakePlacement implements AdditionalPlacement {

    @Override
    public ItemPlacementContext overrideUpdatePlacementContext(ItemPlacementContext pContext) {
        BlockPos pos = pContext.getBlockPos().relative(pContext.getClickedFace());
        World level = pContext.getWorld();
        BlockState state = level.getBlockState(pos);
        if (isValidCake(state)) {
            return ItemPlacementContext.at(pContext, pos, pContext.getClickedFace());
        }
        return null;
    }

    private boolean isValidCake(BlockState state) {
        if (!ServerConfigs.cached.DOUBLE_CAKE_PLACEMENT) return false;
        Block block = state.getBlock();
        return (block == Blocks.CAKE || block == ModRegistry.DIRECTIONAL_CAKE.get()) && state.get(CakeBlock.BITES) == 0;
    }

    @Override
    public BlockState overrideGetPlacementState(ItemPlacementContext pContext) {
        BlockPos pos = pContext.getBlockPos();
        World level = pContext.getWorld();
        BlockState state = level.getBlockState(pos);
        if (isValidCake(state)) {
            return ModRegistry.DOUBLE_CAKE.get().withPropertiesOf(state).with(DoubleCakeBlock.FACING,pContext.getPlayerFacing());
        }
        return null;
    }

}
