package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public record ServerboundClearChunksPacket(boolean allDimensions) implements Packet<ServerboundClearChunksPacket> {

    public static final ServerboundPacketType<ServerboundClearChunksPacket> TYPE = new Type();

    @Override
    public PacketType<ServerboundClearChunksPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ServerboundClearChunksPacket> implements ServerboundPacketType<ServerboundClearChunksPacket> {

        public Type() {
            super(
                ServerboundClearChunksPacket.class,
                new ResourceLocation(Cadmus.MOD_ID, "clear_chunks"),
                ObjectByteCodec.create(
                    ByteCodec.BOOLEAN.fieldOf(ServerboundClearChunksPacket::allDimensions),
                    ServerboundClearChunksPacket::new
                )
            );
        }

        @Override
        public Consumer<Player> handle(ServerboundClearChunksPacket message) {
            return player -> {
                ServerLevel level = (ServerLevel) player.level();
                String id = TeamHelper.getTeamId(player.getServer(), player.getUUID());
                if (!message.allDimensions) {
                    ClaimHandler.clear(level, id);
                } else {
                    level.getServer().getAllLevels().forEach(l -> ClaimHandler.clear(l, id));
                }
            };
        }
    }
}
