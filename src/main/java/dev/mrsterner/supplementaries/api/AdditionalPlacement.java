package dev.mrsterner.supplementaries.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.ActionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * something called by mixin which allows performing extra action when an item is used
 */
public interface AdditionalPlacement {

    @Nullable
    default BlockState overrideGetPlacementState(ItemPlacementContext pContext) {
        return null;
    }

    default ActionResult overrideUseOn(UseOnContext pContext, FoodProperties foodProperties) {
        return ActionResult.PASS;
    }

    default ActionResult overridePlace(ItemPlacementContext pContext) {
        return ActionResult.PASS;
    }

    default void appendHoverText(ItemStack pStack, World pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
    }

    @Nullable
    default ItemPlacementContext overrideUpdatePlacementContext(ItemPlacementContext pContext) {
        return null;
    }

}
