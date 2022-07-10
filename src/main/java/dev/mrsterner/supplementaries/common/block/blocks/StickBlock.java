package dev.mrsterner.supplementaries.common.block.blocks;

import com.google.common.collect.ImmutableMap;
import dev.mrsterner.supplementaries.api.IRotatable;
import dev.mrsterner.supplementaries.api.moonlightlib.WaterloggableBlock;
import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StickBlock extends WaterloggableBlock implements IRotatable { // IRotationLockable,
    protected static final VoxelShape Y_AXIS_AABB = Block.createCuboidShape(7D, 0.0D, 7D, 9D, 16.0D, 9D);
    protected static final VoxelShape Z_AXIS_AABB = Block.createCuboidShape(7D, 7D, 0.0D, 9D, 9D, 16.0D);
    protected static final VoxelShape X_AXIS_AABB = Block.createCuboidShape(0.0D, 7D, 7D, 16.0D, 9D, 9D);
    protected static final VoxelShape Y_Z_AXIS_AABB = VoxelShapes.union(Block.createCuboidShape(7D, 0.0D, 7D, 9D, 16.0D, 9D),
            Block.createCuboidShape(7D, 7D, 0.0D, 9D, 9D, 16.0D));
    protected static final VoxelShape Y_X_AXIS_AABB = VoxelShapes.union(Block.createCuboidShape(7D, 0.0D, 7D, 9D, 16.0D, 9D),
            Block.createCuboidShape(0.0D, 7D, 7D, 16.0D, 9D, 9D));
    protected static final VoxelShape X_Z_AXIS_AABB = VoxelShapes.union(Block.createCuboidShape(7D, 7D, 0.0D, 9D, 9D, 16.0D),
            Block.createCuboidShape(0.0D, 7D, 7D, 16.0D, 9D, 9D));
    protected static final VoxelShape X_Y_Z_AXIS_AABB = VoxelShapes.union(Block.createCuboidShape(7D, 7D, 0.0D, 9D, 9D, 16.0D),
            Block.createCuboidShape(0.0D, 7D, 7D, 16.0D, 9D, 9D),
            Block.createCuboidShape(7D, 0.0D, 7D, 9D, 16.0D, 9D));

    public static final BooleanProperty AXIS_X = BlockProperties.AXIS_X;
    public static final BooleanProperty AXIS_Y = BlockProperties.AXIS_Y;
    public static final BooleanProperty AXIS_Z = BlockProperties.AXIS_Z;

    protected final Map<Direction.Axis, BooleanProperty> AXIS2PROPERTY = ImmutableMap.of(Direction.Axis.X, AXIS_X, Direction.Axis.Y, AXIS_Y, Direction.Axis.Z, AXIS_Z);

    private final int fireSpread;

    public StickBlock(Settings properties, int fireSpread) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, Boolean.FALSE).with(AXIS_Y, true).with(AXIS_X, false).with(AXIS_Z, false));
        this.fireSpread = fireSpread;
    }

    public StickBlock(Settings properties) {
        this(properties, 60);
    }


    @Override
    public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return state.get(Properties.WATERLOGGED) ? 0 : fireSpread;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
        return state.get(BlockStateProperties.WATERLOGGED) ? 0 : fireSpread;
    }

	@Override
	public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
		super.appendTooltip(stack, world, tooltip, options);
		tooltip.add((new TextComponent("You shouldn't have this")).style(Formatting.GRAY));
	}


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AXIS_X, AXIS_Y, AXIS_Z);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView reader, BlockPos pos,  ShapeContext context) {
        boolean x = state.get(AXIS_X);
        boolean y = state.get(AXIS_Y);
        boolean z = state.get(AXIS_Z);
        if (x) {
            if (y) {
                if (z) return X_Y_Z_AXIS_AABB;
                return Y_X_AXIS_AABB;
            } else if (z) return X_Z_AXIS_AABB;
            return X_AXIS_AABB;
        }
        if (z) {
            if (y) return Y_Z_AXIS_AABB;
            return Z_AXIS_AABB;
        }
        return Y_AXIS_AABB;
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getBlockPos());
        BooleanProperty axis = AXIS2PROPERTY.get(context.getSide().getAxis());
        if (blockstate.isOf(this)) {
            return blockstate.with(axis, true);
        } else {
            return super.getPlacementState(context).with(AXIS_Y, false).with(axis, true);
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (!context.shouldCancelInteraction() && context.getStack().isOf(this.asItem())) {
            BooleanProperty axis = AXIS2PROPERTY.get(context.getSide().getAxis());
            if (!state.get(axis)) return true;
        }
        return super.canReplace(state, context);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (player.getStackInHand(hand).isEmpty() && hand == Hand.MAIN_HAND) {
            if (ServerConfigs.cached.STICK_POLE) {
                if (this.material != Material.WOOD) return ActionResult.PASS;
                if (world.isClient()) return ActionResult.SUCCESS;
                else {
                    Direction moveDir = player.isSneaking() ? Direction.DOWN : Direction.UP;
                    findConnectedFlag(world, pos, Direction.UP, moveDir, 0);
                    findConnectedFlag(world, pos, Direction.DOWN, moveDir, 0);
                }
                return ActionResult.CONSUME;
            }
        }
        return ActionResult.PASS;
    }

    private static boolean isVertical(BlockState state) {
        return state.get(AXIS_Y) && !state.get(AXIS_X) && !state.get(AXIS_Z);
    }

    public static boolean findConnectedFlag(World world, BlockPos pos, Direction searchDir, Direction moveDir, int it) {
        if (it > ServerConfigs.cached.STICK_POLE_LENGTH) return false;
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b == ModRegistry.STICK_BLOCK.get() && isVertical(state)) {
            return findConnectedFlag(world, pos.relative(searchDir), searchDir, moveDir, it + 1);
        } else if (b instanceof FlagBlock && it != 0) {
            BlockPos toPos = pos.relative(moveDir);
            BlockState stick = world.getBlockState(toPos);

            if (world.getBlockEntity(pos) instanceof FlagBlockTile tile && stick.getBlock() == ModRegistry.STICK_BLOCK.get() && isVertical(stick)) {

                world.setBlockState(pos, stick);
                world.setBlockState(toPos, state);

                NbtCompound tag = tile.toNbt();
                BlockEntity te = world.getBlockEntity(toPos);
                if (te != null) {
                    te.load(tag);
                }
                world.playSound(null, toPos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1F, 1.4F);
                return true;
            }
        }
        return false;
    }

    //quark
    //TODO: improve for multiple sticks
    //@Override
    public BlockState applyRotationLock(World world, BlockPos blockPos, BlockState state, Direction dir, int half) {
        int i = 0;
        if (state.get(AXIS_X)) i++;
        if (state.get(AXIS_Y)) i++;
        if (state.get(AXIS_Z)) i++;
        if (i == 1) state.with(AXIS_Z, false).with(AXIS_X, false)
                .with(AXIS_Y, false).with(AXIS2PROPERTY.get(dir.getAxis()), true);
        return state;
    }


    @Override
    public Optional<BlockState> getRotatedState(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation, Direction axis, @org.jetbrains.annotations.Nullable Vec3d hit) {
        boolean x = state.get(AXIS_X);
        boolean y = state.get(AXIS_Y);
        boolean z = state.get(AXIS_Z);
        return Optional.of(switch (axis.getAxis()) {
            case Y -> state.with(AXIS_X, z).with(AXIS_Z, x);
            case X -> state.with(AXIS_Y, z).with(AXIS_Z, y);
            case Z -> state.with(AXIS_X, y).with(AXIS_Y, x);
        });
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder pBuilder) {
        int i = 0;
        if (state.get(AXIS_X)) i++;
        if (state.get(AXIS_Y)) i++;
        if (state.get(AXIS_Z)) i++;
        return List.of(new ItemStack(this.asItem(), i));
    }


}
