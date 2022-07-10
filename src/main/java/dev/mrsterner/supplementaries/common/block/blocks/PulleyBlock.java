package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.Winding;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class PulleyBlock extends RotatedPillarBlock implements EntityBlock, IRotatable {
    public static final EnumProperty<Winding> TYPE = BlockProperties.WINDING;
    public static final BooleanProperty FLIPPED = BlockProperties.FLIPPED;

    public PulleyBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState ().with(AXIS, Direction.Axis.Y).with(TYPE, Winding.NONE).with(FLIPPED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE, FLIPPED);
    }

    /**
     * simplified rotate method that only rotates pulley on it axis
     * if direction is null assumes default orientation
     * @return true if rotation was successful
     */
    public boolean windPulley(BlockState state, BlockPos pos, WorldAccess world, BlockRotation rot, @Nullable Direction dir) {
        Direction.Axis axis = state.get(AXIS);
        if (axis == Direction.Axis.Y) return false;
        if (dir == null) dir = axis == Direction.Axis.Z ? Direction.NORTH : Direction.WEST;
        return this.rotateOverAxis(state, world, pos, rot, dir, null).isPresent();
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation, Direction axis, Vec3 hit) {
        Direction.Axis myAxis = state.get(RotatedPillarBlock.AXIS);
        Direction.Axis targetAxis = axis.getAxis();
        if (myAxis == targetAxis) return Optional.of(state.cycle(FLIPPED));
        if (myAxis == Direction.Axis.X) {
            return Optional.of(state.with(AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y));
        } else if (myAxis == Direction.Axis.Z) {
            return Optional.of(state.with(AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y));
        }
        else if(myAxis == Direction.Axis.Y){
            return Optional.of(state.with(AXIS, targetAxis == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z));
        }
        return Optional.of(state);
    }

    //actually unwinds ropes & rotate connected
    @Override
    public void onRotated(BlockState newState, BlockState oldState, WorldAccess world, BlockPos pos, BlockRotation rot, Direction axis, @Nullable Vec3 hit) {
        if (axis.getAxis().isHorizontal() && axis.getAxis() == oldState.get(AXIS)) {

            if (world.getBlockEntity(pos) instanceof PulleyBlockTile pulley) {
                pulley.handleRotation(rot);
            }
            //try turning connected
            BlockPos connectedPos = pos.relative(axis);
            BlockState connected = world.getBlockState(connectedPos);
            if (connected.is(this) && newState.get(AXIS) == connected.get(AXIS)) {
                this.windPulley(connected, connectedPos, world, rot, axis);
            }
        }
    }


    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn,
                                 BlockHitResult hit) {
        if (worldIn.getBlockEntity(pos) instanceof PulleyBlockTile tile && tile.isAccessibleBy(player)) {
            if (player instanceof ServerPlayerEntity) {
                if (!(player.isShiftKeyDown() && this.windPulley(state, pos, worldIn, Rotation.COUNTERCLOCKWISE_90, null)))
                    player.openMenu(tile);
            }
            return ActionResult.success(worldIn.isClient()());
        }
        return ActionResult.PASS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new PulleyBlockTile(pPos, pState);
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof Container tile) {
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
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockUtils.addOptionalOwnership(placer, worldIn, pos);
    }
}
