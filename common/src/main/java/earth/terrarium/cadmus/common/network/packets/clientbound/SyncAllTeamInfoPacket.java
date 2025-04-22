package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.common.teams.TeamInfo;

import java.util.Map;
import java.util.UUID;

public record SyncAllTeamInfoPacket(
    Map<TeamId, TeamInfo> teamInfo
) implements Packet<SyncAllTeamInfoPacket> {

    public static final ClientboundPacketType<SyncAllTeamInfoPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_all_team_info"),
        ObjectByteCodec.create(
            new MapCodec<>(TeamId.BYTE_CODEC, TeamInfo.BYTE_CODEC).fieldOf(SyncAllTeamInfoPacket::teamInfo),
            SyncAllTeamInfoPacket::new
        ),
        NetworkHandle.handle(packet -> {
            CadmusClient.TEAM_INFO.clear();
            CadmusClient.TEAM_INFO.putAll(packet.teamInfo());
        })
    );

    @Override
    public PacketType<SyncAllTeamInfoPacket> type() {
        return TYPE;
    }
}
