package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.block_set.BlockRegistryHelper;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.blocks.VerticalSlabBlock;
import net.mehvahdjukaar.selene.entities.ImprovedFallingBlockEntity;
import net.mehvahdjukaar.selene.items.WoodBasedBlockItem;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.*;
import net.mehvahdjukaar.supplementaries.common.block.tiles.*;
import net.mehvahdjukaar.supplementaries.common.effects.OverencumberedEffect;
import net.mehvahdjukaar.supplementaries.common.effects.StasisEnchantment;
import net.mehvahdjukaar.supplementaries.common.entities.*;
import net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart.DispenserMinecartEntity;
import net.mehvahdjukaar.supplementaries.common.inventories.*;
import net.mehvahdjukaar.supplementaries.common.items.*;
import net.mehvahdjukaar.supplementaries.common.items.crafting.*;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.items.tabs.JarTab;
import net.mehvahdjukaar.supplementaries.common.items.tabs.SupplementariesTab;
import net.mehvahdjukaar.supplementaries.common.world.generation.WorldGenHandler;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.cctweaked.CCPlugin;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.mehvahdjukaar.supplementaries.setup.RegistryConstants.*;
import static net.mehvahdjukaar.supplementaries.setup.RegistryHelper.*;

@SuppressWarnings({"unused", "ConstantConditions"})
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Supplementaries.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Supplementaries.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Supplementaries.MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Supplementaries.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Supplementaries.MOD_ID);
    public static final DeferredRegister<Motive> PAINTINGS = DeferredRegister.create(ForgeRegistries.PAINTING_TYPES, Supplementaries.MOD_ID);
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Supplementaries.MOD_ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Supplementaries.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Supplementaries.MOD_ID);


    public static void registerBus(IEventBus bus) {
        MOD_TAB = !RegistryConfigs.Reg.CREATIVE_TAB.get() ? null : new SupplementariesTab("supplementaries");
        JAR_TAB = !RegistryConfigs.Reg.JAR_TAB.get() ? null : new JarTab("jars");
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        CONTAINERS.register(bus);
        ENTITIES.register(bus);
        PARTICLES.register(bus);
        RECIPES.register(bus);
        PAINTINGS.register(bus);
        ENCHANTMENTS.register(bus);
        EFFECTS.register(bus);
        SOUNDS.register(bus);

        CompatHandler.registerOptionalStuff();
        RegistryHelper.initDynamicRegistry();
    }

    public static boolean isDisabled(String name) {
        return !RegistryConfigs.Reg.isEnabled(name);
    }


    //creative tab
    public static CreativeModeTab MOD_TAB = null;
    public static CreativeModeTab JAR_TAB = null;

    public static final LootItemFunctionType CURSE_LOOT_FUNCTION = new LootItemFunctionType(new CurseLootFunction.Serializer());


    //using this to register overwrites and conditional block items
    @SubscribeEvent
    public static void registerAdditionalStuff(final RegistryEvent.Register<Item> event) {
        WorldGenHandler.onRegisterAdditional();
        Registry.register(Registry.LOOT_FUNCTION_TYPE, Supplementaries.res("curse_loot"), CURSE_LOOT_FUNCTION);

        //CompatHandler.registerOptionalItems(event);
        //shulker shell
        //addOptionalPlaceableItem("quark:ancient_tome", BOOK_PILE.get());

        if (RegistryConfigs.Reg.SHULKER_HELMET_ENABLED.get()) {
            event.getRegistry().register(new ShulkerShellItem(new Item.Properties()
                    .stacksTo(64)
                    .tab(CreativeModeTab.TAB_MATERIALS)).setRegistryName("minecraft:shulker_shell"));
        }
    }

    //entities
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModRegistry.RED_MERCHANT.get(), Mob.createMobAttributes().build());
        //  event.put(ModRegistry.FIREFLY_TYPE.get(), FireflyEntity.setCustomAttributes().build());
    }

    //paintings
    public static final RegistryObject<Motive> BOMB_PAINTING = PAINTINGS.register("bombs", () -> new Motive(32, 32));

    //enchantment
    public static final RegistryObject<Enchantment> STASIS_ENCHANTMENT = ENCHANTMENTS.register(STASIS_NAME, StasisEnchantment::new);

    public static final RegistryObject<MobEffect> OVERENCUMBERED = EFFECTS.register("overencumbered", OverencumberedEffect::new);

    //particles
    public static final RegistryObject<SimpleParticleType> SPEAKER_SOUND = regParticle("speaker_sound");
    public static final RegistryObject<SimpleParticleType> GREEN_FLAME = regParticle("green_flame");
    public static final RegistryObject<SimpleParticleType> DRIPPING_LIQUID = regParticle("dripping_liquid");
    public static final RegistryObject<SimpleParticleType> FALLING_LIQUID = regParticle("falling_liquid");
    public static final RegistryObject<SimpleParticleType> SPLASHING_LIQUID = regParticle("splashing_liquid");
    public static final RegistryObject<SimpleParticleType> BOMB_EXPLOSION_PARTICLE = regParticle("bomb_explosion");
    public static final RegistryObject<SimpleParticleType> BOMB_EXPLOSION_PARTICLE_EMITTER = regParticle("bomb_explosion_emitter");
    public static final RegistryObject<SimpleParticleType> BOMB_SMOKE_PARTICLE = regParticle("bomb_smoke");
    public static final RegistryObject<SimpleParticleType> BOTTLING_XP_PARTICLE = regParticle("bottling_xp");
    public static final RegistryObject<SimpleParticleType> FEATHER_PARTICLE = regParticle("feather");
    public static final RegistryObject<SimpleParticleType> SLINGSHOT_PARTICLE = regParticle("air_burst");
    public static final RegistryObject<SimpleParticleType> STASIS_PARTICLE = regParticle("stasis");
    public static final RegistryObject<SimpleParticleType> CONFETTI_PARTICLE = regParticle("confetti");
    public static final RegistryObject<SimpleParticleType> ROTATION_TRAIL = regParticle("rotation_trail");
    public static final RegistryObject<SimpleParticleType> ROTATION_TRAIL_EMITTER = regParticle("rotation_trail_emitter");
    public static final RegistryObject<SimpleParticleType> SUDS_PARTICLE = regParticle("suds");
    public static final RegistryObject<SimpleParticleType> ASH_PARTICLE = regParticle("ash");
    public static final RegistryObject<SimpleParticleType> BUBBLE_BLOCK_PARTICLE = regParticle("bubble_block");

    //recipes
    public static final RegistryObject<RecipeSerializer<?>> BLACKBOARD_DUPLICATE_RECIPE = RECIPES.register("blackboard_duplicate", () ->
            new SimpleRecipeSerializer<>(BlackboardDuplicateRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> BAMBOO_SPIKES_TIPPED_RECIPE = RECIPES.register("bamboo_spikes_tipped", () ->
            new SimpleRecipeSerializer<>(TippedBambooSpikesRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> ROPE_ARROW_CREATE_RECIPE = RECIPES.register("rope_arrow_create", () ->
            new SimpleRecipeSerializer<>(RopeArrowCreateRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> ROPE_ARROW_ADD_RECIPE = RECIPES.register("rope_arrow_add", () ->
            new SimpleRecipeSerializer<>(RopeArrowAddRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> BUBBLE_BLOWER_REPAIR_RECIPE = RECIPES.register("bubble_blower_charge", () ->
            new SimpleRecipeSerializer<>(RepairBubbleBlowerRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> FLAG_FROM_BANNER_RECIPE = RECIPES.register("flag_from_banner", () ->
            new SimpleRecipeSerializer<>(FlagFromBannerRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> TREASURE_MAP_RECIPE = RECIPES.register("treasure_map", () ->
            new SimpleRecipeSerializer<>(WeatheredMapRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> SOAP_CLEARING_RECIPE = RECIPES.register("soap_clearing", () ->
            new SimpleRecipeSerializer<>(SoapClearRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> PRESENT_DYE_RECIPE = RECIPES.register("present_dye", () ->
            new SimpleRecipeSerializer<>(PresentDyeRecipe::new));
    public static final RegistryObject<RecipeSerializer<?>> TRAPPED_PRESENT_RECIPE = RECIPES.register("trapped_present", () ->
            new SimpleRecipeSerializer<>(TrappedPresentRecipe::new));


    public static final RegistryObject<EntityType<PearlMarker>> PEARL_MARKER = regEntity("pearl_marker",
            EntityType.Builder.<PearlMarker>of(PearlMarker::new, MobCategory.MISC)
                    .sized(0.999F, 0.999F)
                    .updateInterval(-1).setShouldReceiveVelocityUpdates(false)
                    .clientTrackingRange(4));

    //dispenser minecart
    public static final RegistryObject<EntityType<DispenserMinecartEntity>> DISPENSER_MINECART = regEntity(DISPENSER_MINECART_NAME,
            EntityType.Builder.<DispenserMinecartEntity>of(DispenserMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F).clientTrackingRange(8));

    public static final RegistryObject<Item> DISPENSER_MINECART_ITEM = regItem(DISPENSER_MINECART_NAME, () -> new DispenserMinecartItem(new Item.Properties()
            .stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));

    //red trader
    public static final RegistryObject<EntityType<RedMerchantEntity>> RED_MERCHANT = regEntity(RED_MERCHANT_NAME,
            EntityType.Builder.<RedMerchantEntity>of(RedMerchantEntity::new, MobCategory.CREATURE)
                    .setShouldReceiveVelocityUpdates(true)
                    .clientTrackingRange(10)
                    .setUpdateInterval(3)
                    .sized(0.6F, 1.95F));

    public static final RegistryObject<MenuType<RedMerchantContainerMenu>> RED_MERCHANT_CONTAINER = CONTAINERS
            .register(RED_MERCHANT_NAME, () -> IForgeMenuType.create(RedMerchantContainerMenu::new));

    public static final RegistryObject<Item> RED_MERCHANT_SPAWN_EGG_ITEM = ITEMS.register(RED_MERCHANT_NAME + "_spawn_egg", () ->
            new ForgeSpawnEggItem(RED_MERCHANT, 0x7A090F, 0xF4f1e0,
                    new Item.Properties().tab(getTab(null, RED_MERCHANT_NAME))));

    //urn
    public static final RegistryObject<EntityType<FallingUrnEntity>> FALLING_URN = regEntity(FALLING_URN_NAME,
            EntityType.Builder.<FallingUrnEntity>of(FallingUrnEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //ash
    public static final RegistryObject<EntityType<FallingAshEntity>> FALLING_ASH = regEntity(FALLING_ASH_NAME,
            EntityType.Builder.<FallingAshEntity>of(FallingAshEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //ash
    public static final RegistryObject<EntityType<FallingLanternEntity>> FALLING_LANTERN = regEntity(FALLING_LANTERN_NAME,
            EntityType.Builder.<FallingLanternEntity>of(FallingLanternEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    public static final RegistryObject<EntityType<ImprovedFallingBlockEntity>> FALLING_SACK = regEntity(FALLING_SACK_NAME,
            EntityType.Builder.<ImprovedFallingBlockEntity>of(ImprovedFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(20));

    //firefly

//    public static final String FIREFLY_NAME = "firefly";
//    private static final EntityType<FireflyEntity> FIREFLY_TYPE_RAW = (EntityType.Builder.of(FireflyEntity::new, MobCategory.AMBIENT)
//            .setShouldReceiveVelocityUpdates(true).setTrackingRange(12).setUpdateInterval(3)
//            .sized(0.3125f, 1f))
//            .build(FIREFLY_NAME);
//
//    public static final RegistryObject<EntityType<FireflyEntity>> FIREFLY_TYPE = ENTITIES.register(FIREFLY_NAME, () -> FIREFLY_TYPE_RAW);
//
//    public static final RegistryObject<Item> FIREFLY_SPAWN_EGG_ITEM = ITEMS.register(FIREFLY_NAME + "_spawn_egg", () ->
//            new ForgeSpawnEggItem(FIREFLY_TYPE, -5048018, -14409439, //-4784384, -16777216,
//                    new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, FIREFLY_NAME))));

    //brick
    public static final RegistryObject<EntityType<ThrowableBrickEntity>> THROWABLE_BRICK = regEntity(THROWABLE_BRICK_NAME,
            EntityType.Builder.<ThrowableBrickEntity>of(ThrowableBrickEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(ThrowableBrickEntity::new)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    //.size(0.25F, 0.25F).trackingRange(4).updateInterval(10)));

    //bomb
    public static final RegistryObject<EntityType<BombEntity>> BOMB = regEntity(BOMB_NAME,
            EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(BombEntity::new)
                    .sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(10));

    public static final RegistryObject<Item> BOMB_ITEM = regItem(BOMB_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_NAME))));
    public static final RegistryObject<Item> BOMB_ITEM_ON = ITEMS.register("bomb_projectile", () -> new BombItem(new Item.Properties()
            .tab(null)));

    public static final RegistryObject<Item> BOMB_BLUE_ITEM = ITEMS.register(BOMB_BLUE_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_NAME)), BombEntity.BombType.BLUE, true));
    public static final RegistryObject<Item> BOMB_BLUE_ITEM_ON = ITEMS.register("bomb_blue_projectile", () -> new BombItem(new Item.Properties()
            .tab(null), BombEntity.BombType.BLUE, false));

    //sharpnel bomb
    public static final RegistryObject<Item> BOMB_SPIKY_ITEM = ITEMS.register(BOMB_SPIKY_NAME, () -> new BombItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_COMBAT, BOMB_SPIKY_NAME)), BombEntity.BombType.SPIKY, false));
    public static final RegistryObject<Item> BOMB_SPIKY_ITEM_ON = ITEMS.register("bomb_spiky_projectile", () -> new BombItem(new Item.Properties()
            .tab(null), BombEntity.BombType.SPIKY, false));

    //rope arrow
    public static final RegistryObject<EntityType<RopeArrowEntity>> ROPE_ARROW = ENTITIES.register(ROPE_ARROW_NAME, () -> (
            EntityType.Builder.<RopeArrowEntity>of(RopeArrowEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(RopeArrowEntity::new)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20))
            .build(ROPE_ARROW_NAME));
    public static final RegistryObject<Item> ROPE_ARROW_ITEM = ITEMS.register(ROPE_ARROW_NAME, () -> new RopeArrowItem(
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_MISC, ROPE_ARROW_NAME)).defaultDurability(24).setNoRepair()));

    //slingshot projectile
    public static final RegistryObject<EntityType<SlingshotProjectileEntity>> SLINGSHOT_PROJECTILE = ENTITIES.register(SLINGSHOT_PROJECTILE_NAME, () -> (
            EntityType.Builder.<SlingshotProjectileEntity>of(SlingshotProjectileEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(SlingshotProjectileEntity::new)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20))
            .build(SLINGSHOT_PROJECTILE_NAME));


    //label

    public static final RegistryObject<EntityType<LabelEntity>> LABEL =
            null; /*
            ENTITIES.register(LABEL_NAME, () -> (
            EntityType.Builder.<LabelEntity>of(LabelEntity::new, MobCategory.MISC)
                    .setCustomClientFactory(LabelEntity::new)
                    .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(10))
            .build(LABEL_NAME));

    public static final RegistryObject<Item> LABEL_ITEM = regItem(LABEL_NAME, () -> new LabelItem(new Item.Properties()
            .tab(getTab(CreativeModeTab.TAB_DECORATIONS, LABEL_NAME))));
*/

    //soap bubbler
    public static final RegistryObject<Item> BUBBLE_BLOWER = regItem(BUBBLE_BLOWER_NAME, () -> new BubbleBlower((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, BUBBLE_BLOWER_NAME))
            .stacksTo(1).durability(250)));


    //slingshot
    public static final RegistryObject<Item> SLINGSHOT_ITEM = regItem(SLINGSHOT_NAME, () -> new SlingshotItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, SLINGSHOT_NAME))
            .stacksTo(1).durability(192))); //setISTER(() -> SlingshotItemRenderer::new)

    //flute
    public static final RegistryObject<Item> FLUTE_ITEM = regItem(FLUTE_NAME, () -> new FluteItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, FLUTE_NAME)).stacksTo(1).durability(64)));


    //key
    public static final RegistryObject<Item> KEY_ITEM = regItem(KEY_NAME, () -> new KeyItem(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_TOOLS, KEY_NAME))));

    //candy
    public static final RegistryObject<Item> CANDY_ITEM = regItem(CANDY_NAME, () -> new CandyItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_FOOD, CANDY_NAME))));

    //antique ink
    public static final RegistryObject<Item> ANTIQUE_INK = regItem(ANTIQUE_INK_NAME, () -> new Item((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_MISC, ANTIQUE_INK_NAME))));

    //wrench
    public static final RegistryObject<Item> WRENCH = regItem(WRENCH_NAME, () -> new WrenchItem((new Item.Properties())
            .tab(getTab(CreativeModeTab.TAB_TOOLS, WRENCH_NAME)).stacksTo(1).durability(200)));

    //speedometer
    /*
    public static final String SPEEDOMETER_NAME = "speedometer";
    public static final RegistryObject<Item> SPEEDOMETER_ITEM = regItem(SPEEDOMETER_NAME,()-> new SpeedometerItem(
            (new Item.Properties()).tab(null)));
    */


    //blocks

    //variants:

    //dynamic. Handled by wood set handler
    public static final Map<WoodType, HangingSignBlock> HANGING_SIGNS = new LinkedHashMap<>();

    public static final Map<WoodType, Item> HANGING_SIGNS_ITEMS = new LinkedHashMap<>();

    //keeping "hanging_sign_oak" for compatibility even if it should be just hanging_sign
    public static final RegistryObject<BlockEntityType<HangingSignBlockTile>> HANGING_SIGN_TILE = TILES
            .register(HANGING_SIGN_NAME + "_oak", () -> BlockEntityType.Builder.of(HangingSignBlockTile::new,
                    HANGING_SIGNS.values().stream().toArray(Block[]::new)).build(null));


    //sign posts
    public static final RegistryObject<Block> SIGN_POST = BLOCKS.register(SIGN_POST_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                .strength(2f, 3f)
                .sound(SoundType.WOOD)
                .noOcclusion();
        return /*CompatHandler.create ? SchematicCannonStuff.makeSignPost(p) :*/ new SignPostBlock(p);
    });
    public static final RegistryObject<BlockEntityType<SignPostBlockTile>> SIGN_POST_TILE = TILES.register(SIGN_POST_NAME, () -> BlockEntityType.Builder.of(
            SignPostBlockTile::new, SIGN_POST.get()).build(null));

    public static final Map<WoodType, SignPostItem> SIGN_POST_ITEMS = new HashMap<>();

    //flags
    public static final Map<DyeColor, RegistryObject<Block>> FLAGS = RegistryHelper.makeFlagBlocks(FLAG_NAME);
    public static final Map<DyeColor, RegistryObject<Item>> FLAGS_ITEMS = RegistryHelper.makeFlagItems(FLAG_NAME);

    public static final RegistryObject<BlockEntityType<FlagBlockTile>> FLAG_TILE = TILES
            .register(FLAG_NAME, () -> BlockEntityType.Builder.of(FlagBlockTile::new,
                    FLAGS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    //ceiling banner
    public static final Map<DyeColor, RegistryObject<Block>> CEILING_BANNERS = RegistryHelper.makeCeilingBanners(CEILING_BANNER_NAME);

    public static final RegistryObject<BlockEntityType<CeilingBannerBlockTile>> CEILING_BANNER_TILE = TILES
            .register(CEILING_BANNER_NAME, () -> BlockEntityType.Builder.of(CeilingBannerBlockTile::new,
                    CEILING_BANNERS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    //presents

    public static final Map<DyeColor, RegistryObject<Block>> PRESENTS = RegistryHelper.makePresents(PRESENT_NAME, PresentBlock::new);

    public static final RegistryObject<BlockEntityType<PresentBlockTile>> PRESENT_TILE = TILES
            .register(PRESENT_NAME, () -> BlockEntityType.Builder.of(PresentBlockTile::new,
                    PRESENTS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static final Map<DyeColor, RegistryObject<Item>> PRESENTS_ITEMS = RegistryHelper.makePresentsItems(PRESENTS, PRESENT_NAME, CreativeModeTab.TAB_DECORATIONS);

    public static final RegistryObject<MenuType<PresentContainerMenu>> PRESENT_BLOCK_CONTAINER = CONTAINERS
            .register(PRESENT_NAME, () -> IForgeMenuType.create(PresentContainerMenu::new));

    //trapped presents

    public static final Map<DyeColor, RegistryObject<Block>> TRAPPED_PRESENTS = RegistryHelper.makePresents(TRAPPED_PRESENT_NAME, TrappedPresentBlock::new);

    public static final RegistryObject<BlockEntityType<TrappedPresentBlockTile>> TRAPPED_PRESENT_TILE = TILES
            .register(TRAPPED_PRESENT_NAME, () -> BlockEntityType.Builder.of(TrappedPresentBlockTile::new,
                    TRAPPED_PRESENTS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));

    public static final Map<DyeColor, RegistryObject<Item>> TRAPPED_PRESENTS_ITEMS = RegistryHelper.makePresentsItems(TRAPPED_PRESENTS, TRAPPED_PRESENT_NAME, CreativeModeTab.TAB_REDSTONE);

    public static final RegistryObject<MenuType<TrappedPresentContainerMenu>> TRAPPED_PRESENT_BLOCK_CONTAINER = CONTAINERS
            .register(TRAPPED_PRESENT_NAME, () -> IForgeMenuType.create(TrappedPresentContainerMenu::new));


    //decoration blocks

    //planter
    public static final RegistryObject<Block> PLANTER = BLOCKS.register(PLANTER_NAME, () -> new PlanterBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED)
                    .strength(2f, 6f)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<Item> PLANTER_ITEM = regBlockItem(PLANTER, getTab(CreativeModeTab.TAB_DECORATIONS, PLANTER_NAME));

    //pedestal
    public static final RegistryObject<Block> PEDESTAL = BLOCKS.register(PEDESTAL_NAME, () -> new PedestalBlock(
            BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<BlockEntityType<PedestalBlockTile>> PEDESTAL_TILE = TILES.register(PEDESTAL_NAME, () -> BlockEntityType.Builder.of(
            PedestalBlockTile::new, PEDESTAL.get()).build(null));
    public static final RegistryObject<Item> PEDESTAL_ITEM = regBlockItem(PEDESTAL, getTab(CreativeModeTab.TAB_DECORATIONS, PEDESTAL_NAME));

    //notice board
    public static final RegistryObject<Block> NOTICE_BOARD = BLOCKS.register(NOTICE_BOARD_NAME, () -> new NoticeBoardBlock(
            BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<BlockEntityType<NoticeBoardBlockTile>> NOTICE_BOARD_TILE = TILES.register(NOTICE_BOARD_NAME, () -> BlockEntityType.Builder.of(
            NoticeBoardBlockTile::new, NOTICE_BOARD.get()).build(null));

    public static final RegistryObject<Item> NOTICE_BOARD_ITEM = ITEMS.register(NOTICE_BOARD_NAME, () -> new WoodBasedBlockItem(NOTICE_BOARD.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, NOTICE_BOARD_NAME)), 300));

    public static final RegistryObject<MenuType<NoticeBoardContainerMenu>> NOTICE_BOARD_CONTAINER = CONTAINERS
            .register(NOTICE_BOARD_NAME, () -> IForgeMenuType.create(NoticeBoardContainerMenu::new));

    //safe
    public static final RegistryObject<Block> SAFE = BLOCKS.register(SAFE_NAME, () -> new SafeBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
    ));
    public static final RegistryObject<BlockEntityType<SafeBlockTile>> SAFE_TILE = TILES.register(SAFE_NAME, () -> BlockEntityType.Builder.of(
            SafeBlockTile::new, SAFE.get()).build(null));
    public static final RegistryObject<Item> SAFE_ITEM = ITEMS.register(SAFE_NAME, () -> new SafeItem(SAFE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SAFE_NAME)).stacksTo(1).fireResistant()));

    //cage
    public static final RegistryObject<Block> CAGE = BLOCKS.register(CAGE_NAME, () -> new CageBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .sound(SoundType.METAL)
    ));
    public static final RegistryObject<BlockEntityType<CageBlockTile>> CAGE_TILE = TILES.register(CAGE_NAME, () -> BlockEntityType.Builder.of(
            CageBlockTile::new, CAGE.get()).build(null));

    public static final RegistryObject<Item> CAGE_ITEM = ITEMS.register(CAGE_NAME, () -> new CageItem(CAGE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, CAGE_NAME))
                    .stacksTo(16)));

    //jar
    public static final RegistryObject<Block> JAR = BLOCKS.register(JAR_NAME, () -> new JarBlock(
            BlockBehaviour.Properties.of(Material.GLASS, MaterialColor.NONE)
                    .strength(0.5f, 1f)
                    .sound(ModSounds.JAR)
                    .noOcclusion()
    ));

    public static final RegistryObject<BlockEntityType<JarBlockTile>> JAR_TILE = TILES.register(JAR_NAME, () -> BlockEntityType.Builder.of(
            JarBlockTile::new, JAR.get()).build(null));

    public static final RegistryObject<Item> JAR_ITEM = ITEMS.register(JAR_NAME, () -> new JarItem(JAR.get(), new Item.Properties().tab(
            getTab(CreativeModeTab.TAB_DECORATIONS, JAR_NAME)).stacksTo(16)));


    //sack
    public static final RegistryObject<Block> SACK = BLOCKS.register(SACK_NAME, () -> new SackBlock(
            BlockBehaviour.Properties.of(Material.WOOL, MaterialColor.WOOD)
                    .strength(1F)
                    .sound(ModSounds.SACK)
    ));
    public static final RegistryObject<BlockEntityType<SackBlockTile>> SACK_TILE = TILES.register(SACK_NAME, () -> BlockEntityType.Builder.of(
            SackBlockTile::new, SACK.get()).build(null));

    public static final RegistryObject<MenuType<SackContainerMenu>> SACK_CONTAINER = CONTAINERS.register(SACK_NAME, () -> IForgeMenuType.create(
            SackContainerMenu::new));

    public static final RegistryObject<Item> SACK_ITEM = regItem(SACK_NAME, () -> new SackItem(SACK.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, SACK_NAME)).stacksTo(1)));

    //blackboard
    public static final RegistryObject<Block> BLACKBOARD = BLOCKS.register(BLACKBOARD_NAME, () -> new BlackboardBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(2, 3)
    ));
    public static final RegistryObject<BlockEntityType<BlackboardBlockTile>> BLACKBOARD_TILE = TILES.register(BLACKBOARD_NAME, () -> BlockEntityType.Builder.of(
            BlackboardBlockTile::new, BLACKBOARD.get()).build(null));
    public static final RegistryObject<Item> BLACKBOARD_ITEM = ITEMS.register(BLACKBOARD_NAME, () -> new BlackboardItem(BLACKBOARD.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, BLACKBOARD_NAME))));

    //globe
    public static final RegistryObject<Block> GLOBE = BLOCKS.register(GLOBE_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<Item> GLOBE_ITEM = ITEMS.register(GLOBE_NAME, () -> new BlockItem(GLOBE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, GLOBE_NAME)).rarity(Rarity.RARE)));

    public static final RegistryObject<Block> GLOBE_SEPIA = BLOCKS.register(GLOBE_SEPIA_NAME, () -> new GlobeBlock(
            BlockBehaviour.Properties.copy(GLOBE.get())));
    public static final RegistryObject<Item> GLOBE_SEPIA_ITEM = ITEMS.register(GLOBE_SEPIA_NAME, () -> new BlockItem(GLOBE_SEPIA.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, GLOBE_SEPIA_NAME)).rarity(Rarity.RARE)));

    public static final RegistryObject<BlockEntityType<GlobeBlockTile>> GLOBE_TILE = TILES.register(GLOBE_NAME, () -> BlockEntityType.Builder.of(
            GlobeBlockTile::new, GLOBE.get(), GLOBE_SEPIA.get()).build(null));

    /*
    //candle holder
    public static final String CANDLE_HOLDER_NAME = "candle_holder";
    public static final RegistryObject<Block> CANDLE_HOLDER = BLOCKS.register(CANDLE_HOLDER_NAME, () -> new CandleHolderBlock(
            BlockBehaviour.Properties.of(Material.DECORATION)
                    .instabreak()
                    .noCollission()
                    .lightLevel((state) -> state.get(BlockStateProperties.LIT) ? 14 : 0)
                    .sound(SoundType.LANTERN), () -> ParticleTypes.FLAME));
    public static final RegistryObject<Item> CANDLE_HOLDER_ITEM = regBlockItem(CANDLE_HOLDER, getTab(CreativeModeTab.TAB_DECORATIONS, CANDLE_HOLDER_NAME));


    //candelabra
    public static final String CANDELABRA_NAME = "candelabra";
    public static final RegistryObject<Block> CANDELABRA = BLOCKS.register(CANDELABRA_NAME, () -> new CandelabraBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .lightLevel((state) -> state.get(BlockStateProperties.LIT) ? 14 : 0)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM = regBlockItem(CANDELABRA, getTab(CreativeModeTab.TAB_DECORATIONS, CANDELABRA_NAME));

    //silver
    public static final String CANDELABRA_NAME_SILVER = "candelabra_silver";
    public static final RegistryObject<Block> CANDELABRA_SILVER = BLOCKS.register(CANDELABRA_NAME_SILVER, () -> new CandelabraBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .lightLevel((state) -> state.get(BlockStateProperties.LIT) ? 14 : 0)
    ));
    public static final RegistryObject<Item> CANDELABRA_ITEM_SILVER = regBlockItem(CANDELABRA_SILVER, getTab(CreativeModeTab.TAB_DECORATIONS, CANDELABRA_NAME_SILVER));
    */

    //sconce
    //normal
    public static final RegistryObject<Block> SCONCE = BLOCKS.register(SCONCE_NAME, () -> new SconceBlock(
            BlockBehaviour.Properties.of(Material.DECORATION)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.LANTERN),
            14, () -> ParticleTypes.FLAME));
    public static final RegistryObject<Block> SCONCE_WALL = BLOCKS.register("sconce_wall", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE.get()), () -> ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM = ITEMS.register(SCONCE_NAME, () -> new StandingAndWallBlockItem(SCONCE.get(), SCONCE_WALL.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //soul
    public static final RegistryObject<Block> SCONCE_SOUL = BLOCKS.register(SCONCE_NAME_SOUL, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 10,
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_SOUL = BLOCKS.register("sconce_wall_soul", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_SOUL.get())
                    .dropsLike(SCONCE_SOUL.get()),
            () -> ParticleTypes.SOUL_FIRE_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_SOUL = ITEMS.register(SCONCE_NAME_SOUL, () -> new StandingAndWallBlockItem(SCONCE_SOUL.get(), SCONCE_WALL_SOUL.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //optional: endergetic
    public static final RegistryObject<Block> SCONCE_ENDER = BLOCKS.register(SCONCE_NAME_ENDER, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 13,
            CompatObjects.ENDER_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_ENDER = BLOCKS.register("sconce_wall_ender", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_ENDER.get()),
            CompatObjects.ENDER_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_ENDER = ITEMS.register(SCONCE_NAME_ENDER, () -> new StandingAndWallBlockItem(SCONCE_ENDER.get(), SCONCE_WALL_ENDER.get(),
            (new Item.Properties()).tab(getTab("endergetic", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //optional: infernal expansion
    public static final RegistryObject<Block> SCONCE_GLOW = BLOCKS.register(SCONCE_NAME_GLOW, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()), 13,
            CompatObjects.GLOW_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_GLOW = BLOCKS.register("sconce_wall_glow", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE.get())
                    .dropsLike(SCONCE_GLOW.get()),
            CompatObjects.GLOW_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_GLOW = ITEMS.register(SCONCE_NAME_GLOW, () -> new StandingAndWallBlockItem(SCONCE_GLOW.get(), SCONCE_WALL_GLOW.get(),
            (new Item.Properties()).tab(getTab("infernalexp", CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME))));

    //green
    public static final RegistryObject<Block> SCONCE_GREEN = BLOCKS.register(SCONCE_NAME_GREEN, () -> new SconceBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get()), 14, GREEN_FLAME));
    public static final RegistryObject<Block> SCONCE_WALL_GREEN = BLOCKS.register("sconce_wall_green", () -> new SconceWallBlock(
            BlockBehaviour.Properties.copy(SCONCE_ENDER.get())
                    .dropsLike(SCONCE_GREEN.get()), GREEN_FLAME));
    public static final RegistryObject<Item> SCONCE_ITEM_GREEN = ITEMS.register(SCONCE_NAME_GREEN, () -> new StandingAndWallBlockItem(SCONCE_GREEN.get(), SCONCE_WALL_GREEN.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, SCONCE_NAME_GREEN))));

    //copper lantern
    public static final RegistryObject<Block> COPPER_LANTERN = BLOCKS.register(COPPER_LANTERN_NAME, () -> new CopperLanternBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> state.get(LightableLanternBlock.LIT) ? 15 : 0)
                    //TODO: add custom sound mixed
                    .sound(SoundType.COPPER)
    ));
    public static final RegistryObject<Item> COPPER_LANTERN_ITEM = regBlockItem(COPPER_LANTERN, getTab(CreativeModeTab.TAB_DECORATIONS, COPPER_LANTERN_NAME));

    //brass lantern
    public static final RegistryObject<Block> BRASS_LANTERN = BLOCKS.register(BRASS_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get()),
            Shapes.or(Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D),
                    Block.createCuboidShape(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D),
                    Block.createCuboidShape(4.0D, 7.0D, 4.0D, 12.0D, 8.0D, 12.0D))));

    public static final RegistryObject<Item> BRASS_LANTERN_ITEM = regBlockItem(BRASS_LANTERN,
            getTab(CreativeModeTab.TAB_DECORATIONS, BRASS_LANTERN_NAME), "forge:ingots/brass");

    //crimson lantern
    public static final RegistryObject<Block> CRIMSON_LANTERN = BLOCKS.register(CRIMSON_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_RED)
                    .strength(1.5f)
                    .sound(SoundType.WOOL)
                    .lightLevel((state) -> 15)
                    .noOcclusion(),
            Shapes.or(Block.createCuboidShape(4.0D, 1.0D, 4.0D, 12.0D, 8.0D, 12.0D),
                    Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 9.0D, 10.0D))
    ));
    public static final RegistryObject<Item> CRIMSON_LANTERN_ITEM = regBlockItem(CRIMSON_LANTERN, getTab(CreativeModeTab.TAB_DECORATIONS, CRIMSON_LANTERN_NAME));

    //silver lantern
    public static final RegistryObject<Block> SILVER_LANTERN = BLOCKS.register(SILVER_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get()),
            Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D)));

    public static final RegistryObject<Item> SILVER_LANTERN_ITEM = regBlockItem(SILVER_LANTERN,
            getTab(CreativeModeTab.TAB_DECORATIONS, SILVER_LANTERN_NAME), "forge:ingots/silver");

    //lead lantern
    public static final RegistryObject<Block> LEAD_LANTERN = BLOCKS.register(LEAD_LANTERN_NAME, () -> new LightableLanternBlock(
            BlockBehaviour.Properties.copy(COPPER_LANTERN.get()),
            Shapes.or(Block.createCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 7.0D, 12.0D),
                    Block.createCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D))));

    public static final RegistryObject<Item> LEAD_LANTERN_ITEM = regBlockItem(LEAD_LANTERN,
            getTab(CreativeModeTab.TAB_DECORATIONS, LEAD_LANTERN_NAME), "forge:ingots/lead");


    //rope
    public static final RegistryObject<Block> ROPE = BLOCKS.register(ROPE_NAME, () -> new RopeBlock(
            BlockBehaviour.Properties.of(Material.WOOL)
                    .sound(ModSounds.ROPE)
                    .strength(0.25f)
                    .speedFactor(0.7f)
                    .noOcclusion()));
    public static final RegistryObject<Item> ROPE_ITEM = ITEMS.register(ROPE_NAME, () -> new RopeItem(ROPE.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, ROPE_NAME))));

    public static final RegistryObject<Block> ROPE_KNOT = BLOCKS.register(ROPE_KNOT_NAME, () -> new RopeKnotBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));

    public static final RegistryObject<BlockEntityType<RopeKnotBlockTile>> ROPE_KNOT_TILE = TILES.register(ROPE_KNOT_NAME, () -> BlockEntityType.Builder.of(
            RopeKnotBlockTile::new, ROPE_KNOT.get()).build(null));

    //spikes
    public static final RegistryObject<Block> BAMBOO_SPIKES = BLOCKS.register(BAMBOO_SPIKES_NAME, () -> new BambooSpikesBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.SAND)
                    .sound(SoundType.SCAFFOLDING)
                    .isRedstoneConductor((a, b, c) -> false)
                    .strength(2)
                    .noOcclusion()));
    public static final RegistryObject<BlockEntityType<BambooSpikesBlockTile>> BAMBOO_SPIKES_TILE = TILES.register(BAMBOO_SPIKES_NAME, () -> BlockEntityType.Builder.of(
            BambooSpikesBlockTile::new, BAMBOO_SPIKES.get()).build(null));

    public static final RegistryObject<Item> BAMBOO_SPIKES_ITEM = ITEMS.register(BAMBOO_SPIKES_NAME, () -> new BambooSpikesItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, BAMBOO_SPIKES_NAME))));

    public static final RegistryObject<Item> BAMBOO_SPIKES_TIPPED_ITEM = ITEMS.register(TIPPED_SPIKES_NAME, () -> new BambooSpikesTippedItem(BAMBOO_SPIKES.get(),
            (new Item.Properties()).defaultDurability(BambooSpikesBlockTile.MAX_CHARGES).setNoRepair().tab(getTab(CreativeModeTab.TAB_BREWING, TIPPED_SPIKES_NAME))));

    //goblet
    public static final RegistryObject<Block> GOBLET = BLOCKS.register(GOBLET_NAME, () -> new GobletBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(1.5f, 2f)
                    .sound(SoundType.METAL)));

    public static final RegistryObject<Item> GOBLET_ITEM = regBlockItem(GOBLET, getTab(CreativeModeTab.TAB_DECORATIONS, GOBLET_NAME));

    public static final RegistryObject<BlockEntityType<GobletBlockTile>> GOBLET_TILE = TILES.register(GOBLET_NAME, () -> BlockEntityType.Builder.of(
            GobletBlockTile::new, GOBLET.get()).build(null));

    //hourglass
    public static final RegistryObject<Block> HOURGLASS = BLOCKS.register(HOURGLASS_NAME, () -> new HourGlassBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.GOLD)
                    .sound(SoundType.METAL)
                    .strength(2, 4)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<BlockEntityType<HourGlassBlockTile>> HOURGLASS_TILE = TILES.register(HOURGLASS_NAME, () -> BlockEntityType.Builder.of(
            HourGlassBlockTile::new, HOURGLASS.get()).build(null));
    public static final RegistryObject<Item> HOURGLASS_ITEM = regBlockItem(HOURGLASS, getTab(CreativeModeTab.TAB_DECORATIONS, HOURGLASS_NAME));

    //item shelf
    public static final RegistryObject<Block> ITEM_SHELF = BLOCKS.register(ITEM_SHELF_NAME, () -> new ItemShelfBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .sound(SoundType.WOOD)
                    .strength(0.75f, 0.1f)
                    .noOcclusion()
                    .noCollission()
    ));
    public static final RegistryObject<BlockEntityType<ItemShelfBlockTile>> ITEM_SHELF_TILE = TILES.register(ITEM_SHELF_NAME, () -> BlockEntityType.Builder.of(
            ItemShelfBlockTile::new, ITEM_SHELF.get()).build(null));
    public static final RegistryObject<Item> ITEM_SHELF_ITEM = ITEMS.register(ITEM_SHELF_NAME, () -> new WoodBasedBlockItem(ITEM_SHELF.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_DECORATIONS, ITEM_SHELF_NAME)), 100));

    //doormat
    public static final RegistryObject<Block> DOORMAT = BLOCKS.register(DOORMAT_NAME, () -> new DoormatBlock(
            BlockBehaviour.Properties.of(Material.CLOTH_DECORATION, MaterialColor.COLOR_YELLOW)
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<DoormatBlockTile>> DOORMAT_TILE = TILES.register(DOORMAT_NAME, () -> BlockEntityType.Builder.of(
            DoormatBlockTile::new, DOORMAT.get()).build(null));
    public static final RegistryObject<Item> DOORMAT_ITEM = ITEMS.register(DOORMAT_NAME, () -> new WoodBasedBlockItem(DOORMAT.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, DOORMAT_NAME)), 134));

    //magma cream block
    //public static final RegistryObject<Block> MAGMA_CREAM_BLOCK = BLOCKS.register(MAGMA_CREAM_BLOCK_NAME, () -> new MagmaCreamBlock(
    //        BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)));
    //public static final RegistryObject<Item> MAGMA_CREAM_BLOCK_ITEM = ITEMS.register(MAGMA_CREAM_BLOCK_NAME, () -> new BlockItem(MAGMA_CREAM_BLOCK.get(),
    //        (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, MAGMA_CREAM_BLOCK_NAME))));

    //raked gravel
    public static final RegistryObject<Block> RAKED_GRAVEL = BLOCKS.register(RAKED_GRAVEL_NAME, () -> new RakedGravelBlock(
            BlockBehaviour.Properties.copy(Blocks.GRAVEL)
                    .isViewBlocking((w, s, p) -> true)
                    .isSuffocating((w, s, p) -> true)));

    public static final RegistryObject<Item> RAKED_GRAVEL_ITEM = regBlockItem(RAKED_GRAVEL, getTab(CreativeModeTab.TAB_DECORATIONS, RAKED_GRAVEL_NAME));


    //redstone blocks

    //cog block
    public static final RegistryObject<Block> COG_BLOCK = BLOCKS.register(COG_BLOCK_NAME, () -> new CogBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 6f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
    ));
    public static final RegistryObject<Item> COG_BLOCK_ITEM = regBlockItem(COG_BLOCK, getTab(CreativeModeTab.TAB_REDSTONE, COG_BLOCK_NAME));

    //piston launcher base
    public static final RegistryObject<Block> SPRING_LAUNCHER = BLOCKS.register(SPRING_LAUNCHER_NAME, () -> new SpringLauncherBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .isRedstoneConductor((state, reader, pos) -> !state.get(SpringLauncherBlock.EXTENDED))
                    .isSuffocating((state, reader, pos) -> !state.get(SpringLauncherBlock.EXTENDED))
                    .isViewBlocking((state, reader, pos) -> !state.get(SpringLauncherBlock.EXTENDED))
    ));
    public static final RegistryObject<Item> PISTON_LAUNCHER_ITEM = regBlockItem(SPRING_LAUNCHER, getTab(CreativeModeTab.TAB_REDSTONE, SPRING_LAUNCHER_NAME));

    public static final RegistryObject<Block> SPRING_LAUNCHER_HEAD = BLOCKS.register(PISTON_LAUNCHER_HEAD_NAME, () -> new SpringLauncherHeadBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(4f, 5f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noDrops()
                    .jumpFactor(1.18f)
    ));
    public static final RegistryObject<Block> SPRING_LAUNCHER_ARM = BLOCKS.register(PISTON_LAUNCHER_ARM_NAME, () -> new SpringLauncherArmBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(50f, 50f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .noDrops()
    ));
    public static final RegistryObject<BlockEntityType<SpringLauncherArmBlockTile>> SPRING_LAUNCHER_ARM_TILE = TILES.register(PISTON_LAUNCHER_ARM_NAME, () -> BlockEntityType.Builder.of(
            SpringLauncherArmBlockTile::new, SPRING_LAUNCHER_ARM.get()).build(null));

    //speaker Block
    public static final RegistryObject<SpeakerBlock> SPEAKER_BLOCK = BLOCKS.register(SPEAKER_BLOCK_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                .strength(1f, 2f)
                .sound(SoundType.WOOD);
        return CompatHandler.computercraft ? CCPlugin.makeSpeaker(p) : new SpeakerBlock(p);
    });

    public static final RegistryObject<BlockEntityType<?>> SPEAKER_BLOCK_TILE = TILES.register(SPEAKER_BLOCK_NAME, () -> BlockEntityType.Builder.of(
            SpeakerBlockTile::new, SPEAKER_BLOCK.get()).build(null));

    public static final RegistryObject<Item> SPEAKER_BLOCK_ITEM = ITEMS.register(SPEAKER_BLOCK_NAME, () -> new WoodBasedBlockItem(SPEAKER_BLOCK.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_REDSTONE, SPEAKER_BLOCK_NAME)), 300));

    //turn table
    public static final RegistryObject<Block> TURN_TABLE = BLOCKS.register(TURN_TABLE_NAME, () -> new TurnTableBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.STONE)
                    .strength(0.75f, 2f)
                    .sound(SoundType.STONE)
    ));
    public static final RegistryObject<BlockEntityType<TurnTableBlockTile>> TURN_TABLE_TILE = TILES.register(TURN_TABLE_NAME, () -> BlockEntityType.Builder.of(
            TurnTableBlockTile::new, TURN_TABLE.get()).build(null));

    public static final RegistryObject<Item> TURN_TABLE_ITEM = regBlockItem(TURN_TABLE, getTab(CreativeModeTab.TAB_REDSTONE, TURN_TABLE_NAME));

    //illuminator
    public static final RegistryObject<Block> REDSTONE_ILLUMINATOR = BLOCKS.register(REDSTONE_ILLUMINATOR_NAME, () -> new RedstoneIlluminatorBlock(
            BlockBehaviour.Properties.of(Material.BUILDABLE_GLASS, MaterialColor.QUARTZ)
                    .strength(0.3f, 0.3f)
                    .sound(SoundType.GLASS)
    ));
    public static final RegistryObject<Item> REDSTONE_ILLUMINATOR_ITEM = regBlockItem(REDSTONE_ILLUMINATOR, getTab(CreativeModeTab.TAB_REDSTONE, REDSTONE_ILLUMINATOR_NAME));

    //pulley
    public static final RegistryObject<Block> PULLEY_BLOCK = BLOCKS.register(PULLEY_BLOCK_NAME, () -> new PulleyBlock(
            BlockBehaviour.Properties.copy(Blocks.BARREL)));
    public static final RegistryObject<Item> PULLEY_BLOCK_ITEM = regBlockItem(PULLEY_BLOCK, getTab(CreativeModeTab.TAB_DECORATIONS, PULLEY_BLOCK_NAME), 300);

    public static final RegistryObject<MenuType<PulleyBlockContainerMenu>> PULLEY_BLOCK_CONTAINER = CONTAINERS
            .register(PULLEY_BLOCK_NAME, () -> IForgeMenuType.create(PulleyBlockContainerMenu::new));
    public static final RegistryObject<BlockEntityType<PulleyBlockTile>> PULLEY_BLOCK_TILE = TILES.register(PULLEY_BLOCK_NAME, () -> BlockEntityType.Builder.of(
            PulleyBlockTile::new, PULLEY_BLOCK.get()).build(null));

    //lock block
    public static final RegistryObject<Block> LOCK_BLOCK = BLOCKS.register(LOCK_BLOCK_NAME, () -> new LockBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL))
    );
    public static final RegistryObject<Item> LOCK_BLOCK_ITEM = regBlockItem(LOCK_BLOCK, getTab(CreativeModeTab.TAB_REDSTONE, LOCK_BLOCK_NAME));

    //bellows
    public static final RegistryObject<Block> BELLOWS = BLOCKS.register(BELLOWS_NAME, () -> new BellowsBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(3f, 3f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<BellowsBlockTile>> BELLOWS_TILE = TILES.register(BELLOWS_NAME, () -> BlockEntityType.Builder.of(
            BellowsBlockTile::new, BELLOWS.get()).build(null));
    public static final RegistryObject<Item> BELLOWS_ITEM = ITEMS.register(BELLOWS_NAME, () -> new WoodBasedBlockItem(BELLOWS.get(),
            new Item.Properties().tab(getTab(CreativeModeTab.TAB_REDSTONE, BELLOWS_NAME)), 300));

    //clock
    public static final RegistryObject<Block> CLOCK_BLOCK = BLOCKS.register(CLOCK_BLOCK_NAME, () -> new ClockBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.COLOR_BROWN)
                    .strength(3f, 6f)
                    .sound(SoundType.WOOD)
                    .lightLevel((state) -> 1)
    ));
    public static final RegistryObject<BlockEntityType<ClockBlockTile>> CLOCK_BLOCK_TILE = TILES.register(CLOCK_BLOCK_NAME, () -> BlockEntityType.Builder.of(
            ClockBlockTile::new, CLOCK_BLOCK.get()).build(null));

    public static final RegistryObject<Item> CLOCK_BLOCK_ITEM = regBlockItem(CLOCK_BLOCK, getTab(CreativeModeTab.TAB_REDSTONE, CLOCK_BLOCK_NAME));

    //sconce lever
    public static final RegistryObject<Block> SCONCE_LEVER = BLOCKS.register(SCONCE_LEVER_NAME, () -> new SconceLeverBlock(
            BlockBehaviour.Properties.copy(SCONCE.get()),
            () -> ParticleTypes.FLAME));
    public static final RegistryObject<Item> SCONCE_LEVER_ITEM = regBlockItem(SCONCE_LEVER, getTab(CreativeModeTab.TAB_REDSTONE, SCONCE_LEVER_NAME));

    //crank
    public static final RegistryObject<Block> CRANK = BLOCKS.register(CRANK_NAME, () -> new CrankBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.NONE)
                    .strength(0.6f, 0.6f)
                    .noCollission()
                    .noOcclusion()
    ));
    public static final RegistryObject<Item> CRANK_ITEM = regBlockItem(CRANK, getTab(CreativeModeTab.TAB_REDSTONE, CRANK_NAME));

    //wind vane
    public static final RegistryObject<Block> WIND_VANE = BLOCKS.register(WIND_VANE_NAME, () -> new WindVaneBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(5f, 6f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<WindVaneBlockTile>> WIND_VANE_TILE = TILES.register(WIND_VANE_NAME, () -> BlockEntityType.Builder.of(
            WindVaneBlockTile::new, WIND_VANE.get()).build(null));

    public static final RegistryObject<Item> WIND_VANE_ITEM = regBlockItem(WIND_VANE, getTab(CreativeModeTab.TAB_REDSTONE, WIND_VANE_NAME));

    //faucet
    public static final RegistryObject<Block> FAUCET = BLOCKS.register(FAUCET_NAME, () -> new FaucetBlock(
            BlockBehaviour.Properties.of(Material.METAL, MaterialColor.METAL)
                    .strength(3f, 4.8f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    ));
    public static final RegistryObject<BlockEntityType<FaucetBlockTile>> FAUCET_TILE = TILES.register(FAUCET_NAME, () -> BlockEntityType.Builder.of(
            FaucetBlockTile::new, FAUCET.get()).build(null));

    public static final RegistryObject<Item> FAUCET_ITEM = regBlockItem(FAUCET, getTab(CreativeModeTab.TAB_REDSTONE, FAUCET_NAME));

    //gold door
    public static final RegistryObject<Block> GOLD_DOOR = BLOCKS.register(GOLD_DOOR_NAME, () -> new GoldDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion()));
    public static final RegistryObject<Item> GOLD_DOOR_ITEM = regBlockItem(GOLD_DOOR, getTab(CreativeModeTab.TAB_REDSTONE, GOLD_DOOR_NAME));

    //gold trapdoor
    public static final RegistryObject<Block> GOLD_TRAPDOOR = BLOCKS.register(GOLD_TRAPDOOR_NAME, () -> new GoldTrapdoorBlock(
            BlockBehaviour.Properties.copy(GOLD_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)));
    public static final RegistryObject<Item> GOLD_TRAPDOOR_ITEM = regBlockItem(GOLD_TRAPDOOR, getTab(CreativeModeTab.TAB_REDSTONE, GOLD_TRAPDOOR_NAME));

    //silver door
    public static final RegistryObject<Block> SILVER_DOOR = BLOCKS.register(SILVER_DOOR_NAME, () -> new SilverDoorBlock(
            BlockBehaviour.Properties.of(Material.METAL)
                    .strength(4.0F, 5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    public static final RegistryObject<Item> SILVER_DOOR_ITEM = regBlockItem(SILVER_DOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, SILVER_DOOR_NAME), "forge:ingots/silver");

    //silver trapdoor
    public static final RegistryObject<Block> SILVER_TRAPDOOR = BLOCKS.register(SILVER_TRAPDOOR_NAME, () -> new SilverTrapdoorBlock(
            BlockBehaviour.Properties.copy(SILVER_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)));
    public static final RegistryObject<Item> SILVER_TRAPDOOR_ITEM = regBlockItem(SILVER_TRAPDOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, SILVER_TRAPDOOR_NAME), "forge:ingots/silver");

    //lead door
    public static final RegistryObject<Block> LEAD_DOOR = BLOCKS.register(LEAD_DOOR_NAME, () -> new LeadDoorBlock(
            BlockBehaviour.Properties.of(Material.METAL)
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()));
    public static final RegistryObject<Item> LEAD_DOOR_ITEM = regBlockItem(LEAD_DOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, LEAD_DOOR_NAME), "forge:ingots/lead");

    //lead trapdoor
    public static final RegistryObject<Block> LEAD_TRAPDOOR = BLOCKS.register(LEAD_TRAPDOOR_NAME, () -> new LeadTrapdoorBlock(
            BlockBehaviour.Properties.copy(LEAD_DOOR.get())
                    .isValidSpawn((a, b, c, d) -> false)));
    public static final RegistryObject<Item> LEAD_TRAPDOOR_ITEM = regBlockItem(LEAD_TRAPDOOR,
            getTab(CreativeModeTab.TAB_REDSTONE, LEAD_TRAPDOOR_NAME), "forge:ingots/lead");


    //netherite doors
    public static final RegistryObject<Block> NETHERITE_DOOR = BLOCKS.register(NETHERITE_DOOR_NAME, () -> new NetheriteDoorBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
    ));
    public static final RegistryObject<Item> NETHERITE_DOOR_ITEM = ITEMS.register(NETHERITE_DOOR_NAME, () -> new BlockItem(NETHERITE_DOOR.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_REDSTONE, NETHERITE_DOOR_NAME)).fireResistant()));

    //netherite trapdoor
    public static final RegistryObject<Block> NETHERITE_TRAPDOOR = BLOCKS.register(NETHERITE_TRAPDOOR_NAME, () -> new NetheriteTrapdoorBlock(
            BlockBehaviour.Properties.copy(NETHERITE_DOOR.get())
                    .noOcclusion()
                    .isValidSpawn((a, b, c, d) -> false)
    ));
    public static final RegistryObject<Item> NETHERITE_TRAPDOOR_ITEM = ITEMS.register(NETHERITE_TRAPDOOR_NAME, () -> new BlockItem(NETHERITE_TRAPDOOR.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_REDSTONE, NETHERITE_TRAPDOOR_NAME)).fireResistant()));

    public static final RegistryObject<BlockEntityType<KeyLockableTile>> KEY_LOCKABLE_TILE = TILES.register("key_lockable_tile", () -> BlockEntityType.Builder.of(
            KeyLockableTile::new, NETHERITE_DOOR.get(), NETHERITE_TRAPDOOR.get(), LOCK_BLOCK.get()).build(null));

    //iron gate
    public static final RegistryObject<Block> IRON_GATE = BLOCKS.register(IRON_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), false));
    public static final RegistryObject<Item> IRON_GATE_ITEM = regBlockItem(IRON_GATE, getTab(CreativeModeTab.TAB_REDSTONE, IRON_GATE_NAME));

    //gold gate
    public static final RegistryObject<Block> GOLD_GATE = BLOCKS.register(GOLD_GATE_NAME, () -> new IronGateBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_BARS), true));
    public static final RegistryObject<Item> GOLD_GATE_ITEM = regBlockItem(GOLD_GATE, getTab("quark", CreativeModeTab.TAB_REDSTONE, IRON_GATE_NAME));


    //wall lantern
    public static final RegistryObject<WallLanternBlock> WALL_LANTERN = BLOCKS.register(WALL_LANTERN_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.LANTERN)
                .lightLevel((state) -> 15).noDrops();

        return /*CompatHandler.create ? SchematicCannonStuff.makeWallLantern(p):*/  new WallLanternBlock(p);
    });
    public static final RegistryObject<BlockEntityType<WallLanternBlockTile>> WALL_LANTERN_TILE = TILES.register(WALL_LANTERN_NAME, () -> BlockEntityType.Builder.of(
            WallLanternBlockTile::new, WALL_LANTERN.get()).build(null));


    //hanging flower pot
    public static final RegistryObject<Block> HANGING_FLOWER_POT = regPlaceableItem(HANGING_FLOWER_POT_NAME,
            () -> new HangingFlowerPotBlock(BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)),
            () -> Items.FLOWER_POT, ServerConfigs.tweaks.HANGING_POT_PLACEMENT);
    public static final RegistryObject<BlockEntityType<HangingFlowerPotBlockTile>> HANGING_FLOWER_POT_TILE = TILES.register(HANGING_FLOWER_POT_NAME, () -> BlockEntityType.Builder.of(
            HangingFlowerPotBlockTile::new, HANGING_FLOWER_POT.get()).build(null));

    //double cake
    public static final RegistryObject<Block> DOUBLE_CAKE = BLOCKS.register(DOUBLE_CAKE_NAME, () -> new DoubleCakeBlock(
            BlockBehaviour.Properties.copy(Blocks.CAKE)
    ));
    //directional cake
    public static final RegistryObject<Block> DIRECTIONAL_CAKE = BLOCKS.register(DIRECTIONAL_CAKE_NAME, () -> new DirectionalCakeBlock(
            BlockBehaviour.Properties.copy(Blocks.CAKE)
                    .dropsLike(Blocks.CAKE)
    ));

    //checker block
    public static final RegistryObject<Block> CHECKER_BLOCK = BLOCKS.register(CHECKER_BLOCK_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F, 6.0F))
    );
    public static final RegistryObject<Item> CHECKER_BLOCK_ITEM = ITEMS.register(CHECKER_BLOCK_NAME, () -> new BlockItem(CHECKER_BLOCK.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, CHECKER_BLOCK_NAME))
    ));
    //slab
    public static final RegistryObject<Block> CHECKER_SLAB = BLOCKS.register(CHECKER_SLAB_NAME, () -> new SlabBlock(
            BlockBehaviour.Properties.copy(CHECKER_BLOCK.get()))
    );
    public static final RegistryObject<Item> CHECKER_SLAB_ITEM = ITEMS.register(CHECKER_SLAB_NAME, () -> new BlockItem(CHECKER_SLAB.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, CHECKER_BLOCK_NAME))
    ));
    //vertical slab
    public static final RegistryObject<Block> CHECKER_VERTICAL_SLAB = BLOCKS.register(CHECKER_VERTICAL_SLAB_NAME, () -> new VerticalSlabBlock(
            BlockBehaviour.Properties.copy(CHECKER_BLOCK.get()))
    );
    public static final RegistryObject<Item> CHECKER_VERTICAL_SLAB_ITEM = ITEMS.register(CHECKER_VERTICAL_SLAB_NAME, () -> new BlockItem(CHECKER_VERTICAL_SLAB.get(),
            (new Item.Properties()).tab(getTab("quark", CreativeModeTab.TAB_BUILDING_BLOCKS, CHECKER_BLOCK_NAME))
    ));

    //pancakes
    public static final RegistryObject<Block> PANCAKE = BLOCKS.register(PANCAKE_NAME, () -> new PancakeBlock(
            BlockBehaviour.Properties.of(Material.CAKE, MaterialColor.TERRACOTTA_ORANGE)
                    .strength(0.5F)
                    .sound(SoundType.WOOL))
    );
    public static final RegistryObject<Item> PANCAKE_ITEM = ITEMS.register(PANCAKE_NAME, () -> new PancakeItem(PANCAKE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_FOOD, PANCAKE_NAME))
    ));
    public static final RegistryObject<Item> PANCAKE_DISC = ITEMS.register("pancake_disc",
            () -> new RecordItem(15, ModSounds.PANCAKE_MUSIC, new Item.Properties().tab(null)
            ));

    //flax
    public static final RegistryObject<Block> FLAX = BLOCKS.register(FLAX_NAME, () -> new FlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.ROSE_BUSH)
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP))
    );

    public static final RegistryObject<Item> FLAX_ITEM = ITEMS.register(FLAX_NAME, () -> new Item(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, FLAX_NAME))));

    public static final RegistryObject<Item> FLAX_SEEDS_ITEM = ITEMS.register("flax_seeds", () -> new ItemNameBlockItem(FLAX.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, FLAX_NAME))));

    public static final RegistryObject<Block> FLAX_WILD = BLOCKS.register(FLAX_WILD_NAME, () -> new WildFlaxBlock(
            BlockBehaviour.Properties.copy(Blocks.TALL_GRASS))
    );
    public static final RegistryObject<Item> FLAX_WILD_ITEM = ITEMS.register(FLAX_WILD_NAME, () -> new BlockItem(FLAX_WILD.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_DECORATIONS, FLAX_WILD_NAME))));

    //pot
    public static final RegistryObject<Block> FLAX_POT = BLOCKS.register("potted_flax", () -> new FlowerPotBlock(
            () -> (FlowerPotBlock) Blocks.FLOWER_POT, FLAX, BlockBehaviour.Properties.copy(Blocks.FLOWER_POT)));

    //fodder
    public static final RegistryObject<Block> FODDER = BLOCKS.register(FODDER_NAME, () -> new FodderBlock(
            BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK)));
    public static final RegistryObject<Item> FODDER_ITEM = ITEMS.register(FODDER_NAME, () -> new BlockItem(FODDER.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FODDER_NAME))));


    //flax block
    public static final RegistryObject<Block> FLAX_BLOCK = BLOCKS.register(FLAX_BLOCK_NAME, () -> new FlaxBaleBlock(
            BlockBehaviour.Properties.of(Material.GRASS, MaterialColor.TERRACOTTA_LIGHT_GREEN)
                    .strength(0.5F)
                    .sound(SoundType.GRASS)));
    public static final RegistryObject<Item> FLAX_BLOCK_ITEM = ITEMS.register(FLAX_BLOCK_NAME, () -> new BlockItem(FLAX_BLOCK.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FLAX_NAME))));

    //boat in a jar
    public static final RegistryObject<Block> JAR_BOAT = BLOCKS.register(JAR_BOAT_NAME, () -> new JarBoatBlock(
            BlockBehaviour.Properties.copy(Blocks.GLASS)));
    public static final RegistryObject<BlockEntityType<JarBoatTile>> JAR_BOAT_TILE = TILES.register(JAR_BOAT_NAME, () -> BlockEntityType.Builder.of(
            JarBoatTile::new, JAR_BOAT.get()).build(null));
    public static final RegistryObject<Item> JAR_BOAT_ITEM = ITEMS.register(JAR_BOAT_NAME, () -> new BlockItem(JAR_BOAT.get(),
            (new Item.Properties()).tab(null)));

    //block generator
    public static final RegistryObject<Block> STRUCTURE_TEMP = BLOCKS.register(STRUCTURE_TEMP_NAME, () -> new StructureTempBlock(
            BlockBehaviour.Properties.of(Material.STONE).strength(0).noDrops().noCollission().noOcclusion()));
    public static final RegistryObject<BlockEntityType<StructureTempBlockTile>> STRUCTURE_TEMP_TILE = TILES.register(STRUCTURE_TEMP_NAME, () -> BlockEntityType.Builder.of(
            StructureTempBlockTile::new, STRUCTURE_TEMP.get()).build(null));

    public static final RegistryObject<BlockPlacerItem> BLOCK_PLACER = ITEMS.register("placeable_item", () -> new BlockPlacerItem(STRUCTURE_TEMP.get(),
            (new Item.Properties()).tab(null)));

    public static final RegistryObject<Block> BLOCK_GENERATOR = BLOCKS.register(BLOCK_GENERATOR_NAME, () -> new BlockGeneratorBlock(
            BlockBehaviour.Properties.copy(STRUCTURE_TEMP.get()).lightLevel((s) -> 14)));
    public static final RegistryObject<BlockEntityType<BlockGeneratorBlockTile>> BLOCK_GENERATOR_TILE = TILES.register(BLOCK_GENERATOR_NAME, () -> BlockEntityType.Builder.of(
            BlockGeneratorBlockTile::new, BLOCK_GENERATOR.get()).build(null));

    //sticks
    public static final RegistryObject<Block> STICK_BLOCK = regPlaceableItem(STICK_NAME, () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD), 60), () -> Items.STICK, ServerConfigs.tweaks.PLACEABLE_STICKS);
    public static final RegistryObject<Block> EDELWOOD_STICK_BLOCK = regPlaceableItem("edelwood_stick", () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_BROWN)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD)), "forbidden_arcanus:edelwood_stick", ServerConfigs.tweaks.PLACEABLE_STICKS);
    public static final RegistryObject<Block> PRISMARINE_ROD_BLOCK = regPlaceableItem("prismarine_rod", () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_CYAN)
                    .strength(0.25F, 0F)
                    .sound(SoundType.STONE), 0), "upgrade_aquatic:prismarine_rod", ServerConfigs.tweaks.PLACEABLE_STICKS);
    public static final RegistryObject<Block> PROPELPLANT_ROD_BLOCK = regPlaceableItem("propelplant_cane", () -> new StickBlock(
            BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.CRIMSON_STEM)
                    .strength(0.25F, 0F)
                    .sound(SoundType.WOOD)), "nethers_delight:propelplant_cane", ServerConfigs.tweaks.PLACEABLE_STICKS);

    //blaze rod
    //TODO: blaze sound
    public static final RegistryObject<Block> BLAZE_ROD_BLOCK = regPlaceableItem(BLAZE_ROD_NAME, () -> new BlazeRodBlock(
                    BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                            .strength(0.25F, 0F)
                            .lightLevel(state -> 12)
                            .emissiveRendering((p, w, s) -> true)
                            .sound(SoundType.GILDED_BLACKSTONE)),
            () -> Items.BLAZE_ROD, ServerConfigs.tweaks.PLACEABLE_RODS
    );

    //daub
    public static final RegistryObject<Block> DAUB = BLOCKS.register(DAUB_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
                    .strength(1.5f, 3f)
    ));
    public static final RegistryObject<Item> DAUB_ITEM = ITEMS.register(DAUB_NAME, () -> new BlockItem(DAUB.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))
    ));
    //wattle and daub
    //frame
    public static final RegistryObject<Block> DAUB_FRAME = BLOCKS.register(DAUB_FRAME_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())));
    public static final RegistryObject<Item> DAUB_FRAME_ITEM = ITEMS.register(DAUB_FRAME_NAME, () -> new BlockItem(DAUB_FRAME.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))));
    //brace
    public static final RegistryObject<Block> DAUB_BRACE = BLOCKS.register(DAUB_BRACE_NAME, () -> new FlippedBlock(
            BlockBehaviour.Properties.copy(DAUB.get())));
    public static final RegistryObject<Item> DAUB_BRACE_ITEM = ITEMS.register(DAUB_BRACE_NAME, () -> new BlockItem(DAUB_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))));
    //cross brace
    public static final RegistryObject<Block> DAUB_CROSS_BRACE = BLOCKS.register(DAUB_CROSS_BRACE_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(DAUB.get())));
    public static final RegistryObject<Item> DAUB_CROSS_BRACE_ITEM = ITEMS.register(DAUB_CROSS_BRACE_NAME, () -> new BlockItem(DAUB_CROSS_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DAUB_NAME))));
    //timber frame
    public static final RegistryObject<Block> TIMBER_FRAME = BLOCKS.register(TIMBER_FRAME_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(0.1f, 0f).dynamicShape().sound(SoundType.SCAFFOLDING);
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_FRAME) :*/ new FrameBlock(p, DAUB_FRAME);
    });
    public static final RegistryObject<Item> TIMBER_FRAME_ITEM = ITEMS.register(TIMBER_FRAME_NAME, () -> new TimberFrameItem(TIMBER_FRAME.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    //timber brace
    public static final RegistryObject<Block> TIMBER_BRACE = BLOCKS.register(TIMBER_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFrameBraceBlock(p, DAUB_BRACE) :*/ new FrameBraceBlock(p, DAUB_BRACE);
    });
    public static final RegistryObject<Item> TIMBER_BRACE_ITEM = ITEMS.register(TIMBER_BRACE_NAME, () -> new TimberFrameItem(TIMBER_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));

    //timber cross brace
    public static final RegistryObject<Block> TIMBER_CROSS_BRACE = BLOCKS.register(TIMBER_CROSS_BRACE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(TIMBER_FRAME.get());
        return /*CompatHandler.create ? SchematicCannonStuff.makeFramedBlock(p, DAUB_CROSS_BRACE) :*/ new FrameBlock(p, DAUB_CROSS_BRACE);
    });
    public static final RegistryObject<Item> TIMBER_CROSS_BRACE_ITEM = ITEMS.register(TIMBER_CROSS_BRACE_NAME, () -> new TimberFrameItem(TIMBER_CROSS_BRACE.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, TIMBER_FRAME_NAME))));
    public static final RegistryObject<BlockEntityType<FrameBlockTile>> TIMBER_FRAME_TILE = TILES.register(TIMBER_FRAME_NAME, () -> BlockEntityType.Builder.of(
            FrameBlockTile::new, TIMBER_FRAME.get(), TIMBER_CROSS_BRACE.get(), TIMBER_BRACE.get()).build(null));

    //ashen bricks
    public static EnumMap<BlockRegistryHelper.VariantType, RegistryObject<Block>> ASH_BRICKS_BLOCKS =
            BlockRegistryHelper.registerFullBlockSet(BLOCKS, ITEMS, ASH_BRICKS_NAME, Blocks.STONE_BRICKS, isDisabled(ASH_BRICKS_NAME));

    //stone tile
    public static EnumMap<BlockRegistryHelper.VariantType, RegistryObject<Block>> STONE_TILE_BLOCKS =
            BlockRegistryHelper.registerFullBlockSet(BLOCKS, ITEMS, STONE_TILE_NAME, Blocks.STONE_BRICKS, isDisabled(STONE_TILE_NAME));

    //blackstone tile
    public static EnumMap<BlockRegistryHelper.VariantType, RegistryObject<Block>> BLACKSTONE_TILE_BLOCKS =
            BlockRegistryHelper.registerFullBlockSet(BLOCKS, ITEMS, BLACKSTONE_TILE_NAME, Blocks.BLACKSTONE, isDisabled(BLACKSTONE_TILE_NAME));

    //stone lamp
    public static final RegistryObject<Block> STONE_LAMP = BLOCKS.register(STONE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Item> STONE_LAMP_ITEM = ITEMS.register(STONE_LAMP_NAME, () -> new BlockItem(STONE_LAMP.get(),
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, STONE_LAMP_NAME))
    ));

    //blackstone lamp
    public static final RegistryObject<Block> BLACKSTONE_LAMP = BLOCKS.register(BLACKSTONE_LAMP_NAME, () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW)
                    .strength(1.5f, 6f)
                    .lightLevel((s) -> 15)
                    .sound(SoundType.STONE)));
    public static final RegistryObject<Item> BLACKSTONE_LAMP_ITEM = regBlockItem(BLACKSTONE_LAMP, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, BLACKSTONE_LAMP_NAME));

    //deepslate lamp
    public static final RegistryObject<Block> DEEPSLATE_LAMP = BLOCKS.register(DEEPSLATE_LAMP_NAME, () -> new Block(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).lightLevel(s -> 15)));
    public static final RegistryObject<Item> DEEPSLATE_LAMP_ITEM = regBlockItem(DEEPSLATE_LAMP, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, DEEPSLATE_LAMP_NAME));


    //end_stone lamp
    public static final RegistryObject<Block> END_STONE_LAMP = BLOCKS.register(END_STONE_LAMP_NAME, () -> new EndLampBlock(
            BlockBehaviour.Properties.copy(Blocks.END_STONE).lightLevel(s -> 15)));
    public static final RegistryObject<Item> END_STONE_LAMP_ITEM = regBlockItem(END_STONE_LAMP, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, END_STONE_LAMP_NAME));


    //flower box
    public static final RegistryObject<Block> FLOWER_BOX = BLOCKS.register(FLOWER_BOX_NAME, () -> {
        var p = BlockBehaviour.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(0.5F);
        return /*CompatHandler.create ? SchematicCannonStuff.makeFlowerBox(p) : */new FlowerBoxBlock(p);
    });

    public static final RegistryObject<Item> FLOWER_BOX_ITEM = regBlockItem(FLOWER_BOX, getTab(CreativeModeTab.TAB_DECORATIONS, FLOWER_BOX_NAME), 300);

    public static final RegistryObject<BlockEntityType<FlowerBoxBlockTile>> FLOWER_BOX_TILE = TILES.register(FLOWER_BOX_NAME, () -> BlockEntityType.Builder.of(
            FlowerBoxBlockTile::new, FLOWER_BOX.get()).build(null));

    //statue
    public static final RegistryObject<Block> STATUE = BLOCKS.register(STATUE_NAME, () -> new StatueBlock(
            BlockBehaviour.Properties.of(Material.STONE)
                    .strength(2)));
    public static final RegistryObject<Item> STATUE_ITEM = regBlockItem(STATUE, getTab(CreativeModeTab.TAB_DECORATIONS, STATUE_NAME));

    public static final RegistryObject<BlockEntityType<StatueBlockTile>> STATUE_TILE = TILES.register(STATUE_NAME, () -> BlockEntityType.Builder.of(
            StatueBlockTile::new, STATUE.get()).build(null));

    //feather block
    public static final RegistryObject<Block> FEATHER_BLOCK = BLOCKS.register(FEATHER_BLOCK_NAME, () -> new FeatherBlock(
            BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).strength(0.5f)
                    .noCollission()));
    public static final RegistryObject<Item> FEATHER_BLOCK_ITEM = regBlockItem(FEATHER_BLOCK, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FEATHER_BLOCK_NAME));

    //flint block
    public static final RegistryObject<Block> FLINT_BLOCK = BLOCKS.register(FLINT_BLOCK_NAME, () -> new FlintBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(2F, 7.5F)));
    public static final RegistryObject<Item> FLINT_BLOCK_ITEM = regBlockItem(FLINT_BLOCK, getTab(CreativeModeTab.TAB_BUILDING_BLOCKS, FLINT_BLOCK_NAME));

    //gunpowder block
    public static final RegistryObject<Block> GUNPOWDER_BLOCK = regPlaceableItem(GUNPOWDER_BLOCK_NAME, () -> new GunpowderBlock(
                    BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE).sound(SoundType.SAND)),
            () -> Items.GUNPOWDER, ServerConfigs.tweaks.PLACEABLE_GUNPOWDER);

    //placeable book
    public static final RegistryObject<Block> BOOK_PILE = regPlaceableItem(BOOK_PILE_NAME, () -> new BookPileBlock(
                    BlockBehaviour.Properties.of(Material.DECORATION).strength(0.5F).sound(SoundType.WOOD)),
            () -> Items.ENCHANTED_BOOK, ServerConfigs.tweaks.PLACEABLE_BOOKS);

    //placeable book
    public static final RegistryObject<Block> BOOK_PILE_H = regPlaceableItem(BOOK_PILE_H_NAME, () -> new BookPileHorizontalBlock(
                    BlockBehaviour.Properties.copy(BOOK_PILE.get())),
            () -> Items.BOOK, ServerConfigs.tweaks.PLACEABLE_BOOKS);

    public static final RegistryObject<BlockEntityType<BookPileBlockTile>> BOOK_PILE_TILE = TILES.register(BOOK_PILE_NAME, () -> BlockEntityType.Builder.of(
            BookPileBlockTile::new, BOOK_PILE.get(), BOOK_PILE_H.get()).build(null));

    //urn
    public static final RegistryObject<Block> URN = BLOCKS.register(URN_NAME, () -> new UrnBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.WOOD)
                    .sound(SoundType.GLASS)
                    .strength(0.1f, 0)
    ));
    public static final RegistryObject<BlockEntityType<UrnBlockTile>> URN_TILE = TILES.register(URN_NAME, () -> BlockEntityType.Builder.of(
            UrnBlockTile::new, URN.get()).build(null));
    public static final RegistryObject<Item> URN_ITEM = regBlockItem(URN, getTab(CreativeModeTab.TAB_DECORATIONS, URN_NAME));

    //ash
    public static final RegistryObject<Block> ASH_BLOCK = BLOCKS.register(ASH_NAME, () -> new AshLayerBlock(
            BlockBehaviour.Properties.of(Material.TOP_SNOW, MaterialColor.COLOR_GRAY)
                    .sound(SoundType.SAND).randomTicks().strength(0.1F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> ASH_ITEM = regBlockItem(ASH_BLOCK, getTab(CreativeModeTab.TAB_DECORATIONS, ASH_NAME));

    //ash
    public static final RegistryObject<Item> ASH_BRICK_ITEM = regItem(ASH_BRICK_NAME, () -> new Item(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, ASH_BRICKS_NAME))));

    //soap
    public static final RegistryObject<Item> SOAP = regItem(SOAP_NAME, () -> new SoapItem(
            (new Item.Properties()).tab(getTab(CreativeModeTab.TAB_MISC, SOAP_NAME))));

    public static final RegistryObject<Block> SOAP_BLOCK = BLOCKS.register(SOAP_BLOCK_NAME, () -> new SoapBlock(
            BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_PINK)
                    .friction(0.94f)
                    .strength(1.25F, 4.0F)
                    .sound(SoundType.CORAL_BLOCK)));
    public static final RegistryObject<Item> SOAP_BLOCK_ITEM = regBlockItem(SOAP_BLOCK, getTab(CreativeModeTab.TAB_DECORATIONS, SOAP_BLOCK_NAME));

    //stackable skulls
    public static final RegistryObject<Block> SKULL_PILE = BLOCKS.register(SKULL_PILE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);

        return /*CompatHandler.create ? SchematicCannonStuff.makeDoubleSkull(p) :*/ new DoubleSkullBlock(p);
    });

    public static final RegistryObject<BlockEntityType<DoubleSkullBlockTile>> SKULL_PILE_TILE = TILES.register(SKULL_PILE_NAME, () ->
            BlockEntityType.Builder.of(DoubleSkullBlockTile::new, SKULL_PILE.get()).build(null));


    //skulls candles
    public static final RegistryObject<Block> SKULL_CANDLE = BLOCKS.register(SKULL_CANDLE_NAME, () -> {
        var p = BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL).sound(SoundType.BONE_BLOCK);
        return /*CompatHandler.create ? SchematicCannonStuff.makeCandleSkull(p) :*/ new CandleSkullBlock(p);
    });


    public static final RegistryObject<BlockEntityType<CandleSkullBlockTile>> SKULL_CANDLE_TILE = TILES.register(SKULL_CANDLE_NAME, () ->
            BlockEntityType.Builder.of(CandleSkullBlockTile::new, SKULL_CANDLE.get()).build(null));

    //bubble
    public static final RegistryObject<BubbleBlock> BUBBLE_BLOCK = BLOCKS.register(BUBBLE_BLOCK_NAME, () ->
            new BubbleBlock(BlockBehaviour.Properties.of(Material.DECORATION, MaterialColor.COLOR_PINK)
                    .sound(ModSounds.BUBBLE_BLOCK)
                    .noOcclusion()
                    .isSuffocating((a, b, c) -> false)
                    .isViewBlocking((a, b, c) -> false)
                    .isRedstoneConductor((a, b, c) -> false)
                    .instabreak())
    );
    public static final RegistryObject<Item> BUBBLE_BLOCK_ITEM = regItem(BUBBLE_BLOCK_NAME, () -> new BubbleBlockItem(
            BUBBLE_BLOCK.get(), (new Item.Properties()).tab(null)));

    public static final RegistryObject<BlockEntityType<BubbleBlockTile>> BUBBLE_BLOCK_TILE = TILES.register(BUBBLE_BLOCK_NAME, () ->
            BlockEntityType.Builder.of(BubbleBlockTile::new, BUBBLE_BLOCK.get()).build(null));


    //public static final String CRE
    // ATIVE_WAND = "creative_wand";
    //public static final RegistryObject<Item> TELEPORT_WAND = regItem(CREATIVE_WAND, () ->
    //        new TeleportWand((new Item.Properties()).tab(null)));
    /*
    public static final String REDSTONE_DRIVER_NAME = "redstone_driver";
    public static final RegistryObject<Block> REDSTONE_DRIVER = BLOCKS.register(REDSTONE_DRIVER_NAME,()-> new RedstoneDriverBlock(
            AbstractBlock.Properties.copy(Blocks.REPEATER)));
    public static final RegistryObject<Item> REDSTONE_DRIVER_ITEM = ITEMS.register(REDSTONE_DRIVER_NAME,()-> new BlockItem(REDSTONE_DRIVER.get(),
            (new Item.Properties()).tab(getTab(ItemGroup.TAB_REDSTONE,REDSTONE_DRIVER_NAME))));



    */


}
