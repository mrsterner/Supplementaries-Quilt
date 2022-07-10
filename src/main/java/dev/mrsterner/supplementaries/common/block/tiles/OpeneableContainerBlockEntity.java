package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.inventories.IContainerProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public abstract class OpeneableContainerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

    private final ContainerOpenersCounter openersCounter = new ContainerCounter();
    protected NonNullList<ItemStack> items;

    protected OpeneableContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int size) {
        super(blockEntityType, pos, state);
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public void load(NbtCompound tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag) && tag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
    }

    @Override
    protected void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, false);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.items = itemsIn;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getWorld(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getWorld(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getWorld(), this.getBlockPos(), this.getBlockState());
        }
    }

    protected abstract void updateBlockState(BlockState state, boolean b);

    protected abstract void playOpenSound(BlockState state);

    protected abstract void playCloseSound(BlockState state);

    public boolean isUnused(){
        return this.openersCounter.getOpenerCount() == 0;
    };

    private class ContainerCounter extends ContainerOpenersCounter {

        @Override
        protected void onOpen(World level, BlockPos pos, BlockState state) {
            OpeneableContainerBlockEntity.this.playOpenSound(state);
            OpeneableContainerBlockEntity.this.updateBlockState(state, true);
        }

        @Override
        protected void onClose(World level, BlockPos pos, BlockState state) {
            OpeneableContainerBlockEntity.this.playCloseSound(state);
            OpeneableContainerBlockEntity.this.updateBlockState(state, false);
        }

        @Override
        protected void openerCountChanged(World level, BlockPos pos, BlockState state, int i, int i1) {
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof IContainerProvider chestMenu) {
                Container container = chestMenu.getContainer();
                return container == OpeneableContainerBlockEntity.this;
            } else {
                return false;
            }
        }
    }

}
