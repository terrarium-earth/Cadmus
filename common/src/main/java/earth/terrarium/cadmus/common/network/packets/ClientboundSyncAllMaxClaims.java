package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.bytecodecs.defaults.PairCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.limit.ClaimLimitApi;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.resources.ResourceLocation;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

public record ClientboundSyncAllMaxClaims(
    Map<UUID, IntIntPair> maxClaimsByTeam
) implements Packet<ClientboundSyncAllMaxClaims> {

    public static final ClientboundPacketType<ClientboundSyncAllMaxClaims> TYPE = new Type();

    @Override
    public PacketType<ClientboundSyncAllMaxClaims> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundSyncAllMaxClaims> implements ClientboundPacketType<ClientboundSyncAllMaxClaims> {

        public Type() {
            super(
                ClientboundSyncAllMaxClaims.class,
                Cadmus.id("sync_all_max_claims"),
                ObjectByteCodec.create(
                    new MapCodec<>(
                        ByteCodec.UUID,
                        new PairCodec<>(ByteCodec.VAR_INT, ByteCodec.VAR_INT)
                            .map(entry -> IntIntPair.of(entry.getKey(), entry.getValue()),
                                pair -> new AbstractMap.SimpleEntry<>(pair.leftInt(), pair.rightInt())
                            )).fieldOf(ClientboundSyncAllMaxClaims::maxClaimsByTeam),
                    ClientboundSyncAllMaxClaims::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundSyncAllMaxClaims packet) {
            return () -> ClaimLimitApi.API.set(Map.copyOf(packet.maxClaimsByTeam));
        }
    }
}
