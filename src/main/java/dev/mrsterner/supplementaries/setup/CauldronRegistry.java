package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.map.MapHelper;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ActionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraftforge.registries.ForgeRegistries;

public class CauldronRegistry {

    public static void registerInteractions() {
        for (var item : ModRegistry.FLAGS_ITEMS.values()) {
            CauldronInteraction.WATER.put(item.get(), CauldronInteraction.BANNER);
        }

        var atlas = ForgeRegistries.ITEMS.get(new ResourceLocation("map_atlases:atlas"));
        if (atlas != Items.AIR) {
            CauldronInteraction.WATER.put(atlas, MAP_INTERACTION);
        }

        CauldronInteraction.WATER.put(Items.FILLED_MAP, MAP_INTERACTION);
    }

    private static final CauldronInteraction MAP_INTERACTION = (state, level, pos, player, hand, stack) -> {
        if (MapHelper.removeAllCustomMarkers(level, stack, player)) {

            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            return ActionResult.success(level.isClient());
        }
        return ActionResult.PASS;
    };
}
