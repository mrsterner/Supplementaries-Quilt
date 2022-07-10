package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.google.common.collect.Lists;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VillagerTradesHandler {

    private static final float BUY = 0.05f;
    private static final float SELL = 0.2f;

    private static final VillagerTrades.ItemListing[] RED_MERCHANT_TRADES;

    static {
        List<VillagerTrades.ItemListing> trades = new ArrayList<>();

        if (RegistryConfigs.Reg.ROPE_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.ROPE_ITEM.get(), 4, 1, 10));
        }
        trades.add(itemForEmeraldTrade(Items.GUNPOWDER, 2, 1, 8));
        if (RegistryConfigs.Reg.COPPER_LANTERN_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.COPPER_LANTERN.get(), 1, 1, 12));
        }
        if (RegistryConfigs.Reg.BOMB_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.BOMB_ITEM.get(), 1, 3, 8));
        }
        trades.add(new StarForEmeraldTrade(2, 8));
        trades.add(new RocketForEmeraldTrade(3, 1, 3, 8));
        trades.add(itemForEmeraldTrade(Items.TNT, 1, 4, 8));

        if (RegistryConfigs.Reg.ROPE_ARROW_ENABLED.get()) {
            Item i = ModRegistry.ROPE_ARROW_ITEM.get();
            ItemStack stack = new ItemStack(i);
            stack.setDamageValue(Math.max(0, stack.getMaxDamage() - 16));
            trades.add(itemForEmeraldTrade(stack, 4, 6));
        }
        if (RegistryConfigs.Reg.BOMB_ENABLED.get()) {
            trades.add(itemForEmeraldTrade(ModRegistry.BOMB_BLUE_ITEM.get(), 1, ModRegistry.BOMB_ITEM.get(), 1, 40, 3));
        }


        RED_MERCHANT_TRADES = trades.toArray(new VillagerTrades.ItemListing[0]);
    }

    private static VillagerTrades.ItemListing[] CHRISTMAS_SALES = null;

    public static VillagerTrades.ItemListing[] getRedMerchantTrades() {
        if (CommonUtil.FESTIVITY.isChristmas()) {
            if (CHRISTMAS_SALES == null) {
                CHRISTMAS_SALES = Arrays.stream(RED_MERCHANT_TRADES).map(WrappedListing::new)
                        .toList().toArray(new VillagerTrades.ItemListing[0]);
            }
            return CHRISTMAS_SALES;
        }
        return RED_MERCHANT_TRADES;
    }

    private record WrappedListing(
            VillagerTrades.ItemListing original) implements VillagerTrades.ItemListing {
        private static final PresentBlockTile DUMMY = new PresentBlockTile(BlockPos.ZERO,
                ModRegistry.PRESENTS.get(null).get().getDefaultState ());

        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {
            MerchantOffer internal = original.getOffer(entity, random);
            if (internal == null) return null;
            DUMMY.setItem(0, internal.getResult());
            DUMMY.setSender(entity.getName().getString());
            DUMMY.setPublic();
            ItemStack stack = DUMMY.getPresentItem(ModRegistry.PRESENTS.get(DyeColor.values()[
                    random.nextInt(DyeColor.values().length)]).get());

            return new MerchantOffer(internal.getBaseCostA(), internal.getCostB(), stack, internal.getUses(),
                    internal.getMaxUses(), internal.getXp(), internal.getPriceMultiplier(), internal.getDemand());
        }
    }


    static BasicItemListing itemForEmeraldTrade(ItemLike item, int quantity, int price, int maxTrades) {
        return itemForEmeraldTrade(new ItemStack(item, quantity), price, maxTrades);
    }

    static BasicItemListing itemForEmeraldTrade(ItemStack itemStack, int price, int maxTrades) {
        return new BasicItemListing(new ItemStack(Items.EMERALD, price), itemStack, maxTrades, 1, BUY);
    }

    static BasicItemListing itemForEmeraldTrade(ItemLike item, int quantity, ItemLike additional, int addQuantity, int price, int maxTrades) {
        return new BasicItemListing(new ItemStack(Items.EMERALD, price), new ItemStack(additional, addQuantity), new ItemStack(item, quantity), maxTrades, 1, BUY);
    }


    record RocketForEmeraldTrade(int price, int paper, int rockets,
                                 int maxTrades) implements VillagerTrades.ItemListing {

        @Override
        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, rockets);
            NbtCompound tag = itemstack.getOrCreateTagElement("Fireworks");
            ListTag listTag = new ListTag();

            int stars = 0;
            List<FireworkRocketItem.Shape> usedShapes = new ArrayList<>();
            do {
                listTag.add(createRandomFireworkStar(random,usedShapes));
                stars++;
            } while (random.nextFloat() < 0.42f && stars < 7);

            tag.putByte("Flight", (byte) (random.nextInt(3) + 1));
            tag.put("Explosions", listTag);

            return new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(Items.PAPER, paper),
                    itemstack, maxTrades, 1, BUY);
        }


    }

    record StarForEmeraldTrade(int price, int maxTrades) implements VillagerTrades.ItemListing {

        public MerchantOffer getOffer(Entity entity, Random random) {

            ItemStack itemstack = new ItemStack(Items.FIREWORK_STAR);
            itemstack.setSubNbt("Explosion", createRandomFireworkStar(random, List.of()));
            return new MerchantOffer(new ItemStack(Items.EMERALD, price), itemstack, maxTrades, 1, BUY);
        }
    }

    private static final DyeColor[] VIBRANT_COLORS = new DyeColor[]{DyeColor.WHITE,DyeColor.ORANGE,DyeColor.MAGENTA,DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW,DyeColor.LIME,DyeColor.PINK,DyeColor.CYAN,DyeColor.PURPLE,DyeColor.BLUE,DyeColor.GREEN,DyeColor.RED};

    private static NbtCompound createRandomFireworkStar(Random random, List<FireworkRocketItem.Shape> usedShapes) {
        NbtCompound tag = new NbtCompound();
        ArrayList<FireworkRocketItem.Shape> possible =  new ArrayList<>(List.of(FireworkRocketItem.Shape.values()));
        possible.removeAll(usedShapes);
        if(possible.isEmpty()) {
            tag.putByte("Type", (byte) FireworkRocketItem.Shape.values()
                    [random.nextInt(FireworkRocketItem.Shape.values().length)].getId());
        }else{
            tag.putByte("Type", (byte) possible.get(random.nextInt(possible.size())).getId());
        }
        tag.putBoolean("Flicker", random.nextFloat() < 0.42f);
        tag.putBoolean("Trail", random.nextFloat() < 0.42f);
        List<Integer> list = Lists.newArrayList();
        int colors = 0;
        do {
            list.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
            colors++;
        } while (random.nextFloat() < 0.42f && colors < 9);
        tag.putIntArray("Colors", list);


        if (random.nextBoolean()) {
            List<Integer> fadeList = Lists.newArrayList();
            colors = 0;
            do {
                fadeList.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
                colors++;
            } while (random.nextFloat() < 0.42f && colors < 9);
            tag.putIntArray("FadeColors", fadeList);
        }

        return tag;
    }

    public static void registerWanderingTraderTrades(WandererTradesEvent event) {

        if (RegistryConfigs.Reg.GLOBE_ENABLED.get()) {
            //adding twice cause it's showing up too rarely
            for (int i = 0; i < ServerConfigs.cached.GLOBE_TRADES; i++) {
                event.getRareTrades().add(itemForEmeraldTrade(ModRegistry.GLOBE_ITEM.get(), 1, 10, 3));
            }
        }
        if (RegistryConfigs.Reg.FLAX_ENABLED.get()) {
            for (int i = 0; i < 2; i++) {
                event.getGenericTrades().add(itemForEmeraldTrade(ModRegistry.FLAX_SEEDS_ITEM.get(), 1, 6, 8));
            }
        }
    }

    public static void registerVillagerTrades(VillagerTradesEvent event) {
        if (RegistryConfigs.Reg.FLAX_ENABLED.get()) {
            if (event.getType().equals(VillagerProfession.FARMER)) {
                event.getTrades().get(3).add(new BasicItemListing(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get(), 15), new ItemStack(net.minecraft.world.item.Items.EMERALD), 16, 2, 0.05f));
            }
        }
        AdventurerMapsHandler.loadCustomTrades();
        AdventurerMapsHandler.addTrades(event);
    }
}
