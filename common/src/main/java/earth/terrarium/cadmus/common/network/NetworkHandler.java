package earth.terrarium.cadmus.common.network;

import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.messages.ClientboundSendClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.ServerboundClearChunksPacket;
import earth.terrarium.cadmus.common.network.messages.ServerboundRequestClaimedChunksPacket;
import earth.terrarium.cadmus.common.network.messages.ServerboundUpdateClaimedChunksPacket;

public class NetworkHandler {
    public static final NetworkChannel CHANNEL = new NetworkChannel(Cadmus.MOD_ID, 1, "main");

    public static void init() {
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundRequestClaimedChunksPacket.ID, ServerboundRequestClaimedChunksPacket.HANDLER, ServerboundRequestClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundUpdateClaimedChunksPacket.ID, ServerboundUpdateClaimedChunksPacket.HANDLER, ServerboundUpdateClaimedChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER, ServerboundClearChunksPacket.ID, ServerboundClearChunksPacket.HANDLER, ServerboundClearChunksPacket.class);

        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT, ClientboundSendClaimedChunksPacket.ID, ClientboundSendClaimedChunksPacket.HANDLER, ClientboundSendClaimedChunksPacket.class);
    }
}
