package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TeleportWand extends Item {

    public TeleportWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, Player pPlayer) {
        if (!pLevel.isClient()) {
            //this.handleInteraction(pPlayer, pState, pLevel, pPos, false, pPlayer.getItemInHand(Hand.MAIN_HAND));
        }

        return false;
    }

    @Override
    public ActionResultHolder<ItemStack> use(World level, Player player, Hand pUsedHand) {
        ItemStack stack = player.getItemInHand(pUsedHand);
        if (!level.isClient()) {

            if (!player.canUseGameMasterBlocks()) {
                return ActionResultHolder.pass(stack);
            }
            else{
                var trace = CommonUtil.rayTrace(player, level,  ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, 128);
                var v = trace.getLocation();
                boolean success = player.randomTeleport(v.x, v.y, v.z, true);
                if(success) return ActionResultHolder.consume(stack);
            }
        }
        return ActionResultHolder.success(stack,level.isClient());
    }

    private boolean teleport(Player player, World level, double pX, double pY, double pZ) {





        return false;
    }
}
