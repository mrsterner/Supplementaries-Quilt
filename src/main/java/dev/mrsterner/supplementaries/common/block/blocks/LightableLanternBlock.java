package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.entities.FallingLanternEntity;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Optional;

public class LightableLanternBlock extends LanternBlock {
    public final VoxelShape shapeDown;
    public final VoxelShape shapeUp;

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LightableLanternBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(LIT, true)
                .with(HANGING, false));
        this.shapeDown = shape;
        this.shapeUp = shapeDown.move(0, 14/16f - shape.bounds().maxY, 0);
    }

    public LightableLanternBlock(Properties properties) {
        this(properties, Shapes.or(Block.createCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D),
                Block.createCuboidShape(6.0D, 8.0D, 6.0D, 10.0D, 9.0D, 10.0D)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView p_153475_, BlockPos p_153476_,  ShapeContext  p_153477_) {
        return state.get(HANGING) ? shapeUp : shapeDown;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // if (state.get(HANGING)) return RenderShape.ENTITYBLOCK_ANIMATED;
        return RenderShape.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIT);
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        var optional = toggleLight(state, worldIn, pos, player, handIn);
        if (optional.isPresent()) {
            if (!worldIn.isClient()) {
                worldIn.setBlockStateAndUpdate(pos, optional.get());
            }
            return ActionResult.success(worldIn.isClient());
        }
        return ActionResult.PASS;
    }


    public static Optional<BlockState> toggleLight(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn) {
        if (player.getAbilities().mayBuild && handIn == Hand.MAIN_HAND) {
            ItemStack item = player.getItemInHand(handIn);
            if (!state.get(LIT)) {
                if (item.getItem() instanceof FlintAndSteelItem) {

                    worldIn.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, worldIn.getRandom().nextFloat() * 0.4F + 0.8F);
                    state = state.with(LIT, true);

                    item.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                    return Optional.of(state);
                } else if (item.getItem() instanceof FireChargeItem) {

                    worldIn.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (worldIn.getRandom().nextFloat() - worldIn.getRandom().nextFloat()) * 0.2F + 1.0F);
                    state = state.with(LIT, true);

                    if (!player.isCreative()) item.decrement(1);
                    return Optional.of(state);
                }
            } else if (item.isEmpty()) {

                worldIn.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
                state = state.with(LIT, false);

                return Optional.of(state);
            }
        }
        return Optional.empty();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, world, pos);
    }

    // @Nullable
    //@Override
    // public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
    //     return new LightableLanternBlockTile(pPos, pState);
    // }

    //TODO: hitting sounds
    //called by mixin
    public static boolean canSurviveCeilingAndMaybeFall(BlockState state, BlockPos pos, LevelReader worldIn) {
        if (!RopeBlock.isSupportingCeiling(pos.above(), worldIn) && worldIn instanceof World l) {
            if (ServerConfigs.cached.FALLING_LANTERNS.isOn() && l.getBlockState(pos).is(state.getBlock())) {
                return createFallingLantern(state, pos, l);
            }
            return false;
        }
        return true;
    }

    public static boolean createFallingLantern(BlockState state, BlockPos pos, World level) {
        if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            if (state.hasProperty(LanternBlock.HANGING)) {
                double maxY = state.getShape(level, pos).bounds().maxY;
                state = state.with(LanternBlock.HANGING, false);
                double yOffset = maxY - state.getShape(level, pos).bounds().maxY;
                FallingLanternEntity.fall(level, pos, state, yOffset);
                return true;
            }
        }
        return false;
    }

    public enum FallMode {
        ON,
        OFF,
        NO_FIRE;

        public boolean hasFire() {
            return this != NO_FIRE;
        }

        public boolean isOn() {
            return this != OFF;
        }
    }
}
