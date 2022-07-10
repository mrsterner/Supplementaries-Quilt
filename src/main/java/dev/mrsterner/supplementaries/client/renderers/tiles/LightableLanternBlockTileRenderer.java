package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightableLanternBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.VerticalLanternBlockTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;


public class LightableLanternBlockTileRenderer implements BlockEntityRenderer<VerticalLanternBlockTile> {
    protected final BlockRenderDispatcher blockRenderer;

    public LightableLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(VerticalLanternBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (tile.getBlockState().get(LanternBlock.HANGING)) {

            BlockState state = tile.getBlockState().getBlock().getDefaultState ().with(LightableLanternBlock.LIT, tile.getBlockState().get(LightableLanternBlock.LIT));
            matrixStackIn.pushPose();
            // rotate towards direction
            matrixStackIn.translate(0.5, 0.875, 0.5);
            if(!tile.isFlipped()) matrixStackIn.mulPose(RotHlpr.Y90);

            float angle = tile.getSwingAngle(partialTicks);

            // animation
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angle * 1.5f));
            matrixStackIn.translate(-0.5, -0.5625, -0.5);

            // render block
            RendererUtil.renderBlockState(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getBlockPos());
            matrixStackIn.popPose();
        }

    }
}
