package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundSyncMaxClaims(
    UUID id,
    int maxClaims,
    int maxChunkLoaded
) implements Packet<ClientboundSyncMaxClaims> {

    public static final ClientboundPacketType<ClientboundSyncMaxClaims> TYPE = new Type();

    @Override
    public PacketType<ClientboundSyncMaxClaims> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundSyncMaxClaims> implements ClientboundPacketType<ClientboundSyncMaxClaims> {

        public Type() {
            super(
                ClientboundSyncMaxClaims.class,
                Cadmus.id("sync_max_claims"),
                ObjectByteCodec.create(
                    ByteCodec.UUID.fieldOf(ClientboundSyncMaxClaims::id),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSyncMaxClaims::maxClaims),
                    ByteCodec.VAR_INT.fieldOf(ClientboundSyncMaxClaims::maxChunkLoaded),
                    ClientboundSyncMaxClaims::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundSyncMaxClaims packet) {
            return () -> ClaimLimitApi.API.set(packet.id, packet.maxClaims, packet.maxChunkLoaded);
        }
    }
}
