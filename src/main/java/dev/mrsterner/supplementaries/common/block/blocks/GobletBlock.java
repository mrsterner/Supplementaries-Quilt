package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GobletBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class GobletBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(5, 0, 5, 11, 9, 11);

    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;

    public GobletBlock(Properties properties) {
        super(properties.lightLevel(state->state.get(LIGHT_LEVEL)));
        this.setDefaultState(this.stateManager.getDefaultState().with(LIGHT_LEVEL, 0).with(WATERLOGGED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof GobletBlockTile tile && tile.isAccessibleBy(player)) {
            // make te do the work
            if (tile.handleInteraction(player, handIn)) {
                if (!worldIn.isClient()())
                    tile.setChanged();
                return ActionResult.success(worldIn.isClient());
            }
        }
        return ActionResult.PASS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new GobletBlockTile(pPos, pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof GobletBlockTile tile) {
            return tile.fluidHolder.isEmpty() ? 0 : 15;
        }
        return 0;
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        if (0.05 > random.nextFloat()) {
            if (world.getBlockEntity(pos) instanceof GobletBlockTile tile) {
                SoftFluidHolder holder = tile.getSoftFluidHolder();
                if (holder.getFluid().get() == SoftFluidRegistry.POTION.get()) {
                    int i = holder.getTintColor(world, pos);
                    double d0 = (double) (i >> 16 & 255) / 255.0D;
                    double d1 = (double) (i >> 8 & 255) / 255.0D;
                    double d2 = (double) (i & 255) / 255.0D;

                    world.addParticle(ParticleTypes.ENTITY_EFFECT, pos.getX() + 0.3125 + random.nextFloat() * 0.375, pos.getY() + 0.5625, pos.getZ() + 0.3125 + random.nextFloat() * 0.375, d0, d1, d2);
                }
            }
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }
}
