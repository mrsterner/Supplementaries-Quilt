package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.api.ISoftFluidConsumer;
import net.mehvahdjukaar.selene.fluids.*;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FaucetBlock;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.integration.inspirations.CauldronPlugin;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.mehvahdjukaar.supplementaries.setup.ModSoftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

public class FaucetBlockTile extends BlockEntity {
    private static final int COOLDOWN = 20;

    private int transferCooldown = 0;
    public final SoftFluidHolder tempFluidHolder = new SoftFluidHolder(5);

    public FaucetBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FAUCET_TILE.get(), pos, state);
    }


    public void updateLight() {
        if (this.level == null) return;
        int light = this.tempFluidHolder.getFluid().get().getLuminosity();
        if (light != 0) light = (int) MathHelper.clamp(light / 2f, 1, 7);
        if (light != this.getBlockState().get(FaucetBlock.LIGHT_LEVEL)) {
            this.level.setBlockState(this.worldPosition, this.getBlockState().with(FaucetBlock.LIGHT_LEVEL, light), 2);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(0, -1, 0), getBlockPos().offset(1, 1, 1));
    }

    public static void tick(World pLevel, BlockPos pPos, BlockState pState, FaucetBlockTile tile) {
        if (tile.transferCooldown > 0) {
            tile.transferCooldown--;
        } else if (tile.isOpen()) {
            boolean flag = tile.tryExtract(pLevel, pPos, pState, true);
            if (flag) {
                tile.transferCooldown += COOLDOWN;
            }
        }
    }

    //------fluids------

    //TODO: make it connect with pipes
    //returns true if it has water
    public boolean updateContainedFluidVisuals(World level, BlockPos pos, BlockState state) {
        //fluid stuff
        FluidState fluidState = level.getFluidState(pos.relative(state.get(FaucetBlock.FACING).getOpposite()));
        if (!fluidState.isEmpty()) {
            this.tempFluidHolder.fill(SoftFluidRegistry.fromForgeFluid(fluidState.getType()));
            this.updateLight();
            return true;
        }
        boolean r = this.tryExtract(level, pos, state, false);
        this.updateLight();
        return r;
    }

    //TODO: fix trasnfer to cauldrons
    @SuppressWarnings("ConstantConditions")
    private boolean tryExtract(World level, BlockPos pos, BlockState state, boolean doTransfer) {
        Direction dir = state.get(FaucetBlock.FACING);
        BlockPos behind = pos.relative(dir.getOpposite());
        BlockState backState = level.getBlockState(behind);
        Block backBlock = backState.getBlock();
        this.tempFluidHolder.clear();
        if (backState.isAir()) {
            return false;
        } else if (backBlock instanceof ISoftFluidProvider provider) {
            Pair<SoftFluid, NbtCompound> stack = provider.getProvidedFluid(level, backState, behind);
            this.prepareToTransferBottle(stack.getLeft(), stack.getRight());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                provider.consumeProvidedFluid(level, backState, behind, this.tempFluidHolder.getFluid().get(), this.tempFluidHolder.getNbt(), 1);
                return true;
            }
        }
        //beehive
        else if (backState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (backState.get(BlockStateProperties.LEVEL_HONEY) == 5) {
                this.prepareToTransferBottle(SoftFluidRegistry.HONEY.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlockState(behind, backState.with(BlockStateProperties.LEVEL_HONEY,
                            backState.get(BlockStateProperties.LEVEL_HONEY) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //TODO: move in compat class
        //honey pot
        else if (CompatHandler.buzzier_bees && backState.hasProperty(BlockProperties.HONEY_LEVEL_POT)) {
            if (backState.get(BlockProperties.HONEY_LEVEL_POT) > 0) {
                this.prepareToTransferBottle(SoftFluidRegistry.HONEY.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlockState(behind, backState.with(BlockProperties.HONEY_LEVEL_POT,
                            backState.get(BlockProperties.HONEY_LEVEL_POT) - 1), 3);
                    return true;
                }
            }
            return false;
        }
        //sap log
        else if (CompatHandler.autumnity && (backBlock == CompatObjects.SAPPY_MAPLE_LOG.get() || backBlock == CompatObjects.SAPPY_MAPLE_WOOD.get())) {
            this.prepareToTransferBottle(ModSoftFluids.SAP.get());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                Block log = ForgeRegistries.BLOCKS.get(new ResourceLocation(backBlock.getRegistryName().toString().replace("sappy", "stripped")));
                if (log != null) {

                    level.setBlockState(behind, log.withPropertiesOf(backState), 3);
                }
                return true;
            }
        }/* else if (CompatHandler.malum && MalumPlugin.isSappyLog(backBlock)) {
            this.prepareToTransferBottle(MalumPlugin.getSap(backBlock));
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                MalumPlugin.extractSap(level, backState, behind);
                return true;
            }
        }*/
        //cauldron
        else if (backBlock == Blocks.WATER_CAULDRON) {
            int waterWorld = backState.get(BlockStateProperties.LEVEL_CAULDRON);
            if (waterWorld > 0) {
                if (CompatHandler.inspirations) {
                    return CauldronPlugin.doStuff(level.getBlockEntity(behind), this.tempFluidHolder, doTransfer, () -> this.tryFillingBlockBelow(level, pos));
                }

                this.prepareToTransferBottle(SoftFluidRegistry.WATER.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    if (waterWorld > 1) {
                        level.setBlockState(behind, backState.with(BlockStateProperties.LEVEL_CAULDRON,
                                waterWorld - 1), 3);
                    } else level.setBlockState(behind, Blocks.CAULDRON.getDefaultState (), 3);
                    return true;
                }
            }
        } else if (backBlock == Blocks.LAVA_CAULDRON) {
            this.prepareToTransferBucket(SoftFluidRegistry.LAVA.get());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                level.setBlockState(behind, Blocks.CAULDRON.getDefaultState (), 3);
                this.transferCooldown += COOLDOWN * 3;
                return true;
            }
        } else if (backBlock == Blocks.POWDER_SNOW_CAULDRON) {
            int waterWorld = backState.get(BlockStateProperties.LEVEL_CAULDRON);
            if (waterWorld == 3) {
                this.prepareToTransferBucket(SoftFluidRegistry.POWDERED_SNOW.get());
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    level.setBlockState(behind, Blocks.CAULDRON.getDefaultState (), 3);
                    this.transferCooldown += COOLDOWN * 3;
                    return true;
                }
            }
        }

        //soft fluid holders
        BlockEntity tileBack = level.getBlockEntity(behind);
        if (tileBack != null) {
            if (tileBack instanceof ISoftFluidHolder holder && holder.canInteractWithFluidHolder()) {
                SoftFluidHolder fluidHolder = holder.getSoftFluidHolder();
                this.tempFluidHolder.copy(fluidHolder);
                this.tempFluidHolder.setCount(2);
                if (doTransfer && tryFillingBlockBelow(level, pos)) {
                    fluidHolder.decrement(1);
                    tileBack.setChanged();
                    return true;
                }
            }
            //forge tanks
            else {
                IFluidHandler handlerBack = tileBack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir).orElse(null);
                //TODO: fix create fluid int bug
                if (handlerBack != null && !backBlock.getRegistryName().getPath().equals("fluid_interface")) {
                    //only works in 250 increment
                    if (handlerBack.getFluidInTank(0).getAmount() < 250) return false;
                    this.tempFluidHolder.copy(handlerBack);
                    this.tempFluidHolder.setCount(2);
                    if (doTransfer && tryFillingBlockBelow(level, pos)) {
                        handlerBack.drain(250, IFluidHandler.FluidAction.EXECUTE);
                        tileBack.setChanged();
                        return true;
                    }
                }
            }
            if (!doTransfer) return !this.tempFluidHolder.isEmpty();
            //pull other items from containers
            return this.spillItemsFromInventory(level, pos, dir, tileBack);
        } else if (level.getFluidState(behind).getType() == Fluids.WATER) {
            //Unlimited water!!
            this.prepareToTransferBottle(SoftFluidRegistry.WATER.get());
            if (doTransfer && tryFillingBlockBelow(level, pos)) {
                return true;
            }
            return true;
        }

        if (!doTransfer) return !this.tempFluidHolder.isEmpty();
        return false;
    }
    //TODO: maybe add registry block-> interaction

    private void prepareToTransferBottle(SoftFluid softFluid) {
        this.tempFluidHolder.fill(softFluid);
        this.tempFluidHolder.setCount(2);
    }

    private void prepareToTransferBottle(SoftFluid softFluid, NbtCompound tag) {
        this.tempFluidHolder.fill(softFluid, tag);
        this.tempFluidHolder.setCount(2);
    }

    private void prepareToTransferBucket(SoftFluid softFluid) {
        this.tempFluidHolder.fill(softFluid);
    }

    //sf->ff/sf
    @SuppressWarnings("ConstantConditions")
    private boolean tryFillingBlockBelow(World level, BlockPos pos) {
        SoftFluid softFluid = this.tempFluidHolder.getFluid().get();
        //can't full below if empty
        if (softFluid.isEmpty()) return false;

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();


        //consumer
        if (belowBlock instanceof ISoftFluidConsumer consumer) {
            return consumer.tryAcceptingFluid(level, belowState, below, softFluid, this.tempFluidHolder.getNbt(), 1);
        }
        //sponge voiding
        if (belowBlock == Blocks.SPONGE) {
            return true;
        }
        //beehive
        else if (softFluid == SoftFluidRegistry.HONEY.get()) {

            //beehives
            if (belowState.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
                int h = belowState.get(BlockStateProperties.LEVEL_HONEY);
                if (h == 0) {
                    level.setBlockState(below, belowState.with(BlockStateProperties.LEVEL_HONEY, 5), 3);
                    return true;
                }
                return false;
            }
            //honey pot
            else if (CompatHandler.buzzier_bees && belowState.hasProperty(BlockProperties.HONEY_LEVEL_POT)) {
                int h = belowState.get(BlockProperties.HONEY_LEVEL_POT);
                if (h < 4) {
                    level.setBlockState(below, belowState.with(BlockProperties.HONEY_LEVEL_POT, h + 1), 3);
                    return true;
                }
                return false;
            }
        } else if (softFluid == SoftFluidRegistry.XP.get() && belowState.isAir()) {
            this.dropXP(level, pos);
            return true;
        } else if (belowBlock instanceof AbstractCauldronBlock) {
            //if any other mod adds a cauldron tile this will crash
            if (CompatHandler.inspirations) {
                return CauldronPlugin.tryAddFluid(level.getBlockEntity(below), this.tempFluidHolder);
            } else if (softFluid == SoftFluidRegistry.WATER.get()) {
                //TODO: finish
                if (belowBlock == Blocks.WATER_CAULDRON) {
                    int levels = belowState.get(BlockStateProperties.LEVEL_CAULDRON);
                    if (levels < 3) {
                        level.setBlockState(below, belowState.with(BlockStateProperties.LEVEL_CAULDRON, levels + 1), 3);
                        return true;
                    }
                    return false;
                } else if (belowBlock instanceof CauldronBlock) {
                    level.setBlockState(below, Blocks.WATER_CAULDRON.getDefaultState ().with(BlockStateProperties.LEVEL_CAULDRON, 1), 3);
                    return true;
                }
            } else if (softFluid == SoftFluidRegistry.LAVA.get()) {
                if (belowBlock instanceof CauldronBlock && this.tempFluidHolder.getCount() == 5) {
                    level.setBlockState(below, Blocks.LAVA_CAULDRON.getDefaultState (), 3);
                    return true;
                }
            } else if (softFluid == SoftFluidRegistry.POWDERED_SNOW.get()) {
                if (belowBlock instanceof CauldronBlock && this.tempFluidHolder.getCount() == 5) {
                    level.setBlockState(below, Blocks.POWDER_SNOW_CAULDRON.getDefaultState ()
                            .with(PowderSnowCauldronBlock.LEVEL, 3), 3);
                    return true;
                }
            }
            return false;
        }


        //default behavior
        boolean result;
        //soft fluid holders
        BlockEntity tileBelow = level.getBlockEntity(below);
        if (tileBelow instanceof ISoftFluidHolder holder) {
            SoftFluidHolder fluidHolder = holder.getSoftFluidHolder();
            result = this.tempFluidHolder.tryTransferFluid(fluidHolder, this.tempFluidHolder.getCount() - 1);
            if (result) {
                tileBelow.setChanged();
                this.tempFluidHolder.fillCount();
            }
            return result;
        }
        if (tileBelow != null) {
            //forge tanks
            IFluidHandler handlerDown = tileBelow.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).orElse(null);
            if (handlerDown != null) {
                result = this.tempFluidHolder.tryTransferToFluidTank(handlerDown, this.tempFluidHolder.getCount() - 1);
                if (result) {
                    tileBelow.setChanged();
                    this.tempFluidHolder.fillCount();
                }
                return result;
            }
        }

        return false;
    }

    private void dropXP(World level, BlockPos pos) {
        int i = 3 + level.random.nextInt(5) + level.random.nextInt(5);
        while (i > 0) {
            int xp = ExperienceOrb.getExperienceValue(i);
            i -= xp;
            ExperienceOrb orb = new ExperienceOrb(level, pos.getX() + 0.5, pos.getY() - 0.125f, pos.getZ() + 0.5, xp);
            orb.setDeltaMovement(new Vec3(0, 0, 0));
            level.addFreshEntity(orb);
        }
        float f = (level.random.nextFloat() - 0.5f) / 4f;
        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
    }


    //------end-fluids------

    public boolean isOpen() {
        return (this.getBlockState().get(BlockStateProperties.POWERED) ^ this.getBlockState().get(BlockStateProperties.ENABLED));
    }

    public boolean hasWater() {
        return this.getBlockState().get(FaucetBlock.HAS_WATER);
    }

    public boolean isConnectedBelow() {
        return this.getBlockState().get(FaucetBlock.HAS_JAR);
    }

    //------items------

    @SuppressWarnings("ConstantConditions")
    public boolean spillItemsFromInventory(World level, BlockPos pos, Direction dir, BlockEntity tile) {
        //TODO: maybe add here insertion in containers below
        if (this.isConnectedBelow()) return false;
        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
        if (itemHandler != null) {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                ItemStack itemstack = itemHandler.getStackInSlot(slot);
                if (!itemstack.isEmpty()) {
                    ItemStack extracted = itemHandler.extractItem(slot, 1, false);
                    //empty stack means it can't extract from inventory
                    if (!extracted.isEmpty()) {
                        tile.setChanged();
                        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, extracted);
                        drop.setDeltaMovement(new Vec3(0, 0, 0));
                        level.addFreshEntity(drop);
                        float f = (level.random.nextFloat() - 0.5f) / 4f;
                        level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.3F, 0.5f + f);
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        this.transferCooldown = compound.getInt("TransferCooldown");
        this.tempFluidHolder.load(compound);
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        tag.putInt("TransferCooldown", this.transferCooldown);
        this.tempFluidHolder.save(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

}
