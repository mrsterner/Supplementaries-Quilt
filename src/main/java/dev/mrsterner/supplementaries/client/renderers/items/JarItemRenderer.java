package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer.renderFluid;


public class JarItemRenderer extends CageItemRenderer {

    private static final Random RAND = new Random(420);

    public JarItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderTileStuff(NbtCompound tag, ItemTransforms.TransformType transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        super.renderTileStuff(tag, transformType, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        if (tag.contains("MobHolder") || tag.contains("BucketHolder")) {
            NbtCompound com = tag.getCompound("BucketHolder");
            if (com.isEmpty()) com = tag.getCompound("MobHolder");
            if (com.contains("FishTexture")) {
                int fishTexture = com.getInt("FishTexture");
                if (fishTexture >= 0) {
                    matrixStackIn.pushPose();
                    VertexConsumer builder1 = bufferIn.getBuffer(RenderType.cutout());
                    matrixStackIn.translate(0.5, 0.3125, 0.5);
                    matrixStackIn.mulPose(RotHlpr.YN45);
                    matrixStackIn.scale(1.5f, 1.5f, 1.5f);
                    RendererUtil.renderFish(builder1, matrixStackIn, 0, 0, fishTexture, combinedLightIn);
                    matrixStackIn.popPose();
                }
                SoftFluid s = SoftFluidRegistry.WATER.get();
                renderFluid(0.5625f, s.getTintColor(), 0, s.getStillTexture(),
                        matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
            }
        }
        if (tag.contains("FluidHolder")) {
            NbtCompound com = tag.getCompound("FluidHolder");
            int count = com.getInt("Count");
            if (count != 0) {
                int color = com.getInt("CachedColor");
                SoftFluid fluid = SoftFluidRegistry.get(com.getString("Fluid"));
                if (!fluid.isEmpty() && count > 0)
                    renderFluid(getHeight(count, 0.75f) , color, 0, fluid.getStillTexture(),
                            matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
            }
        }
        if (tag.contains("Items")) {
            ItemStack cookieStack = ItemStack.of((tag.getList("Items", 10)).getCompound(0));
            int height = cookieStack.getCount();
            if (height != 0) {
                RAND.setSeed(420);
                matrixStackIn.pushPose();
                matrixStackIn.translate(0.5, 0.5, 0.5);
                matrixStackIn.mulPose(RotHlpr.XN90);
                matrixStackIn.translate(0, 0, -0.5);
                float scale = 8f / 14f;
                matrixStackIn.scale(scale, scale, scale);
                for (float i = 0; i < height; i++) {
                    matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(RAND.nextInt(360)));
                    // matrixStackIn.translate(0, 0, 0.0625);
                    matrixStackIn.translate(0, 0, 1 / (16f * scale));
                    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                    BakedModel model = itemRenderer.getModel(cookieStack, null, null, 0);
                    itemRenderer.render(cookieStack, ItemTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn,
                            combinedOverlayIn, model);
                }
                matrixStackIn.popPose();
            }
        }

    }

    public static float getHeight(float count, float maxHeight) {
        return maxHeight * count / (float) ServerConfigs.cached.JAR_CAPACITY;
    }
}

