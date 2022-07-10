package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.BubbleBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.BlockUtils;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes. ShapeContext ;
import net.minecraft.world.phys.shapes.Entity ShapeContext ;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BubbleBlock extends Block implements EntityBlock {
    public BubbleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext placeContext) {
        return !placeContext.isSecondaryUseActive();
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView blockGetter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView blockGetter, BlockPos p_60557_,  ShapeContext   ShapeContext ) {
        return VoxelShapes.fullCube();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView getter, BlockPos pos,  ShapeContext   ShapeContext ) {
        if ( ShapeContext .isAbove(VoxelShapes.fullCube(), pos, false)
                &&  ShapeContext  instanceof Entity ShapeContext  ec && ec.getEntity() instanceof LivingEntity)
            return VoxelShapes.fullCube();
        else return Shapes.empty();
    }

    @Override
    public void onEntityCollision(BlockState state, World level, BlockPos pos, Entity entity) {
        if (ServerConfigs.cached.BUBBLE_BREAK && level instanceof ServerWorld serverLevel) {
            breakBubble(serverLevel, pos,state);
        }
    }

    @Override
    protected void spawnDestroyParticles(World level, Player player, BlockPos pos, BlockState state) {
        makeParticle(pos, level);
        playBreakSound(state, level, pos, player);
    }

    private void playBreakSound(BlockState state, World level, BlockPos pos, Player player){
        SoundType soundtype = state.getSoundType(level, pos, null);
        level.playSound(player,pos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    public void makeParticle(BlockPos pos, World level) {
        level.addParticle(ModRegistry.BUBBLE_BLOCK_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
    }

    public void sendParticles(BlockPos pos, ServerWorld level) {
        level.sendParticles(ModRegistry.BUBBLE_BLOCK_PARTICLE.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1, 0, 0, 0, 0);
    }

    public void breakBubble(ServerWorld level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
        sendParticles(pos, level);
        playBreakSound(state, level, pos, null);
    }

    @Override
    public void onSteppedOn(World level, BlockPos pos, BlockState state, Entity entity) {
        if (ServerConfigs.cached.BUBBLE_BREAK) level.scheduleTick(pos, this, 5);
        super.onSteppedOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(World level, BlockState state, BlockPos pos, Entity p_152429_, float v) {
        super.fallOn(level, state, pos, p_152429_, v);
        if (!level.isClient() && ServerConfigs.cached.BUBBLE_BREAK) {
            if (v > 3) breakBubble((ServerLevel) level, pos, state);
            else level.scheduleTick(pos, this, (int) MathHelper.clamp(7 - v / 2, 1, 5));

        }
    }

    @Override
    public void tick(BlockState state, ServerWorld serverLevel, BlockPos pos, Random random) {
        breakBubble(serverLevel, pos, state);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BubbleBlockTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World p_153212_, BlockState state, BlockEntityType<T> tBlockEntityType) {
        return BlockUtils.getTicker(tBlockEntityType, ModRegistry.BUBBLE_BLOCK_TILE.get(), BubbleBlockTile::tick);
    }
}
