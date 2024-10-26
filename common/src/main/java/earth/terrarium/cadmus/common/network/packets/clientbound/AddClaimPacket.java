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

import java.util.UUID;

public record AddClaimPacket(
    UUID id,
    ChunkPos pos,
    boolean chunkLoad
) implements Packet<AddClaimPacket> {
    public static final ClientboundPacketType<AddClaimPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("add_claim"),
        ObjectByteCodec.create(
            ByteCodec.UUID.fieldOf(AddClaimPacket::id),
            ExtraByteCodecs.CHUNK_POS.fieldOf(AddClaimPacket::pos),
            ByteCodec.BOOLEAN.fieldOf(AddClaimPacket::chunkLoad),
            AddClaimPacket::new
        ),
        NetworkHandle.handle((packet) -> ClaimApi.API.claim(CadmusClient.level(), packet.id(), packet.pos(), packet.chunkLoad()))
    );

    @Override
    public PacketType<AddClaimPacket> type() {
        return TYPE;
    }
}
