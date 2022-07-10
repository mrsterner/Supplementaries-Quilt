package net.mehvahdjukaar.supplementaries.common.items.additional_behaviors;

import net.mehvahdjukaar.supplementaries.api.AdditionalPlacement;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.common.items.BlockPlacerItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SkullCandlesPlacement implements AdditionalPlacement {

    BlockPlacerItem getMimic() {
        return ModRegistry.BLOCK_PLACER.get();
    }

    @Override
    public BlockState overrideGetPlacementState(ItemPlacementContext pContext) {
        if (CompatHandler.torchslab) {
            double y = pContext.getClickLocation().y() % 1;
            if (y < 0.5) return null;
        }
        BlockState state = ModRegistry.WALL_LANTERN.get().getPlacementState(pContext);
        return (state != null && this.getMimic().canPlace(pContext, state)) ? state : null;
    }

    @Override
    public ActionResult overrideUseOn(UseOnContext pContext, FoodProperties foodProperties) {
        Player player = pContext.getPlayer();
        if (player.getAbilities().mayBuild) {
            World level = pContext.getWorld();
            BlockPos pos = pContext.getBlockPos();

            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof SkullBlockEntity oldTile) {
                BlockState state = oldTile.getBlockState();
                if ((state.getBlock() instanceof SkullBlock skullBlock && skullBlock.getType() != SkullBlock.Types.DRAGON)) {

                    ItemStack stack = pContext.getItemInHand();
                    ItemStack copy = stack.copy();

                    ActionResult result = ItemsOverrideHandler.replaceSimilarBlock(ModRegistry.SKULL_CANDLE.get(),
                            player, stack, pos, level, state, SoundType.CANDLE, SkullBlock.ROTATION);

                    if (result.consumesAction()) {
                        if (level.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
                            tile.initialize(oldTile, skullBlock, copy, player, pContext.getHand());
                        }
                    }
                    return result;
                }
            }
        }
        return ActionResult.PASS;
    }
}
