package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.resourcefullib.common.lib.Constants;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.maxclaims.MaxClaimProviderApi;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public record ServerboundUpdateClaimedChunksPacket(Map<ChunkPos, ClaimType> addedChunks,
                                                   Map<ChunkPos, ClaimType> removedChunks) implements Packet<ServerboundUpdateClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "update_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ServerboundUpdateClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ServerboundUpdateClaimedChunksPacket> {
        @Override
        public void encode(ServerboundUpdateClaimedChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeMap(packet.addedChunks, FriendlyByteBuf::writeChunkPos, FriendlyByteBuf::writeEnum);
            buf.writeMap(packet.removedChunks, FriendlyByteBuf::writeChunkPos, FriendlyByteBuf::writeEnum);
        }

        @Override
        public ServerboundUpdateClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Map<ChunkPos, ClaimType> addedChunks = buf.readMap(HashMap::new, FriendlyByteBuf::readChunkPos, buf1 -> buf1.readEnum(ClaimType.class));
            Map<ChunkPos, ClaimType> removedChunks = buf.readMap(HashMap::new, FriendlyByteBuf::readChunkPos, buf1 -> buf1.readEnum(ClaimType.class));
            return new ServerboundUpdateClaimedChunksPacket(addedChunks, removedChunks);
        }

        @Override
        public PacketContext handle(ServerboundUpdateClaimedChunksPacket message) {
            return (player, level) -> {
                if (message.addedChunks().isEmpty() && message.removedChunks().isEmpty()) return;
                ServerLevel serverLevel = (ServerLevel) level;
                String id = TeamProviderApi.API.getSelected().getTeamId(player.getServer(), player.getUUID());

                ClaimHandler.updateChunkLoaded(serverLevel, id, false);

                // Check if the player is claiming more chunks than allowed
                var teamClaims = ClaimHandler.getTeamClaims(serverLevel, id);
                int maxClaims = MaxClaimProviderApi.API.getSelected().getMaxClaims(id, player.getServer(), player);
                if (!message.addedChunks.isEmpty() && (teamClaims == null ? 0 : teamClaims.values().size()) + message.addedChunks.size() - message.removedChunks.size() > maxClaims) {
                    Constants.LOGGER.warn("Player {} tried to claim more chunks than allowed! ({} > {})", player.getName().getString(), teamClaims == null ? 0 : teamClaims.values().size() + message.addedChunks.size() - message.removedChunks.size(), maxClaims);
                    return;
                }

                // Check if the player is claiming more chunk loaded chunks than allowed
                int maxChunkLoaded = MaxClaimProviderApi.API.getSelected().getMaxChunkLoaded(id, player.getServer(), player);
                int currentChunkLoaded = 0;
                int addedChunkLoaded = message.addedChunks.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
                int removedChunkLoaded = message.removedChunks.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
                if (teamClaims != null) {
                    currentChunkLoaded = teamClaims.values().stream().filter(claim -> claim == ClaimType.CHUNK_LOADED).toArray().length;
                }
                if (currentChunkLoaded + addedChunkLoaded - removedChunkLoaded > maxChunkLoaded) {
                    Constants.LOGGER.warn("Player {} tried to claim more chunk loaded chunks than allowed! ({} > {})", player.getName().getString(), currentChunkLoaded + addedChunkLoaded - removedChunkLoaded, maxChunkLoaded);
                    return;
                }

                ClaimHandler.addClaims(serverLevel, id, message.addedChunks);
                ClaimHandler.removeClaims(serverLevel, id, message.removedChunks.keySet());

                ClaimHandler.updateChunkLoaded(serverLevel, id, true);
                serverLevel.players().forEach(ModUtils::displayTeamName);
            };
        }
    }
}
