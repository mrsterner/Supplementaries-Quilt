package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.IMobContainerProvider;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.MobContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;


public class CageBlockTileRenderer<T extends BlockEntity & IMobContainerProvider> implements BlockEntityRenderer<T> {
    private final EntityRenderDispatcher entityRenderer;

    public CageBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    @Override
    public int getViewDistance() {
        return 80;
    }

    public void renderMob(MobContainer mobHolder, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, Direction dir) {
        Entity mob = mobHolder.getDisplayedMob();
        if (mob != null) {
            matrixStack.pushPose();

            float s = mobHolder.getData().getScale();

            renderMobStatic(mob, s, entityRenderer, matrixStack, partialTicks, bufferIn, combinedLightIn, dir.asRotation());

            matrixStack.popPose();
        }
    }

    public static void renderMobStatic(Entity mob, float scale, EntityRenderDispatcher renderer, PoseStack matrixStack, float partialTicks,  MultiBufferSource bufferIn, int combinedLightIn, float rot){

        double y = MathHelper.lerp(partialTicks, mob.yOld, mob.getY());//0;//mobHolder.getYOffset(partialTicks);
        double x = mob.getX();
        double z = mob.getZ();

        y = relativeOffset(y);
        x = relativeOffset(x);
        z = relativeOffset(z);

        matrixStack.translate(x, y, z);

        matrixStack.mulPose(RotHlpr.rot(-(int) rot));

        matrixStack.scale(scale, scale, scale);

        renderer.setRenderShadow(false);
        renderer.render(mob, 0, 0, 0, 0.0F, partialTicks, matrixStack, bufferIn, combinedLightIn);
        renderer.setRenderShadow(true);
    }


    public static double relativeOffset(double pos) {
        if (pos < 0) return 1 + pos % 1;
        return pos % 1;
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        this.renderMob(tile.getMobContainer(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, tile.getDirection());

   }
}

