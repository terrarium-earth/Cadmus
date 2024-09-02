package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.client.events.CadmusClientEvents;
import earth.terrarium.cadmus.client.CadmusClient;
import it.unimi.dsi.fastutil.objects.ObjectCharPair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record ClientboundSyncTeamInfo(
    UUID id,
    String name,
    char color,
    boolean updateMaps
) implements Packet<ClientboundSyncTeamInfo> {

    public static final ClientboundPacketType<ClientboundSyncTeamInfo> TYPE = new Type();

    @Override
    public PacketType<ClientboundSyncTeamInfo> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ClientboundSyncTeamInfo> implements ClientboundPacketType<ClientboundSyncTeamInfo> {

        public Type() {
            super(
                ClientboundSyncTeamInfo.class,
                Cadmus.id("sync_team_info"),
                ObjectByteCodec.create(
                    ByteCodec.UUID.fieldOf(ClientboundSyncTeamInfo::id),
                    ByteCodec.STRING.fieldOf(ClientboundSyncTeamInfo::name),
                    ByteCodec.CHAR.fieldOf(ClientboundSyncTeamInfo::color),
                    ByteCodec.BOOLEAN.fieldOf(ClientboundSyncTeamInfo::updateMaps),
                    ClientboundSyncTeamInfo::new
                )
            );
        }

        @Override
        public Runnable handle(ClientboundSyncTeamInfo packet) {
            return () -> {
                CadmusClient.TEAM_INFO.put(packet.id, ObjectCharPair.of(packet.name, packet.color));
                Minecraft.getInstance().execute(() ->
                    CadmusClientEvents.UpdateTeamInfo.fire(packet.id, packet.name, ChatFormatting.getByCode(packet.color), packet.updateMaps));
            };
        }
    }
}
