package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.Criteria;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class RopeItem extends BlockItem {
    public RopeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {

        Player player = context.getPlayer();
        if (player == null || player.getAbilities().mayBuild) {
            World world = context.getWorld();
            BlockPos pos = context.getBlockPos().relative(context.getClickedFace().getOpposite());
            BlockState state = world.getBlockState(pos);
            BlockProperties.PostType type = BlockProperties.PostType.get(state);

            if (type != null) {

                if (RopeKnotBlock.convertToRopeKnot(type, state, world, pos) == null) {
                    return ActionResult.FAIL;
                }

                ItemStack stack = context.getItemInHand();
                if (player instanceof ServerPlayerEntity) {
                    Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }

                SoundType soundtype = ModRegistry.ROPE.get().getDefaultState ().getSoundType(world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                if (player == null || !player.getAbilities().instabuild) {
                    stack.decrement(1);
                }
                if (player instanceof ServerPlayerEntity) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                return ActionResult.success(world.isClient());
            }
        }
        return super.place(context);
    }


    //this fixes some stuff
    //@Override
    //protected boolean mustSurvive() {
    //    return false;
    //}
}
