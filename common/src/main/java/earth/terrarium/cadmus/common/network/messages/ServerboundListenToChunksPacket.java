package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.networking.base.CodecPacketHandler;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimListenHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public record ServerboundListenToChunksPacket(
    ResourceKey<Level> dimension, boolean subscribe
) implements Packet<ServerboundListenToChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "listen_to_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ServerboundListenToChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler extends CodecPacketHandler<ServerboundListenToChunksPacket> {
        public Handler() {
            super(ObjectByteCodec.create(
                ExtraByteCodecs.DIMENSION.fieldOf(ServerboundListenToChunksPacket::dimension),
                ByteCodec.BOOLEAN.fieldOf(ServerboundListenToChunksPacket::subscribe),
                ServerboundListenToChunksPacket::new
            ));
        }

        @Override
        public PacketContext handle(ServerboundListenToChunksPacket message) {
            return (player, level) -> {
                MinecraftServer server = player.getServer();
                if (server == null) return;
                ClaimListenHandler handler = ClaimHandler.getListener(server.getLevel(message.dimension()));
                if (message.subscribe) {
                    handler.addListener(player);
                } else {
                    handler.removeListener(player);
                }
            };
        }
    }
}
