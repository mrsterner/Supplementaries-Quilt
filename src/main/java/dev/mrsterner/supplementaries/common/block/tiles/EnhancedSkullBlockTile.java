package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Hand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class EnhancedSkullBlockTile extends BlockEntity {

    @Nullable
    protected SkullBlockEntity innerTile = null;

    public EnhancedSkullBlockTile(BlockEntityType type, BlockPos pWorldPosition, BlockState pBlockState) {
        super(type, pWorldPosition, pBlockState);
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        this.saveInnerTile("Skull", this.innerTile, tag);
    }

    @Override
    public void load(NbtCompound tag) {
        super.load(tag);
        this.innerTile = loadInnerTile("Skull", this.innerTile, tag);
    }

    protected void saveInnerTile(String tagName, @Nullable SkullBlockEntity tile, NbtCompound tag) {
        if (tile != null) {
            tag.put(tagName + "State", NbtUtils.writeBlockState(tile.getBlockState()));
            tag.put(tagName, tile.saveWithFullMetadata());
        }
    }
    @Nullable
    protected SkullBlockEntity loadInnerTile(String tagName, @Nullable SkullBlockEntity tile, NbtCompound tag) {
        if (tag.contains(tagName)) {
            BlockState state = NbtUtils.readBlockState(tag.getCompound(tagName + "State"));
            NbtCompound tileTag = tag.getCompound(tagName);
            if (tile == null) {
                BlockEntity newTile = BlockEntity.loadStatic(this.getBlockPos(), state, tileTag);
                if (newTile instanceof SkullBlockEntity skullTile) return skullTile;
            } else {
                tile.load(tileTag);
                return tile;
            }
        }
        return null;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public ItemStack getSkullItem() {
        if (this.innerTile != null) {
            return new ItemStack(innerTile.getBlockState().getBlock());
        }
        return ItemStack.EMPTY;
    }


    public void initialize(SkullBlockEntity oldTile, SkullBlock skullBlock, ItemStack stack, Player player, Hand hand) {
        // this.setOwner(oldTile.getOwnerProfile());
        this.innerTile = (SkullBlockEntity) oldTile.getType().create(this.getBlockPos(), oldTile.getBlockState());
        if (this.innerTile != null) this.innerTile.load(oldTile.saveWithoutMetadata());
    }

    @Nullable
    public BlockState getSkull() {
        if (innerTile != null) {
            return innerTile.getBlockState();
        }
        return null;
    }
    @Nullable
    public BlockEntity getSkullTile() {
        return innerTile;
    }


    //player head stuff
/*


    @Nullable
    public GameProfile getOwnerProfile() {
        return this.owner;
    }

    public void setOwner(@Nullable GameProfile pOwner) {
        synchronized (this) {
            this.owner = pOwner;
        }
        this.updateOwnerProfile();
    }

    private void updateOwnerProfile() {
        SkullBlockEntity.updateGameprofile(this.owner, (gameProfile) -> {
            this.owner = gameProfile;
            this.setChanged();
        });
    }*/
}
