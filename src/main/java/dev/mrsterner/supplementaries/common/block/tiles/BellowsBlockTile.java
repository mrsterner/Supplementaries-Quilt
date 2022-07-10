package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.math.MathHelperUtils;
import net.mehvahdjukaar.supplementaries.client.particles.ParticleUtil;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BellowsBlock;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.WetSpongeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

//TODO: this is a mess
public class BellowsBlockTile extends BlockEntity {

    public float height = 0;
    public float prevHeight = 0;
    private long startTime = 0;
    public boolean isPressed = false;

    //for sounds
    private boolean lastBlowing = false;

    public BellowsBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BELLOWS_TILE.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition);
    }

    private AABB getHalfBoundingBox(Direction dir) {
        return new AABB(this.worldPosition)
                .contract(-0.5 * dir.getStepX(), -0.5 * dir.getStepY(), -0.5 * dir.getStepZ());
    }

    //TODO: rewrite some of this

    private void moveCollidedEntities(World level) {
        Direction dir = this.getDirection().getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        for (int j = 0; j < 2; j++) {
            AABB halfBoundingBox = this.getHalfBoundingBox(dir);
            List<Entity> list = level.getEntities(null, halfBoundingBox);
            if (!list.isEmpty()) {
                for (Entity entity : list) {
                    if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                        AABB entityBB = entity.getBoundingBox();
                        double dy = 0.0D;
                        double dz = 0.0D;
                        float f = this.height + 0.01f;
                        switch (dir) {
                            case SOUTH -> {
                                dz = halfBoundingBox.maxZ + f - entityBB.minZ;
                                if (dz < 0) continue;
                            }
                            case NORTH -> {
                                dz = halfBoundingBox.minZ - f - entityBB.maxZ;
                                if (dz > 0) continue;
                            }
                            case UP -> {
                                dy = halfBoundingBox.maxY + f - entityBB.minY;
                                if (dy < 0) continue;
                            }
                            case DOWN -> {
                                dy = halfBoundingBox.minY - f - entityBB.maxY;
                                if (dy > 0) continue;
                            }
                        }
                        entity.move(MoverType.SHULKER_BOX, new Vec3(0, dy, dz));
                    }
                }
            }
            dir = dir.getOpposite();
        }
    }

    private void pushEntities(Direction facing, float period, float range, World level) {

        double velocity = ServerConfigs.cached.BELLOWS_BASE_VEL_SCALING / period; // Affects acceleration
        double maxVelocity = ServerConfigs.cached.BELLOWS_MAX_VEL; // Affects max speed

        AABB facingBox = CommonUtil.getDirectionBB(this.worldPosition, facing, (int) range);
        List<Entity> list = level.getEntitiesOfClass(Entity.class, facingBox);

        for (Entity entity : list) {

            if (!this.inLineOfSight(entity, facing, level)) continue;
            if (facing == Direction.UP) maxVelocity *= 0.5D;
            AABB entityBB = entity.getBoundingBox();
            double dist;
            double b;
            switch (facing) {
                default -> {
                    b = worldPosition.getZ() + 1;
                    if (entityBB.minZ < b) continue;
                    dist = entity.getZ() - b;
                }
                case NORTH -> {
                    b = worldPosition.getZ();
                    if (entityBB.maxZ > b) continue;
                    dist = b - entity.getZ();
                }
                case EAST -> {
                    b = worldPosition.getX() + 1;
                    if (entityBB.minX < b) continue;
                    dist = entity.getX() - b;
                }
                case WEST -> {
                    b = worldPosition.getX();
                    if (entityBB.maxX > b) continue;
                    dist = b - entity.getX();
                }
                case UP -> {
                    b = worldPosition.getY() + 1;
                    if (entityBB.minY < b) continue;
                    dist = entity.getY() - b;
                }
                case DOWN -> {
                    b = worldPosition.getY();
                    if (entityBB.maxY > b) continue;
                    dist = b - entity.getY();
                }
            }
            //dist, vel>0
            velocity *= (range - dist) / range;

            if (Math.abs(entity.getDeltaMovement().get(facing.getAxis())) < maxVelocity) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(facing.getStepX() * velocity, facing.getStepY() * velocity, facing.getStepZ() * velocity));
                if (ServerConfigs.cached.BELLOWS_FLAG) entity.hurtMarked = true;
            }
        }
    }

    private void blowParticles(float air, Direction facing, World level, boolean waterInFront) {
        if (level.random.nextFloat() < air) {
            AirType type = AirType.BUBBLE;
            BlockPos facingPos = this.worldPosition.relative(facing);
            BlockPos frontPos = facingPos;
            boolean hasSponge = false;
            if (!waterInFront) {
                BlockState frontState = level.getBlockState(facingPos);
                if (frontState.getBlock() instanceof WetSpongeBlock) {
                    hasSponge = true;
                    frontPos = frontPos.relative(facing);
                }
                type = AirType.AIR;
            }
            if (!Block.canSupportCenter(level, frontPos, facing.getOpposite())) {
                BlockPos p = this.worldPosition;
                if (hasSponge) {
                    EnumSet<Direction> directions = EnumSet.allOf(Direction.class);
                    directions.remove(facing.getOpposite());
                    directions.remove(facing);
                    for (Direction d : directions) {
                        if (level.getBlockState(facingPos.relative(d)).is(ModRegistry.SOAP_BLOCK.get())) {
                            type = AirType.SOAP;
                            p = facingPos;
                            break;
                        }
                    }
                    if (type != AirType.SOAP) return;
                }
                this.spawnParticle(level, p, facing, type);
            }
        }
    }

    private enum AirType {
        AIR, BUBBLE, SOAP
    }

    @SuppressWarnings("unchecked")
    private <T extends BlockEntity> void tickFurnaces(BlockPos frontPos, BlockState frontState, World level, T tile) {
        if (tile != null) {
            BlockEntityTicker<T> ticker = (BlockEntityTicker<T>) frontState.getTicker(level, tile.getType());
            if (ticker != null) {
                ticker.tick(level, frontPos, frontState, tile);
            }
        }
    }

    private void tickFurnaces(BlockPos pos, World level) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModTags.BELLOWS_TICKABLE_TAG)) {
            BlockEntity te = level.getBlockEntity(pos);
            this.tickFurnaces(pos, state, level, te);
        }
        //maybe lower chance
        else if (state instanceof ChangeOverTimeBlock && level instanceof ServerWorld serverLevel) {
            state.randomTick(serverLevel, pos, level.random);
        }
    }

    private void refreshFire(int n, Direction facing, BlockPos frontPos, World level) {
        for (int i = 0; i < n; i++) {
            BlockState fb = level.getBlockState(frontPos);
            if (fb.getBlock() instanceof FireBlock) {
                int age = fb.get(FireBlock.AGE);
                if (age != 0) {
                    level.setBlockState(frontPos, fb.with(FireBlock.AGE,
                            MathHelper.clamp(age - 7, 0, 15)), 4);
                }
            }
            frontPos = frontPos.relative(facing);
        }
    }

    private float getPeriodForPower(int power) {
        return ((float) ServerConfigs.cached.BELLOWS_BASE_PERIOD) - (power - 1) * ((float) ServerConfigs.cached.BELLOWS_POWER_SCALING);
    }

    //TODO: optimize this (also for flywheel)
    public static void tick(World level, BlockPos pos, BlockState state, BellowsBlockTile tile) {

        int power = state.get(BellowsBlock.POWER);
        tile.prevHeight = tile.height;

        if (power != 0 && !(tile.startTime == 0 && tile.height != 0)) {
            long time = level.getGameTime();
            if (tile.startTime == 0) {
                tile.startTime = time;
            }

            float period = tile.getPeriodForPower(power);

            //slope of animation. for particles and pushing entities
            float arg = (float) Math.PI * 2 * (((time - tile.startTime) / period) % 1);
            float sin = MathHelper.sin(arg);
            float cos = MathHelper.cos(arg);
            final float dh = 1 / 16f;//0.09375f;
            tile.height = dh * cos - dh;

            tile.pushAir(level, pos, state, power, time, period, sin);

            //sound
            boolean blowing = MathHelper.sin(arg - 0.8f) > 0;
            if (tile.lastBlowing != blowing) {
                level.playSound(null, pos,
                        blowing ? ModSounds.BELLOWS_BLOW.get() : ModSounds.BELLOWS_RETRACT.get(),
                        SoundSource.BLOCKS, 0.1f,
                        MathHelperUtils.nextWeighted(level.random, 0.1f) + 0.85f + 0.6f * power / 15f);
            }

            tile.lastBlowing = blowing;

        } else if (tile.isPressed) {
            float minH = -2 / 16f;
            tile.height = Math.max(tile.height - 0.01f, minH);

            if (tile.height > minH) {
                long time = level.getGameTime();
                //when operated by a mob it behaves like a constant with 7 power
                int p = 7;
                float period = tile.getPeriodForPower(p);


                tile.pushAir(level, pos, state, power, time, period, 0.8f);
            }
        }
        //resets counter when powered off
        else {
            tile.startTime = 0;
            if (tile.height < 0) {
                tile.height = Math.min(tile.height + 0.01f, 0);
            }
        }
        if (tile.prevHeight != 0 && tile.height != 0) {
            tile.moveCollidedEntities(level);
        }
        tile.isPressed = false;
    }

    private void pushAir(World level, BlockPos pos, BlockState state, int power, long time, float period, float airIntensity) {
        Direction facing = state.get(BellowsBlock.FACING);
        BlockPos frontPos = pos.relative(facing);
        //TODO: optimize and add campfire smoke
        FluidState fluid = level.getFluidState(frontPos);

        //client. particles
        if (level.isClient()) {
            this.blowParticles(airIntensity, facing, level, fluid.getType().is(FluidTags.WATER));

        }
        //server
        else if (fluid.isEmpty()) {
            float range = ServerConfigs.cached.BELLOWS_RANGE;
            //push entities (only if pushing air)
            if (airIntensity > 0) {
                this.pushEntities(facing, period, range, level);
            }

            //speeds up furnaces
            if (time % (10 - (power / 2)) == 0) {
                this.tickFurnaces(frontPos, level);
            }

            //refresh fire blocks
            //refreshTextures more frequently block closed to it
            //fire updates (previous random tick) at a minimum of 30 ticks
            int n = 0;
            for (int a = 0; a <= range; a++) {
                if (time % (15L * (a + 1)) != 0) {
                    n = a;
                    break;
                }
            }
            //only first 4 block will ultimately be kept active. this could change with random ticks if unlucky
            this.refreshFire(n, facing, frontPos, level);
        }
    }

    public boolean inLineOfSight(Entity entity, Direction facing, World level) {
        int x = facing.getStepX() * (MathHelper.floor(entity.getX()) - this.worldPosition.getX());
        int y = facing.getStepY() * (MathHelper.floor(entity.getY()) - this.worldPosition.getY());
        int z = facing.getStepZ() * (MathHelper.floor(entity.getZ()) - this.worldPosition.getZ());
        boolean flag = true;

        for (int i = 1; i < Math.abs(x + y + z); i++) {

            if (Block.canSupportCenter(level, this.worldPosition.relative(facing, i), facing.getOpposite())) {
                flag = false;
            }
        }
        return flag;
    }

    public void spawnParticle(World world, BlockPos pos, Direction dir, AirType airType) {
        if (airType == AirType.SOAP) {
            for (int m = 0; m < (1 + world.random.nextInt(3)); m++) {
                ParticleUtil.spawnParticleOnFace(world, pos, dir, ModRegistry.SUDS_PARTICLE.get(), 0.3f, 0.5f, true);
            }

        } else {
            double xo = dir.getStepX();
            double yo = dir.getStepY();
            double zo = dir.getStepZ();
            double x = xo * 0.5 + pos.getX() + 0.5 + (world.random.nextFloat() - 0.5) / 3d;
            double y = yo * 0.5 + pos.getY() + 0.5 + (world.random.nextFloat() - 0.5) / 3d;
            double z = zo * 0.5 + pos.getZ() + 0.5 + (world.random.nextFloat() - 0.5) / 3d;

            double vel = 0.125F + world.random.nextFloat() * 0.2F;

            double velX = xo * vel;
            double velY = yo * vel;
            double velZ = zo * vel;

            if (airType == AirType.BUBBLE) {
                world.addParticle(ParticleTypes.BUBBLE, x, y, z, velX * 0.8, velY * 0.8, velZ * 0.8);
            } else {
                world.addParticle(ParticleTypes.SMOKE, x, y, z, velX, velY, velZ);
            }
        }

    }

    public Direction getDirection() {
        return this.getBlockState().get(BellowsBlock.FACING);
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        this.startTime = compound.getLong("Offset");
    }

    @Override
    public void saveAdditional(NbtCompound compound) {
        super.saveAdditional(compound);
        compound.putLong("Offset", this.startTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void onSteppedOn(Entity entityIn) {
        if (this.isPressed) return;
        double b = entityIn.getBoundingBox().getSize();
        if (b > 0.8 && this.getBlockState().get(BellowsBlock.FACING).getAxis() != Direction.Axis.Y) {
            this.isPressed = true;
        }
    }
}
