package net.mehvahdjukaar.supplementaries.common.block.util;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.selene.math.MathHelperUtils;
import net.mehvahdjukaar.supplementaries.api.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Hand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockUtils {

    public static <T extends Comparable<T>, A extends Property<T>> BlockState replaceProperty(BlockState from, BlockState to, A property) {
        if (from.hasProperty(property)) {
            return to.with(property, from.get(property));
        }
        return to;
    }

    public static <T extends BlockEntity & IOwnerProtected> void addOptionalOwnership(LivingEntity placer, T tileEntity) {
        if (ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            tileEntity.setOwner(placer.getUUID());
        }
    }

    public static void addOptionalOwnership(LivingEntity placer, World world, BlockPos pos) {
        if (ServerConfigs.cached.SERVER_PROTECTION && placer instanceof Player) {
            if (world.getBlockEntity(pos) instanceof IOwnerProtected tile) {
                tile.setOwner(placer.getUUID());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    public static class PlayerLessContext extends ItemPlacementContext {
        public PlayerLessContext(World worldIn, @Nullable Player playerIn, Hand handIn, ItemStack stackIn, BlockHitResult rayTraceResultIn) {
            super(worldIn, playerIn, handIn, stackIn, rayTraceResultIn);
        }
    }

    //rotation stuff
    //returns rotation direction axis which might be different that the clicked face
    public static Optional<Direction> tryRotatingBlockAndConnected(Direction face, boolean ccw, BlockPos targetPos, World level, Vec3 hit) {
        BlockState state = level.getBlockState(targetPos);
        if (state.getBlock() instanceof IRotatable rotatable) {
            return rotatable.rotateOverAxis(state, level, targetPos, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, face, hit);
        }
        Optional<Direction> special = tryRotatingSpecial(face, ccw, targetPos, level, state, hit);
        if (special.isPresent()) return special;
        return tryRotatingBlock(face, ccw, targetPos, level, state, hit);
    }

    public static Optional<Direction> tryRotatingBlock(Direction face, boolean ccw, BlockPos targetPos, World level, Vec3 hit) {
        var opt = tryRotatingBlock(face, ccw, targetPos, level, level.getBlockState(targetPos), hit);
        //try again using up direction if previously failed. Doing this cause many people dont even realize you have to click on the axis you want to rotate
        if (opt.isEmpty()) {
            opt = tryRotatingBlock(Direction.UP, ccw, targetPos, level, level.getBlockState(targetPos), hit);
        }
        return opt;
    }

    // can be called on both sides
    // returns the direction onto which the block was actually rotated
    public static Optional<Direction> tryRotatingBlock(Direction dir, boolean ccw, BlockPos targetPos, World world, BlockState state, Vec3 hit) {

        //interface stuff
        if (state.getBlock() instanceof IRotatable rotatable) {
            return rotatable.rotateOverAxis(state, world, targetPos, ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90, dir, hit);
        }

        Optional<BlockState> optional = getRotatedState(dir, ccw, targetPos, world, state);
        if (optional.isPresent()) {
            BlockState rotated = optional.get();

            if (rotated.canSurvive(world, targetPos)) {
                rotated = Block.updateFromNeighbourShapes(rotated, world, targetPos);

                if (rotated != state) {
                    if (world instanceof ServerWorld serverLevel) {
                        world.setBlockState(targetPos, rotated, 11);
                        //level.updateNeighborsAtExceptFromFacing(pos, newState.getBlock(), mydir.getOpposite());
                    }
                    return Optional.of(dir);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<BlockState> getRotatedState(Direction dir, boolean ccw, BlockPos targetPos, World world, BlockState state) {

        // is block blacklisted?
        if (isBlacklisted(state)) return Optional.empty();

        BlockRotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        Block block = state.getBlock();

        if (state.hasProperty(BlockProperties.FLIPPED)) {
            return Optional.of(state.cycle(BlockProperties.FLIPPED));
        }
        //horizontal facing blocks -easy
        if (dir.getAxis() == Direction.Axis.Y) {

            if (block == Blocks.CAKE) {
                int bites = state.get(CakeBlock.BITES);
                if (bites != 0) return Optional.of(ModRegistry.DIRECTIONAL_CAKE.get().getDefaultState ()
                        .with(CakeBlock.BITES, bites).rotate(world, targetPos, rot));
            }

            BlockState rotated = state.rotate(world, targetPos, rot);
            //also hardcoding vanilla rotation methods cause some mods just dont implement rotate methods for their blocks
            //this could cause problems for mods that do and dont want it to be rotated but those should really be added to the blacklist
            if (rotated == state) {
                if (state.hasProperty(BlockStateProperties.FACING)) {
                    rotated = state.with(BlockStateProperties.FACING,
                            rot.rotate(state.get(BlockStateProperties.FACING)));
                } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    rotated = state.with(BlockStateProperties.HORIZONTAL_FACING,
                            rot.rotate(state.get(BlockStateProperties.HORIZONTAL_FACING)));
                } else if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                    rotated = RotatedPillarBlock.rotatePillar(state, rot);
                } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                    rotated = state.cycle(BlockStateProperties.HORIZONTAL_AXIS);
                }
            }
            return Optional.of(rotated);
        } else if (state.hasProperty(BlockStateProperties.ATTACH_FACE) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return Optional.of(rotateFaceBlockHorizontal(dir, ccw, state));
        }
        // 6 dir blocks blocks
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return Optional.of(rotateBlockStateOnAxis(state, dir, ccw));
        }
        // axis blocks
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis targetAxis = state.get(BlockStateProperties.AXIS);
            Direction.Axis myAxis = dir.getAxis();
            if (myAxis == Direction.Axis.X) {
                return Optional.of(state.with(BlockStateProperties.AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.Z : Direction.Axis.Y));
            } else if (myAxis == Direction.Axis.Z) {
                return Optional.of(state.with(BlockStateProperties.AXIS, targetAxis == Direction.Axis.Y ? Direction.Axis.X : Direction.Axis.Y));
            }
        }
        if (block instanceof StairBlock) {
            Direction facing = state.get(StairBlock.FACING);
            if (facing.getAxis() == dir.getAxis()) return Optional.empty();

            boolean flipped = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ^ ccw;
            Half half = state.get(StairBlock.HALF);
            boolean top = half == Half.TOP;
            boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;

            if ((top ^ positive) ^ flipped) {
                half = top ? Half.BOTTOM : Half.TOP;
            } else {
                facing = facing.getOpposite();
            }

            return Optional.of(state.with(StairBlock.HALF, half).with(StairBlock.FACING, facing));
        }
        if (state.hasProperty(SlabBlock.TYPE)) {
            SlabType type = state.get(SlabBlock.TYPE);
            if (type == SlabType.DOUBLE) return Optional.empty();
            return Optional.of(state.with(SlabBlock.TYPE, type == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM));
        }
        if (state.hasProperty(TrapDoorBlock.HALF)) {
            return Optional.of(state.cycle(TrapDoorBlock.HALF));
        }
        return Optional.empty();
    }

    //check if it has facing property
    private static BlockState rotateBlockStateOnAxis(BlockState state, Direction axis, boolean ccw) {
        Vec3 targetNormal = MathHelperUtils.V3itoV3(state.get(BlockStateProperties.FACING).getNormal());
        Vec3 myNormal = MathHelperUtils.V3itoV3(axis.getNormal());
        if (!ccw) targetNormal = targetNormal.scale(-1);

        Vec3 rotated = myNormal.cross(targetNormal);
        // not on same axis, can rotate
        if (rotated != Vec3.ZERO) {
            Direction newDir = Direction.getNearest(rotated.x(), rotated.y(), rotated.z());
            return state.with(BlockStateProperties.FACING, newDir);
        }
        return state;
    }

    private static boolean isBlacklisted(BlockState state) {
        // double blocks
        if (state.getBlock() instanceof BedBlock) return true;
        if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
            if (state.get(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE) return true;
        }
        // no piston bases
        if (state.hasProperty(BlockStateProperties.EXTENDED)) {
            if (state.get(BlockStateProperties.EXTENDED)) return true;
        }
        // nor piston arms
        if (state.hasProperty(BlockStateProperties.SHORT)) return true;

        return state.is(ModTags.ROTATION_BLACKLIST);
    }


    private static Optional<Direction> tryRotatingSpecial(Direction face, boolean ccw, BlockPos pos, World level, BlockState state, Vec3 hit) {
        Block b = state.getBlock();
        BlockRotation rot = ccw ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
            int r = state.get(BlockStateProperties.ROTATION_16);
            r += (ccw ? -1 : 1);
            if (r < 0) r += 16;
            r = r % 16;
            level.setBlockState(pos, state.with(BlockStateProperties.ROTATION_16, r), 2);
            return Optional.of(Direction.UP);
        }

        if (state.hasProperty(BlockStateProperties.EXTENDED) && state.get(BlockStateProperties.EXTENDED)) {
            if (state.hasProperty(PistonHeadBlock.FACING)) {
                BlockState newBase = rotateBlockStateOnAxis(state, face, ccw);
                BlockPos headPos = pos.relative(state.get(PistonHeadBlock.FACING));
                if (level.getBlockState(headPos).hasProperty(PistonHeadBlock.SHORT)) {
                    BlockPos newHeadPos = pos.relative(newBase.get(PistonHeadBlock.FACING));
                    if (level.getBlockState(newHeadPos).getMaterial().isReplaceable()) {

                        level.setBlockState(newHeadPos, rotateBlockStateOnAxis(level.getBlockState(headPos), face, ccw), 2);
                        level.setBlockState(pos, newBase, 2);
                        level.removeBlock(headPos, false);
                        return Optional.of(face);
                    }
                }
                return Optional.empty();
            }
        }
        if (state.hasProperty(BlockStateProperties.SHORT)) {
            if (state.hasProperty(PistonHeadBlock.FACING)) {
                BlockState newBase = rotateBlockStateOnAxis(state, face, ccw);
                BlockPos headPos = pos.relative(state.get(PistonHeadBlock.FACING).getOpposite());
                if (level.getBlockState(headPos).hasProperty(PistonBaseBlock.EXTENDED)) {
                    BlockPos newHeadPos = pos.relative(newBase.get(PistonHeadBlock.FACING).getOpposite());
                    if (level.getBlockState(newHeadPos).getMaterial().isReplaceable()) {

                        level.setBlockState(newHeadPos, rotateBlockStateOnAxis(level.getBlockState(headPos), face, ccw), 2);
                        level.setBlockState(pos, newBase, 2);
                        level.removeBlock(headPos, false);
                        return Optional.of(face);
                    }
                }
                return Optional.empty();
            }
        }
        if (b instanceof BedBlock) {
            BlockState newBed = state.rotate(level, pos, rot);
            BlockPos oldPos = pos.relative(getConnectedBedDirection(state));
            BlockPos targetPos = pos.relative(getConnectedBedDirection(newBed));
            if (level.getBlockState(targetPos).getMaterial().isReplaceable()) {
                level.setBlockState(targetPos, level.getBlockState(oldPos).rotate(level, oldPos, rot), 2);
                level.setBlockState(pos, newBed, 2);
                level.removeBlock(oldPos, false);
                return Optional.of(face);
            }
            return Optional.empty();
        }
        if (b instanceof ChestBlock) {
            if (state.get(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockState newChest = state.rotate(level, pos, rot);
                BlockPos oldPos = pos.relative(ChestBlock.getConnectedDirection(state));
                BlockPos targetPos = pos.relative(ChestBlock.getConnectedDirection(newChest));
                if (level.getBlockState(targetPos).getMaterial().isReplaceable()) {
                    BlockState connectedNewState = level.getBlockState(oldPos).rotate(level, oldPos, rot);
                    level.setBlockState(targetPos, connectedNewState, 2);
                    level.setBlockState(pos, newChest, 2);

                    BlockEntity tile = level.getBlockEntity(oldPos);
                    if (tile != null) {
                        NbtCompound tag = tile.saveWithoutMetadata();
                        if (level.getBlockEntity(targetPos) instanceof ChestBlockEntity newChestTile) {
                            newChestTile.load(tag);
                        }
                        tile.setRemoved();
                    }

                    level.setBlockStateAndUpdate(oldPos, Blocks.AIR.getDefaultState ());
                    return Optional.of(face);
                }
            }
            return Optional.empty();
        }
        if (DoorBlock.isWoodenDoor(state)) {
            //TODO: add
            //level.setBlockStateAndUpdate(state.rotate(level, pos, rot));

        }
        return Optional.empty();
    }

    private static Direction getConnectedBedDirection(BlockState bedState) {
        BedPart part = bedState.get(BedBlock.PART);
        Direction dir = bedState.get(BedBlock.FACING);
        return part == BedPart.FOOT ? dir : dir.getOpposite();
    }

    //TODO: add rotation vertical slabs & doors

    private static BlockState rotateFaceBlockHorizontal(Direction dir, boolean ccw, BlockState original) {

        Direction facingDir = original.get(BlockStateProperties.HORIZONTAL_FACING);
        if (facingDir.getAxis() == dir.getAxis()) return original;

        var face = original.get(BlockStateProperties.ATTACH_FACE);
        return switch (face) {
            case FLOOR -> original.with(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                    .with(BlockStateProperties.HORIZONTAL_FACING, ccw ? dir.getClockWise() : dir.getCounterClockWise());
            case CEILING -> original.with(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                    .with(BlockStateProperties.HORIZONTAL_FACING, !ccw ? dir.getClockWise() : dir.getCounterClockWise());
            case WALL -> {
                ccw = ccw ^ (dir.getAxisDirection() != Direction.AxisDirection.POSITIVE);
                yield original.with(BlockStateProperties.ATTACH_FACE,
                        (facingDir.getAxisDirection() == Direction.AxisDirection.POSITIVE) ^ ccw ? AttachFace.CEILING : AttachFace.FLOOR);
            }

        };

    }

}
