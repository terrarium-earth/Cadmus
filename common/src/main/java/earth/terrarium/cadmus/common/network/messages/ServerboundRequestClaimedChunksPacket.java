package earth.terrarium.cadmus.common.network.messages;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.constants.ConstantComponents;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public record ServerboundRequestClaimedChunksPacket(
    int renderDistance) implements Packet<ServerboundRequestClaimedChunksPacket> {

    public static final ServerboundPacketType<ServerboundRequestClaimedChunksPacket> TYPE = new Type();

    @Override
    public PacketType<ServerboundRequestClaimedChunksPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ServerboundRequestClaimedChunksPacket> implements ServerboundPacketType<ServerboundRequestClaimedChunksPacket> {

        public Type() {
            super(
                ServerboundRequestClaimedChunksPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "request_claimed_chunks"),
                ObjectByteCodec.create(
                    ByteCodec.VAR_INT.fieldOf(ServerboundRequestClaimedChunksPacket::renderDistance),
                    ServerboundRequestClaimedChunksPacket::new
                )
            );
        }

        @Override
        public Consumer<Player> handle(ServerboundRequestClaimedChunksPacket packet) {
            return player -> {
                ServerLevel level = (ServerLevel) player.level();
                var start = player.chunkPosition();
                int viewDistance = Math.min(((ServerLevel) level).getServer().getPlayerList().getViewDistance(), 32);
                int renderDistance = Math.min(packet.renderDistance, viewDistance);

                String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());

                // Get all claims within the render distance
                var claimData = ClaimHandler.getAllTeamClaims(level);
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
                        Optional.ofNullable(TeamHelper.getTeamName(teamId, player.getServer())).orElse(ConstantComponents.UNKNOWN)
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
