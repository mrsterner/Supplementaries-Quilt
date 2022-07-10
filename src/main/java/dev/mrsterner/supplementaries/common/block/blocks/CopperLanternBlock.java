package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class CopperLanternBlock extends LightableLanternBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public CopperLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.Z)
                .with(WATERLOGGED, false).with(LIT,true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        var b = super.getPlacementState(context);
        if (b != null) b = b.with(AXIS, context.getPlayerFacing().getAxis());
        return b;
    }
}
