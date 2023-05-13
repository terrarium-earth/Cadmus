package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamProviderApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record ServerboundUpdateClaimedChunksPacket(Map<ChunkPos, ClaimType> addedChunks,
                                                   Set<ChunkPos> removedChunks) implements Packet<ServerboundUpdateClaimedChunksPacket> {

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
            buf.writeCollection(packet.removedChunks, FriendlyByteBuf::writeChunkPos);
        }

        @Override
        public ServerboundUpdateClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Map<ChunkPos, ClaimType> addedChunks = buf.readMap(HashMap::new, FriendlyByteBuf::readChunkPos, buf1 -> buf1.readEnum(ClaimType.class));
            Set<ChunkPos> removedChunks = buf.readCollection(HashSet::new, FriendlyByteBuf::readChunkPos);
            return new ServerboundUpdateClaimedChunksPacket(addedChunks, removedChunks);
        }

        @Override
        public PacketContext handle(ServerboundUpdateClaimedChunksPacket message) {
            return (player, level) -> {
                if (message.addedChunks().isEmpty() && message.removedChunks().isEmpty()) return;
                ServerLevel serverLevel = (ServerLevel) level;
                String id = TeamProviderApi.API.getSelected().getTeamId(player.getServer(), player.getUUID());

                ClaimHandler.updateChunkLoaded(serverLevel, id, false);

                ClaimHandler.addClaims(serverLevel, id, message.addedChunks);
                ClaimHandler.removeClaims(serverLevel, id, message.removedChunks);

                ClaimHandler.updateChunkLoaded(serverLevel, id, true);
                serverLevel.players().forEach(ModUtils::displayTeamName);
            };
        }
    }
}
