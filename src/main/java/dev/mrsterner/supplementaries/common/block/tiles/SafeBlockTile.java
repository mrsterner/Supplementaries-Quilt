package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SafeBlock;
import net.mehvahdjukaar.supplementaries.common.inventories.IContainerProvider;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.UUID;

public class SafeBlockTile extends OpeneableContainerBlockEntity implements IOwnerProtected {

    public String password = null;
    public String ownerName = null;
    public UUID owner = null;

    public SafeBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.SAFE_TILE.get(), pos, state, 27);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @org.jetbrains.annotations.Nullable Direction facing) {
        //if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return LazyOptional.empty();
        return super.getCapability(capability, facing);
    }

    public boolean canPlayerOpen(Player player, boolean feedbackMessage) {
        if (player == null || player.isCreative()) return true;
        if (ServerConfigs.cached.SAFE_SIMPLE) {
            if (this.isNotOwnedBy(player)) {
                if (feedbackMessage)
                    player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.owner", this.ownerName), true);
                return false;
            }
        } else {
            return KeyLockableTile.doesPlayerHaveKeyToOpen(player, this.password, feedbackMessage, "safe");
        }
        return true;
    }

    //TODO: use vanilla system??
    //default lockable tile method. just used for compat
    @Override
    public boolean canOpen(Player pPlayer) {
        return canPlayerOpen(pPlayer, false);
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        if (this.level != null) {
            if (owner != null) {
                this.ownerName = level.getPlayerByUUID(owner).getName().getString();
            }
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void clearOwner() {
        this.ownerName = null;
        this.owner = null;
        this.password = null;
    }

    @Override
    public Component getDisplayName() {
        if (ServerConfigs.cached.SAFE_SIMPLE) {
            if (this.ownerName != null) {
                return (new TranslatableComponent("gui.supplementaries.safe.name", this.ownerName, super.getDisplayName()));
            }
        } else if (this.password != null) {
            return (new TranslatableComponent("gui.supplementaries.safe.password", this.password, super.getDisplayName()));
        }
        return super.getDisplayName();
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.safe");
    }

    @Override
    protected void playOpenSound(BlockState state) {
        Vec3i vec3i = state.get(SafeBlock.FACING).getNormal();
        double d0 = (double) this.worldPosition.getX() + 0.5D + (double) vec3i.getX() / 2.0D;
        double d1 = (double) this.worldPosition.getY() + 0.5D + (double) vec3i.getY() / 2.0D;
        double d2 = (double) this.worldPosition.getZ() + 0.5D + (double) vec3i.getZ() / 2.0D;
        this.level.playSound(null, d0, d1, d2, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
    }

    @Override
    protected void playCloseSound(BlockState state) {
        Vec3i vec3i = state.get(SafeBlock.FACING).getNormal();
        double d0 = (double) this.worldPosition.getX() + 0.5D + (double) vec3i.getX() / 2.0D;
        double d1 = (double) this.worldPosition.getY() + 0.5D + (double) vec3i.getY() / 2.0D;
        double d2 = (double) this.worldPosition.getZ() + 0.5D + (double) vec3i.getZ() / 2.0D;
        this.level.playSound(null, d0, d1, d2, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
    }

    @Override
    protected void updateBlockState(BlockState state, boolean open) {
        this.level.setBlockState(this.getBlockPos(), state.with(SafeBlock.OPEN, open), 3);
    }

    @Override
    public void load(NbtCompound tag) {
        super.load(tag);
        if (tag.contains("Owner"))
            this.owner = tag.getUUID("Owner");
        if (tag.contains("OwnerName"))
            this.ownerName = tag.getString("OwnerName");
        if (tag.contains("Password"))
            this.password = tag.getString("Password");
    }

    @Override
    public void saveAdditional(NbtCompound compound) {
        super.saveAdditional(compound);
        this.saveOwner(compound);
        if (this.ownerName != null)
            compound.putString("OwnerName", this.ownerName);
        if (this.password != null)
            compound.putString("Password", this.password);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new SafeContainerMenu(id, player, this);
    }

    private static class SafeContainerMenu extends ShulkerBoxMenu implements IContainerProvider {

        private final Container container;

        public SafeContainerMenu(int id, Inventory inventory, Container container) {
            super(id, inventory, container);
            this.container = container;
        }

        @Override
        public Container getContainer() {
            return container;
        }


    }
}
