package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PancakeItem extends BlockItem {
    public PancakeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ActionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) {
            World world = context.getWorld();
            BlockPos blockpos = context.getBlockPos();
            BlockState blockstate = world.getBlockState(blockpos);
            if (blockstate.is(Blocks.JUKEBOX) && !blockstate.get(JukeboxBlock.HAS_RECORD)) {
                ItemStack itemstack = context.getItemInHand();
                if (!world.isClient()) {
                    ((JukeboxBlock) Blocks.JUKEBOX).setRecord(world, blockpos, blockstate, itemstack.split(1));
                    world.syncWorldEvent(null, 1010, blockpos, Item.getId(ModRegistry.PANCAKE_DISC.get()));
                    Player player = context.getPlayer();
                    if (player != null) {
                        player.awardStat(Stats.PLAY_RECORD);
                    }
                }
                return ActionResult.success(world.isClient());
            }
        }
        return super.useOn(context);
    }
}
