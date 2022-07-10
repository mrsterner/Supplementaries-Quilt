package net.mehvahdjukaar.supplementaries.common.capabilities.antique_ink;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

//actual capability provider (which provides itself as a cap instance)
public class AntiqueInkProvider implements IAntiqueTextProvider, ICapabilitySerializable<NbtCompound> {

    private boolean hasAntiqueInk = false;

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        return capability == CapabilityHandler.ANTIQUE_TEXT_CAP ?
                LazyOptional.of(() -> this).cast() : LazyOptional.empty();
    }

    @Override
    public NbtCompound serializeNBT() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("ink", this.hasAntiqueInk);
        return tag;
    }

    @Override
    public void deserializeNBT(NbtCompound tag) {
        this.hasAntiqueInk = tag.getBoolean("ink");
    }

    @Override
    public boolean hasAntiqueInk() {
        return this.hasAntiqueInk;
    }

    @Override
    public void setAntiqueInk(boolean hasInk) {
        this.hasAntiqueInk = hasInk;
    }
}
