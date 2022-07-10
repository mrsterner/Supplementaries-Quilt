package dev.mrsterner.supplementaries.common.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.NavigationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;

public class GlobeBlock extends WaterBlock implements EntityBlock {
    protected static final VoxelShape SHAPE = VoxelShapes.cuboid(0.125D, 0D, 0.125D, 0.875D, 1D, 0.875D);

    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GlobeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(WATERLOGGED, false).with(TRIGGERED, false).with(FACING, Direction.NORTH));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView worldIn, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        boolean powered = world.getReceivedRedstonePower(pos) > 0;
        if (powered != state.get(TRIGGERED)) {
            world.setBlockState(pos, state.with(TRIGGERED, powered), 4);
            //server
            //calls event on server and client through packet
            if (powered) {
                world.gameEvent(GameEvent.BLOCK_PRESS, pos);
                world.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    @Override
    public ActionResult use(BlockState state, World level, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
            if (player.getItemInHand(handIn).getItem() instanceof ShearsItem) {

                tile.sheared = !tile.sheared;
                tile.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                if (level.isClient()) {
                    Minecraft.getInstance().particleEngine.destroy(pos, state);
                }
                return ActionResult.success(level.isClient());

            }

            if (!level.isClient()) {
                if (tile.yaw > 1500 && player instanceof ServerPlayerEntity serverPlayer) {
                    Advancement advancement = level.getServer().getAdvancements().getAdvancement(new ResourceLocation("supplementaries", "adventure/globe"));
                    if (advancement != null) {
                        serverPlayer.getAdvancements().award(advancement, "unlock");
                    }
                }
                level.gameEvent(player, GameEvent.BLOCK_PRESS, pos);
                level.blockEvent(pos, state.getBlock(), 1, 0);
            } else {
                player.displayClientMessage(new TextComponent("X: " + pos.getX() + ", Z: " + pos.getZ()), true);
            }
        }
        return ActionResult.success(level.isClient());
    }

    @Override
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (CommonUtil.FESTIVITY.isEarthDay() && worldIn.isClient()) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            for (int l = 0; l < 1; ++l) {
                double d0 = (x + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                double d1 = (y + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                double d2 = (z + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0, 0, 0);
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == Direction.DOWN && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState () : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !worldIn.isEmptyBlock(pos.below());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, TRIGGERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView world, BlockPos pos,  ShapeContext  context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite()).with(WATERLOGGED, flag);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new GlobeBlockTile(pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity tile = world.getBlockEntity(pos);
        return tile != null && tile.triggerEvent(eventID, eventParam);
    }

    @Override
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof GlobeBlockTile tile) {
            if (tile.yaw != 0) return 15;
            else return tile.face / -90 + 1;
        }
        return 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BlockUtils.getTicker(pBlockEntityType, ModRegistry.GLOBE_TILE.get(), GlobeBlockTile::tick);
    }
}
