package earth.terrarium.cadmus.common.network;


import com.teamresourceful.resourcefullib.common.network.Network;
import com.teamresourceful.resourcefullib.common.network.Packet;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public class NetworkHandler {

    public static final Network CHANNEL = new Network(Cadmus.id("main"), 1, true);

    public static void init() {
        CHANNEL.register(ServerboundSendClaimChatCommandPacket.TYPE);
        CHANNEL.register(ClientboundSyncClaimsPacket.TYPE);
        CHANNEL.register(ClientboundAddClaimPacket.TYPE);
        CHANNEL.register(ClientboundAddClaimsPacket.TYPE);
        CHANNEL.register(ClientboundRemoveClaimPacket.TYPE);
        CHANNEL.register(ClientboundRemoveClaimsPacket.TYPE);
        CHANNEL.register(ClientboundClearClaimsPacket.TYPE);
        CHANNEL.register(ClientboundSyncTeamInfo.TYPE);
        CHANNEL.register(ClientboundSyncAllTeamInfoPacket.TYPE);
        CHANNEL.register(ClientboundSyncMaxClaims.TYPE);
        CHANNEL.register(ClientboundSyncAllMaxClaims.TYPE);
    }

    /**
     * Sends to all clients that have Cadmus installed
     */
    public static <T extends Packet<T>> void sendToAllClientPlayers(T packet, MinecraftServer server) {
        server.getPlayerList().getPlayers().forEach(player -> {
            if (CHANNEL.canSendToPlayer(player, packet.type())) {
                CHANNEL.sendToPlayer(packet, player);
            }
        });
    }
}
