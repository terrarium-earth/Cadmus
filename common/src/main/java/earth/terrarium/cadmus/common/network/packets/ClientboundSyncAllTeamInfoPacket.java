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
import earth.terrarium.cadmus.client.CadmusClient;
import it.unimi.dsi.fastutil.objects.ObjectCharPair;
import net.minecraft.resources.ResourceLocation;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

public record ClientboundSyncAllTeamInfoPacket(
    Map<UUID, ObjectCharPair<String>> teamInfo
) implements Packet<ClientboundSyncAllTeamInfoPacket> {

    public static final ClientboundPacketType<ClientboundSyncAllTeamInfoPacket> TYPE = new Type();

    @Override
    public PacketType<ClientboundSyncAllTeamInfoPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundSyncAllTeamInfoPacket> implements ClientboundPacketType<ClientboundSyncAllTeamInfoPacket> {

        public Type() {
            super(
                ClientboundSyncAllTeamInfoPacket.class,
                Cadmus.id("sync_all_team_info"),
                ObjectByteCodec.create(
                    new MapCodec<>(
                        ByteCodec.UUID,
                        new PairCodec<>(ByteCodec.STRING, ByteCodec.CHAR)
                            .map(entry -> ObjectCharPair.of(entry.getKey(), entry.getValue()),
                                pair -> new AbstractMap.SimpleEntry<>(pair.left(), pair.rightChar())
                            )).fieldOf(ClientboundSyncAllTeamInfoPacket::teamInfo),
                    ClientboundSyncAllTeamInfoPacket::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundSyncAllTeamInfoPacket packet) {
            return () -> {
                CadmusClient.TEAM_INFO.clear();
                CadmusClient.TEAM_INFO.putAll(packet.teamInfo());
            };
        }
    }
}
