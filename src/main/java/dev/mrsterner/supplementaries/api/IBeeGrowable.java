package dev.mrsterner.supplementaries.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IBeeGrowable {

    boolean getPollinated(World level, BlockPos pos, BlockState state);
}
