package earth.terrarium.cadmus.common.network;

import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.messages.client.SendClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.server.ClearChunksPacket;
import earth.terrarium.cadmus.common.network.messages.server.RequestClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.server.UpdateClaimedChunksPacket;

public class NetworkHandler {
    public static final NetworkChannel CHANNEL = new NetworkChannel(Cadmus.MOD_ID, 1, "main");

    public static void init() {
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, RequestClaimedChunksPacket.ID, RequestClaimedChunksPacket.HANDLER, RequestClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, UpdateClaimedChunksPacket.ID, UpdateClaimedChunksPacket.HANDLER, UpdateClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ClearChunksPacket.ID, ClearChunksPacket.HANDLER, ClearChunksPacket.class);

        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, SendClaimedChunksPacket.ID, SendClaimedChunksPacket.HANDLER, SendClaimedChunksPacket.class);
    }
}
