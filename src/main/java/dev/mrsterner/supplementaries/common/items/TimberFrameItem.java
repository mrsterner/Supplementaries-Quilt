package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.tiles.FrameBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class TimberFrameItem extends BlockItem {


    public TimberFrameItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ActionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (ServerConfigs.cached.SWAP_TIMBER_FRAME && player != null && player.isShiftKeyDown() && player.getAbilities().mayBuild) {
            World world = context.getWorld();
            BlockPos pos = context.getBlockPos();
            BlockState clicked = world.getBlockState(pos);
            if (FrameBlockTile.isValidBlock(clicked, pos, world)) {
                BlockState frame = this.getBlock().getPlacementState(new ItemPlacementContext(context));
                if(frame != null) {
                    world.setBlockStateAndUpdate(pos, frame);
                    if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
                        SoundType s = frame.getSoundType(world, pos, player);
                        tile.acceptBlock(clicked);
                        world.playSound(player, pos, s.getPlaceSound(), SoundSource.BLOCKS, (s.getVolume() + 1.0F) / 2.0F, s.getPitch() * 0.8F);
                        if (!player.isCreative() && !world.isClient()()) {
                            context.getItemInHand().decrement(1);
                        }
                        return ActionResult.success(world.isClient());
                    }
                    return ActionResult.FAIL;
                }
            }

        }
        return super.useOn(context);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 200;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (!ClientConfigs.cached.TOOLTIP_HINTS || !flagIn.isAdvanced()) return;
        tooltip.add((new TranslatableComponent("message.supplementaries.timber_frame")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
