package net.mehvahdjukaar.supplementaries.common.world.data.map.markers;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.markers.NamedMapBlockMarker;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CeilingBannerBlockTile;
import net.mehvahdjukaar.supplementaries.common.world.data.map.CMDreg;
import net.mehvahdjukaar.supplementaries.common.world.data.map.ColoredDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.Objects;

public class CeilingBannerMarker extends NamedMapBlockMarker<ColoredDecoration> {

    private DyeColor color;

    public CeilingBannerMarker() {
        super(CMDreg.BANNER_DECORATION_TYPE);
    }

    public CeilingBannerMarker(BlockPos pos, DyeColor color, @Nullable Component name) {
        super(CMDreg.BANNER_DECORATION_TYPE,pos);
        this.color = color;
        this.name = name;
    }

    @Override
    public NbtCompound saveToNBT(NbtCompound tag) {
        super.saveToNBT(tag);
        tag.putString("Color", this.color.getName());
        return tag;
    }

    @Override
    public void loadFromNBT(NbtCompound compound) {
        super.loadFromNBT(compound);
        this.color = DyeColor.byName(compound.getString("Color"), DyeColor.WHITE);
    }

    @Nullable
    public static CeilingBannerMarker getFromWorld(BlockGetter world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof CeilingBannerBlockTile tile) {
            DyeColor dyecolor = tile.getBaseColor(tile::getBlockState);
            Component name = tile.hasCustomName() ? tile.getCustomName() : null;
            return new CeilingBannerMarker(pos, dyecolor, name);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public ColoredDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new ColoredDecoration(this.getType(), mapX, mapY, rot, this.name, this.color);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other != null && this.getClass() == other.getClass()) {
            CeilingBannerMarker marker = (CeilingBannerMarker) other;
            return Objects.equals(this.getPos(), marker.getPos()) && this.color == marker.color;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getPos(), this.color);
    }

}
