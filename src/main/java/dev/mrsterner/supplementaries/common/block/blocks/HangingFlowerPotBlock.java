package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class HangingFlowerPotBlock extends Block implements EntityBlock {

    protected static final VoxelShape SHAPE = Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public HangingFlowerPotBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        Item i = stack.getItem();
        if (world.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile tile) {
            if (i instanceof BlockItem blockItem) {
                BlockState mimic = blockItem.getBlock().getDefaultState ();
                tile.setHeldBlock(mimic);
            }
            BlockUtils.addOptionalOwnership(entity, tile);
        }
    }

    @Override
    public MutableComponent getName() {
        return new TranslatableComponent("block.minecraft.flower_pot");
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return context.getClickedFace() == Direction.DOWN ? super.getPlacementState(context) : null;
    }
    //TODO: use dynamic block model
    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile tile && tile.isAccessibleBy(player)) {
            Block pot = tile.getHeldBlock().getBlock();
            if (pot instanceof FlowerPotBlock flowerPot) {
                ItemStack itemstack = player.getItemInHand(handIn); //&& FlowerPotHandler.isEmptyPot(flowerPot)
                Item item = itemstack.getItem();
                //mimics flowerPorBlock behavior
                Block newPot = item instanceof BlockItem bi ? FlowerPotHandler.getFullPot(flowerPot, bi.getBlock()) : Blocks.AIR;
                /*Block newPot = item instanceof BlockItem ? FlowerPotHelper.FULL_POTS.get(((FlowerPotBlock) pot).getEmptyPot())
                        .getOrDefault(((BlockItem)item).getBlock().getRegistryName(), Blocks.AIR.delegate).get() : Blocks.AIR;*/

                boolean isEmptyFlower = newPot == Blocks.AIR;
                boolean isPotEmpty = FlowerPotHandler.isEmptyPot(pot);

                if (isEmptyFlower != isPotEmpty) {
                    if (isPotEmpty) {
                        tile.setHeldBlock(newPot.getDefaultState ());
                        player.awardStat(Stats.POT_FLOWER);
                        if (!player.getAbilities().instabuild) {
                            itemstack.decrement(1);
                        }
                    } else {
                        //drop item
                        ItemStack flowerItem = pot.getPickStack(worldIn, pos, state);
                        if (!flowerItem.equals(new ItemStack(this))) {
                            if (itemstack.isEmpty()) {
                                player.setItemInHand(handIn, flowerItem);
                            } else if (!player.addItem(flowerItem)) {
                                player.drop(flowerItem, false);
                            }
                        }
                        tile.setHeldBlock(((FlowerPotBlock) pot).getEmptyPot().getDefaultState ());
                    }
                    return ActionResult.success(worldIn.isClient());
                } else {
                    return ActionResult.CONSUME;
                }
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new HangingFlowerPotBlockTile(pPos, pState);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult target, BlockView world, BlockPos pos, Player player) {

        if (world.getBlockEntity(pos) instanceof HangingFlowerPotBlockTile te) {
            if (te.getHeldBlock().getBlock() instanceof FlowerPotBlock b) {
                Block flower = b.getContent();
                if (flower == Blocks.AIR) return new ItemStack(b.getEmptyPot());
                return new ItemStack(flower);
            }
        }
        return new ItemStack(Blocks.FLOWER_POT, 1);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof HangingFlowerPotBlockTile tile) {
            Block b = tile.getHeldBlock().getBlock();
            if (b instanceof FlowerPotBlock)
                return Arrays.asList(new ItemStack(((FlowerPotBlock) b).getContent()), new ItemStack(((FlowerPotBlock) b).getEmptyPot()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView worldIn, BlockPos pos) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.UP && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState () : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return RopeBlock.isSupportingCeiling(pos.relative(Direction.UP), worldIn);
    }
}
