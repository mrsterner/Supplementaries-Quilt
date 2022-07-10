package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PresentItem extends BlockItem implements IColored {

    private final Map<DyeColor, RegistryObject<Item>> registry;

    public PresentItem(Block block, Properties properties, Map<DyeColor, RegistryObject<Item>> registry) {
        super(block, properties);
        this.registry = registry;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        NbtCompound tag = stack.getTag();
        if (tag != null) {
            NbtCompound t = tag.getCompound("BlockEntityTag");
            if(!t.isEmpty()){
                boolean isPacked = false;
                if(t.contains("Sender")){
                    var c = PresentBlockTile.getSenderMessage(t.getString("Sender"));
                    if(c != null) components.add(c);
                    isPacked = true;
                }
                if(t.contains("Recipient")){
                    var c = PresentBlockTile.getRecipientMessage(t.getString("Recipient"));
                    if(c != null) components.add(c);
                    isPacked = true;
                }
                if(!isPacked && t.contains("Items")){
                    components.add(new TranslatableComponent("message.supplementaries.present.public"));
                }
            }
        }
    }

    @Override
    public DyeColor getColor() {
        return ((PresentBlock) this.getBlock()).getColor();
    }

    @Nullable
    @Override
    public  Map<DyeColor, RegistryObject<Item>> getItemColorMap() {
        return registry;
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }
}
