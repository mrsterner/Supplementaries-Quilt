package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Hand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CandleSkullBlockTile extends EnhancedSkullBlockTile {

    private BlockState candle = Blocks.AIR.getDefaultState ();
    //client only
    private DyeColor color = null;

    public CandleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_CANDLE_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        tag.put("Candle", NbtUtils.writeBlockState(this.candle));
    }

    @Override
    public void load(NbtCompound tag) {
        super.load(tag);
        if (tag.contains("Candle", 10)) {
            this.candle = NbtUtils.readBlockState(tag.getCompound("Candle"));
            this.color = colorFromCandle(this.candle.getBlock());
        }
    }

    @Nullable
    public DyeColor getCandleColor() {
        return this.color;
    }

    public BlockState getCandle() {
        return candle;
    }

    public void setCandle(BlockState candle) {
        this.candle = candle;
    }

    public boolean tryAddingCandle(Block candle) {
        if (this.candle.isAir() || (candle == this.candle.getBlock() && this.candle.get(CandleBlock.CANDLES) != 4)) {

            if (this.candle.isAir()) {
                this.candle = candle.getDefaultState ();
                this.color = colorFromCandle(this.candle.getBlock());
            } else {
                this.candle.cycle(CandleBlock.CANDLES);
            }

            if (!this.level.isClient()) {
                BlockState state = this.getBlockState();
                BlockState newState = BlockUtils.replaceProperty(this.candle, state, CandleBlock.CANDLES);
                this.level.setBlockStateAndUpdate(this.worldPosition, newState);
                //this.level.sendBlockUpdated(this.worldPosition, state,newState,2);
                this.setChanged();
            }
            return true;
        }
        return false;
    }


    @Override
    public void initialize(SkullBlockEntity oldTile, SkullBlock skullBlock, ItemStack stack, Player player, Hand hand) {
        super.initialize(oldTile, skullBlock, stack, player, hand);
        if (stack.getItem() instanceof BlockItem blockItem) {
            tryAddingCandle(blockItem.getBlock());
        }
    }

    @Nullable
    public static DyeColor colorFromCandle(Block b) {
        if (b instanceof CandleBlock) {
            String n = b.getRegistryName().getPath().replace("_candle", "");
            return DyeColor.byName(n, null);
        }
        return null;
    }

}
