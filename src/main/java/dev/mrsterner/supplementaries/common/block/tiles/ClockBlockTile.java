package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.math.MathHelperUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ClockBlockTile extends BlockEntity {

    public float roll = 0;
    public float prevRoll = 0;
    public float targetRoll = 0;

    public float sRoll = 0;
    public float sPrevRoll = 0;
    public float sTargetRoll = 0;

    public int power = 0;

    public ClockBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.CLOCK_BLOCK_TILE.get(), pos, state);
    }

    @Override
    public void load(@Nonnull NbtCompound compound) {
        super.load(compound);
        this.roll = compound.getFloat("MinRoll");
        this.prevRoll = this.roll;
        this.targetRoll = this.roll;

        this.sRoll = compound.getFloat("SecRoll");
        this.sPrevRoll = this.sRoll;
        this.sTargetRoll = this.sRoll;
        this.power = compound.getInt("Power");
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        tag.putFloat("MinRoll", this.targetRoll);
        tag.putFloat("SecRoll", this.sTargetRoll);
        tag.putInt("Power", this.power);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void updateInitialTime(World level, BlockState state, BlockPos pos) {
        int time = (int) (level.getDayTime() % 24000);
        this.updateTime(time, level, state, pos);
        this.roll = this.targetRoll;
        this.prevRoll = this.targetRoll;
        this.sRoll = this.sTargetRoll;
        this.sPrevRoll = this.sTargetRoll;
    }

    //TODO: rewrite
    public void updateTime(int time, World level, BlockState state, BlockPos pos) {

        if (level.dimensionType().natural()) {

            //minute here are 1 rl second -> 50m in a minecraft hour
            int minute = MathHelper.clamp((time % 1000) / 20, 0, 50);
            int hour = MathHelper.clamp(time / 1000, 0, 24);

            //server
            if (!level.isClient()) {

                if (hour != state.get(ClockBlock.HOUR)) {
                    //if they are sent to the client the animation gets broken. Side effect is that you can't see hour with f3
                    level.setBlockState(pos, state.with(ClockBlock.HOUR, hour), 3);
                }
                int p = MathHelper.clamp(time / 1500, 0, 15);
                if (p != this.power) {
                    this.power = p;
                    level.updateNeighbourForOutputSignal(pos, this.getBlockState().getBlock());
                }
                this.level.playSound(null, this.worldPosition,
                        (minute % 2 == 0 ? ModSounds.CLOCK_TICK_1 : ModSounds.CLOCK_TICK_2).get(), SoundSource.BLOCKS,
                        0.08f, MathHelperUtils.nextWeighted(level.random, 0.1f) + 0.95f);

            }
            //hours
            this.targetRoll = (hour * 30) % 360;
            //minutes
            this.sTargetRoll = (minute * 7.2f + 180) % 360f;

        } else {

            /*
            double d0 = Math.random() - (this.targetRoll/360f);
            d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
            this.rota += d0 * 0.1D;
            this.rota *= 0.9D;
            this.targetRoll = 360*((float) MathHelper.positiveModulo(this.targetRoll/360f + this.rota, 1.0D));

            this.roll = this.targetRoll;
            */

            this.targetRoll = level.random.nextFloat() * 360;
            this.sTargetRoll = level.random.nextFloat() * 360;
            //TODO: make it wobbly
        }
    }

    public static void tick(World pLevel, BlockPos pPos, BlockState pState, ClockBlockTile tile) {
        int dayTime = (int) (pLevel.getDayTime() % 24000);
        int time = pLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) ?
                dayTime : (int) (pLevel.getGameTime() % 24000);
        if (time % 20 == 0) {
            tile.updateTime(dayTime, pLevel, pState, pPos);

        }
        //TODO: fix hour hand
        //hours
        tile.prevRoll = tile.roll;
        if (tile.roll != tile.targetRoll) {
            float r = (tile.roll + 8) % 360;
            if ((r >= tile.targetRoll) && (r <= tile.targetRoll + 8)) {
                r = tile.targetRoll;
            }
            tile.roll = r;
        }
        //minutes
        tile.sPrevRoll = tile.sRoll;
        if (tile.sRoll != tile.sTargetRoll) {
            float r = (tile.sRoll + 8) % 360;
            if ((r >= tile.sTargetRoll) && (r <= tile.sTargetRoll + 8)) {
                r = tile.sTargetRoll;
            }
            tile.sRoll = r;
        }
    }

    public Direction getDirection() {
        return this.getBlockState().get(ClockBlock.FACING);
    }
}

