package dev.mrsterner.supplementaries.common.block.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.core.jmx.Server;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class AshLayerBlock extends FallingBlock {
	private static final int MAX_LAYERS = 8;
	public static final IntProperty LAYERS = Properties.LAYERS;
	protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[MAX_LAYERS + 1];


	public AshLayerBlock(Settings properties) {
		super(properties);
		this.setDefaultState(this.getStateManager().getDefaultState().with(LAYERS, 1));
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult pHit, ProjectileEntity projectile) {
		BlockPos pos = pHit.getBlockPos();
		if (projectile instanceof PotionEntity potion && PotionUtil.getPotion(potion.getStack()) == Potions.WATER) {
			Entity entity = projectile.getOwner();
			boolean flag = entity == null || entity instanceof PlayerEntity || ForgeEventFactory.getMobGriefingEvent(world, entity);
			if (flag) {
				this.removeOneLayer(state, pos, world);
			}
		}
	}

	@Override
	public int getColor(BlockState state, BlockView reader, BlockPos pos) {
		return 0x9a9090;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() != oldState.getBlock())
			worldIn.scheduleBlockTick(pos, this, this.getFallDelay());
	}

	@Override
	public VoxelShape getOutlineShape(BlockState pState, BlockView pLevel, BlockPos pPos,  ShapeContext  pContext) {
		return SHAPE_BY_LAYER[pState.get(LAYERS)];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockView pLevel, BlockPos pPos,  ShapeContext pContext) {
		if (pContext instanceof EntityShapeContext c) {
			var e = c.getEntity();
			if (e instanceof LivingEntity) {
				return SHAPE_BY_LAYER[pState.get(LAYERS) - 1];
			}
		}
		return this.getShape(pState, pLevel, pPos, pContext);
	}

	@Override
	public VoxelShape getSidesShape(BlockState pState, BlockView pReader, BlockPos pPos) {
		return SHAPE_BY_LAYER[pState.get(LAYERS)];
	}

	@Override
	public VoxelShape getCameraCollisionShape(BlockState pState, BlockView pReader, BlockPos pPos,  ShapeContext  pContext) {
		return SHAPE_BY_LAYER[pState.get(LAYERS)];
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView blockGetter, BlockPos pos, NavigationType pathType) {
		if (pathType == NavigationType.LAND) {
			return state.get(LAYERS) <= MAX_LAYERS / 2;
		}
		return false;
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	//ugly but works
	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState facingState, WorldAccess world, BlockPos currentPos, BlockPos otherPos) {
		if (world instanceof ServerWorld serverLevel) {
			BlockPos pos = currentPos.up();
			BlockState state1 = world.getBlockState(pos);
			;
			while (state1.isOf(this)) {
				serverLevel.scheduleBlockTick(pos, this, this.getFallDelay());
				pos = pos.up();
				state1 = serverLevel.getBlockState(pos);
			}
		}
		return super.getStateForNeighborUpdate(state, direction, facingState, world, currentPos, otherPos);
	}

	@Override
	public void tick(BlockState state, ServerWorld level, BlockPos pos, Random pRand) {
		BlockState below = level.getBlockState(pos.down());
		if ((FallingAshEntity.isFree(below) || hasIncompleteAshPileBelow(below)) && pos.getY() >= level.getBottomY()) {

			while (state.isOf(this)) {
				FallingBlockEntity fallingblockentity = FallingAshEntity.fall(level, pos, state);
				this.configureFallingBlockEntity(fallingblockentity);

				pos = pos.up();
				state = level.getBlockState(pos);
			}
		}
	}

	private boolean hasIncompleteAshPileBelow(BlockState state) {
		return state.isOf(this) && state.get(LAYERS) != MAX_LAYERS;
	}

	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockState blockstate = context.getWorld().getBlockState(context.getBlockPos());
		if (blockstate.isOf(this)) {
			int i = blockstate.get(LAYERS);
			return blockstate.with(LAYERS, Math.min(MAX_LAYERS, i + 1));
		} else {
			return super.getPlacementState(context);
		}
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(LAYERS);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
		if (ServerConfigs.cached.ASH_RAIN) {
			if (world.hasRaidAt(pos.up()) && world.random.nextInt(4) == 0) {
				this.removeOneLayer(state, pos, world);
			}
		}
	}


	@Override
	public void precipitationTick(BlockState pState, World level, BlockPos pPos, Biome.Precipitation pPrecipitation) {
		super.precipitationTick(pState, level, pPos, pPrecipitation);
		if (ServerConfigs.cached.ASH_RAIN) {
			if (level.random.nextInt(2) == 0) {
				this.removeOneLayer(pState, pPos, level);
			}
		}
	}

	private void removeOneLayer(BlockState state, BlockPos pos, World level) {
		int levels = state.get(LAYERS);
		if (levels > 1) level.setBlockStateState(pos, state.with(LAYERS, levels - 1));
		else level.removeBlock(pos, false);
	}

	@Override
	public boolean canReplace(BlockState pState, ItemPlacementContext pUseContext) {
		int i = pState.get(LAYERS);
		if (pUseContext.getStack().isOf(this.asItem()) && i < MAX_LAYERS) {
			return true;
		} else {
			return i == 1;
		}
	}

	public static boolean tryConvertToAsh(World level, BlockPos pPos, BlockState state) {
		if (ServerConfigs.cached.ASH_BURN) {

			Item i = state.getBlock().asItem();
			int count = ForgeHooks.getBurnTime(i.getDefaultStack(), null) / 100;
			if (i.builtInRegistryHolder().is(ItemTags.LOGS_THAT_BURN)) count += 2;

			if (count > 0) {
				int layers = MathHelper.clamp(level.random.nextInt(count), 1, 8);
				if (layers != 0) {
					((ServerWorld) level).addParticle(ModRegistry.ASH_PARTICLE.get(), (double) pPos.getX() + 0.5D,
							(double) pPos.getY() + 0.5D, (double) pPos.getZ() + 0.5D, 10 + layers,
							0.5D, 0.5D, 0.5D, 0.0D);
					return level.setBlockStateState(pPos, ModRegistry.ASH_BLOCK.get()
							.getDefaultState ().with(AshLayerBlock.LAYERS, layers), 3);
				}
			}
		}
		return false;
	}

	private void addParticle(Entity entity, BlockPos pos, World level, int layers, float upSpeed) {
		level.addParticle(ModRegistry.ASH_PARTICLE.get(), entity.getX(), pos.getY() + layers * (1 / 8f), entity.getZ(),
				MathHelper.nextBetween(level.random, -1.0F, 1.0F) * 0.083333336F,
				upSpeed,
				MathHelper.nextBetween(level.random, -1.0F, 1.0F) * 0.083333336F);
	}

	@Override
	public void onEntityCollision(BlockState state, World level, BlockPos pos, Entity entity) {
		if (level.isClient()) {
			if (!(entity instanceof LivingEntity) || entity.getBlockStateAtPos().isOf(this)) {

				boolean bl = entity.prevX != entity.getX() || entity.prevZ != entity.getZ();
				if (bl && level.random.nextInt(2) == 0) {
					addParticle(entity, pos, level, state.get(LAYERS), 0.05f);
				}
			}
		}
		super.onEntityCollision(state, level, pos, entity);
	}

	@Override
	public void onLandedUpon(World level, BlockState state, BlockPos pos, Entity entity, float height) {
		int layers = state.get(LAYERS);
		entity.handleFallDamage(height, layers > 2 ? 0.3f : 1, DamageSource.FALL);
		if (level.isClient()) {
			for (int i = 0; i < Math.min(12, height * 1.4); i++) {

				addParticle(entity, pos, level, layers, 0.12f);
			}
		}
	}

	//TODO: bonemeal thing
	public static boolean applyBonemeal(ItemStack stack, World level, BlockPos pos, PlayerEntity player) {
		BlockState blockstate = level.getBlockState(pos);
		if (blockstate.getBlock() instanceof Fertilizable bonemealableblock) {
			if (bonemealableblock.isFertilizable(level, pos, blockstate, level.isClient())) {

				if (level instanceof ServerWorld) {
					if (bonemealableblock.canGrow(level, level.random, pos, blockstate)) {
						bonemealableblock.grow((ServerWorld) level, level.random, pos, blockstate);
					}

					stack.decrement(1);
				}

				return true;
			}
		}

		return false;
	}

	//TODO: add this
	public static final int GRASS_SPREAD_WIDTH = 3;
    /*
    public void performBonemeal(ServerWorld level, Random random, BlockPos pos, BlockState state) {
        BlockPos blockpos = pos.above();
        BlockState blockstate = Blocks.GRASS.getDefaultState ();
        label46:
        for(int i = 0; i < 128; ++i) {
            BlockPos pos1 = blockpos;
            for(int j = 0; j < i / 16; ++j) {
                pos1 = pos1.offset(random.nextInt(GRASS_SPREAD_WIDTH) - 1,
                        (random.nextInt(GRASS_SPREAD_WIDTH) - 1) * random.nextInt(3) / 2,
                        random.nextInt(GRASS_SPREAD_WIDTH) - 1);
                if (!level.getBlockState(pos1.below()).is(this) ||
                        level.getBlockState(pos1).isCollisionShapeFullBlock(level, pos1)) {
                    continue label46;
                }
            }
            BlockState state1 = level.getBlockState(pos1);
            //if (state1.is(blockstate.getBlock()) && random.nextInt(10) == 0) {
            //    ((BonemealableBlock)blockstate.getBlock()).performBonemeal(level, random, pos1, state1);
            //}
            if (state1.isAir()) {
                PlacedFeature placedfeature;
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> list = level.getBiome(pos1).getGenerationSettings().getFlowerFeatures();
                    if (list.isEmpty()) {
                        continue;
                    }
                    placedfeature = ((RandomPatchConfiguration)list.get(0).config()).feature().get();
                } else {
                    placedfeature = VegetationPlacements.GRASS_BONEMEAL;
                }
                placedfeature.place(level, level.getChunkSource().getGenerator(), random, pos1);
            }
        }
    }
    */

	static {
		Arrays.setAll(SHAPE_BY_LAYER, l -> Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, l * 2, 16.0D));
	}

}
