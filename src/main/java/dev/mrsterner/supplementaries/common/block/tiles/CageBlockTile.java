package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.MobContainer;
import net.mehvahdjukaar.supplementaries.common.items.AbstractMobContainerItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class CageBlockTile extends BlockEntity implements IMobContainerProvider {

    @Nonnull
    public MobContainer mobContainer;

    public CageBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.CAGE_TILE.get(), pos, state);
        AbstractMobContainerItem item = ((AbstractMobContainerItem) ModRegistry.CAGE_ITEM.get());
        this.mobContainer = new MobContainer(item.getMobContainerWidth(), item.getMobContainerHeight());
    }

    public void saveToNbt(ItemStack stack) {
        if(!this.mobContainer.isEmpty()) {
            NbtCompound compound = new NbtCompound();
            this.saveAdditional(compound);
            stack.setSubNbt("BlockEntityTag", compound);
        }
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        this.mobContainer.load(compound);
    }

    @Override
    public void saveAdditional(NbtCompound compound) {
        super.saveAdditional(compound);
        this.mobContainer.save(compound);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public MobContainer getMobContainer() {
        return this.mobContainer;
    }

    @Override
    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }

    public static void tick(World pLevel, BlockPos pPos, BlockState pState, CageBlockTile tile) {
        tile.mobContainer.tick(pLevel, pPos);
    }

}
