package earth.terrarium.cadmus.common.network.messages.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.client.SendClaimedChunksPacket;
import earth.terrarium.cadmus.common.registry.ModGameRules;
import earth.terrarium.cadmus.common.team.Team;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

                Map<ChunkPos, ClaimInfo> claims = new HashMap<>();
                for (var claimedChunk : ClaimChunkSaveData.getAll(player.level).entrySet()) {
                    var chunkPos = new ChunkPos(start.x - claimedChunk.getKey().x, start.z - claimedChunk.getKey().z);
                    if (chunkPos.x < message.renderDistance && chunkPos.x > -message.renderDistance && chunkPos.z < message.renderDistance && chunkPos.z > -message.renderDistance) {
                        claims.put(claimedChunk.getKey(), claimedChunk.getValue());
                    }
                }

                Team team = TeamSaveData.getPlayerTeam(player);
                Optional<UUID> teamId = Optional.ofNullable(team).map(Team::teamId);
                Optional<String> teamName = Optional.ofNullable(team).map(Team::name);
                int maxClaims = ModUtils.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CLAIMED_CHUNKS);
                int maxChunkLoaded = ModUtils.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CHUNK_LOADED);
                NetworkHandler.CHANNEL.sendToPlayer(new SendClaimedChunksPacket(claims, teamId, teamName, maxClaims, maxChunkLoaded), player);
            };
        }
    }
}
