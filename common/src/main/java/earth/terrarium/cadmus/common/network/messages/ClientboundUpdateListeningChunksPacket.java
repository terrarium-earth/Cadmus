package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.ClientClaims;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record ClientboundUpdateListeningChunksPacket(
    ResourceKey<Level> dimension,
    Component displayName, int color,
    Object2BooleanMap<ChunkPos> claims // true if claimed, false if unclaimed
) implements Packet<ClientboundUpdateListeningChunksPacket> {

    public static final ClientboundPacketType<ClientboundUpdateListeningChunksPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundUpdateListeningChunksPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundUpdateListeningChunksPacket> implements ClientboundPacketType<ClientboundUpdateListeningChunksPacket> {

        public Type() {
            super(
                ClientboundUpdateListeningChunksPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "update_listening_chunks"),
                ObjectByteCodec.create(
                    ExtraByteCodecs.DIMENSION.fieldOf(ClientboundUpdateListeningChunksPacket::dimension),
                    ExtraByteCodecs.COMPONENT.fieldOf(ClientboundUpdateListeningChunksPacket::displayName),
                    ByteCodec.VAR_INT.fieldOf(ClientboundUpdateListeningChunksPacket::color),
                    new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ByteCodec.BOOLEAN).map(map -> {
                            Object2BooleanMap<ChunkPos> claims = new Object2BooleanOpenHashMap<>(map.size());
                            claims.putAll(map);
                            return claims;
                        }, map -> map
                    ).fieldOf(ClientboundUpdateListeningChunksPacket::claims),
                    ClientboundUpdateListeningChunksPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundUpdateListeningChunksPacket packet) {
            return () -> ClientClaims.get(packet.dimension()).update(packet.displayName(), packet.color(), packet.claims());
        }
    }
}
