package dev.mrsterner.supplementaries.common.block.blocks;

import dev.mrsterner.supplementaries.api.moonlightlib.WaterloggableBlock;
import net.mehvahdjukaar.supplementaries.api.ISoapWashable;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.minecraft.state.property.Properties.*;

public class BambooSpikesBlock extends WaterloggableBlock implements ISoftFluidConsumer, IForgeBlock, BlockEntityProvider, ISoapWashable {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 13.0D, 16.0D);
    protected static final VoxelShape SHAPE_UP = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    protected static final VoxelShape SHAPE_DOWN = Block.createCuboidShape(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty TIPPED = BlockProperties.TIPPED;

    public BambooSpikesBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(TIPPED, false));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    //this could be improved
    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BambooSpikesBlockTile tile) {
            NbtCompound com = stack.getNbt();
            if (com != null) {
                Potion p = PotionUtil.getPotion(stack);
                if (p != Potions.EMPTY && com.contains("Damage")) {
                    tile.potion = p;
                    tile.setMissingCharges(com.getInt("Damage"));
                }
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
		NbtCompound com = context.getStack().getNbt();
        int charges = com != null ? context.getStack().getMaxDamage() - com.getInt("Damage") : 0;
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        return this.getDefaultState().with(FACING, context.getPlayerLookDirection()).with(WATERLOGGED, flag)
                .with(TIPPED, charges != 0 && PotionUtil.getPotion(com) != Potions.EMPTY);
    }

    public ItemStack getSpikeItem(BlockEntity te) {
        if (te instanceof BambooSpikesBlockTile) {
            return ((BambooSpikesBlockTile) te).getSpikeItem();
        }
        return new ItemStack(ModRegistry.BAMBOO_SPIKES_ITEM.get());
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        List<ItemStack> list = new ArrayList<>();
        list.add(this.getSpikeItem(builder.getNullable(LootContextParameters.BLOCK_ENTITY)));
        return list;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext  context) {
        return switch (state.get(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
        };
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    //TODO: fix pathfinding

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).isCreative()) return;
        if (entityIn instanceof LivingEntity && entityIn.isAlive()) {
            boolean up = state.get(FACING) == Direction.UP;
            double vy = up ? 0.45 : 0.95;
            entityIn.slowMovement(state, new Vec3d(0.95D, vy, 0.95D));
            if (!worldIn.isClient()) {
                if (up && entityIn instanceof PlayerEntity && entityIn.isSneaking()) return;
                float damage = entityIn.getY() > (pos.getY() + 0.0625) ? 3 : 1.5f;
                entityIn.damage(CommonUtil.SPIKE_DAMAGE, damage);
                if (state.get(TIPPED)) {
                    BlockEntity te = worldIn.getBlockEntity(pos);
                    if (te instanceof BambooSpikesBlockTile) {
                        if (((BambooSpikesBlockTile) te).interactWithEntity(((LivingEntity) entityIn), worldIn)) {
                            worldIn.setBlockState(pos, state.with(BambooSpikesBlock.TIPPED, false), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public PathNodeType getAiPathNodeType(BlockState state, BlockView world, BlockPos pos, MobEntity entity) {
        return PathNodeType.DAMAGE_OTHER;
    }

    public static boolean tryAddingPotion(BlockState state, WorldAccess world, BlockPos pos, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BambooSpikesBlockTile bambooSpikesBlockTile) {
            if (bambooSpikesBlockTile.tryApplyPotion(PotionUtil.getPotion(stack))) {
                world.playSound(null, pos, SoundEvents.BLOCK_HONEY_BLOCK_FALL, SoundCategory.BLOCKS, 0.5F, 1.5F);
                world.setBlockState(pos, state.with(TIPPED, true), 3);
                return true;
            }
        }
        return false;
    }

	@Override
    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
        if (!tippedEnabled.get()) return ActionResult.PASS;
        ItemStack stack = player.getStackInHand(handIn);

        if (stack.getItem() instanceof LingeringPotionItem) {
            if (tryAddingPotion(state, worldIn, pos, stack)) {
                if (!player.isCreative())
                    player.setStackInHand(handIn, ItemUsage.exchangeStack(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
            }
            return ActionResult.success(worldIn.isClient());
        }
        return ActionResult.PASS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, TIPPED);
    }

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return this.getSpikeItem(world.getBlockEntity(pos));
	}

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return pState.get(TIPPED) ? new BambooSpikesBlockTile(pPos, pState) : null;
    }

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, RandomGenerator random) {
		if (0.01 > random.nextFloat() && state.get(TIPPED)) {
			if (world.getBlockEntity(pos) instanceof BambooSpikesBlockTile tile) {
				tile.makeParticle(world);
			}
		}
	}


    public Lazy<Boolean> tippedEnabled = Lazy.of(() -> RegistryConfigs.Reg.TIPPED_SPIKES_ENABLED.get());

    @Override
    public boolean tryAcceptingFluid(World world, BlockState state, BlockPos pos, SoftFluid f, @Nullable NbtCompound nbt, int amount) {
        if (!tippedEnabled.get()) return false;
        if (f == SoftFluidRegistry.POTION.get() && nbt != null && !state.get(TIPPED) && nbt.getString("PotionType").equals("Lingering")) {
            if (world.getBlockEntity(pos) instanceof BambooSpikesBlockTile te) {
                if (te.tryApplyPotion(PotionUtil.getPotion(nbt))) {
                    world.playSound(null, pos, SoundEvents.BLOCK_HONEY_BLOCK_FALL, SoundCategory.BLOCKS, 0.5F, 1.5F);
                    world.setBlockState(pos, state.with(TIPPED, true), 3);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean tryWash(World World, BlockPos pos, BlockState state) {
        if (state.get(TIPPED)) {
            if (!World.isClient) {
                var te = World.getBlockEntity(pos);
                if (te != null) te.setRemoved();
                World.setBlockState(pos, state.with(TIPPED, false), 3);
            }
            return true;
        }
        return false;
    }
}
