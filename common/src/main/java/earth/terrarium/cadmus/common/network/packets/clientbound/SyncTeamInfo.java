package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.client.events.CadmusClientEvents;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.teams.TeamInfo;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public record SyncTeamInfo(
    UUID id,
    String name,
    Color color,
    boolean updateMaps
) implements Packet<SyncTeamInfo> {

    public static final ClientboundPacketType<SyncTeamInfo> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_team_info"),
        ObjectByteCodec.create(
            ByteCodec.UUID.fieldOf(SyncTeamInfo::id),
            ByteCodec.STRING.fieldOf(SyncTeamInfo::name),
            Color.BYTE_CODEC.fieldOf(SyncTeamInfo::color),
            ByteCodec.BOOLEAN.fieldOf(SyncTeamInfo::updateMaps),
            SyncTeamInfo::new
        ),
        NetworkHandle.handle(packet -> {
            CadmusClient.TEAM_INFO.put(packet.id, new TeamInfo(packet.name, packet.color));
            Minecraft.getInstance().execute(() ->
                CadmusClientEvents.UpdateTeamInfo.fire(packet.id, packet.name, packet.color, packet.updateMaps));
        })
    );

    @Override
    public PacketType<SyncTeamInfo> type() {
        return TYPE;
    }
}
