package earth.terrarium.cadmus.common.network.packets.serverbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;

import java.util.Map;

public record BulkClaimSettingsPacket(TeamId id, Map<String, TriState> settings) implements Packet<BulkClaimSettingsPacket> {
    public static final ServerboundPacketType<BulkClaimSettingsPacket> TYPE = CodecPacketType.Server.create(
        Cadmus.id("update_bulk_claim_settings"),
        ObjectByteCodec.create(
            TeamId.BYTE_CODEC.fieldOf(BulkClaimSettingsPacket::id),
            ByteCodec.mapOf(ByteCodec.STRING, TriState.BYTE_CODEC).fieldOf(BulkClaimSettingsPacket::settings),
            BulkClaimSettingsPacket::new
        ),
        NetworkHandle.handle((packet, player) -> {
            packet.settings().forEach((setting, value) -> {
                if (ModUtils.canUsePermission(player, packet.id, setting) == null) {
                    CadmusSaveData.setClaimSetting(player.getServer(), packet.id, setting, value);
                }
            });
        })
    );

    @Override
    public PacketType<BulkClaimSettingsPacket> type() {
        return TYPE;
    }
}
