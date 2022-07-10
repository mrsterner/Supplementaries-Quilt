package net.mehvahdjukaar.supplementaries.common.network;

import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerBoundSetBlackboardPacket {
    private final BlockPos pos;
    private final byte[][] pixels;

    public ServerBoundSetBlackboardPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.pixels = new byte[16][16];
        for (int i = 0; i < this.pixels.length; i++) {
            this.pixels[i] = buf.readByteArray();
        }
    }

    public ServerBoundSetBlackboardPacket(BlockPos pos, byte[][] pixels) {
        this.pos = pos;
        this.pixels = pixels;
    }

    public static void buffer(ServerBoundSetBlackboardPacket message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        for (int i = 0; i < message.pixels.length; i++) {
            buf.writeByteArray(message.pixels[i]);
        }
    }

    public static void handler(ServerBoundSetBlackboardPacket message, Supplier<NetworkEvent.Context> ctx) {
        // server world
        World world = Objects.requireNonNull(ctx.get().getSender()).level;
        ctx.get().enqueueWork(() -> {
            BlockPos pos = message.pos;
            if (world.getBlockEntity(pos) instanceof BlackboardBlockTile board) {
                world.playSound(null, message.pos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 1, 0.8f);
                board.setPixels(message.pixels);
                //updates client
                //set changed also sends a block update
                board.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
