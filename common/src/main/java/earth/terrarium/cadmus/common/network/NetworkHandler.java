package earth.terrarium.cadmus.common.network;

import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.messages.*;

public class NetworkHandler {
    public static final NetworkChannel CHANNEL = new NetworkChannel(Cadmus.MOD_ID, 1, "main", true);

    public static void init() {
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundRequestClaimedChunksPacket.ID, ServerboundRequestClaimedChunksPacket.HANDLER, ServerboundRequestClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundUpdateClaimedChunksPacket.ID, ServerboundUpdateClaimedChunksPacket.HANDLER, ServerboundUpdateClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundClearChunksPacket.ID, ServerboundClearChunksPacket.HANDLER, ServerboundClearChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundListenToChunksPacket.ID, ServerboundListenToChunksPacket.HANDLER, ServerboundListenToChunksPacket.class);

        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, ClientboundSendClaimedChunksPacket.ID, ClientboundSendClaimedChunksPacket.HANDLER, ClientboundSendClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, ClientboundUpdateListeningChunksPacket.ID, ClientboundUpdateListeningChunksPacket.HANDLER, ClientboundUpdateListeningChunksPacket.class);
    }
}
