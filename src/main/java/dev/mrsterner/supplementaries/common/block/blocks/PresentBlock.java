package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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

import java.util.Collections;
import java.util.List;

public class PresentBlock extends WaterBlock implements EntityBlock, IColored {

    public static final BooleanProperty PACKED = BlockProperties.PACKED;

    private final DyeColor color;

    public static final VoxelShape SHAPE_LID = Block.createCuboidShape(1, 10, 1, 15, 14, 15);
    public static final VoxelShape SHAPE_OPEN = Block.createCuboidShape(2, 0, 2, 14, 10, 14);
    public static final VoxelShape SHAPE_CLOSED = Shapes.or(SHAPE_OPEN, SHAPE_LID);

    public PresentBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.setDefaultState(this.stateManager.getDefaultState().with(PACKED, false).with(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new PresentBlockTile(pPos, pState);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PACKED, WATERLOGGED);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (worldIn.isClient()) {
            return ActionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof PresentBlockTile tile) {
                return tile.interact((ServerPlayerEntity) player, pos);
            }
        }
        return ActionResult.PASS;
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof PresentBlockTile tile) {
            if (!worldIn.isClient() && player.isCreative() && !tile.isEmpty()) {
                ItemStack itemstack = tile.getPresentItem(this);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //normal drop
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PresentBlockTile tile) {
            ItemStack itemstack = tile.getPresentItem(this);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        ItemStack itemstack = super.getPickStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof PresentBlockTile tile) {
            return tile.getPresentItem(this);
        }
        return itemstack;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof PresentBlockTile tile) {
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
        if (state.get(PACKED))
            return SHAPE_CLOSED;
        return SHAPE_OPEN;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state =  super.getPlacementState(context);
        NbtCompound tag = context.getItemInHand().getTag();
        if(tag != null && tag.contains("BlockEntityTag")){
            NbtCompound t = tag.getCompound("BlockEntityTag");
            if(t.contains("Items")) state = state.with(PACKED, true);
        }
        return state;
    }
}
