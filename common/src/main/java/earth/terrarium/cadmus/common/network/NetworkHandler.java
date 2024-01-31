package earth.terrarium.cadmus.common.network;


import com.teamresourceful.resourcefullib.common.network.Network;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.network.messages.*;
import net.minecraft.resources.ResourceLocation;

public class NetworkHandler {
    public static final Network CHANNEL = new Network(new ResourceLocation(Cadmus.MOD_ID, "main"), 1, true);

    public static void init() {
        CHANNEL.register(ServerboundRequestClaimedChunksPacket.TYPE);
        CHANNEL.register(ServerboundUpdateClaimedChunksPacket.TYPE);
        CHANNEL.register(ServerboundClearChunksPacket.TYPE);
        CHANNEL.register(ServerboundListenToChunksPacket.TYPE);

        CHANNEL.register(ClientboundSendClaimedChunksPacket.TYPE);
        CHANNEL.register(ClientboundUpdateListeningChunksPacket.TYPE);
    }
}
