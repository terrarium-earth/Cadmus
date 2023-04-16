package earth.terrarium.cadmus.common.network.messages.client;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.team.Team;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record SyncClaimedChunksPacket(ChunkPos pos, ClaimInfo info) implements Packet<SyncClaimedChunksPacket> {

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
            Team team = packet.info.team();
            buf.writeChunkPos(packet.pos);
            buf.writeEnum(packet.info.type());
            buf.writeUUID(team.teamId());
            buf.writeUUID(team.creator());
            buf.writeCollection(team.members(), FriendlyByteBuf::writeUUID);
            buf.writeUtf(team.name() == null ? "TODO" : team.name());
        }

        @Override
        public SyncClaimedChunksPacket decode(FriendlyByteBuf buf) {
            ChunkPos pos = buf.readChunkPos();
            ClaimType type = buf.readEnum(ClaimType.class);
            UUID teamId = buf.readUUID();
            UUID creator = buf.readUUID();
            Set<UUID> members = buf.readCollection(HashSet::new, FriendlyByteBuf::readUUID);
            String name = buf.readUtf();
            return new SyncClaimedChunksPacket(pos, new ClaimInfo(new Team(teamId, creator, members, name), type));
        }

        @Override
        public PacketContext handle(SyncClaimedChunksPacket message) {
            return (player, level) -> ClaimChunkSaveData.set(level, message.pos, message.info);
        }
    }
}
