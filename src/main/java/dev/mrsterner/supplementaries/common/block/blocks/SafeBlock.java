package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.ILavaAndWaterLoggable;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SafeBlock extends Block implements ILavaAndWaterLoggable, EntityBlock {
    public static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LAVALOGGED = BlockProperties.LAVALOGGED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public SafeBlock(Properties properties) {
        super(properties.lightLevel(state->state.get(LAVALOGGED) ? 15 : 0));
        this.setDefaultState(this.stateManager.getDefaultState().with(OPEN, false)
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(LAVALOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING, WATERLOGGED, LAVALOGGED);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            tile.recheckOpen();
        }
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(LAVALOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(worldIn));
        } else if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        Fluid fluid = fluidState.getType();
        boolean full = fluidState.getAmount() == 8;
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite())
                .with(WATERLOGGED, full && fluid == Fluids.WATER)
                .with(LAVALOGGED, full && fluid == Fluids.LAVA);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new SafeBlockTile(pPos, pState);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (worldIn.isClient()) {
            return ActionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
                ItemStack stack = player.getItemInHand(handIn);
                Item item = stack.getItem();

                //clear ownership with tripwire
                boolean cleared = false;
                if (ServerConfigs.cached.SAFE_SIMPLE) {
                    if ((item == Items.TRIPWIRE_HOOK || stack.is(ModTags.KEY)) &&
                            (tile.isOwnedBy(player) || (tile.isNotOwnedBy(player) && player.isCreative()))) {
                        cleared = true;
                    }
                } else {
                    if (player.isShiftKeyDown() && stack.is(ModTags.KEY) && (player.isCreative() ||
                            KeyLockableTile.isCorrectKey(stack, tile.password))) {
                        cleared = true;
                    }
                }

                if (cleared) {
                    tile.clearOwner();
                    player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.cleared"), true);
                    worldIn.playSound(null, pos,
                            SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
                    return ActionResult.CONSUME;
                }

                BlockPos p = pos.relative(state.get(FACING));
                if (!worldIn.getBlockState(p).isRedstoneConductor(worldIn, p)) {
                    if (ServerConfigs.cached.SAFE_SIMPLE) {
                        UUID owner = tile.owner;
                        if (owner == null) {
                            owner = player.getUUID();
                            tile.setOwner(owner);
                        }
                        if (!owner.equals(player.getUUID())) {
                            player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.owner", tile.ownerName), true);
                            if (!player.isCreative()) return ActionResult.CONSUME;
                        }
                    } else {
                        String key = tile.password;
                        if (key == null) {
                            if (stack.is(ModTags.KEY)) {
                                tile.password = stack.getHoverName().getString();
                                player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.assigned_key", tile.password), true);
                                worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
                                return ActionResult.CONSUME;
                            }
                        } else if (!tile.canPlayerOpen(player, true) && !player.isCreative()) {
                            return ActionResult.CONSUME;
                        }
                    }
                    player.openMenu(tile);
                    PiglinAi.angerNearbyPiglins(player, true);
                }

                return ActionResult.CONSUME;
            }
            return ActionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockView worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        NbtCompound compoundTag = stack.getTagElement("BlockEntityTag");
        if (compoundTag != null) {
            if (ServerConfigs.cached.SAFE_SIMPLE) {
                if (compoundTag.contains("Owner")) {
                    UUID id = compoundTag.getUUID("Owner");
                    if (!id.equals(Minecraft.getInstance().player.getUUID())) {
                        String name = compoundTag.getString("OwnerName");
                        tooltip.add((new TranslatableComponent("container.supplementaries.safe.owner", name)).withStyle(ChatFormatting.GRAY));
                        return;
                    }
                }
                if (compoundTag.contains("LootTable", 8)) {
                    tooltip.add(new TextComponent("???????").withStyle(ChatFormatting.GRAY));
                }
                if (compoundTag.contains("Items", 9)) {
                    NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(compoundTag, itemStacks);
                    int i = 0;
                    int j = 0;

                    for (ItemStack itemstack : itemStacks) {
                        if (!itemstack.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                MutableComponent component = itemstack.getHoverName().copy();
                                component.append(" x").append(String.valueOf(itemstack.getCount()));
                                tooltip.add(component.withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }

                    if (j - i > 0) {
                        tooltip.add((new TranslatableComponent("container.shulkerBox.more", j - i)).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                    }
                }
                return;
            } else {
                if (compoundTag.contains("Password")) {
                    tooltip.add((new TranslatableComponent("message.supplementaries.safe.bound")).withStyle(ChatFormatting.GRAY));
                    return;
                }
            }
        }
        tooltip.add((new TranslatableComponent("message.supplementaries.safe.unbound")).withStyle(ChatFormatting.GRAY));

    }

    public ItemStack getSafeItem(SafeBlockTile te) {
        NbtCompound compoundTag = new NbtCompound();
        te.saveAdditional(compoundTag);
        ItemStack itemstack = new ItemStack(this);
        if (!compoundTag.isEmpty()) {
            itemstack.setSubNbt("BlockEntityTag", compoundTag);
        }

        if (te.hasCustomName()) {
            itemstack.setHoverName(te.getCustomName());
        }
        return itemstack;
    }

    //break protection
    @Override
    public boolean onDestroyedByPlayer(BlockState state, World world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (ServerConfigs.cached.SAFE_UNBREAKABLE) {
            if (world.getBlockEntity(pos) instanceof SafeBlockTile tile) {
                if (!tile.canPlayerOpen(player, true)) return false;
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            if (!worldIn.isClient() && player.isCreative() && !tile.isEmpty()) {
                ItemStack itemstack = this.getSafeItem(tile);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //TODO: use loot table instead
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SafeBlockTile tile) {
            ItemStack itemstack = this.getSafeItem(tile);
            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        ItemStack itemstack = super.getPickStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            return getSafeItem(tile);
        }
        return itemstack;
    }


    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            if (placer instanceof Player) {
                if (tile.owner == null)
                    tile.setOwner(placer.getUUID());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
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
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(LAVALOGGED)) return Fluids.LAVA.getSource(false);
        else if (state.get(WATERLOGGED)) return Fluids.WATER.getSource(false);
        return super.getFluidState(state);
    }

}
