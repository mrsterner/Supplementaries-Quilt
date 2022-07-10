package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

import java.util.Random;
import java.util.TreeMap;

public class FeatherBlock extends Block {

    protected static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 11, 16);

    private static final TreeMap<Float, VoxelShape> COLLISIONS = new TreeMap<>() {{
        float y = (float) COLLISION_SHAPE.max(Direction.Axis.Y);

        float i = 0.0015f;
        //adds an extra lower one for lower key access
        put(y - i, VoxelShapes.cuboid(0, 0, 0, 1, y, 1));

        while (y < 1) {
            put(y, VoxelShapes.cuboid(0, 0, 0, 1, y, 1));
            i *= 1.131;
            y += i;
        }
        put(1f, VoxelShapes.fullCube());
        put(0f, VoxelShapes.fullCube());

    }};


    public FeatherBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(World world, BlockState state, BlockPos pos, Entity entity, float height) {
        if (!world.isClient()) {
            if (height > 2) {
                //TODO: sound here
                world.playSound(null, pos, SoundEvents.WOOL_FALL, SoundSource.BLOCKS, 1F, 0.9F);
            }
        } else {
            for (int i = 0; i < Math.min(6, height * 0.8); i++) {
                Random random = world.getRandom();
                double dy = MathHelper.clamp((0.03 * height / 7f), 0.03, 0.055);
                world.addParticle(ModRegistry.FEATHER_PARTICLE.get(), entity.getX() + r(random, 0.35),
                        entity.getY(), entity.getZ() + r(random, 0.35), r(random, 0.007), dy*0.5, r(random, 0.007));
            }
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter reader, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0D, 0.4D, 1.0D));
    }


    private final VoxelShape COLLISION_CHECK_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16.1, 16);

    private boolean isColliding(Entity e, BlockPos pos) {
        if (e == null) return false;
        VoxelShape voxelshape = COLLISION_CHECK_SHAPE.move(pos.getX(), pos.getY(), pos.getZ());
        return Shapes.joinIsNotEmpty(voxelshape, Shapes.create(e.getBoundingBox()), BooleanOp.AND);
    }

    @Override
    public void onEntityCollision(BlockState state, World level, BlockPos blockPos, Entity entity) {
        if (level.isClient()) {
            if (!(entity instanceof LivingEntity) || entity.getFeetBlockState().is(this)) {


                Random random = level.getRandom();
                boolean isMoving = entity.xOld != entity.getX() || entity.zOld != entity.getZ();
                if (isMoving && random.nextInt(10) == 0) {
                    double dy = 0.005;
                    level.addParticle(ModRegistry.FEATHER_PARTICLE.get(), entity.getX() + r(random, 0.15), entity.getY(), entity.getZ() + r(random, 0.15), 0, dy, 0);
                }
            }
        }
    }

    private double r(Random random, double a) {
        return a * (random.nextFloat() + random.nextFloat() - 1);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockView blockGetter, BlockPos blockPos,  ShapeContext   ShapeContext ) {
        if ( ShapeContext  instanceof Entity ShapeContext  entity ShapeContext ) {
            Entity e = entity ShapeContext .getEntity();
            if (e instanceof LivingEntity entity) {

                float dy = (float) (entity.getY() - blockPos.getY());

                if (dy > 0) {
                    Float key = COLLISIONS.lowerKey(dy);
                    if (key != null) {
                        return COLLISIONS.getOrDefault(key, COLLISION_SHAPE);
                    }
                }
            }
        }
        return VoxelShapes.fullCube();
    }

    @Override
    protected void spawnDestroyParticles(World level, Player player, BlockPos pos, BlockState state) {
        SoundType soundtype = state.getSoundType(level, pos, null);
        level.playSound(null, pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        if (level.isClient()) {
            Random r = level.random;
            for (int i = 0; i < 10; i++) {
                level.addParticle(ModRegistry.FEATHER_PARTICLE.get(), pos.getX() + r.nextFloat(),
                        pos.getY() + r.nextFloat(), pos.getZ() + r.nextFloat(),
                        (0.5 - r.nextFloat()) * 0.02, (0.5 - r.nextFloat()) * 0.02, (0.5 - r.nextFloat()) * 0.02);
            }
        }
    }


}
