package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;

import java.util.UUID;

public record ClearClaimsPacket(
    UUID id
) implements Packet<ClearClaimsPacket> {

    public static final ClientboundPacketType<ClearClaimsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("clear_claims"),
        ByteCodec.UUID.map(ClearClaimsPacket::new, ClearClaimsPacket::id),
        NetworkHandle.handle(packet -> ClaimApi.API.clear(CadmusClient.level(), packet.id()))
    );

    @Override
    public PacketType<ClearClaimsPacket> type() {
        return TYPE;
    }
}
