package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.common.block.blocks.CogBlock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CogBlockColor implements BlockColor {

    private static final int[] COLORS = new int[16];

    static {
        for (int i = 0; i <= 15; ++i) {
            float f = (float) i / 15.0F;
            float f1 = f * 0.5F + (f > 0.0F ? 0.5F : 0.3F);
            float f2 = MathHelper.clamp(f * f * 0.5F - 0.3F, 0.0F, 1.0F);
            float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            COLORS[i] = MathHelper.color(f1, f2, f3);
        }
    }

    @Override
    public int getColor(BlockState state, BlockAndTintGetter reader, BlockPos pos, int color) {
        if (color != 1) return -1;
        return COLORS[state.get(CogBlock.POWER)];
    }
}
