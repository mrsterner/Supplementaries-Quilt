package dev.mrsterner.supplementaries.common.block.blocks;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.api.ILightable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CandleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.Util;
import net.minecraft.advancements.Criteria;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CandleSkullBlock extends AbstractCandleBlock implements EntityBlock {

    private static final Int2ObjectMap<List<Vec3>> PARTICLE_OFFSETS = Util.make(() -> {
        Int2ObjectMap<List<Vec3>> map = new Int2ObjectOpenHashMap<>();
        map.defaultReturnValue(ImmutableList.of());
        map.put(1, ImmutableList.of(new Vec3(0.5D, 0.5 + 0.5D, 0.5D)));
        map.put(2, ImmutableList.of(new Vec3(0.375D, 0.5 + 0.44D, 0.5D), new Vec3(0.625D, 0.5 + 0.5D, 0.44D)));
        map.put(3, ImmutableList.of(new Vec3(0.5D, 0.5 + 0.313D, 0.625D), new Vec3(0.375D, 0.5 + 0.44D, 0.5D), new Vec3(0.56D, 0.5 + 0.5D, 0.44D)));
        map.put(4, ImmutableList.of(new Vec3(0.44D, 0.5 + 0.313D, 0.56D), new Vec3(0.625D, 0.5 + 0.44D, 0.56D), new Vec3(0.375D, 0.5 + 0.44D, 0.375D), new Vec3(0.56D, 0.5 + 0.5D, 0.375D)));
        return Int2ObjectMaps.unmodifiable(map);
    });

    protected static final VoxelShape BASE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);

    private static final VoxelShape ONE_AABB = Shapes.or(BASE, Block.createCuboidShape(7.0D, 8.0D, 7.0D, 9.0D, 14.0D, 9.0D));
    private static final VoxelShape TWO_AABB = Shapes.or(BASE, Block.createCuboidShape(5.0D, 8.0D, 6.0D, 11.0D, 14.0D, 9.0D));
    private static final VoxelShape THREE_AABB = Shapes.or(BASE, Block.createCuboidShape(5.0D, 8.0D, 6.0D, 10.0D, 14.0D, 11.0D));
    private static final VoxelShape FOUR_AABB = Shapes.or(BASE, Block.createCuboidShape(5.0D, 8.0D, 5.0D, 11.0D, 14.0D, 10.0D));

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final IntegerProperty CANDLES = BlockStateProperties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;

    public CandleSkullBlock(Properties properties) {
        super(properties.lightLevel(CandleBlock.LIGHT_EMISSION));
        this.setDefaultState(this.getDefaultState ().with(CANDLES, 1)
                .with(ROTATION, 0).with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> pBuilder) {
        super.appendProperties(pBuilder);
        pBuilder.add(CANDLES, LIT, ROTATION);
    }

    @Override
    public boolean canPathfindThrough(BlockState pState, BlockView pLevel, BlockPos pPos, NavigationType pType) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new CandleSkullBlockTile(pPos, pState);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CandleSkullBlockTile tile) {
            List<ItemStack> loot = tile.getCandle().with(CANDLES, state.get(CANDLES)).getDrops(builder);

            BlockEntity skullTile = tile.getSkullTile();
            if (skullTile != null) {
                BlockState skull = skullTile.getBlockState();
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, skullTile);
                loot.addAll(skull.getDrops(builder));
            }

            return loot;
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult hitResult, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof CandleSkullBlockTile tile) {
            double y = hitResult.getLocation().y;
            boolean up = y % ((int) y) > 0.5d;
            return up ? tile.getCandle().getBlock().getPickStack(state, hitResult, world, pos, player) : tile.getSkullItem();
        }
        return super.getPickStack(state, hitResult, world, pos, player);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockView pLevel, BlockPos pPos,  ShapeContext  pContext) {
        return switch (pState.get(CANDLES)) {
            default -> ONE_AABB;
            case 2 -> TWO_AABB;
            case 3 -> THREE_AABB;
            case 4 -> FOUR_AABB;
        };
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        return PARTICLE_OFFSETS.get(pState.get(CANDLES).intValue());
    }

    //same as ILightUpBlock (todo: try to merge)
    @Override
    public ActionResult use(BlockState state, World level, BlockPos pos, Player player, Hand hand, BlockHitResult pHit) {
        if (player.getAbilities().mayBuild) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(ItemTags.CANDLES) && stack.getItem() instanceof BlockItem blockItem) {
                int count = state.get(CANDLES);
                if (count < 4 && ServerConfigs.cached.SKULL_CANDLES_MULTIPLE &&
                        level.getBlockEntity(pos) instanceof CandleSkullBlockTile tile
                        && tile.getCandle().getBlock().asItem() == stack.getItem()) {

                    SoundType sound = blockItem.getBlock().getDefaultState ().getSoundType(level, pos, player);
                    level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    if (!player.getAbilities().instabuild) {
                        stack.decrement(1);
                    }
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                    }
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                    level.setBlockState(pos, state.with(CANDLES, count + 1), 2);

                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    return ActionResult.success(level.isClient());
                }
                return ActionResult.PASS;
            }

            if (!state.get(LIT)) {

                Item item = stack.getItem();
                if (item instanceof FlintAndSteelItem || stack.is(ModTags.FLINT_AND_STEELS)) {
                    if (this.lightUp(player, state, pos, level, ILightable.FireSound.FLINT_AND_STEEL)) {

                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(hand));
                        return ActionResult.success(level.isClient());
                    }
                } else if (item instanceof FireChargeItem) {
                    if (this.lightUp(player, state, pos, level, ILightable.FireSound.FIRE_CHANGE)) {
                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(hand));
                        if (!player.isCreative()) stack.decrement(1);
                        return ActionResult.success(level.isClient());
                    }
                } else if (item instanceof PotionItem && PotionUtils.getPotion(stack) == Potions.WATER) {
                    extinguish(player, state, level, pos);
                    Utils.swapItem(player, hand, stack, new ItemStack(Items.GLASS_BOTTLE));
                    return ActionResult.success(level.isClient());
                }
            } else if (stack.isEmpty()) {
                extinguish(player, state, level, pos);
                return ActionResult.success(level.isClient());
            }
        }
        return ActionResult.PASS;
    }

    public boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, WorldAccess world, ILightable.FireSound sound) {
        state = state.with(LIT, true);
        if (!world.isClient()()) {
            world.setBlockState(pos, state, 11);
            sound.play(world, pos);
        }
        world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }


}
