package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamId;

import java.util.UUID;

public record SyncMaxClaimsPacket(
    TeamId id,
    int maxClaims,
    int maxChunkLoaded
) implements Packet<SyncMaxClaimsPacket> {

    public static final ClientboundPacketType<SyncMaxClaimsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_max_claims"),
        ObjectByteCodec.create(
            TeamId.BYTE_CODEC.fieldOf(SyncMaxClaimsPacket::id),
            ByteCodec.VAR_INT.fieldOf(SyncMaxClaimsPacket::maxClaims),
            ByteCodec.VAR_INT.fieldOf(SyncMaxClaimsPacket::maxChunkLoaded),
            SyncMaxClaimsPacket::new
        ),
        NetworkHandle.handle(packet -> ClaimLimitApi.API.set(packet.id(), packet.maxClaims(), packet.maxChunkLoaded()))
    );

    @Override
    public PacketType<SyncMaxClaimsPacket> type() {
        return TYPE;
    }
}
