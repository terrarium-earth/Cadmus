package earth.terrarium.cadmus.common.network.message.client;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.map.ClaimMapScreen;
import earth.terrarium.cadmus.common.claiming.ClaimedChunk;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record SendClaimedChunksPacket(Set<ClaimedChunk> friendlyChunks,
                                      Set<ClaimedChunk> unfriendlyChunks) implements Packet<SendClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "send_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<SendClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<SendClaimedChunksPacket> {
        @Override
        public void encode(SendClaimedChunksPacket packet, FriendlyByteBuf buf) {
            ClaimedChunk.encode(packet.friendlyChunks(), buf);
            ClaimedChunk.encode(packet.unfriendlyChunks(), buf);
        }

        @Override
        public SendClaimedChunksPacket decode(FriendlyByteBuf buf) {
            var claimedChunks = ClaimedChunk.decode(buf);
            var otherChunks = ClaimedChunk.decode(buf);
            return new SendClaimedChunksPacket(claimedChunks, otherChunks);
        }

        @Override
        public PacketContext handle(SendClaimedChunksPacket message) {
            return (player, level) -> ClaimMapScreen.update(message.friendlyChunks(), message.unfriendlyChunks());
        }
    }
}
