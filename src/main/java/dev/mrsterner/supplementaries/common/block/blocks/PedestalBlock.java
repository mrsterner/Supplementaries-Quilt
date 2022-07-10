package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PedestalBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Shapes.or(VoxelShapes.cuboid(0.1875D, 0.125D, 0.1875D, 0.815D, 0.885D, 0.815D),
            VoxelShapes.cuboid(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D),
            VoxelShapes.cuboid(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D));
    protected static final VoxelShape SHAPE_UP = Shapes.or(VoxelShapes.cuboid(0.1875D, 0.125D, 0.1875D, 0.815D, 1, 0.815D),
            VoxelShapes.cuboid(0.0625D, 0D, 0.0625D, 0.9375D, 0.1875D, 0.9375D));
    protected static final VoxelShape SHAPE_DOWN = Shapes.or(VoxelShapes.cuboid(0.1875D, 0, 0.1875D, 0.815D, 0.885D, 0.815D),
            VoxelShapes.cuboid(0.0625D, 0.8125D, 0.0625D, 0.9375D, 1D, 0.9375D));
    protected static final VoxelShape SHAPE_UP_DOWN = VoxelShapes.cuboid(0.1875D, 0, 0.1875D, 0.815D, 1, 0.815D);

    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty HAS_ITEM = BlockProperties.HAS_ITEM;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public PedestalBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(UP, false).with(AXIS, Direction.Axis.X)
                .with(DOWN, false).with(WATERLOGGED, false).with(HAS_ITEM, false));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        if (ServerConfigs.cached.CRYSTAL_ENCHANTING && world.getBlockEntity(pos) instanceof PedestalBlockTile te) {
            if (te.type == PedestalBlockTile.DisplayType.CRYSTAL) return 3;
        }
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, WATERLOGGED, HAS_ITEM, AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        boolean flag = world.getFluidState(pos).getType() == Fluids.WATER;
        return this.getDefaultState ().with(WATERLOGGED, flag).with(AXIS, context.getPlayerFacing().getAxis())
                .with(UP, canConnect(world.getBlockState(pos.above()), pos, world, Direction.UP, false))
                .with(DOWN, canConnect(world.getBlockState(pos.below()), pos, world, Direction.DOWN, false));
    }

    public static boolean canConnect(BlockState state, BlockPos pos, WorldAccess world, Direction dir, boolean hasItem) {
        if (state.getBlock() instanceof PedestalBlock) {
            if (dir == Direction.DOWN) {
                return !state.get(HAS_ITEM);
            } else if (dir == Direction.UP) {
                return !hasItem;
            }
        }
        return false;
    }

    //called when a neighbor is placed
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn,facing,facingState,worldIn,currentPos,facingPos);

        if (facing == Direction.UP) {
            return stateIn.with(UP, canConnect(facingState, currentPos, worldIn, facing, stateIn.get(HAS_ITEM)));
        } else if (facing == Direction.DOWN) {
            return stateIn.with(DOWN, canConnect(facingState, currentPos, worldIn, facing, stateIn.get(HAS_ITEM)));
        }
        return stateIn;
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (target.getLocation().y() > pos.getY() + 1 - 0.1875) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                ItemStack i = tile.getDisplayedItem();
                if (!i.isEmpty()) return i;
            }
        }
        return super.getPickStack(state, target, world, pos, player);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        //create new tile
        if (!state.get(HAS_ITEM)) {
            worldIn.setBlockState(pos, state.with(HAS_ITEM, true), (1 << 2) | (1 << 1));
        }
        ActionResult resultType = ActionResult.PASS;
        if (worldIn.getBlockEntity(pos) instanceof PedestalBlockTile tile && tile.isAccessibleBy(player)) {

            ItemStack handItem = player.getItemInHand(handIn);

            //Indiana Jones swap
            if (handItem.getItem() instanceof SackItem) {

                ItemStack it = handItem.copy();
                it.setCount(1);
                ItemStack removed = tile.removeItemNoUpdate(0);
                tile.setDisplayedItem(it);

                if (!player.isCreative()) {
                    handItem.decrement(1);
                }
                if (!worldIn.isClient()()) {
                    player.setItemInHand(handIn, removed);
                    worldIn.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, worldIn.random.nextFloat() * 0.10F + 0.95F);
                    tile.setChanged();
                } else {
                    //also refreshTextures visuals on client. will get overwritten by packet tho
                    tile.updateClientVisualsOnLoad();
                }
                resultType = ActionResult.success(worldIn.isClient());
            } else {
                resultType = tile.interact(player, handIn);
            }
            if (resultType.consumesAction()) {
                Direction.Axis axis = player.getDirection().getAxis();
                boolean isEmpty = tile.getDisplayedItem().isEmpty();
                if (axis != state.get(AXIS) || isEmpty) {
                    worldIn.setBlockState(pos, state.with(AXIS, axis).with(HAS_ITEM, !isEmpty), 2);
                    if (isEmpty) worldIn.removeBlockEntity(pos);
                }
            }
        }
        return resultType;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        boolean up = state.get(UP);
        boolean down = state.get(DOWN);
        if (!up) {
            if (!down) {
                return SHAPE;
            } else {
                return SHAPE_DOWN;
            }
        } else {
            if (!down) {
                return SHAPE_UP;
            } else {
                return SHAPE_UP_DOWN;
            }
        }
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        if(pState.get(HAS_ITEM)){
            return new PedestalBlockTile(pPos, pState);
        }
        return null;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof PedestalBlockTile tile)
            return tile.isEmpty() ? 0 : 15;
        else
            return 0;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        if(rotation == Rotation.CLOCKWISE_180){
            return state;
        }
        else{
            return switch (state.get(AXIS)) {
                case Z -> state.with(AXIS, Direction.Axis.X);
                case X -> state.with(AXIS, Direction.Axis.Z);
                default -> state;
            };
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }
}
