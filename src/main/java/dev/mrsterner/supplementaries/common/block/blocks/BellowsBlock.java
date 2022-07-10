package dev.mrsterner.supplementaries.common.block.blocks;

import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BellowsBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BellowsBlock extends Block implements BlockEntityProvider {

    private static final VoxelShape DEFAULT_SHAPE = VoxelShapes.cuboid(VoxelShapes.fullCube().getBoundingBox().expand(0.1f));
    private static final Float2ObjectAVLTreeMap<VoxelShape> SHAPES_Y_CACHE = new Float2ObjectAVLTreeMap<>();
    private static final Float2ObjectAVLTreeMap<VoxelShape> SHAPES_X_Z_CACHE = new Float2ObjectAVLTreeMap<>();

    public static final DirectionProperty FACING = Properties.FACING;
    public static final IntProperty POWER = Properties.POWER;

    public BellowsBlock(Settings properties) {
        super(properties);
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(POWER, 0));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasDynamicBounds() {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext context) {

        if (worldIn.getBlockEntity(pos) instanceof BellowsBlockTile tile) {
            float height = tile.height;
            //3 digit
            height = (float) (Math.round(height * 1000.0) / 1000.0);

            if (state.get(FACING).getAxis() == Direction.Axis.Y) {
                return SHAPES_Y_CACHE.computeIfAbsent(height, BellowsBlock::createVoxelShapeY);
            } else {
                return SHAPES_X_Z_CACHE.computeIfAbsent(height, BellowsBlock::createVoxelShapeXZ);
            }
        }
        return VoxelShapes.fullCube();
    }

    public static VoxelShape createVoxelShapeY(float height) {
        return VoxelShapes.cuboid(0, 0, -height, 1, 1, 1 + height);
    }

    public static VoxelShape createVoxelShapeXZ(float height) {
        return VoxelShapes.cuboid(0, -height, 0, 1, 1 + height, 1);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER);
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
        return this.getDefaultState ().with(FACING, context.getPlayerLookDirection ().getOpposite());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        int signal = world.getReceivedRedstonePower(pos);
        int currentPower = state.get(POWER);
        // on-off
        if (signal != currentPower) {
            world.setBlockState(pos, state.with(POWER, signal), 2 | 4);
            //returns if state changed
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new BellowsBlockTile(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.BELLOWS_TILE.get(), BellowsBlockTile::tick);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityCollision(state, worldIn, pos, entityIn);
        if (worldIn.getBlockEntity(pos) instanceof BellowsBlockTile te) te.onSteppedOn(entityIn);
    }

    @Override
    public void onSteppedOn(World worldIn, BlockPos pos, BlockState state, Entity entityIn) {
        if (worldIn.getBlockEntity(pos) instanceof BellowsBlockTile te) te.onSteppedOn(entityIn);
    }
}
