package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class JarBlock extends WaterBlock implements EntityBlock {
    public static final VoxelShape SHAPE = Shapes.or(Block.createCuboidShape(3, 0, 3, 13, 14, 13),
            Block.createCuboidShape(5, 14, 5, 11, 16, 11));

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public JarBlock(Properties properties) {
        super(properties.lightLevel(state -> state.get(JarBlock.LIGHT_LEVEL)));
        this.setDefaultState(this.stateManager.getDefaultState().with(LIGHT_LEVEL, 0).with(FACING, Direction.NORTH).with(WATERLOGGED, false));
    }

    //check if it only gets called client side
    public int getJarLiquidColor(BlockPos pos, LevelReader world) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof JarBlockTile tile) {
            return tile.fluidHolder.getParticleColor(world, pos);
        }
        return 0xffffff;
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
        int color = getJarLiquidColor(pos, world);
        if (color == -1) return null;
        float r = (float) ((color >> 16 & 255)) / 255.0F;
        float g = (float) ((color >> 8 & 255)) / 255.0F;
        float b = (float) ((color & 255)) / 255.0F;
        return new float[]{r, g, b};
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof JarBlockTile tile && tile.isAccessibleBy(player)) {
            // make te do the work
            if (tile.handleInteraction(player, handIn, worldIn, pos)) {
                if (!worldIn.isClient()()) {
                    tile.setChanged();
                }
                return ActionResult.success(worldIn.isClient());
            }
            if(ServerConfigs.cached.JAR_CAPTURE) {
                return tile.mobContainer.onInteract(worldIn, pos, player, handIn);
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof JarBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            BlockUtils.addOptionalOwnership(placer, tile);
        }
    }

    //TODO: improve
    public ItemStack getJarItem(JarBlockTile te) {
        ItemStack returnStack = new ItemStack(this);

        if (te.hasContent()) {
            NbtCompound compoundTag = new NbtCompound();
            te.saveAdditional(compoundTag);
            //hax
            if (compoundTag.contains("Owner")) compoundTag.remove("Owner");
            if (!compoundTag.isEmpty()) {
                returnStack.setSubNbt("BlockEntityTag", compoundTag);
            }
        }
        if (te.hasCustomName()) {
            returnStack.setHoverName(te.getCustomName());
        }
        return returnStack;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof JarBlockTile tile) {
            ItemStack itemstack = this.getJarItem(tile);
            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof JarBlockTile tile) {
            return this.getJarItem(tile);
        }
        return super.getPickStack(state, target, world, pos, player);
    }

    // end shoulker box code
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
        return new JarBlockTile(pPos, pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof JarBlockTile tile) {
            if (!tile.isEmpty())
                return AbstractContainerMenu.getRedstoneSignalFromContainer(tile);
            else if (!tile.fluidHolder.isEmpty()) {
                return tile.fluidHolder.getComparatorOutput();
            } else if (!tile.mobContainer.isEmpty()) return 15;
        }
        return 0;
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.JAR_TILE.get(), JarBlockTile::tick);
    }

}
