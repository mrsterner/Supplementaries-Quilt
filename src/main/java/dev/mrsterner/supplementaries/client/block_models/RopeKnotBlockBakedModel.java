package net.mehvahdjukaar.supplementaries.client.block_models;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MimicBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock.*;

public class RopeKnotBlockBakedModel implements IDynamicBakedModel {
    private final BakedModel knot;
    private final BlockModelShaper blockModelShaper;

    public RopeKnotBlockBakedModel(BakedModel knot) {
        this.knot = knot;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {

        List<BakedQuad> quads = new ArrayList<>();

        //mimic
        try {

            BlockState mimic = extraData.getData(BlockProperties.MIMIC);
            if (mimic != null && !(mimic.getBlock() instanceof MimicBlock) && !mimic.isAir()) {
                BakedModel model = blockModelShaper.getBlockModel(mimic);

                quads.addAll(model.getQuads(mimic, side, rand, EmptyModelData.INSTANCE));
            }


        } catch (Exception ignored) {
        }

        //knot & rope
        try {
            if (state != null && state.getBlock() instanceof RopeKnotBlock) {
                BlockState rope = ModRegistry.ROPE.get().getDefaultState ()
                        .with(UP, state.get(UP))
                        .with(DOWN, state.get(DOWN))
                        .with(NORTH, state.get(NORTH))
                        .with(SOUTH, state.get(SOUTH))
                        .with(EAST, state.get(EAST))
                        .with(WEST, state.get(WEST));

                BakedModel model = blockModelShaper.getBlockModel(rope);
                //rope
                quads.addAll(model.getQuads(rope, side, rand, EmptyModelData.INSTANCE));

                //knot
                quads.addAll(knot.getQuads(state, side, rand, EmptyModelData.INSTANCE));
            }
        } catch (Exception ignored) {
        }


        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return knot.getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull IModelData data) {
        BlockState mimic = data.getData(BlockProperties.MIMIC);
        if (mimic != null && !mimic.isAir()) {

            BakedModel model = blockModelShaper.getBlockModel(mimic);
            try {
                return model.getParticleIcon();
            } catch (Exception ignored) {
            }

        }
        return getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }


}
