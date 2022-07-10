package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;


public class HangingFlowerPotBlockTileRenderer implements BlockEntityRenderer<HangingFlowerPotBlockTile> {

    protected final BlockRenderDispatcher blockRenderer;

    public HangingFlowerPotBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(HangingFlowerPotBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockState state = CommonUtil.FESTIVITY.isAprilsFool() ? FlowerPotHandler.getAprilPot() : tile.getHeldBlock();

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.getSwingAngle(partialTicks) * 1.5f));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        RendererUtil.renderBlockState(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getBlockPos());

        RendererUtil.renderBlockModel(ClientRegistry.HANGING_POT_BLOCK_MODEL, matrixStackIn, bufferIn, blockRenderer,
                combinedLightIn, combinedOverlayIn, true);


        matrixStackIn.popPose();


    }
}
