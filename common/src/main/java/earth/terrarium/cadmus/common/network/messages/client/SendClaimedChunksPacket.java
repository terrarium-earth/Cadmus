package earth.terrarium.cadmus.common.network.messages.client;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claims.ClaimMapScreen;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.team.Team;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public record SendClaimedChunksPacket(Map<ChunkPos, ClaimInfo> claims,
                                      Optional<UUID> team, int maxClaims,
                                      int maxChunkLoaded) implements Packet<SendClaimedChunksPacket> {

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
            Map<UUID, Team> teams = new HashMap<>();
            buf.writeMap(packet.claims, FriendlyByteBuf::writeChunkPos, (buf1, info) -> {
                buf1.writeUUID(info.team().teamId());
                buf1.writeEnum(info.type());
                teams.put(info.team().teamId(), info.team());
            });
            buf.writeMap(teams, FriendlyByteBuf::writeUUID, (buf1, team) -> {
                buf1.writeUUID(team.creator());
                buf1.writeCollection(team.members(), FriendlyByteBuf::writeUUID);
                buf1.writeUtf(team.name());
            });
            buf.writeOptional(packet.team, FriendlyByteBuf::writeUUID);
            buf.writeVarInt(packet.maxClaims);
            buf.writeVarInt(packet.maxChunkLoaded);
        }

        @Override
        public SendClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Map<ChunkPos, Pair<UUID, ClaimType>> claims = buf.readMap(
                FriendlyByteBuf::readChunkPos,
                (buf1) -> Pair.of(buf1.readUUID(), buf1.readEnum(ClaimType.class))
            );
            int size = buf.readVarInt();
            Map<UUID, Team> teams = Maps.newHashMapWithExpectedSize(size);
            for (int i = 0; i < size; i++) {
                UUID id = buf.readUUID();
                UUID creator = buf.readUUID();
                Set<UUID> members = buf.readCollection(HashSet::new, FriendlyByteBuf::readUUID);
                String name = buf.readUtf();
                teams.put(id, new Team(id, creator, members, name));
            }
            Optional<UUID> team = buf.readOptional(FriendlyByteBuf::readUUID);
            Map<ChunkPos, ClaimInfo> newClaims = Maps.newHashMapWithExpectedSize(claims.size());
            claims.forEach((key, value) ->
                newClaims.put(key, new ClaimInfo(teams.get(value.getFirst()), value.getSecond())));
            int maxClaims = buf.readVarInt();
            int maxChunkLoaded = buf.readVarInt();
            return new SendClaimedChunksPacket(newClaims, team, maxClaims, maxChunkLoaded);
        }

        @Override
        public PacketContext handle(SendClaimedChunksPacket message) {
            return (player, level) -> ClaimMapScreen.update(message.claims(), message.team().orElse(null), message.maxClaims, message.maxChunkLoaded);
        }
    }
}
