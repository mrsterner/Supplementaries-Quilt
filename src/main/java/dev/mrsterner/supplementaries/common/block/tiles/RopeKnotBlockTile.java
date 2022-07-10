package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RopeBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.mehvahdjukaar.supplementaries.common.block.blocks.RopeKnotBlock.*;


public class RopeKnotBlockTile extends MimicBlockTile {

    private VoxelShape collisionShape = null;
    private VoxelShape shape = null;

    public RopeKnotBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.ROPE_KNOT_TILE.get(), pos, state);
        this.setHeldBlock(Blocks.AIR.getDefaultState ());
    }

    public VoxelShape getCollisionShape() {
        if (collisionShape == null) this.recalculateShapes(this.getBlockState());
        return collisionShape;
    }

    public VoxelShape getShape() {
        if (shape == null) this.recalculateShapes(this.getBlockState());
        return shape;
    }

    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(6, 0, 6, 10, 6, 10);

    public void recalculateShapes(BlockState state) {
        try {
            if (state == null || !state.is(ModRegistry.ROPE_KNOT.get()) || this.level == null) return;
            BlockState mimic = this.getHeldBlock();
            if (mimic.isAir()) mimic = Blocks.STONE.getDefaultState ();
            boolean up = state.get(UP);
            boolean down = state.get(DOWN);
            VoxelShape r;
            if (down && !up) {
                r = DOWN_SHAPE;
            } else {
                BlockState rope = ModRegistry.ROPE.get().getDefaultState ()
                        .with(RopeBlock.KNOT, false)
                        .with(UP, up)
                        .with(DOWN, down)
                        .with(NORTH, state.get(NORTH))
                        .with(SOUTH, state.get(SOUTH))
                        .with(EAST, state.get(EAST))
                        .with(WEST, state.get(WEST));
                r = rope.getShape(this.level, this.worldPosition);
            }
            VoxelShape c = mimic.getCollisionShape(this.level, this.worldPosition);
            VoxelShape s = mimic.getShape(this.level, this.worldPosition);
            c = Shapes.or(c, r);
            s = Shapes.or(s, r);
            this.collisionShape = c.optimize();
            this.shape = s.optimize();
        } catch (Exception e) {
            Supplementaries.LOGGER.warn("failed to calculate roped fence hitbox: " + e);
        }
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
        //not sure if needed here
        this.requestModelDataUpdate();
        this.collisionShape = null;
        this.shape = null;
        super.setChanged();
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        this.collisionShape = null;
        this.shape = null;
    }
}
