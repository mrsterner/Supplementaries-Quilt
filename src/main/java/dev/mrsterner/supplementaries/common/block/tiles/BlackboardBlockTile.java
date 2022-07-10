package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.client.gui.BlackBoardGui;
import net.mehvahdjukaar.supplementaries.client.gui.IScreenProvider;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager.BlackboardKey;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BlackboardBlockTile extends BlockEntity implements IOwnerProtected, IScreenProvider {

    public static final ModelProperty<BlackboardKey> BLACKBOARD = BlockProperties.BLACKBOARD;

    private UUID owner = null;
    private boolean waxed = false;
    private byte[][] pixels = new byte[16][16];

    //client side
    private BlackboardKey textureKey = null;

    public BlackboardBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BLACKBOARD_TILE.get(), pos, state);
        this.clear();
    }

    @Override
    public IModelData getModelData() {
        //return data;
        return new ModelDataMap.Builder()
                .withInitial(BLACKBOARD, getTextureKey())
                .build();
    }

    public BlackboardKey getTextureKey() {
        if (textureKey == null) refreshTexture();
        return textureKey;
    }

    public void refreshTexture() {
        this.textureKey = new BlackboardKey(this.pixels);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        refreshTexture();
        ModelDataManager.requestModelDataRefresh(this);
        //update other clients (we are already on the client here)
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }//if(this.level != null && this.level.isClient()) refreshTexture();

    //I need this for when it's changed manually
    @Override
    public void setChanged() {
        if (this.level == null || this.level.isClient()) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        super.setChanged();
    }

    @Override
    public void load(NbtCompound compound) {
        super.load(compound);
        loadFromTag(compound);
        this.loadOwner(compound);
    }

    @Override
    public void saveAdditional(NbtCompound compound) {
        super.saveAdditional(compound);
        this.savePixels(compound);
        this.saveOwner(compound);
    }

    public NbtCompound savePixels(NbtCompound compound) {
        if (this.waxed) compound.putBoolean("Waxed", true);
        compound.putLongArray("Pixels", packPixels(pixels));
        return compound;
    }

    public void loadFromTag(NbtCompound compound) {
        this.waxed = compound.contains("Waxed") && compound.getBoolean("Waxed");
        this.pixels = new byte[16][16];
        if (compound.contains("Pixels")) {
            this.pixels = unpackPixels(compound.getLongArray("Pixels"));
        }
    }

    public static long[] packPixels(byte[][] pixels) {
        long[] packed = new long[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            long l = 0;
            for (int j = 0; j < pixels[i].length; j++) {
                l = l | (((long) (pixels[i][j] & 15)) << j * 4);
            }
            packed[i] = l;
        }
        return packed;
    }

    public static byte[][] unpackPixels(long[] packed) {
        byte[][] bytes = new byte[16][16];
        for (int i = 0; i < packed.length; i++) {
            for (int j = 0; j < 16; j++) {
                bytes[i][j] = (byte) ((packed[i] >> j * 4) & 15);
            }
        }
        return bytes;
    }

    public void clear() {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                this.pixels[x][y] = 0;
            }
        }
    }

    public boolean isEmpty() {
        boolean flag = false;
        for (byte[] pixel : pixels) {
            for (byte b : pixel) {
                if (b != 0) {
                    flag = true;
                    break;
                }
            }
        }
        return !flag;
    }

    public void setPixel(int x, int y, byte b) {
        this.pixels[x][y] = b;
    }

    public byte getPixel(int xx, int yy) {
        return this.pixels[xx][yy];
    }

    public void setPixels(byte[][] pixels) {
        this.pixels = pixels;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public NbtCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public Direction getDirection() {
        return this.getBlockState().get(NoticeBoardBlock.FACING);
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
    public void openScreen(World level, BlockPos pos, Player player) {
        BlackBoardGui.open(this);
    }


    public void setWaxed(boolean b) {
        this.waxed = b;
    }

    public boolean isWaxed() {
        return this.waxed;
    }
}
