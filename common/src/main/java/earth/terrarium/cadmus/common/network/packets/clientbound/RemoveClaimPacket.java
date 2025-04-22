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
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public record RemoveClaimPacket(
    TeamId id,
    ChunkPos pos
) implements Packet<RemoveClaimPacket> {

    public static final ClientboundPacketType<RemoveClaimPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("remove_claim"),
        ObjectByteCodec.create(
            TeamId.BYTE_CODEC.fieldOf(RemoveClaimPacket::id),
            ExtraByteCodecs.CHUNK_POS.fieldOf(RemoveClaimPacket::pos),
            RemoveClaimPacket::new
        ),
        NetworkHandle.handle(packet -> ClaimApi.API.unclaim(CadmusClient.level(), packet.id(), packet.pos))
    );

    @Override
    public PacketType<RemoveClaimPacket> type() {
        return TYPE;
    }
}
