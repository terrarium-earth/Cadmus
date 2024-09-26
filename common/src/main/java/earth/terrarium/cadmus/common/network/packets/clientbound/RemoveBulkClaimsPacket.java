package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;
import java.util.UUID;

public record RemoveBulkClaimsPacket(
    UUID id,
    Set<ChunkPos> positions
) implements Packet<RemoveBulkClaimsPacket> {

    public static final ClientboundPacketType<RemoveBulkClaimsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("remove_claims"),
        ObjectByteCodec.create(
            ByteCodec.UUID.fieldOf(RemoveBulkClaimsPacket::id),
            ExtraByteCodecs.CHUNK_POS.setOf().fieldOf(RemoveBulkClaimsPacket::positions),
            RemoveBulkClaimsPacket::new
        ),
        NetworkHandle.handle(packet -> ClaimApi.API.unclaim(CadmusClient.level(), packet.id(), packet.positions()))
    );

    @Override
    public PacketType<RemoveBulkClaimsPacket> type() {
        return TYPE;
    }
}
