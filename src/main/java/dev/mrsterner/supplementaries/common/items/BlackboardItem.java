package net.mehvahdjukaar.supplementaries.common.items;


import net.mehvahdjukaar.supplementaries.client.renderers.items.BlackboardItemRenderer;
import net.mehvahdjukaar.supplementaries.setup.ClientRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BlackboardItem extends BlockItem {
    public BlackboardItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        ClientRegistry.registerISTER(consumer, BlackboardItemRenderer::new);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        NbtCompound tag = pStack.getTagElement("BlockEntityTag");
        if (tag != null && tag.contains("Waxed")) {
            pTooltip.add((new TranslatableComponent("message.supplementaries.blackboard")).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        NbtCompound cmp = pStack.getTagElement("BlockEntityTag");
        if (cmp != null && cmp.contains("Pixels")) {
            return Optional.of(new BlackboardTooltip(cmp.getLongArray("Pixels")));
        }
        return Optional.empty();
    }

    public record BlackboardTooltip(long[] packed) implements TooltipComponent {
    }
}
