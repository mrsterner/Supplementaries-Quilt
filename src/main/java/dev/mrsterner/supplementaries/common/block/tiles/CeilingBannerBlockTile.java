package net.mehvahdjukaar.supplementaries.common.block.tiles;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;


public class CeilingBannerBlockTile extends BlockEntity implements Nameable {
    @Nullable
    private Component name;
    @Nullable
    private DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public CeilingBannerBlockTile(BlockPos pos, BlockState state) {
        this(pos, state, DyeColor.WHITE);
    }

    public CeilingBannerBlockTile(BlockPos pos, BlockState state, DyeColor color) {
        super(ModRegistry.CEILING_BANNER_TILE.get(), pos, state);
        this.baseColor = color;
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : new TranslatableComponent("block.minecraft.banner");
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component p_213136_1_) {
        this.name = p_213136_1_;
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        if (this.itemPatterns != null) {
            tag.put("Patterns", this.itemPatterns);
        }

        if (this.name != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(NbtCompound pTag) {
        super.load(pTag);
        if (pTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }

        if (this.hasLevel()) {
            this.baseColor = ((AbstractBannerBlock) this.getBlockState().getBlock()).getColor();
        } else {
            this.baseColor = null;
        }

        this.itemPatterns = pTag.getList("Patterns", 10);
        this.patterns = null;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null) {
            this.patterns = BannerBlockEntity.createPatterns(this.baseColor, this.itemPatterns);
        }
        return this.patterns;
    }

    public DyeColor getBaseColor(Supplier<BlockState> blockStateSupplier) {
        if (this.baseColor == null) {
            this.baseColor = ((AbstractBannerBlock) blockStateSupplier.get().getBlock()).getColor();
        }

        return this.baseColor;
    }
}

