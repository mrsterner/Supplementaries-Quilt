package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RopeArrowCreateRecipe extends CustomRecipe {
    public RopeArrowCreateRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, World worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.ARROW) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            }
            else if (stack.is(ModTags.ROPES)) {

                itemstack1 = stack;

            }
            else if(!stack.isEmpty())return false;
        }
        return itemstack != null && itemstack1 != null;
    }


    @Override
    public ItemStack assemble(CraftingContainer inv) {
        int ropes = 0;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (inv.getItem(i).is(ModTags.ROPES)) {
                ropes++;
            }
        }
        ItemStack stack = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        stack.setDamageValue(stack.getMaxDamage() - ropes);
        return stack;

    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.ROPE_ARROW_CREATE_RECIPE.get();
    }


}
