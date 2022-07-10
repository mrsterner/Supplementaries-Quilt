package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class SconceLeverBlock extends SconceWallBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SconceLeverBlock(Properties properties, Supplier<SimpleParticleType> particleData) {
        super(properties, particleData);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false)
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(LIT, true));
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        ActionResult result = super.use(state, worldIn, pos, player, handIn, hit);
        if (result.consumesAction()) {
            this.updateNeighbors(state, worldIn, pos);
            return result;
        }
        if (worldIn.isClient()) {
            state.cycle(POWERED);
            return ActionResult.SUCCESS;
        } else {
            BlockState blockstate = this.setPowered(state, worldIn, pos);
            boolean enabled = blockstate.get(POWERED);
            float f = enabled ? 0.6F : 0.5F;
            worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            worldIn.gameEvent(player, enabled ? GameEvent.BLOCK_SWITCH : GameEvent.BLOCK_UNSWITCH, pos);
            return ActionResult.CONSUME;
        }
    }

    public BlockState setPowered(BlockState state, World world, BlockPos pos) {
        state = state.cycle(POWERED);
        world.setBlockState(pos, state, 3);
        this.updateNeighbors(state, world, pos);
        return state;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            if (state.get(POWERED)) {
                this.updateNeighbors(state, worldIn, pos);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public int getSignal(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED) ^ !blockState.get(LIT) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED) ^ !blockState.get(LIT) && getFacing(blockState) == side ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    private void updateNeighbors(BlockState state, World world, BlockPos pos) {
        world.updateNeighborsAt(pos, this);
        world.updateNeighborsAt(pos.relative(getFacing(state).getOpposite()), this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    protected static Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!stateIn.get(POWERED)) {
            super.animateTick(stateIn, worldIn, pos, rand);
        } else if (stateIn.get(LIT)) {
            Direction direction = stateIn.get(FACING);
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.65D;
            double d2 = (double) pos.getZ() + 0.5D;
            Direction direction1 = direction.getOpposite();
            worldIn.addParticle(ParticleTypes.SMOKE, d0 + 0.125D * (double) direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0 + 0.125D * (double) direction1.getStepX(), d1 + 0.15D, d2 + 0.125D * (double) direction1.getStepZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean lightUp(Entity entity, BlockState state, BlockPos pos, WorldAccess world, FireSound sound) {
        boolean ret = super.lightUp(entity, state, pos, world, sound);
        if (ret && world instanceof ServerWorld level) updateNeighbors(state, level, pos);
        return ret;
    }

    @Override
    public boolean extinguish(@Nullable Entity player, BlockState state, BlockPos pos, WorldAccess world) {
        boolean ret = super.extinguish(player, state, pos, world);
        if (ret && world instanceof ServerWorld level) updateNeighbors(state, level, pos);
        return ret;
    }
}
