package net.mehvahdjukaar.supplementaries.common.items.additional_behaviors;

import net.mehvahdjukaar.supplementaries.api.AdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ActionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * something called by mixin which should place or alter a block when clicked on
 */
public record SimplePlacement(@Nullable Block placeable) implements AdditionalPlacement {

    public SimplePlacement(@Nullable Block placeable) {
        this.placeable = placeable;
    }

    BlockPlacerItem getMimic() {
        return ModRegistry.BLOCK_PLACER.get();
    }

    @Override
    @Nullable
    public BlockState overrideGetPlacementState(ItemPlacementContext pContext) {
        return getMimic().mimicGetPlacementState(pContext, placeable);
    }

    @Override
    public ActionResult overrideUseOn(UseOnContext pContext, FoodProperties foodProperties) {
        return getMimic().mimicUseOn(pContext, placeable, foodProperties);
    }

    @Override
    public ActionResult overridePlace(ItemPlacementContext pContext) {
        return getMimic().mimicPlace(pContext, placeable, null);
    }

    @Override
    public void appendHoverText(ItemStack pStack, World pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (ClientConfigs.cached.PLACEABLE_TOOLTIPS) {
            pTooltipComponents.add(new TranslatableComponent("message.supplementaries.placeable").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }


}
