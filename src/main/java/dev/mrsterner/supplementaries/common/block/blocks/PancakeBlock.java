package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.api.ISoftFluidConsumer;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.Topping;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

public class PancakeBlock extends WaterBlock implements ISoftFluidConsumer {

    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[8];

    static {
        Arrays.setAll(SHAPE_BY_LAYER, l -> Block.createCuboidShape(2, 0.0D, 2, 14.0D, 2 + l * 2, 14.0D));
    }

    public static final IntegerProperty PANCAKES = BlockProperties.PANCAKES_1_8;
    public static final EnumProperty<Topping> TOPPING = BlockProperties.TOPPING;

    public PancakeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(PANCAKES, 1).with(TOPPING, Topping.NONE).with(WATERLOGGED, false));
    }

    private Topping getTopping(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.is(ModTags.SYRUP)) return Topping.SYRUP;
        if (item instanceof HoneyBottleItem) return BlockProperties.Topping.HONEY;
        var tag = Registry.ITEM.getTag(ModTags.CHOCOLATE_BARS);
        if ((item == Items.COCOA_BEANS && (tag.isEmpty() || tag.get().stream().findAny().isEmpty())) || stack.is(ModTags.CHOCOLATE_BARS)) {
            return Topping.CHOCOLATE;
        }
        return Topping.NONE;
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(handIn);
        Item item = stack.getItem();
        Topping t = getTopping(stack);
        if (t != Topping.NONE) {
            if (state.get(TOPPING) == Topping.NONE) {
                if (!worldIn.isClient()) {
                    worldIn.setBlockState(pos, state.with(TOPPING, t), 3);
                    worldIn.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1, 1.2f);
                }
                ItemStack returnItem = t == Topping.CHOCOLATE ? ItemStack.EMPTY : new ItemStack(Items.GLASS_BOTTLE);
                if (!player.isCreative()) Utils.swapItem(player, handIn, returnItem);
                //player.setHeldItem(handIn, DrinkHelper.fill(stack.copy(), player, new ItemStack(Items.GLASS_BOTTLE), false));
                return ActionResult.success(worldIn.isClient());
            }
        } else if (item == ModRegistry.PANCAKE_ITEM.get()) {
            return ActionResult.PASS;
        } else if (player.canEat(false)) {
            //player.addStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(1, 0.1F);
            if (!worldIn.isClient()) {


                removeLayer(state, pos, worldIn, player);
                player.playNotifySound(SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1, 1);
                return ActionResult.CONSUME;
            } else {
                Minecraft.getInstance().particleEngine.destroy(player.blockPosition().above(1), state);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }


    public static void removeLayer(BlockState state, BlockPos pos, World world, Player player) {
        int i = state.get(PANCAKES);
        if (i == 8) {
            BlockPos up = pos.above();
            BlockState upState = world.getBlockState(up);
            if (upState.getBlock() == state.getBlock()) {
                removeLayer(upState, up, world, player);
                return;
            }
        }
        if (i > 1) {
            world.setBlockState(pos, state.with(PANCAKES, i - 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
        if (state.get(TOPPING) != Topping.NONE) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8 * 20));
        }
    }


    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getBlockPos());
        if (blockstate.is(this)) {
            return blockstate.with(PANCAKES, Math.min(8, blockstate.get(PANCAKES) + 1));
        }
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        return this.getDefaultState ().with(WATERLOGGED, flag);
    }

    protected boolean isValidGround(BlockState state, BlockView worldIn, BlockPos pos) {
        return !state.getCollisionShape(worldIn, pos).getFaceShape(Direction.UP).isEmpty() || state.isFaceSturdy(worldIn, pos, Direction.UP);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return this.isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (!stateIn.canSurvive(worldIn, currentPos)) {
            return Blocks.AIR.getDefaultState ();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext useContext) {
        return useContext.getItemInHand().getItem() == this.asItem() && state.get(PANCAKES) < 8 || super.canReplace(state, useContext);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return SHAPE_BY_LAYER[state.get(PANCAKES) - 1];
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PANCAKES, TOPPING, WATERLOGGED);
    }

    @Override
    public boolean tryAcceptingFluid(World world, BlockState state, BlockPos pos, SoftFluid f, NbtCompound nbt, int amount) {
        Topping topping = Topping.fromFluid(f);
        if (state.get(TOPPING) == Topping.NONE && topping != Topping.NONE) {
            world.setBlockState(pos, state.with(TOPPING, topping), 2);
            world.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1, 1.2f);
            return true;
        }
        return false;
    }
}
