package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.bytecodecs.defaults.PairCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import it.unimi.dsi.fastutil.ints.IntIntPair;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

public record SyncAllMaxClaimsPacket(
    Map<TeamId, IntIntPair> maxClaimsByTeam
) implements Packet<SyncAllMaxClaimsPacket> {

    public static final ClientboundPacketType<SyncAllMaxClaimsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_all_max_claims"),
        ObjectByteCodec.create(
            new MapCodec<>(
                TeamId.BYTE_CODEC,
                new PairCodec<>(ByteCodec.VAR_INT, ByteCodec.VAR_INT)
                    .map(entry -> IntIntPair.of(entry.getKey(), entry.getValue()),
                        pair -> new AbstractMap.SimpleEntry<>(pair.leftInt(), pair.rightInt())
                    )).fieldOf(SyncAllMaxClaimsPacket::maxClaimsByTeam),
            SyncAllMaxClaimsPacket::new
        ),
        NetworkHandle.handle(packet -> ClaimLimitApi.API.set(Map.copyOf(packet.maxClaimsByTeam)))
    );

    @Override
    public PacketType<SyncAllMaxClaimsPacket> type() {
        return TYPE;
    }
}
