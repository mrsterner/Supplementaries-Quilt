package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkTooltipPlugin;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class SafeItem extends BlockItem {
    public SafeItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        NbtCompound compoundtag = pItemEntity.getItem().getTag();
        if (compoundtag != null) {
            ListTag listtag = compoundtag.getCompound("BlockEntityTag").getList("Items", 10);
            ItemUtils.onContainerDestroyed(pItemEntity, listtag.stream().map(NbtCompound.class::cast).map(ItemStack::of));
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
        return ItemsUtil.tryInteractingWithContainerItem(stack, incoming, slot, action, player, true);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return ItemsUtil.tryInteractingWithContainerItem(stack, slot.getItem(), slot, action, player, false);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        if (CompatHandler.quark && QuarkTooltipPlugin.canRenderTooltip()) {
            NbtCompound cmp = pStack.getTagElement("BlockEntityTag");
            if (cmp != null && !cmp.contains("LootTable")) {
                return Optional.of(new ItemsUtil.InventoryTooltip(cmp, this, 27));
            }
        }
        return Optional.empty();
    }
}
