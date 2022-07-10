package dev.mrsterner.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.api.ILightable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Hand;
import net.minecraft.world.ActionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.ItemPlacementContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateManager;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public abstract class LightUpBlock extends Block implements ILightable {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LightUpBlock(Properties properties) {
        super(properties);
    }

    public boolean isLit(BlockState state) {
        return state.get(LIT);
    }

    public BlockState toggleLitState(BlockState state, boolean lit) {
        return state.with(LIT, lit);
    }

    @Override
    public boolean canReplace(BlockState state, Fluid fluid) {
        return this.material.isReplaceable();
    }

    @Override
    public ActionResult use(BlockState state, World worldIn, BlockPos pos, Player player, Hand handIn, BlockHitResult hit) {
        return interactWithPlayer(state, worldIn, pos, player, handIn);
    }

    @Override
    public void onProjectileHit(World level, BlockState state, BlockHitResult pHit, ProjectileEntity projectile) {
        BlockPos pos = pHit.getBlockPos();
        interactWithProjectile(level, state, projectile, pos);
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof ProjectileEntity projectile) {
            interactWithProjectile(worldIn, state, projectile, pos);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        boolean flag = context.getWorld().getFluidState(context.getBlockPos()).getType() == Fluids.WATER;
        BlockState state = this.getDefaultState ();
        return toggleLitState(state, !flag);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

}
