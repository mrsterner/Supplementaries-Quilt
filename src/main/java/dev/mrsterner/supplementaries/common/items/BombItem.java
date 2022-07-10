package net.mehvahdjukaar.supplementaries.common.items;

import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class BombItem extends Item {
    private final BombEntity.BombType type;
    private final boolean glint;

    public BombItem(Item.Properties builder) {
        this(builder, BombEntity.BombType.NORMAL, false);

    }

    public BombItem(Item.Properties builder, BombEntity.BombType type, boolean glint) {
        super(builder);
        this.type = type;
        this.glint = glint;
    }

    public BombEntity.BombType getType() {
        return type;
    }

    @Override
    protected boolean allowdedIn(CreativeModeTab pCategory) {
        if(this.type == BombEntity.BombType.SPIKY && !Registry.ITEM.getTagOrEmpty(TagKey.create(Registry.ITEM_REGISTRY,
                new ResourceLocation("forge:ingots/lead"))).iterator().hasNext()){
            return false;
        }
        return super.allowdedIn(pCategory);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {
        super.fillItemCategory(pCategory, pItems);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return glint;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return type== BombEntity.BombType.BLUE ? Rarity.EPIC : Rarity.RARE;
    }

    @Override
    public ActionResultHolder<ItemStack> use(World worldIn, Player playerIn, Hand handIn) {

        ItemStack itemstack = playerIn.getItemInHand(handIn);

        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldowns().addCooldown(this, 30);
        if (!worldIn.isClient()) {
            BombEntity bombEntity = new BombEntity(worldIn, playerIn, type);
            float pitch = -10;//playerIn.isSneaking()?0:-20;
            bombEntity.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), pitch, 1.25F, 0.9F);
            worldIn.addFreshEntity(bombEntity);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));
        if (!playerIn.getAbilities().instabuild) {
            itemstack.decrement(1);

        }

        return ActionResultHolder.success(itemstack, worldIn.isClient()());
    }


}

