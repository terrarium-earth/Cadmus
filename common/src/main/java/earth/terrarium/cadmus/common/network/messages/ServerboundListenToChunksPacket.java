package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimListenHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public record ServerboundListenToChunksPacket(
    ResourceKey<Level> dimension, boolean subscribe
) implements Packet<ServerboundListenToChunksPacket> {

    public static final ServerboundPacketType<ServerboundListenToChunksPacket> TYPE = new Type();

    @Override
    public PacketType<ServerboundListenToChunksPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ServerboundListenToChunksPacket> implements ServerboundPacketType<ServerboundListenToChunksPacket> {

        public Type() {
            super(
                ServerboundListenToChunksPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "listen_to_chunks"),
                ObjectByteCodec.create(
                    ExtraByteCodecs.DIMENSION.fieldOf(ServerboundListenToChunksPacket::dimension),
                    ByteCodec.BOOLEAN.fieldOf(ServerboundListenToChunksPacket::subscribe),
                    ServerboundListenToChunksPacket::new
                )
            );
        }

        @Override
        public Consumer<Player> handle(ServerboundListenToChunksPacket packet) {
            return player -> {
                MinecraftServer server = player.getServer();
                if (server == null) return;
                ClaimListenHandler handler = ClaimHandler.getListener(server.getLevel(packet.dimension()));
                if (packet.subscribe) {
                    handler.addListener(player);
                } else {
                    handler.removeListener(player);
                }
            };
        }
    }
}
