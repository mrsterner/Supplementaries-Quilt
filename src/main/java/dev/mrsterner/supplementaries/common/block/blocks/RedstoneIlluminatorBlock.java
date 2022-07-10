package dev.mrsterner.supplementaries.common.block.blocks;


import net.minecraft.core.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.extensions.IForgeBlock;

public class RedstoneIlluminatorBlock extends Block implements IForgeBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public RedstoneIlluminatorBlock(Properties properties) {
        super(properties.lightLevel((state) -> 15 - state.get(POWER)));
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        if (!world.isClient()) {
            int pow = world.getReceivedRedstonePower(pos);
            world.setBlockState(pos, state.with(POWER, MathHelper.clamp(pow, 0, 15)), 2);
        }
    }
}
