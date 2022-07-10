package net.mehvahdjukaar.supplementaries.integration.botania;

import net.mehvahdjukaar.selene.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.decor.BlockTinyPotato;
import vazkii.botania.common.block.tile.TileTinyPotato;

import java.util.Collections;
import java.util.List;

public class TaterInAJarBlock extends BlockTinyPotato {
    private static final VoxelShape SHAPE = JarBlock.SHAPE;

    public TaterInAJarBlock(Properties builder) {
        super(builder);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  ctx) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, WorldAccess world, BlockPos pos, BlockRotation direction) {
        return state.with(BlockStateProperties.HORIZONTAL_FACING, direction.rotate(state.get(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @NotNull
    @Override
    public BlockEntity createBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TaterInAJarBlockTile(pos, state);
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos, Player player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof TaterInAJarBlockTile tile) {
            tile.interact(player, hand, player.getItemInHand(hand), hit.getDirection());
            if (world instanceof ServerWorld serverLevel) {
                AABB box = SHAPE.bounds();
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, (double) pos.getX() + box.minX + Math.random() * (box.maxX - box.minX), (double) pos.getY() + box.maxY - 1, (double) pos.getZ() + box.minZ + Math.random() * (box.maxZ - box.minZ), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ItemStack stack = new ItemStack(this);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TileTinyPotato te) {
            if (te.hasCustomName())
                stack.setHoverName(te.getCustomName());
        }
        return Collections.singletonList(stack);
    }

    @NotNull
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World level = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Player player = ctx.getPlayer();
        if (player != null && !player.isShiftKeyDown()) {
            FluidState fluidState = level.getFluidState(pos);
            Item i = ModRegistry.JAR_ITEM.get();
            // i.playReleaseSound( level, ctx.getClickLocation());
            if (!level.isClient()) {
                Utils.swapItemNBT(player, ctx.getHand(), ctx.getItemInHand(), new ItemStack(i));
            }
            BlockState state = ModBlocks.tinyPotato.getDefaultState ().with(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
            return state.with(BlockStateProperties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
        }
        return super.getPlacementState(ctx);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, BotaniaCompatRegistry.TATER_IN_A_JAR_TILE.get(), TileTinyPotato::commonTick);
    }
}
