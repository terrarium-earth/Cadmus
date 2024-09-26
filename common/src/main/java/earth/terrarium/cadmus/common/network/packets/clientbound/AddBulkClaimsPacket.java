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
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public record AddBulkClaimsPacket(
    UUID id,
    Object2BooleanMap<ChunkPos> positions
) implements Packet<AddBulkClaimsPacket> {
    public static final ClientboundPacketType<AddBulkClaimsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("add_claims"),
        ObjectByteCodec.create(
            ByteCodec.UUID.fieldOf(AddBulkClaimsPacket::id),
            new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ByteCodec.BOOLEAN)
                .map(map -> (Object2BooleanMap<ChunkPos>) new Object2BooleanOpenHashMap<>(map), map -> map
                ).fieldOf(AddBulkClaimsPacket::positions),
            AddBulkClaimsPacket::new
        ),
        NetworkHandle.handle(packet -> ClaimApi.API.claim(CadmusClient.level(), packet.id(), packet.positions()))
    );

    @Override
    public PacketType<AddBulkClaimsPacket> type() {
        return TYPE;
    }
}
