package earth.terrarium.cadmus.common.network.messages;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record ServerboundRequestClaimedChunksPacket(
    int renderDistance) implements Packet<ServerboundRequestClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "request_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ServerboundRequestClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ServerboundRequestClaimedChunksPacket> {
        @Override
        public void encode(ServerboundRequestClaimedChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeVarInt(packet.renderDistance);
        }

        @Override
        public ServerboundRequestClaimedChunksPacket decode(FriendlyByteBuf buf) {
            return new ServerboundRequestClaimedChunksPacket(Math.min(buf.readVarInt(), 32));
        }

        @Override
        public PacketContext handle(ServerboundRequestClaimedChunksPacket message) {
            return (player, level) -> {
                var start = player.chunkPosition();
                int viewDistance = Math.min(((ServerLevel) level).getServer().getPlayerList().getViewDistance(), 32);
                int renderDistance = Math.min(message.renderDistance, viewDistance);

                String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                // Get all claims within the render distance
                var claimData = ClaimHandler.getAllTeamClaims((ServerLevel) level);
                Map<ChunkPos, Pair<String, ClaimType>> claims = new HashMap<>();
                if (claimData != null) {
                    claimData.forEach((teamId, teamClaims) ->
                        teamClaims.forEach((pos, type) -> {
                            var chunkPos = new ChunkPos(start.x - pos.x, start.z - pos.z);
                            if (chunkPos.x < renderDistance && chunkPos.x > -renderDistance && chunkPos.z < renderDistance && chunkPos.z > -renderDistance) {
                                claims.put(pos, Pair.of(teamId, type));
                            }
                        }));
                }

                Optional<String> displayName = Optional.ofNullable(Optionull.map(TeamHelper.getTeamName(id, player.getServer()), Component::getString));
                ChatFormatting color = TeamHelper.getTeamColor(id, player.getServer());

                Map<String, Component> teamDisplayNames = ClaimHandler.getAllTeamClaims((ServerLevel) level).keySet().stream()
                    .filter(t -> !t.equals(id))
                    .collect(HashMap::new, (map, teamId) -> map.put(teamId,
                        Optional.ofNullable(TeamHelper.getTeamName(teamId, player.getServer())).orElse(Component.literal("ERROR"))
                    ), HashMap::putAll);

                int claimedChunks = 0;
                int chunkLoadedCount = 0;
                for (var serverLevel : level.getServer().getAllLevels()) {
                    var teamClaims = ClaimHandler.getTeamClaims(serverLevel, id);
                    if (teamClaims == null) continue;
                    for (var data : teamClaims.entrySet()) {
                        claimedChunks++;
                        if (data.getValue() == ClaimType.CHUNK_LOADED) {
                            chunkLoadedCount++;
                        }
                    }
                }

                int maxClaims = MaxClaimProviderApi.API.getSelected().getMaxClaims(id, player.getServer(), player);
                int maxChunkLoaded = MaxClaimProviderApi.API.getSelected().getMaxChunkLoaded(id, player.getServer(), player);
                NetworkHandler.CHANNEL.sendToPlayer(new ClientboundSendClaimedChunksPacket(claims, id, color, displayName, teamDisplayNames, claimedChunks, chunkLoadedCount, maxClaims, maxChunkLoaded, renderDistance), player);
            };
        }
    }
}
