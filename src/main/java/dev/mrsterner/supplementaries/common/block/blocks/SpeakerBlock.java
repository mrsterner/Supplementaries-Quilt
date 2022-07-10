package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.client.gui.SpeakerBlockGui;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.Criteria;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SpeakerBlock extends Block implements EntityBlock{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ANTIQUE = BlockProperties.ANTIQUE;

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH)
                .with(ANTIQUE, false).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, ANTIQUE);
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
        return this.getDefaultState ().with(FACING, context.getPlayerFacing().getOpposite());
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        if (worldIn.getBlockEntity(pos) instanceof SpeakerBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            BlockUtils.addOptionalOwnership(placer, tile);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborUpdate(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state, world, pos);
    }

    public void updatePower(BlockState state, World world, BlockPos pos) {
        if (!world.isClient()()) {
            boolean pow = world.hasNeighborSignal(pos);
            // state changed
            if (pow != state.get(POWERED)) {
                world.setBlockState(pos, state.with(POWERED, pow), 3);
                // can I emit sound?
                Direction facing = state.get(FACING);
                if (pow && world.isEmptyBlock(pos.relative(facing))) {
                    if (world.getBlockEntity(pos) instanceof SpeakerBlockTile tile) {
                        tile.sendMessage();
                        world.gameEvent(GameEvent.RING_BELL, pos);
                    }
                }
            }
        }
    }

    @Override
    public ActionResult use(BlockState state, World level, BlockPos pos, Player player, Hand hand,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SpeakerBlockTile tile && tile.isAccessibleBy(player)) {
            //ink
            if (player.getAbilities().mayBuild && !state.get(ANTIQUE)) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.is(ModRegistry.ANTIQUE_INK.get())) {
                    level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                        level.setBlockStateAndUpdate(pos, state.with(ANTIQUE, true));
                    }
                    return ActionResult.success(level.isClient());
                }
            }
            // client
            if (level.isClient()) {
                SpeakerBlockGui.open(tile);
                return ActionResult.SUCCESS;
            }
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
        return new SpeakerBlockTile(pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        if (eventID == 0) {
            Direction facing = state.get(FACING);
            world.addParticle(ModRegistry.SPEAKER_SOUND.get(), pos.getX() + 0.5 + facing.getStepX() * 0.725, pos.getY() + 0.5,
                    pos.getZ() + 0.5 + facing.getStepZ() * 0.725, (double) world.random.nextInt(24) / 24.0D, 0.0D, 0.0D);
            return true;
        }
        return super.triggerEvent(state, world, pos, eventID, eventParam);
    }

}
