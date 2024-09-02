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

public record ClientboundRemoveClaimPacket(
    UUID id,
    ChunkPos pos
) implements Packet<ClientboundRemoveClaimPacket> {

    public static final ClientboundPacketType<ClientboundRemoveClaimPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundRemoveClaimPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundRemoveClaimPacket> implements ClientboundPacketType<ClientboundRemoveClaimPacket> {

        public Type() {
            super(
                ClientboundRemoveClaimPacket.class,
                Cadmus.id("remove_claim"),
                ObjectByteCodec.create(
                    ByteCodec.UUID.fieldOf(ClientboundRemoveClaimPacket::id),
                    ExtraByteCodecs.CHUNK_POS.fieldOf(ClientboundRemoveClaimPacket::pos),
                    ClientboundRemoveClaimPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundRemoveClaimPacket packet) {
            return () -> ClaimApi.API.unclaim(CadmusClient.level(), packet.id, packet.pos);
        }
    }
}
