package earth.terrarium.cadmus.common.network;


import com.teamresourceful.resourcefullib.common.network.Network;
import com.teamresourceful.resourcefullib.common.network.Packet;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.packets.clientbound.*;
import earth.terrarium.cadmus.common.network.packets.serverbound.BulkClaimSettingsPacket;
import earth.terrarium.cadmus.common.network.packets.serverbound.ChatClaimPacket;
import earth.terrarium.cadmus.common.network.packets.serverbound.ClaimSettingsPacket;
import earth.terrarium.cadmus.common.network.packets.serverbound.RequestClaimSettingsPacket;
import net.minecraft.server.MinecraftServer;

public class NetworkHandler {

    public static final Network CHANNEL = new Network(Cadmus.id("main"), 1, true);

    public static void init() {
        CHANNEL.register(ChatClaimPacket.TYPE);
        CHANNEL.register(SyncClaimsPacket.TYPE);
        CHANNEL.register(AddClaimPacket.TYPE);
        CHANNEL.register(AddBulkClaimsPacket.TYPE);
        CHANNEL.register(RemoveClaimPacket.TYPE);
        CHANNEL.register(RemoveBulkClaimsPacket.TYPE);
        CHANNEL.register(ClearClaimsPacket.TYPE);
        CHANNEL.register(SyncTeamInfo.TYPE);
        CHANNEL.register(SyncAllTeamInfoPacket.TYPE);
        CHANNEL.register(SyncMaxClaimsPacket.TYPE);
        CHANNEL.register(SyncAllMaxClaimsPacket.TYPE);
        CHANNEL.register(SyncClaimSettingsPacket.TYPE);
        CHANNEL.register(ClaimSettingsPacket.TYPE);
        CHANNEL.register(RequestClaimSettingsPacket.TYPE);
        CHANNEL.register(BulkClaimSettingsPacket.TYPE);
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
