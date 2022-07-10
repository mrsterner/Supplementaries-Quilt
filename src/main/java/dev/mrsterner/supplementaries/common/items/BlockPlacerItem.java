package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.api.IExtendedItem;
import net.mehvahdjukaar.supplementaries.common.items.additional_behaviors.SimplePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

//hacky registered item that handles placing placeable stuff
public class BlockPlacerItem extends BlockItem {

    public static final Map<Block,Pair<Supplier<? extends Item>, ForgeConfigSpec.BooleanValue>> PLACEABLE_ITEMS = new HashMap<>();

    public static void registerPlaceableItem(Block block, Supplier<? extends Item> item, ForgeConfigSpec.BooleanValue config) {
        PLACEABLE_ITEMS.put(block,Pair.of(item,config));
    }

    private FoodProperties mimicFood;
    private Block mimicBlock;
    private SoundType overrideSound;

    public BlockPlacerItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
        super.registerBlocks(pBlockToItemMap, pItem);
        for (var v : PLACEABLE_ITEMS.entrySet()) {
            Block b = v.getKey();
            Item i = v.get().getFirst().get();
            if (i != null && i != Items.AIR && b != null && b != Blocks.AIR) {
                ((IExtendedItem)i).addAdditionalBehavior(new SimplePlacement(b));
                pBlockToItemMap.put(b, i);
            }
        }
    }

    private boolean isDisabled(Block b){
        var p = PLACEABLE_ITEMS.get(b);
        return p != null && !p.getSecond().get();
    }

    @Nullable
    public BlockState mimicGetPlacementState(ItemPlacementContext pContext, Block toPlace) {
        if(this.isDisabled(toPlace))return null;
        this.mimicBlock = toPlace;
        var r = getPlacementState(pContext);
        this.mimicBlock = null;
        return r;
    }

    public ActionResult mimicUseOn(UseOnContext pContext, Block toPlace, FoodProperties foodProperties) {
        if(this.isDisabled(toPlace))return ActionResult.PASS;
        this.mimicFood = foodProperties;
        this.mimicBlock = toPlace;
        var r = super.useOn(pContext);
        this.mimicFood = null;
        this.mimicBlock = null;
        return r;
    }

    public ActionResult mimicPlace(ItemPlacementContext pContext, Block toPlace, @Nullable SoundType overrideSound) {
        if(this.isDisabled(toPlace))return ActionResult.PASS;
        this.overrideSound = overrideSound;
        this.mimicBlock = toPlace;
        var r = super.place(pContext);
        this.overrideSound = null;
        this.mimicBlock = null;
        return r;
    }

    @Override
    public Block getBlock() {
        if (this.mimicBlock != null) return mimicBlock;
        return super.getBlock();
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties() {
        return mimicFood;
    }

    @Override
    public boolean isEdible() {
        return mimicFood != null;
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState pState) {
        if (this.overrideSound != null) return this.overrideSound.getPlaceSound();
        return super.getPlaceSound(pState);
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, Player entity) {
        if (this.overrideSound != null) return this.overrideSound.getPlaceSound();
        return super.getPlaceSound(state, world, pos, entity);
    }

    @Override
    public boolean canPlace(ItemPlacementContext pContext, BlockState pState) {
        this.mimicBlock = pState.getBlock();
        boolean r = super.canPlace(pContext, pState);
        this.mimicBlock = null;
        return r;
    }

}
