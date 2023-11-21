package earth.terrarium.cadmus.common.network;

import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.messages.*;
import net.minecraft.network.protocol.PacketFlow;

public class NetworkHandler {
    public static final NetworkChannel CHANNEL = new NetworkChannel(Cadmus.MOD_ID, 1, "main", true);

    public static void init() {
        CHANNEL.registerPacket(PacketFlow.SERVERBOUND, ServerboundRequestClaimedChunksPacket.ID, ServerboundRequestClaimedChunksPacket.HANDLER, ServerboundRequestClaimedChunksPacket.class);
        CHANNEL.registerPacket(PacketFlow.SERVERBOUND, ServerboundUpdateClaimedChunksPacket.ID, ServerboundUpdateClaimedChunksPacket.HANDLER, ServerboundUpdateClaimedChunksPacket.class);
        CHANNEL.registerPacket(PacketFlow.SERVERBOUND, ServerboundClearChunksPacket.ID, ServerboundClearChunksPacket.HANDLER, ServerboundClearChunksPacket.class);
        CHANNEL.registerPacket(PacketFlow.SERVERBOUND, ServerboundListenToChunksPacket.ID, ServerboundListenToChunksPacket.HANDLER, ServerboundListenToChunksPacket.class);

        CHANNEL.registerPacket(PacketFlow.CLIENTBOUND, ClientboundSendClaimedChunksPacket.ID, ClientboundSendClaimedChunksPacket.HANDLER, ClientboundSendClaimedChunksPacket.class);
        CHANNEL.registerPacket(PacketFlow.CLIENTBOUND, ClientboundUpdateListeningChunksPacket.ID, ClientboundUpdateListeningChunksPacket.HANDLER, ClientboundUpdateListeningChunksPacket.class);
    }
}
