package net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MovingBlockSource<T extends BlockEntity> implements BlockSource {

    private final Entity entity;
    private final T blockEntity;

    public MovingBlockSource(Entity entity, T internal) {
        this.entity = entity;
        this.blockEntity = internal;
    }

    @Override
    public double x() {
        return entity.getX();
    }

    @Override
    public double y() {
        return entity.getY() + 0.5;
    }

    @Override
    public double z() {
        return entity.getZ();
    }

    @Override
    public BlockPos getPos() {
        return entity.blockPosition();
    }

    @Override
    public BlockState getBlockState() {
        return blockEntity.getBlockState();
    }

    @Override
    public <A extends BlockEntity> A getEntity() {
        return (A) blockEntity;
    }

    @Override
    public ServerWorld getWorld() {
        return ((ServerLevel) entity.getWorld());
    }

    public Entity getMinecartEntity() {
        return entity;
    }
}
