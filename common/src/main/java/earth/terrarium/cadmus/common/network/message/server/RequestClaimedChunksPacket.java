package earth.terrarium.cadmus.common.network.message.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claiming.ClaimedChunk;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.message.client.SendClaimedChunksPacket;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;

public record RequestClaimedChunksPacket(int renderDistance) implements Packet<RequestClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "request_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<RequestClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<RequestClaimedChunksPacket> {
        @Override
        public void encode(RequestClaimedChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeVarInt(packet.renderDistance);
        }

        @Override
        public RequestClaimedChunksPacket decode(FriendlyByteBuf buf) {
            return new RequestClaimedChunksPacket(Math.min(buf.readVarInt(), 32));
        }

        @Override
        public PacketContext handle(RequestClaimedChunksPacket message) {
            return (player, level) -> {
                var start = player.chunkPosition();
                var friendlyChunks = TeamSaveData.get((ServerPlayer) player);

                Set<ClaimedChunk> unfriendlyChunks = new HashSet<>();
                for (var chunk : TeamSaveData.getAll((ServerPlayer) player)) {
                    var chunkPos = new ChunkPos(start.x - chunk.pos().x, start.z - chunk.pos().z);
                    if (chunkPos.x < message.renderDistance && chunkPos.x > -message.renderDistance && chunkPos.z < message.renderDistance && chunkPos.z > -message.renderDistance) {
                        if (!friendlyChunks.contains(chunk)) {
                            unfriendlyChunks.add(chunk);
                        }
                    }
                }

                NetworkHandler.CHANNEL.sendToPlayer(new SendClaimedChunksPacket(friendlyChunks, unfriendlyChunks), player);
            };
        }
    }
}
