package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.world.level.block.state.BlockState;

public class SilverDoorBlock extends GoldDoorBlock {

    public SilverDoorBlock(Properties builder) {
        super(builder);
    }

    @Override
    public boolean canBeOpened(BlockState state) {
        return state.get(POWERED);
    }
}
