package earth.terrarium.cadmus.common.network.message.client;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.claiming.ClaimedChunk;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record SyncClaimedChunksPacket(Map<String, Set<ClaimedChunk>> teams) implements Packet<SyncClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "sync_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<SyncClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<SyncClaimedChunksPacket> {
        @Override
        public void encode(SyncClaimedChunksPacket packet, FriendlyByteBuf buf) {
            var teams = packet.teams();
            buf.writeVarInt(teams.size());
            for (var entry : teams.entrySet()) {
                buf.writeUtf(entry.getKey());
                ClaimedChunk.encode(entry.getValue(), buf);
            }
        }

        @Override
        public SyncClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Map<String, Set<ClaimedChunk>> teams = new HashMap<>();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                teams.put(buf.readUtf(), ClaimedChunk.decode(buf));
            }
            return new SyncClaimedChunksPacket(teams);
        }

        @Override
        public PacketContext handle(SyncClaimedChunksPacket message) {
            return (player, level) -> {
                CadmusClient.TEAMS.clear();
                CadmusClient.TEAMS.putAll(message.teams());
            };
        }
    }
}
