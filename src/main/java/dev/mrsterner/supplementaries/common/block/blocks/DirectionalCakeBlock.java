package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.farmersdelight.FDCompatRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Random;

public class DirectionalCakeBlock extends CakeBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape[] SHAPES_NORTH = new VoxelShape[]{
            Block.createCuboidShape(1, 0, 1, 15, 8, 15),
            Block.createCuboidShape(1, 0, 3, 15, 8, 15),
            Block.createCuboidShape(1, 0, 5, 15, 8, 15),
            Block.createCuboidShape(1, 0, 7, 15, 8, 15),
            Block.createCuboidShape(1, 0, 9, 15, 8, 15),
            Block.createCuboidShape(1, 0, 11, 15, 8, 15),
            Block.createCuboidShape(1, 0, 13, 15, 8, 15)};
    protected static final VoxelShape[] SHAPES_SOUTH = new VoxelShape[]{
            Block.createCuboidShape(1, 0, 1, 15, 8, 15),
            Block.createCuboidShape(1, 0, 1, 15, 8, 13),
            Block.createCuboidShape(1, 0, 1, 15, 8, 11),
            Block.createCuboidShape(1, 0, 1, 15, 8, 9),
            Block.createCuboidShape(1, 0, 1, 15, 8, 7),
            Block.createCuboidShape(1, 0, 1, 15, 8, 5),
            Block.createCuboidShape(1, 0, 1, 15, 8, 3)};
    protected static final VoxelShape[] SHAPES_EAST = new VoxelShape[]{
            Block.createCuboidShape(1, 0, 1, 15, 8, 15),
            Block.createCuboidShape(1, 0, 1, 13, 8, 15),
            Block.createCuboidShape(1, 0, 1, 11, 8, 15),
            Block.createCuboidShape(1, 0, 1, 9, 8, 15),
            Block.createCuboidShape(1, 0, 1, 7, 8, 15),
            Block.createCuboidShape(1, 0, 1, 5, 8, 15),
            Block.createCuboidShape(1, 0, 1, 3, 8, 15)};


    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public DirectionalCakeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(BITES, 0)
                .with(FACING, Direction.WEST).with(WATERLOGGED, false));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public ActionResult use(BlockState state, World level, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        ItemStack itemstack = player.getItemInHand(handIn);
        Item item = itemstack.getItem();

        if (CompatHandler.farmers_delight) {
            ActionResult res = FDCompatRegistry.onCakeInteraction(state, pos, level, itemstack);
            if (res.consumesAction()) return res;
        }

        if (itemstack.is(ItemTags.CANDLES) && state.get(BITES) == 0 && state.is(ModRegistry.DIRECTIONAL_CAKE.get())) {
            Block block = Block.byItem(item);
            if (block instanceof CandleBlock) {
                if (!player.isCreative()) {
                    itemstack.decrement(1);
                }

                level.playSound(null, pos, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockStateAndUpdate(pos, CandleCakeBlock.byCandle(block));
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                player.awardStat(Stats.ITEM_USED.get(item));
                return ActionResult.SUCCESS;
            }
        }
        return this.eatSliceD(level, pos, state, player,
                hit.getDirection().getAxis() != Direction.Axis.Y ? hit.getDirection() : player.getDirection().getOpposite());

    }

    public ActionResult eatSliceD(WorldAccess world, BlockPos pos, BlockState state, Player player, Direction dir) {
        if (!player.canEat(false)) {
            return ActionResult.PASS;
        } else {
            player.awardStat(Stats.EAT_CAKE_SLICE);
            player.getFoodData().eat(2, 0.1F);
            if (!world.isClient()()) {
                this.removeSlice(state, pos, world, dir);
            }
            return ActionResult.success(world.isClient()());
        }
    }

    public void removeSlice(BlockState state, BlockPos pos, WorldAccess world, Direction dir) {
        int i = state.get(BITES);
        if (i < 6) {
            if (i == 0 && ServerConfigs.cached.DIRECTIONAL_CAKE) state = state.with(FACING, dir);
            world.setBlockState(pos, state.with(BITES, i + 1), 3);
        } else {
            world.removeBlock(pos, false);
        }
    }


    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        return new ItemStack(Items.CAKE);
    }

    @Override
    public MutableComponent getName() {
        return Blocks.CAKE.getName();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, BITES, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return switch (state.get(FACING)) {
            default -> SHAPE_BY_BITE[state.get(BITES)];
            case EAST -> SHAPES_EAST[state.get(BITES)];
            case SOUTH -> SHAPES_SOUTH[state.get(BITES)];
            case NORTH -> SHAPES_NORTH[state.get(BITES)];
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite())
                .with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.get(FACING)));
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (CommonUtil.FESTIVITY.isStValentine()) {
            if (rand.nextFloat() > 0.8) {
                double d0 = (pos.getX() + 0.5 + (rand.nextFloat() - 0.5));
                double d1 = (pos.getY() + 0.25 + (rand.nextFloat() - 0.25));
                double d2 = (pos.getZ() + 0.5 + (rand.nextFloat() - 0.5));
                worldIn.addParticle(ParticleTypes.HEART, d0, d1, d2, 0, 0, 0);
            }
        }
    }
}
