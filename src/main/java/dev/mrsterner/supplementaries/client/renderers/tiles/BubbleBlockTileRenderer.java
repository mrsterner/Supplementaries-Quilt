package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BubbleBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.MathHelper;

public class BubbleBlockTileRenderer implements BlockEntityRenderer<BubbleBlockTile> {

    public BubbleBlockTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BubbleBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float scale = MathHelper.lerp(partialTicks, tile.prevScale, tile.scale);
        poseStack.scale(scale, scale, scale);

        TextureAtlasSprite sprite = ClientRegistry.BUBBLE_BLOCK_MATERIAL.sprite();

        RendererUtil.renderBubble(buffer.getBuffer(RenderType.translucent()), poseStack, 1, sprite, light,
                false, tile.getBlockPos(), tile.getWorld(), partialTicks);

        poseStack.popPose();
    }


}
