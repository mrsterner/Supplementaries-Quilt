package net.mehvahdjukaar.supplementaries.client.gui;

import net.mehvahdjukaar.supplementaries.common.network.ClientBoundOpenScreenPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

public interface IScreenProvider {

    @OnlyIn(Dist.CLIENT)
    void openScreen(World level, BlockPos pos, Player player);

    default void sendOpenGuiPacket(World level, BlockPos pos, Player player) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                new ClientBoundOpenScreenPacket(pos));
    }
}
