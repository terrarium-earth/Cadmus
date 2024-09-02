package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundClearClaimsPacket(
    UUID id
) implements Packet<ClientboundClearClaimsPacket> {

    public static final ClientboundPacketType<ClientboundClearClaimsPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundClearClaimsPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundClearClaimsPacket> implements ClientboundPacketType<ClientboundClearClaimsPacket> {

        public Type() {
            super(
                ClientboundClearClaimsPacket.class,
                Cadmus.id("clear_claims"),
                ObjectByteCodec.create(
                    ByteCodec.UUID.fieldOf(ClientboundClearClaimsPacket::id),
                    ClientboundClearClaimsPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundClearClaimsPacket packet) {
            return () -> ClaimApi.API.clear(CadmusClient.level(), packet.id());
        }
    }
}
