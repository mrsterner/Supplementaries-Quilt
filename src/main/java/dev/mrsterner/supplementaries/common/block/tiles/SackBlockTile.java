package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SafeBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.SackContainerMenu;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class SackBlockTile extends OpeneableContainerBlockEntity {

    public SackBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SACK_TILE.get(), pos, state, 27);
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.sack");
    }

    @Override
    protected void playOpenSound(BlockState state) {
        double d0 = (double) this.worldPosition.getX() + 0.5D;
        double d1 = (double) this.worldPosition.getY() + 1;
        double d2 = (double) this.worldPosition.getZ() + 0.5D;

        this.level.playSound(null, d0, d1, d2, ModSounds.SACK_OPEN.get(), SoundSource.BLOCKS, 1,
                this.level.random.nextFloat() * 0.1F + 0.95F);
    }

    @Override
    protected void playCloseSound(BlockState state) {
        double d0 = (double) this.worldPosition.getX() + 0.5D;
        double d1 = (double) this.worldPosition.getY() + 1;
        double d2 = (double) this.worldPosition.getZ() + 0.5D;
        this.level.playSound(null, d0, d1, d2, ModSounds.SACK_OPEN.get(), SoundSource.BLOCKS, 1,
                this.level.random.nextFloat() * 0.1F + 0.8F);
    }

    @Override
    protected void updateBlockState(BlockState state, boolean open) {
        this.level.setBlockState(this.getBlockPos(), state.with(SafeBlock.OPEN, open), 3);
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
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, false);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new SackContainerMenu(id, player, this);
    }

    public int getUnlockedSlots() {
        return ServerConfigs.cached.SACK_SLOTS;
    }

    public boolean isSlotUnlocked(int ind) {
        return ind < this.getUnlockedSlots();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isSlotUnlocked(index) && CommonUtil.isAllowedInShulker(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return isSlotUnlocked(index);
    }

    //@Override
    public boolean acceptsTransfer(Player player) {
        return true;
    }
}
