package dev.mrsterner.supplementaries.common.block.blocks;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Map;
import java.util.function.Predicate;

public class AshCauldronBlock extends LeveledCauldronBlock {
	public AshCauldronBlock(AbstractBlock.Settings properties, Predicate<Biome.Precipitation> predicate, Map<Item, CauldronBehavior> interactionMap) {
		super(AbstractBlock.Settings.copy(Blocks.CAULDRON), predicate, interactionMap);
	}

	@Override
	public void precipitationTick(BlockState pState, World world, BlockPos pPos, Biome.Precipitation pPrecipitation) {
		pState.get(LEVEL);
		world.setBlockStateState(pPos, pState.with(LEVEL, pState.get(LEVEL)-1));
	}
}
