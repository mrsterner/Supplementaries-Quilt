package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.entities.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class SackBlock extends FallingBlock implements EntityBlock {

    public static final VoxelShape SHAPE_CLOSED = Shapes.or(Block.createCuboidShape(2, 0, 2, 14, 12, 14),
            Block.createCuboidShape(6, 12, 6, 10, 13, 10), Block.createCuboidShape(5, 13, 5, 11, 16, 11));
    public static final VoxelShape SHAPE_OPEN = Shapes.or(Block.createCuboidShape(2, 0, 2, 14, 12, 14),
            Block.createCuboidShape(6, 12, 6, 10, 13, 10), Block.createCuboidShape(3, 13, 3, 13, 14, 13));


    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public SackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(OPEN, false).with(WATERLOGGED, false));
    }

    @Override
    public int getDustColor(BlockState state, BlockView reader, BlockPos pos) {
        return 0xba8f6a;
    }

    //falling block
    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getBlock() != oldState.getBlock()) {
            worldIn.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        return this.getDefaultState ().with(WATERLOGGED, flag);
    }

    //@Override
    //protected void onStartFalling(FallingBlockEntity fallingEntity) { fallingEntity.setHurtEntities(true); }

    public static boolean canFall(BlockPos pos, WorldAccess world) {
        return (world.isEmptyBlock(pos.below()) || isFree(world.getBlockState(pos.below()))) &&
                pos.getY() >= world.getMinBuildHeight() && !RopeBlock.isSupportingCeiling(pos.above(), world);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerWorld level, BlockPos pos, Random rand) {
        if (level.getBlockEntity(pos) instanceof SackBlockTile tile) {
            tile.recheckOpen();
            if (canFall(pos, level)) {
                ImprovedFallingBlockEntity entity = ImprovedFallingBlockEntity.fall(ModRegistry.FALLING_SACK.get(),
                        level, pos, state, true);
                entity.blockData = tile.saveWithoutMetadata();
                entity.setHurtsEntities(1, 20);
            }
        }
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new SackBlockTile(pPos, pState);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (worldIn.isClient()) {
            return ActionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {

                player.openMenu(tile);
                PiglinAi.angerNearbyPiglins(player, true);

                return ActionResult.CONSUME;
            } else {
                return ActionResult.PASS;
            }
        }
    }

    //for creative drop
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
            if (!worldIn.isClient() && player.isCreative() && !tile.isEmpty()) {
                NbtCompound compoundTag = new NbtCompound();
                tile.saveAdditional(compoundTag);
                ItemStack itemstack = new ItemStack(this);
                if (!compoundTag.isEmpty()) {
                    itemstack.setSubNbt("BlockEntityTag", compoundTag);
                }

                if (tile.hasCustomName()) {
                    itemstack.setHoverName(tile.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SackBlockTile tile) {
            builder = builder.withDynamicDrop(CONTENTS, (context, stackConsumer) -> {
                for (int i = 0; i < tile.getContainerSize(); ++i) {
                    stackConsumer.accept(tile.getItem(i));
                }
            });
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        ItemStack itemstack = super.getPickStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof SackBlockTile tile) {
            NbtCompound compoundTag = new NbtCompound();
            tile.saveAdditional(compoundTag);
            if (!compoundTag.isEmpty()) {
                itemstack.setSubNbt("BlockEntityTag", compoundTag);
            }
        }
        return itemstack;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        if (state.get(OPEN))
            return SHAPE_OPEN;
        return SHAPE_CLOSED;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
            int i = 0;
            float f = 0.0F;
            int slots = tile.getUnlockedSlots();
            for (int j = 0; j < slots; ++j) {
                ItemStack itemstack = tile.getItem(j);
                if (!itemstack.isEmpty()) {
                    f += (float) itemstack.getCount() / (float) Math.min(tile.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }
            f = f / (float) slots;
            return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Override
    public void onLand(World level, BlockPos pos, BlockState state, BlockState state1, FallingBlockEntity blockEntity) {
        super.onLand(level, pos, state, state1, blockEntity);
        //land sound
        if (!blockEntity.isSilent()) {
            level.playSound(null, pos, state.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

}
