package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SpringLauncherArmBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpringLauncherBlock extends Block {
    protected static final VoxelShape PISTON_BASE_EAST_AABB = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_WEST_AABB = Block.createCuboidShape(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_SOUTH_AABB = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape PISTON_BASE_NORTH_AABB = Block.createCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_UP_AABB = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    protected static final VoxelShape PISTON_BASE_DOWN_AABB = Block.createCuboidShape(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // is base only?
    public SpringLauncherBlock(Properties properties){
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(EXTENDED, false));
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return state.get(EXTENDED)?PushReaction.BLOCK:PushReaction.NORMAL;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView reader, BlockPos pos) {
        return state.get(EXTENDED);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return state.get(EXTENDED);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
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
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState ().with(FACING, context.getPlayerLookDirection ().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos,  ShapeContext  context) {
        if (state.get(EXTENDED)) {
            return switch (state.get(FACING)) {
                case DOWN -> PISTON_BASE_DOWN_AABB;
                default -> PISTON_BASE_UP_AABB;
                case NORTH -> PISTON_BASE_NORTH_AABB;
                case SOUTH -> PISTON_BASE_SOUTH_AABB;
                case WEST -> PISTON_BASE_WEST_AABB;
                case EAST -> PISTON_BASE_EAST_AABB;
            };
        } else {
            return VoxelShapes.fullCube();
        }
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.checkForMove(state, worldIn, pos);
    }

    public void checkForMove(BlockState state, World world, BlockPos pos) {
        if (!world.isClient()()) {
            boolean flag = this.shouldBeExtended(world, pos, state.get(FACING));
            BlockPos offset = pos.offset(state.get(FACING).getNormal());
            if (flag && !state.get(EXTENDED)) {
                boolean flag2 = false;
                BlockState targetBlock = world.getBlockState(offset);
                if (targetBlock.getPistonPushReaction() == PushReaction.DESTROY || targetBlock.isAir()) {
                    BlockEntity blockEntity = targetBlock.hasBlockEntity() ? world.getBlockEntity(offset) : null;
                    dropResources(targetBlock, world, offset, blockEntity);
                    flag2 = true;
                }
                /*
                 * else if (targetBlock.getBlock() instanceof FallingBlock &&
                 * world.getBlockState(offset.add(state.get(FACING).getDirectionVec())).isAir(
                 * world, offset)){ FallingBlockEntity fallingblockentity = new
                 * FallingBlockEntity(world, (double)offset.getX() + 0.5D, (double)offset.getY() ,
                 * (double)offset.getZ() + 0.5D, world.getBlockState(offset));
                 *
                 * world.addEntity(fallingblockentity); flag2=true; }
                 */
                if (flag2) {
                    world.setBlockStateAndUpdate(offset, ModRegistry.SPRING_LAUNCHER_ARM.get().getDefaultState ()
                            .with(SpringLauncherArmBlock.EXTENDING, true).with(FACING, state.get(FACING)));
                    world.setBlockStateAndUpdate(pos, state.with(EXTENDED, true));
                    world.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.53F,
                            world.random.nextFloat() * 0.25F + 0.45F);
                    world.gameEvent(GameEvent.PISTON_EXTEND, pos);
                }
            } else if (!flag && state.get(EXTENDED)) {
                BlockState bs = world.getBlockState(offset);
                if (bs.getBlock() instanceof SpringLauncherHeadBlock && state.get(FACING) == bs.get(FACING)) {
                    // world.setBlockStateState(offset, Blocks.AIR.getDefaultState(), 3);
                    world.setBlockStateAndUpdate(offset, ModRegistry.SPRING_LAUNCHER_ARM.get().getDefaultState ()
                                    .with(SpringLauncherArmBlock.EXTENDING, false).with(FACING, state.get(FACING)));
                    world.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.53F,
                            world.random.nextFloat() * 0.15F + 0.45F);
                    world.gameEvent(GameEvent.PISTON_CONTRACT, pos);
                } else if (bs.getBlock() instanceof SpringLauncherArmBlock
                        && state.get(FACING) == bs.get(FACING)) {
                    if (world.getBlockEntity(offset) instanceof SpringLauncherArmBlockTile) {
                        world.scheduleTick(pos, world.getBlockState(pos).getBlock(), 1);
                    }
                }
            }
        }
    }

    // piston code
    private boolean shouldBeExtended(World worldIn, BlockPos pos, Direction facing) {
        for (Direction direction : Direction.values()) {
            if (direction != facing && worldIn.hasSignal(pos.relative(direction), direction)) {
                return true;
            }
        }
        if (worldIn.hasSignal(pos, Direction.DOWN)) {
            return true;
        } else {
            BlockPos blockpos = pos.above();
            for (Direction direction1 : Direction.values()) {
                if (direction1 != Direction.DOWN && worldIn.hasSignal(blockpos.relative(direction1), direction1)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        this.checkForMove(state, world, pos);
    }
}
