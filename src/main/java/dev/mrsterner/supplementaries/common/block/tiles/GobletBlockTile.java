package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.selene.fluids.ISoftFluidHolder;
import net.mehvahdjukaar.selene.fluids.SoftFluidHolder;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.world.Hand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GobletBlockTile extends BlockEntity implements ISoftFluidHolder, IOwnerProtected {

    private UUID owner = null;

    public SoftFluidHolder fluidHolder;

    public GobletBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GOBLET_TILE.get(), pos, state);
        int CAPACITY = 1;
        this.fluidHolder = new SoftFluidHolder(CAPACITY);
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock());
        int light = this.fluidHolder.getFluid().get().getLuminosity();
        if (light != this.getBlockState().get(BlockProperties.LIGHT_LEVEL_0_15)) {
            this.level.setBlockState(this.worldPosition, this.getBlockState().with(BlockProperties.LIGHT_LEVEL_0_15, light), 2);
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, Hand hand) {

        //interact with fluid holder
        if (this.fluidHolder.interactWithPlayer(player, hand, level, worldPosition)) {
            return true;
        }
        //empty hand: eat food
        if (!player.isShiftKeyDown()) {
            //from drink
            if (ServerConfigs.cached.GOBLET_DRINK) {
                boolean b = this.fluidHolder.tryDrinkUpFluid(player, this.level);
                if(b && player instanceof ServerPlayerEntity serverPlayer){
                    Advancement advancement = level.getServer().getAdvancements().getAdvancement(new ResourceLocation("supplementaries:nether/goblet"));
                    if(advancement != null){
                        serverPlayer.getAdvancements().award(advancement, "unlock");
                    }
                }
                return b;
            }
        }
        return false;
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        this.fluidHolder.load(compound);
        this.loadOwner(compound);
    }

    @Override
    public void saveAdditional(NbtCompound tag) {
        super.saveAdditional(tag);
        this.fluidHolder.save(tag);
        this.saveOwner(tag);
    }

    @Override
    public SoftFluidHolder getSoftFluidHolder() {
        return this.fluidHolder;
    }
}
