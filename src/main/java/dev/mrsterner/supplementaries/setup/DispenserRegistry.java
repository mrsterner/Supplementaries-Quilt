package net.mehvahdjukaar.supplementaries.setup;

import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.util.DispenserHelper;
import net.mehvahdjukaar.selene.util.DispenserHelper.AddItemToInventoryBehavior;
import net.mehvahdjukaar.selene.util.DispenserHelper.AdditionalDispenserBehavior;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.api.ILightable;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BambooSpikesBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.LightUpBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PancakeBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.common.capabilities.mobholder.BucketHelper;
import net.mehvahdjukaar.supplementaries.common.entities.BombEntity;
import net.mehvahdjukaar.supplementaries.common.entities.PearlMarker;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.mehvahdjukaar.supplementaries.common.entities.ThrowableBrickEntity;
import net.mehvahdjukaar.supplementaries.common.items.BombItem;
import net.mehvahdjukaar.supplementaries.common.items.DispenserMinecartItem;
import net.mehvahdjukaar.supplementaries.common.items.ItemsUtil;
import net.mehvahdjukaar.supplementaries.common.items.SoapItem;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ActionResult;
import net.minecraft.world.ActionResultHolder;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class DispenserRegistry {

    public static void registerBehaviors() {

        if (!RegistryConfigs.Reg.DISPENSERS.get()) return;

        DispenserHelper.registerCustomBehavior(new EnderPearlBehavior());

        //dispenser minecart
        if(RegistryConfigs.Reg.DISPENSER_MINECART_ENABLED.get()){
            DispenserBlock.registerBehavior(ModRegistry.DISPENSER_MINECART_ITEM.get(), DispenserMinecartItem.DISPENSE_ITEM_BEHAVIOR);
        }

        //fodder
        if (RegistryConfigs.Reg.FODDER_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.FODDER.get());
        }
        //bubble
        if (RegistryConfigs.Reg.BUBBLE_BLOWER_ENABLED.get()) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.BUBBLE_BLOCK.get());
        }

        if(RegistryConfigs.Reg.SACK_ENABLED.get()){
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.SACK.get());
        }

        //jar
        boolean jar = RegistryConfigs.Reg.JAR_ENABLED.get();
        if (jar) {
            DispenserHelper.registerPlaceBlockBehavior(ModRegistry.JAR_ITEM.get());
            DispenserHelper.registerCustomBehavior(new AddItemToInventoryBehavior(Items.COOKIE));
        }

        DispenserHelper.registerCustomBehavior(new FlintAndSteelDispenserBehavior(Items.FLINT_AND_STEEL));
        DispenserHelper.registerCustomBehavior(new BambooSpikesDispenserBehavior(Items.LINGERING_POTION));
        DispenserHelper.registerCustomBehavior(new PancakesDispenserBehavior(Items.HONEY_BOTTLE));

        if (ServerConfigs.cached.THROWABLE_BRICKS_ENABLED) {
            Registry.ITEM.getTagOrEmpty(ModTags.BRICKS).iterator().forEachRemaining(h->
                    DispenserHelper.registerCustomBehavior(new ThrowableBricksDispenserBehavior(h.value()))
            );

        }

        //bomb
        if (RegistryConfigs.Reg.BOMB_ENABLED.get()) {
            //default behaviors for modded items
            var bombBehavior = new BombsDispenserBehavior();
            DispenserBlock.registerBehavior(ModRegistry.BOMB_ITEM.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_ITEM_ON.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_BLUE_ITEM_ON.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_SPIKY_ITEM.get(), bombBehavior);
            DispenserBlock.registerBehavior(ModRegistry.BOMB_SPIKY_ITEM_ON.get(), bombBehavior);
        }
        //gunpowder
        if (ServerConfigs.cached.PLACEABLE_GUNPOWDER) {
            DispenserHelper.registerCustomBehavior(new GunpowderBehavior(Items.GUNPOWDER));
        }
        if (RegistryConfigs.Reg.ROPE_ARROW_ENABLED.get()) {

            DispenserBlock.registerBehavior(ModRegistry.ROPE_ARROW_ITEM.get(), new AbstractProjectileDispenseBehavior() {
                protected ProjectileEntity getProjectile(World world, Position pos, ItemStack stack) {
                    NbtCompound com = stack.getTag();
                    int charges = stack.getMaxDamage();
                    if (com != null) {
                        if (com.contains("Damage")) {
                            charges = charges - com.getInt("Damage");
                        }
                    }
                    RopeArrowEntity arrow = new RopeArrowEntity(world, pos.x(), pos.y(), pos.z(), charges);
                    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return arrow;
                }
            });

        }

        if (RegistryConfigs.Reg.SOAP_ENABLED.get()) {
            DispenserHelper.registerCustomBehavior(new SoapBehavior(ModRegistry.SOAP.get()));
        }

        boolean axe = ServerConfigs.tweaks.AXE_DISPENSER_BEHAVIORS.get();
        if (axe || jar) {
            for (Item i : ForgeRegistries.ITEMS) {
                try {
                    if (jar && BucketHelper.isFishBucket(i)) {
                        DispenserHelper.registerCustomBehavior(new FishBucketJarDispenserBehavior(i));
                    }
                    if (axe && i instanceof AxeItem) {
                        DispenserHelper.registerCustomBehavior(new AxeDispenserBehavior(i));
                    }
                } catch (Exception e) {
                    Supplementaries.LOGGER.warn("Error white registering dispenser behavior for item {}: {}", i, e);
                }
            }
        }
    }

    private static class AxeDispenserBehavior extends AdditionalDispenserBehavior {

        protected AxeDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld level = source.getWorld();
            BlockPos pos = source.getPos().relative(source.getBlockState().get(DispenserBlock.FACING));
            BlockState state = level.getBlockState(pos);
            Block b = state.getBlock();


            Optional<BlockState> optional = Optional.ofNullable(b.getToolModifiedState(state, level, pos, null, stack, ToolActions.AXE_STRIP));
            if (optional.isPresent()) {
                level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlockState(pos, optional.get(), 11);
                if (stack.hurt(1, level.getRandom(), null)) {
                    stack.setCount(0);
                }
                return ActionResultHolder.success(stack);
            }

            optional = Optional.ofNullable(b.getToolModifiedState(state, level, pos, null, stack, ToolActions.AXE_SCRAPE));
            if (optional.isPresent()) {
                level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.syncWorldEvent(null, 3005, pos, 0);
                level.setBlockState(pos, optional.get(), 11);
                if (stack.hurt(1, level.getRandom(), null)) {
                    stack.setCount(0);
                }
                return ActionResultHolder.success(stack);
            }
            optional = Optional.ofNullable(b.getToolModifiedState(state, level, pos, null, stack, ToolActions.AXE_WAX_OFF));
            if (optional.isPresent()) {
                level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.syncWorldEvent(null, 3004, pos, 0);
                level.setBlockState(pos, optional.get(), 11);
                if (stack.hurt(1, level.getRandom(), null)) {
                    stack.setCount(0);
                }
                return ActionResultHolder.success(stack);
            }

            return ActionResultHolder.fail(stack);
        }
    }

    private static class SoapBehavior extends AdditionalDispenserBehavior {

        protected SoapBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld level = source.getWorld();
            BlockPos pos = source.getPos().relative(source.getBlockState().get(DispenserBlock.FACING));

            if (SoapItem.tryCleaning(stack, level, pos, null)) {
                return ActionResultHolder.success(stack);
            }

            return ActionResultHolder.fail(stack);
        }
    }


    private static class FlintAndSteelDispenserBehavior extends AdditionalDispenserBehavior {

        protected FlintAndSteelDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().get(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof ILightable block) {
                if (block.lightUp(null, state, blockpos, world, LightUpBlock.FireSound.FLINT_AND_STEEL)) {
                    if (stack.hurt(1, world.random, null)) {
                        stack.setCount(0);
                    }
                    return ActionResultHolder.success(stack);
                }
                return ActionResultHolder.fail(stack);
            }
            return ActionResultHolder.pass(stack);
        }
    }

    private static class ThrowableBricksDispenserBehavior extends AdditionalDispenserBehavior {

        protected ThrowableBricksDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            World world = source.getWorld();
            Position dispensePosition = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().get(DispenserBlock.FACING);
            ProjectileEntity projectileEntity = this.getProjectileEntity(world, dispensePosition, stack);
            projectileEntity.shoot(direction.getStepX(), (float) direction.getStepY() + 0.1F, direction.getStepZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
            world.addFreshEntity(projectileEntity);
            stack.decrement(1);
            return ActionResultHolder.success(stack);
        }

        @Override
        protected void playSound(BlockSource source, boolean success) {
            source.getWorld().playSound(null, source.x() + 0.5, source.y() + 0.5, source.z() + 0.5, SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (source.getWorld().getRandom().nextFloat() * 0.4F + 0.8F));
        }

        protected ProjectileEntity getProjectileEntity(World worldIn, Position position, ItemStack stackIn) {
            return new ThrowableBrickEntity(worldIn, position.x(), position.y(), position.z());
        }

        protected float getProjectileInaccuracy() {
            return 7.0F;
        }

        //TODO: fix throwable bricks rendering glitchyness
        protected float getProjectileVelocity() {
            return 0.9F;
        }

    }

    private static class BombsDispenserBehavior extends AbstractProjectileDispenseBehavior {

        public BombsDispenserBehavior() {
        }

        @Override
        protected ProjectileEntity getProjectile(World worldIn, Position position, ItemStack stackIn) {
            return new BombEntity(worldIn, position.x(), position.y(), position.z(), ((BombItem) stackIn.getItem()).getType());
        }

        @Override
        protected float getUncertainty() {
            return 11.0F;
        }

        @Override
        protected float getPower() {
            return 1.3F;
        }
    }

    private static class BambooSpikesDispenserBehavior extends AdditionalDispenserBehavior {

        protected BambooSpikesDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().get(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof BambooSpikesBlock) {
                if (BambooSpikesBlock.tryAddingPotion(state, world, blockpos, stack)) {
                    return ActionResultHolder.success(new ItemStack(Items.GLASS_BOTTLE));
                }
                return ActionResultHolder.fail(stack);
            }

            return ActionResultHolder.pass(stack);
        }
    }

    //TODO: generalize for fluid consumer & put into library
    private static class PancakesDispenserBehavior extends AdditionalDispenserBehavior {

        protected PancakesDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().get(DispenserBlock.FACING));
            BlockState state = world.getBlockState(blockpos);
            if (state.getBlock() instanceof PancakeBlock block) {
                if (block.tryAcceptingFluid(world, state, blockpos, SoftFluidRegistry.HONEY.get(), null, 1)) {
                    return ActionResultHolder.consume(new ItemStack(Items.GLASS_BOTTLE));
                }
                return ActionResultHolder.fail(stack);
            }
            return ActionResultHolder.pass(stack);
        }
    }

    private static class FishBucketJarDispenserBehavior extends AdditionalDispenserBehavior {

        protected FishBucketJarDispenserBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            //this.setSuccessful(false);
            ServerWorld world = source.getWorld();
            BlockPos blockpos = source.getPos().relative(source.getBlockState().get(DispenserBlock.FACING));
            if (world.getBlockEntity(blockpos) instanceof JarBlockTile tile) {
                //TODO: add fish buckets
                if (tile.fluidHolder.isEmpty() && tile.isEmpty()) {
                    if (tile.mobContainer.interactWithBucket(stack, world, blockpos, null, null)) {
                        tile.setChanged();
                        return ActionResultHolder.success(new ItemStack(Items.BUCKET));
                    }
                }
                return ActionResultHolder.fail(stack);
            }
            return ActionResultHolder.pass(stack);
        }
    }

    public static class GunpowderBehavior extends AdditionalDispenserBehavior {

        protected GunpowderBehavior(Item item) {
            super(item);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {

            Direction direction = source.getBlockState().get(DispenserBlock.FACING);
            BlockPos blockpos = source.getPos().relative(direction);
            Direction direction1 = source.getWorld().isEmptyBlock(blockpos.below()) ? direction : Direction.UP;
            ActionResult result = ItemsUtil.place(new DirectionalPlaceContext(source.getWorld(), blockpos, direction, stack, direction1),
                    ModRegistry.GUNPOWDER_BLOCK.get());
            if (result.consumesAction()) return ActionResultHolder.success(stack);

            return ActionResultHolder.fail(stack);
        }
    }

    public static class EnderPearlBehavior extends AdditionalDispenserBehavior {

        protected EnderPearlBehavior() {
            super(Items.ENDER_PEARL);
        }

        @Override
        protected ActionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
            World level = source.getWorld();
            BlockPos pos = source.getPos();

            ThrownEnderpearl pearl = PearlMarker.getPearlToDispense(source, level, pos);


            Direction direction = source.getBlockState().get(DispenserBlock.FACING);

            pearl.shoot(direction.getStepX(), (float)direction.getStepY() + 0.1F, direction.getStepZ(), this.getPower(), this.getUncertainty());
            level.addFreshEntity(pearl);

            stack.decrement(1);

            return ActionResultHolder.success(stack);
        }


        @Override
        protected void playSound(BlockSource source, boolean success) {
            source.getWorld().syncWorldEvent(1002, source.getPos(), 0);
        }

        protected float getUncertainty() {
            return 6.0F;
        }

        protected float getPower() {
            return 1.1F;
        }
    }

}
