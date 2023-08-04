package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.networking.base.CodecPacketHandler;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.ClientClaims;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public record ClientboundUpdateListeningChunksPacket(
    ResourceKey<Level> dimension,
    Component displayName, int color,
    Map<ChunkPos, Boolean> claims // true if claimed, false if unclaimed
) implements Packet<ClientboundUpdateListeningChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "update_listening_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ClientboundUpdateListeningChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler extends CodecPacketHandler<ClientboundUpdateListeningChunksPacket> {

        public Handler() {
            super(ObjectByteCodec.create(
                ExtraByteCodecs.DIMENSION.fieldOf(ClientboundUpdateListeningChunksPacket::dimension),
                ExtraByteCodecs.COMPONENT.fieldOf(ClientboundUpdateListeningChunksPacket::displayName),
                ByteCodec.VAR_INT.fieldOf(ClientboundUpdateListeningChunksPacket::color),
                new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ByteCodec.BOOLEAN).fieldOf(ClientboundUpdateListeningChunksPacket::claims),
                ClientboundUpdateListeningChunksPacket::new
            ));
        }

        @Override
        public PacketContext handle(ClientboundUpdateListeningChunksPacket message) {
            return (player, level) -> ClientClaims.get(message.dimension).update(message.displayName(), message.color(), message.claims());
        }
    }
}
