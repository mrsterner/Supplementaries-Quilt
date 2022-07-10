package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CageBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CageBlock extends WaterBlock implements EntityBlock {

    protected static final VoxelShape SHAPE = Block.createCuboidShape(1D, 0D, 1D, 15.0D, 16.0D, 15.0D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public CageBlock(Properties properties) {
        super(properties.lightLevel(state->state.get(LIGHT_LEVEL)));
        this.setDefaultState(this.stateManager.getDefaultState().with(LIGHT_LEVEL, 0).with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof CageBlockTile tile) {
            return tile.mobContainer.isEmpty() ? 0 : 15;
        }
        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // shulker box code
    public ItemStack getCageItem(CageBlockTile te) {
        ItemStack returnStack = new ItemStack(this);
        te.saveToNbt(returnStack);
        return returnStack;
    }

    //loot table does the same. frick the loot table
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CageBlockTile tile) {
            ItemStack itemstack = this.getCageItem(tile);
            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    //for pick block

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof CageBlockTile tile) {
            return this.getCageItem(tile);
        }
        return super.getPickStack(state, target, world, pos, player);
    }

    // end shulker box code
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new CageBlockTile(pPos, pState);
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
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite())
                .with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER);
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof CageBlockTile cage) {
            return cage.mobContainer.onInteract(world, pos, player, hand);
        }
        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.CAGE_TILE.get(), CageBlockTile::tick);
    }
}
