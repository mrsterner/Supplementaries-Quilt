package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.common.block.IDynamicContainer;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.TrappedPresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.common.block.util.IPresentItemBehavior;
import net.mehvahdjukaar.supplementaries.common.inventories.TrappedPresentContainerMenu;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class TrappedPresentBlockTile extends OpeneableContainerBlockEntity implements IColored, IDynamicContainer {

    private long lastActivated = 0;

    public TrappedPresentBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TRAPPED_PRESENT_TILE.get(), pos, state, 1);
    }

    @Override
    public boolean canHoldItems() {
        return this.isPrimed();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return LazyOptional.empty();
        return super.getCapability(capability, facing);
    }

    @Override
    @Nullable
    public DyeColor getColor() {
        return ((TrappedPresentBlock) this.getBlockState().getBlock()).getColor();
    }

    public static boolean isPrimed(ItemStack stack) {
        NbtCompound com = stack.getTag();
        if (com != null) {
            NbtCompound tag = com.getCompound("BlockEntityTag");
            return tag.contains("Items");
        }
        return false;
    }

    public boolean isPrimed() {
        return this.getBlockState().get(TrappedPresentBlock.PRIMED);
    }

    public void updateState(boolean primed) {

        if (!this.level.isClient() && this.isPrimed() != primed) {
            if (primed) {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_PACK.get(), SoundSource.BLOCKS, 1,
                        level.random.nextFloat() * 0.1F + 0.95F);
            } else {
                this.level.playSound(null, this.worldPosition,
                        ModSounds.PRESENT_BREAK.get(), SoundSource.BLOCKS, 0.75F,
                        level.random.nextFloat() * 0.1F + 1.2F);

            }
            this.level.setBlockState(this.getBlockPos(), this.getBlockState().with(PresentBlock.PACKED, primed), 3);
        }
    }

    @Override
    public boolean canOpen(Player player) {
        return !this.isPrimed();
    }

    public ActionResult interact(ServerPlayerEntity player, BlockPos pos) {
        long time = player.level.getGameTime();
        if (this.isUnused() &&
                MathHelper.abs(time - lastActivated) > 10) {
            if (this.canOpen(player)) {
                NetworkHooks.openGui(player, this, pos);
                PiglinAi.angerNearbyPiglins(player, true);
            } else {
                detonate(player.getWorld(), pos);
                this.lastActivated = time;
            }
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    public void detonate(ServerWorld level, BlockPos pos) {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(level, pos);
        ItemStack stack = this.getItem(0);
        IPresentItemBehavior presentItemBehavior = TrappedPresentBlock.getPresentBehavior(stack);
        this.updateState(false);
        presentItemBehavior.trigger(blocksourceimpl, stack);
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("gui.supplementaries.trapped_present");
    }

    @Override
    protected void updateBlockState(BlockState state, boolean b) {
    }

    @Override
    protected void playOpenSound(BlockState state) {
    }

    @Override
    protected void playCloseSound(BlockState state) {
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new TrappedPresentContainerMenu(id, player, this, this.worldPosition);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return PresentBlockTile.isAcceptableItem(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_19235_, ItemStack p_19236_, @Nullable Direction p_19237_) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int p_19239_, ItemStack p_19240_, Direction p_19241_) {
        return false;
    }

    //sync stuff to client. Needed for pick block
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getPresentItem(ItemLike block) {
        NbtCompound compoundTag = new NbtCompound();
        this.saveAdditional(compoundTag);
        ItemStack itemstack = new ItemStack(block);
        if (!compoundTag.isEmpty()) {
            itemstack.setSubNbt("BlockEntityTag", compoundTag);
        }

        if (this.hasCustomName()) {
            itemstack.setHoverName(this.getCustomName());
        }
        return itemstack;
    }

}
