package dev.mrsterner.supplementaries.common.block.blocks;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;

public class FlagBlock extends WaterBlock implements EntityBlock, IColored {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(4, 0D, 4D, 12.0D, 16.0D, 12.0D);
    private static final Map<DyeColor, Block> BY_COLOR = Maps.newHashMap();
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final DyeColor color;

    public FlagBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        BY_COLOR.put(color, this);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    @Override
    public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return state.get(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return state.get(BlockStateProperties.WATERLOGGED) ? 0 : 60;
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        return world.getBlockEntity(pos) instanceof FlagBlockTile tile ? tile.getItem(state) : super.getPickStack(state, target, world, pos, player);
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }

    public static Block byColor(DyeColor color) {
        return BY_COLOR.getOrDefault(color, Blocks.WHITE_BANNER);
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new FlagBlockTile(pPos, pState);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof FlagBlockTile tile) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (itemstack.getItem() instanceof MapItem) {
                if (!world.isClient()) {
                    if (MapItem.getSavedData(itemstack, world) instanceof ExpandedMapData data) {
                        data.toggleCustomDecoration(world, pos);
                    }
                }
                return ActionResult.success(world.isClient());
            } else if (itemstack.isEmpty() && hand == Hand.MAIN_HAND) {
                if (ServerConfigs.cached.STICK_POLE) {
                    if (world.isClient()) return ActionResult.SUCCESS;
                    else {
                        Direction moveDir = player.isShiftKeyDown() ? Direction.DOWN : Direction.UP;
                        StickBlock.findConnectedFlag(world, pos.below(), Direction.UP, moveDir, 0);
                        StickBlock.findConnectedFlag(world, pos.above(), Direction.DOWN, moveDir, 0);
                    }
                    return ActionResult.CONSUME;
                }
            }
        }
        return ActionResult.PASS;
    }
}
