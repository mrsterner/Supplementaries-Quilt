package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.Lazy;

import java.util.Random;
import java.util.function.Supplier;

public class SconceBlock extends LightUpWaterBlock {
    protected static final VoxelShape SHAPE = box(6.0D, 0.0D, 6.0D, 10.0D, 11.0D, 10.0D);
    protected final Lazy<SimpleParticleType> particleData;

    public <T extends ParticleType<?>> SconceBlock(Properties properties, Supplier<T> particleData) {
        super(properties);
        this.particleData = Lazy.of(() -> {
            SimpleParticleType data = (SimpleParticleType) particleData.get();
            if (data == null) data = ParticleTypes.FLAME;
            return data;
        });
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(LIT, true));
    }
    public <T extends ParticleType<?>> SconceBlock(Properties properties, int lightLevel, Supplier<T> particleData) {
        this(properties.lightLevel((state) -> state.get(BlockStateProperties.LIT) ? lightWorld : 0), particleData);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == Direction.DOWN && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState () : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return canSupportCenter(worldIn, pos.below(), Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
    }


    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.get(LIT)) {
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + 0.75D;
            double d2 = (double) pos.getZ() + 0.5D;
            worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            worldIn.addParticle(this.particleData.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockView world, BlockPos pos, Mob entity) {
        return BlockPathTypes.OPEN;
    }
}
