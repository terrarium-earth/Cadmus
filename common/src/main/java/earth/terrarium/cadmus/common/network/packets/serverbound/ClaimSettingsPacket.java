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

public record ClaimSettingsPacket(TeamId id, String setting, TriState value) implements Packet<ClaimSettingsPacket> {
    public static final ServerboundPacketType<ClaimSettingsPacket> TYPE = CodecPacketType.Server.create(
        Cadmus.id("claim_settings"),
        ObjectByteCodec.create(
            TeamId.BYTE_CODEC.fieldOf(ClaimSettingsPacket::id),
            ByteCodec.STRING_COMPONENT.fieldOf(ClaimSettingsPacket::setting),
            TriState.BYTE_CODEC.fieldOf(ClaimSettingsPacket::value),
            ClaimSettingsPacket::new
        ),
        NetworkHandle.handle((packet, player) -> {
            if (player.getCommandSenderWorld().isClientSide()) return;
            var error = ModUtils.canUsePermission(player, packet.id, packet.setting);
            if (error != null) {
                player.displayClientMessage(error, false);
                return;
            }
            CadmusSaveData.setClaimSetting(player.getServer(), packet.id, packet.setting, packet.value);
        })
    );

    @Override
    public PacketType<ClaimSettingsPacket> type() {
        return TYPE;
    }
}
