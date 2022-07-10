package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.LabelEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class LabelItem extends Item {

    public LabelItem(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResult useOn(UseOnContext pContext) {
        BlockPos blockpos = pContext.getBlockPos();
        Direction direction = pContext.getClickedFace();
        BlockPos relative = blockpos.relative(direction);
        Player player = pContext.getPlayer();
        ItemStack itemstack = pContext.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemstack, relative)) {
            return ActionResult.FAIL;
        } else {
            World level = pContext.getWorld();
            BlockState state = level.getBlockState(blockpos);
            var type = LabelEntity.AttachType.get(state);

            LabelEntity label = new LabelEntity(level, relative, direction);
            label.setAttachmentType(type);

            NbtCompound compoundtag = itemstack.getTag();
            if (compoundtag != null) {
                EntityType.updateCustomEntityTag(level, player, label, compoundtag);
            }

            if (label.survives()) {
                if (!level.isClient()) {
                    label.playPlacementSound();
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, blockpos);
                    level.addFreshEntity(label);
                }

                itemstack.decrement(1);
                return ActionResult.success(level.isClient());
            } else {
                return ActionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player pPlayer, Direction pDirection, ItemStack pHangingEntityStack, BlockPos pPos) {
        return !pDirection.getAxis().isVertical() && pPlayer.mayUseItemAt(pPos, pDirection, pHangingEntityStack);
    }
}
