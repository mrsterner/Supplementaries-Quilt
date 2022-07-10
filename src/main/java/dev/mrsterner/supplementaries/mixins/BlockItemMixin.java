package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.AdditionalPlacement;
import net.mehvahdjukaar.supplementaries.api.IExtendedItem;
import net.minecraft.world.ActionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item implements IExtendedItem {

    public BlockItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void getPlacementState(ItemPlacementContext pContext, CallbackInfoReturnable<BlockState> cir) {
        AdditionalPlacement behavior = this.getAdditionalBehavior();
        if (behavior != null) {
            BlockState overrideBlockState = behavior.overrideGetPlacementState(pContext);
            if (overrideBlockState != null ) {
                cir.setReturnValue(overrideBlockState);
            }
        }
    }

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void place(ItemPlacementContext pContext, CallbackInfoReturnable<ActionResult> cir) {
        AdditionalPlacement behavior = this.getAdditionalBehavior();
        if (behavior != null) {
            var result = behavior.overridePlace(pContext);
            if (result.consumesAction()) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(method = "updatePlacementContext", at = @At("HEAD"), cancellable = true)
    private void updatePlacementContext(ItemPlacementContext pContext, CallbackInfoReturnable<ItemPlacementContext> cir) {
        AdditionalPlacement behavior = this.getAdditionalBehavior();
        if (behavior != null) {
            var result = behavior.overrideUpdatePlacementContext(pContext);
            if(result != null){
                cir.setReturnValue(result);
            }
        }
    }

}
