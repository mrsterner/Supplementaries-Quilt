package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BookPileBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BookPileBlock extends WaterBlock implements EntityBlock {

    private static final VoxelShape SHAPE_1 = Block.createCuboidShape(3D, 0D, 3D, 13D, 4D, 13D);
    private static final VoxelShape SHAPE_2 = Block.createCuboidShape(3D, 0D, 3D, 13D, 8D, 13D);
    private static final VoxelShape SHAPE_3 = Block.createCuboidShape(3D, 0D, 3D, 13D, 12D, 13D);
    private static final VoxelShape SHAPE_4 = Block.createCuboidShape(3D, 0D, 3D, 13D, 16D, 13D);

    public static final IntegerProperty BOOKS = BlockProperties.BOOKS;

    public BookPileBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(WATERLOGGED, false).with(BOOKS, 1));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            tile.setItem(state.get(BOOKS) - 1, copy);
        }
    }

    public boolean isAcceptedItem(Item i) {
        return isEnchantedBook(i) || (ServerConfigs.cached.MIXED_BOOKS && isNormalBook(i));
    }

    public static boolean isEnchantedBook(Item i) {
        return i == Items.ENCHANTED_BOOK || isQuarkTome(i);
    }

    public static boolean isNormalBook(Item i) {
        return i.builtInRegistryHolder().is(ModTags.BOOKS) || (ServerConfigs.cached.WRITTEN_BOOKS && isWrittenBook(i));
    }

    public static boolean isWrittenBook(Item i) {
        return i instanceof WrittenBookItem || i instanceof WritableBookItem;
    }

    public static boolean isQuarkTome(Item i) {
        return CompatHandler.quark && QuarkPlugin.isTome(i);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (state.get(BOOKS) < 4) {
            Item item = context.getItemInHand().getItem();
            if (isAcceptedItem(item)) {
                return true;
            }
        }
        return super.canReplace(state, context);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BOOKS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockstate = context.getWorld().getBlockState(context.getBlockPos());
        if (blockstate.getBlock() instanceof BookPileBlock) {
            return blockstate.with(BOOKS, blockstate.get(BOOKS) + 1);
        }
        return super.getPlacementState(context);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new BookPileBlockTile(pPos, pState, false);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
                Containers.dropContents(world, pos, tile);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            double f = 5 * (target.getLocation().y - pos.getY()) / SHAPE_4.bounds().maxY;
            return tile.getItem(MathHelper.clamp((int) f, 0, state.get(BOOKS)-1));
        }
        return Items.BOOK.getDefaultInstance();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return switch (state.get(BOOKS)) {
            default -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            case 4 -> SHAPE_4;
        };
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof BookPileBlockTile tile) {
            return tile.getEnchantPower();
        }
        return 0;
    }
}
