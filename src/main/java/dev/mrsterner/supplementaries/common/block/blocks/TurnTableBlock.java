package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TurnTableBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TurnTableBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public static final BooleanProperty ROTATING = BlockProperties.ROTATING;

    public TurnTableBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP)
                .with(POWER, 0).with(INVERTED, false).with(ROTATING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER, INVERTED, ROTATING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerLookDirection ().getOpposite());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (this.updatePower(state, world, pos) && world.getBlockState(pos).get(POWER) != 0) {
            this.tryRotate(world, pos);
        }
        // if power changed and is powered or facing block changed
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        Direction face = hit.getDirection();
        Direction myDir = state.get(FACING);
        if (face != myDir && face != myDir.getOpposite()) {
            if (!player.getAbilities().mayBuild) {
                return ActionResult.PASS;
            } else {
                state = state.cycle(INVERTED);
                float f = state.get(INVERTED) ? 0.55F : 0.5F;
                worldIn.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, f);
                worldIn.setBlockState(pos, state, 2 | 4);
                return ActionResult.success(worldIn.isClient());
            }
        }
        return ActionResult.PASS;
    }

    public boolean updatePower(BlockState state, World world, BlockPos pos) {
        int bestNeighborSignal = world.getReceivedRedstonePower(pos);
        int currentPower = state.get(POWER);
        // on-off
        if (bestNeighborSignal != currentPower) {
            world.setBlockState(pos, state.with(POWER, bestNeighborSignal).with(ROTATING, bestNeighborSignal!=0), 2 | 4);
            return true;
            //returns if state changed
        }
        return false;
    }

    private void tryRotate(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TurnTableBlockTile te) {
            te.tryRotate();
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        boolean powerChanged = this.updatePower(state, world, pos);
        // if power changed and is powered or facing block changed
        if (world.getBlockState(pos).get(POWER) != 0 && (powerChanged || fromPos.equals(pos.relative(state.get(FACING)))))
            this.tryRotate(world, pos);
    }

    private static Vec3 rotateY(Vec3 vec, double deg) {
        if (deg == 0)
            return vec;
        if (vec == Vec3.ZERO)
            return vec;
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        float angle = (float) ((deg / 180f) * Math.PI);
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        return new Vec3(x * c + z * s, y, z * c - x * s);
    }

    public static int getPeriod(BlockState state) {
        return (60 - state.get(POWER) * 4) + 4;
    }

    // rotate entities
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity e) {
        super.onSteppedOn(world, pos, state, e);
        if (!ServerConfigs.cached.TURN_TABLE_ROTATE_ENTITIES) return;
        if (!e.isOnGround()) return;
        if (state.get(POWER) != 0 && state.get(FACING) == Direction.UP) {
            float period = getPeriod(state) + 1;
            float ANGLE_INCREMENT = 90f / period;

            float increment = state.get(INVERTED) ? ANGLE_INCREMENT : -1 * ANGLE_INCREMENT;
            Vec3 origin = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            Vec3 oldPos = e.position();
            Vec3 oldOffset = oldPos.subtract(origin);
            Vec3 newOffset = rotateY(oldOffset, increment);
            Vec3 posDiff = origin.add(newOffset).subtract(oldPos);

            e.move(MoverType.SHULKER_BOX, posDiff);
            // e.setMotion(e.getMotion().add(adjustedposdiff));
            e.hurtMarked = true;


            //TODO: use setMotion
            if ((e instanceof LivingEntity entity)) {

                if(e instanceof ServerPlayerEntity player){
                    Advancement advancement = world.getServer().getAdvancements().getAdvancement(new ResourceLocation("supplementaries","story/turn_table"));
                    if(advancement != null){
                        player.getAdvancements().award(advancement, "unlock");
                    }
                }

                e.setOnGround(false); //remove this?
                float diff = e.getYHeadRot() - increment;
                e.setYBodyRot(diff);
                e.setYHeadRot(diff);
                entity.yHeadRotO = ((LivingEntity) e).yHeadRot;
                entity.setNoActionTime(20);
                //e.velocityChanged = true;

                if (e instanceof Cat cat && cat.isOrderedToSit() && !world.isClient()) {
                    if (world.getBlockEntity(pos) instanceof TurnTableBlockTile tile) {
                        if (tile.cat == 0) {
                            tile.cat = 20 * 20;
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, ModSounds.TOM.get(), SoundSource.BLOCKS, 0.85f, 1);
                        }
                    }
                }

            }
            // e.prevRotationYaw = e.rotationYaw;
            e.yRotO = e.getYRot();
            e.setYRot(e.getYRot() - increment);

            //e.rotateTowards(e.rotationYaw - increment, e.rotationPitch);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new TurnTableBlockTile(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.TURN_TABLE_TILE.get(), !pLevel.isClient() ? TurnTableBlockTile::tick : null);
    }

    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        if (eventID == 0) {
            if (world.isClient() && ClientConfigs.cached.TURN_TABLE_PARTICLES) {
                Direction dir = state.get(TurnTableBlock.FACING);
                BlockPos front = pos.relative(dir);

                world.addParticle(ModRegistry.ROTATION_TRAIL_EMITTER.get(),
                        front.getX() + 0.5D, front.getY() + 0.5, front.getZ() + 0.5D,
                        dir.get3DDataValue(),
                        0.71, (state.get(INVERTED) ? 1 : -1));
            }
            return true;
        }

        return super.triggerEvent(state, world, pos, eventID, eventParam);
    }
}
