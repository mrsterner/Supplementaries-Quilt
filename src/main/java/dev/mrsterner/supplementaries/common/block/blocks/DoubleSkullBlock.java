package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoubleSkullBlock extends SkullBlock implements IRotatable {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public DoubleSkullBlock(Properties properties) {
        super(Types.SKELETON, properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockView pLevel, BlockPos pPos,  ShapeContext  pContext) {
        return SHAPE;
    }

    @Override
    public BlockState rotate(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation) {
        return super.rotate(state, world, pos, rotation);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new DoubleSkullBlockTile(pPos, pState);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DoubleSkullBlockTile tile) {
            List<ItemStack> loot = new ArrayList<>();
            BlockEntity skullTile = tile.getSkullTile();
            if(skullTile!=null){
                BlockState skull = skullTile.getBlockState();
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, skullTile);

                loot.addAll(skull.getDrops(builder));
            }
            BlockEntity skullTileUp = tile.getSkullTileUp();
            if(skullTileUp!=null){
                BlockState skull = skullTileUp.getBlockState();
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, skullTileUp)
                        .withOptionalParameter(LootContextParams.BLOCK_STATE, skull);
                loot.addAll(skull.getDrops(builder));
            }
            return loot;
        }
        return super.getDrops(pState, builder);
    }

    @Override
    public ItemStack getPickStack(BlockState state, HitResult hitResult, BlockView world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {
            double y = hitResult.getLocation().y;
            boolean up = y % ((int) y) > 0.5d;
            return up ? tile.getSkullItemUp() : tile.getSkullItem();
        }
        return super.getPickStack(state, hitResult, world, pos, player);
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rotation, Direction axis, @Nullable Vec3 hit) {
        return Optional.of(this.rotate(state, world, pos, rotation));
    }

    @Override
    public Optional<Direction> rotateOverAxis(BlockState state, WorldAccess world, BlockPos pos, BlockRotation rot, Direction axis, @Nullable Vec3 hit) {
        if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {

            boolean simple = hit == null;

            if (simple) {
                tile.rotateUp(rot);
                IRotatable.super.rotateOverAxis(state, world, pos, rot, axis, null);
            } else {
                boolean up = hit.y % ((int) hit.y) > 0.5d;
                int inc = rot == Rotation.CLOCKWISE_90 ? -1 : 1;
                if (up) tile.rotateUpStep(inc);
                else {
                    if (world instanceof ServerLevel) {
                        world.setBlockState(pos, state.with(ROTATION, (state.get(ROTATION) - inc + 16) % 16), 11);
                        //level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                    }
                }
            }

            //world.notifyBlockUpdate(pos, tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
            tile.setChanged();
            if (world instanceof World level) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return Optional.of(Direction.UP);

        }
        return Optional.empty();
    }


    @Override
    public BlockState updateShape(BlockState pState, Direction dir, BlockState pNeighborState, WorldAccess pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (dir == Direction.UP && pLevel.getBlockEntity(pCurrentPos) instanceof DoubleSkullBlockTile tile) {
            tile.updateWax(pNeighborState);
        }
        return super.updateShape(pState, dir, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }
}
