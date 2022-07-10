package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.blocks.WindVaneBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindVaneBlockTile extends BlockEntity {
    public float yaw = 0;
    public float prevYaw = 0;
    private float offset = 0;

    public WindVaneBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.WIND_VANE_TILE.get(), pos, state);
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        float tp = (float) (Math.PI * 2);
        this.offset = 400 * (MathHelper.sin((0.005f * this.worldPosition.getX()) % tp) + MathHelper.sin((0.005f * this.worldPosition.getZ()) % tp) + MathHelper.sin((0.005f * this.worldPosition.getY()) % tp));
    }

    public static void tick(World pLevel, BlockPos pPos, BlockState pState, WindVaneBlockTile tile) {

        float currentYaw = tile.yaw;
        tile.prevYaw = currentYaw;
        if (!pLevel.isClient()()) {
            if (pLevel.getGameTime() % 20L == 0L) {
                Block block = pState.getBlock();
                if (block instanceof WindVaneBlock) {
                    WindVaneBlock.updatePower(pState, pLevel, pPos);
                }
            }
        } else {
            int power = pState.get(WindVaneBlock.WIND_STRENGTH);
            // TODO:cache some of this maybe?
            float tp = (float) (2f * Math.PI);
            //float offset = 3f * (MathHelper.sin(0.1f*this.pos.getX()) + 0.1f*MathHelper.sin(this.pos.getZ()) + 0.1f*MathHelper.sin(this.pos.getY()));
            float t = pLevel.getGameTime() % 24000 + tile.offset;
            float b = (float) Math.max(1, (power * ClientConfigs.cached.WIND_VANE_POWER_SCALING));
            float max_angle_1 = (float) ClientConfigs.cached.WIND_VANE_ANGLE_1;
            float max_angle_2 = (float) ClientConfigs.cached.WIND_VANE_ANGLE_2;
            float period_1 = (float) ClientConfigs.cached.WIND_VANE_PERIOD_1;
            float period_2 = (float) ClientConfigs.cached.WIND_VANE_PERIOD_2;
            float newYaw = max_angle_1 * MathHelper.sin(tp * ((t * b / period_1) % 360))
                    + max_angle_2 * MathHelper.sin(tp * ((t * b / period_2) % 360));
            tile.yaw = MathHelper.clamp(newYaw, currentYaw - 8, currentYaw + 8);
        }
    }
}
