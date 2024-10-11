package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public record ClientboundAddClaimPacket(
    UUID id,
    ChunkPos pos,
    boolean chunkLoad
) implements Packet<ClientboundAddClaimPacket> {

    public static final ClientboundPacketType<ClientboundAddClaimPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundAddClaimPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundAddClaimPacket> implements ClientboundPacketType<ClientboundAddClaimPacket> {

        public Type() {
            super(
                ClientboundAddClaimPacket.class,
                Cadmus.id("add_claim"),
                ObjectByteCodec.create(
                    ByteCodec.UUID.fieldOf(ClientboundAddClaimPacket::id),
                    ExtraByteCodecs.CHUNK_POS.fieldOf(ClientboundAddClaimPacket::pos),
                    ByteCodec.BOOLEAN.fieldOf(ClientboundAddClaimPacket::chunkLoad),
                    ClientboundAddClaimPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundAddClaimPacket packet) {
            return () -> ClaimApi.API.claim(CadmusClient.level(), packet.id(), packet.pos(), packet.chunkLoad());
        }
    }
}
