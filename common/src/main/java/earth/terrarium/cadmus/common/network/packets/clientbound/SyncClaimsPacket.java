package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.client.CadmusClient;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public record SyncClaimsPacket(
    ResourceKey<Level> dimension,
    Map<UUID, Object2BooleanMap<ChunkPos>> claims
) implements Packet<SyncClaimsPacket> {

    public static final ClientboundPacketType<SyncClaimsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_claims"),
        ObjectByteCodec.create(
            ExtraByteCodecs.resourceKey(Registries.DIMENSION).fieldOf(SyncClaimsPacket::dimension),
            ByteCodec.mapOf(ByteCodec.UUID,
                ByteCodec.mapOf(ExtraByteCodecs.CHUNK_POS, ByteCodec.BOOLEAN)
                    .<Object2BooleanMap<ChunkPos>>map(Object2BooleanOpenHashMap::new, map -> map
                    )).fieldOf(SyncClaimsPacket::claims),
            SyncClaimsPacket::new
        ),
        NetworkHandle.handle(packet -> packet.claims.forEach((id, claims) -> ClaimApi.API.claim(CadmusClient.level(), id, claims)))
    );

    @Override
    public PacketType<SyncClaimsPacket> type() {
        return TYPE;
    }
}
