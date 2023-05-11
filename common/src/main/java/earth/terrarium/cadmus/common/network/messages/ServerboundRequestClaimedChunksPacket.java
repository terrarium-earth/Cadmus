package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.Team;
import earth.terrarium.cadmus.common.teams.TeamSaveData;
import earth.terrarium.cadmus.common.util.ModGameRules;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
                int renderDistance = Math.min(message.renderDistance, 32);

                Map<ChunkPos, ClaimInfo> claims = new HashMap<>();
                for (var claimedChunk : ClaimSaveData.getAll((ServerLevel) level).entrySet()) {
                    var chunkPos = new ChunkPos(start.x - claimedChunk.getKey().x, start.z - claimedChunk.getKey().z);
                    if (chunkPos.x < renderDistance && chunkPos.x > -renderDistance && chunkPos.z < renderDistance && chunkPos.z > -renderDistance) {
                        claims.put(claimedChunk.getKey(), claimedChunk.getValue());
                    }
                }

                Team team = TeamSaveData.getOrCreateTeam((ServerPlayer) player);
                UUID teamId = team.teamId();
                String id = team.name();
                Optional<String> displayName = Optional.ofNullable(Optionull.map(TeamProviderApi.API.getSelected().getTeamName(id, player.getServer()), Component::getString));
                ChatFormatting color = TeamProviderApi.API.getSelected().getTeamColor(id, player.getServer());

                Map<UUID, Component> teamDisplayNames = TeamSaveData.getTeams(player.getServer()).stream()
                    .filter(t -> !t.teamId().equals(teamId))
                    .collect(HashMap::new, (map, team1) -> map.put(team1.teamId(),
                        Optional.ofNullable(TeamProviderApi.API.getSelected().getTeamName(String.valueOf(team1.teamId()), player.getServer())).orElse(Component.literal(""))
                    ), HashMap::putAll);

                int claimedChunks = 0;
                int chunkLoadedCount = 0;
                for (var l : level.getServer().getAllLevels()) {
                    for (var info : ClaimSaveData.getAll(l).values()) {
                        if (teamId.equals(info.teamId())) {
                            claimedChunks++;
                            if (info.type() == ClaimType.CHUNK_LOADED) {
                                chunkLoadedCount++;
                            }
                        }
                    }
                }

                int maxClaims = ModGameRules.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CLAIMED_CHUNKS);
                int maxChunkLoaded = ModGameRules.getOrCreateIntGameRule(level, ModGameRules.RULE_MAX_CHUNK_LOADED);
                NetworkHandler.CHANNEL.sendToPlayer(new ClientboundSendClaimedChunksPacket(claims, teamId, color, displayName, teamDisplayNames, claimedChunks, chunkLoadedCount, maxClaims, maxChunkLoaded), player);
            };
        }
    }
}