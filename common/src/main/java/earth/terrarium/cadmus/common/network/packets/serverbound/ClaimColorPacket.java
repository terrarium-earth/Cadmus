package earth.terrarium.cadmus.common.network.packets.serverbound;

import com.teamresourceful.resourcefullib.common.color.Color;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;

public record ClaimColorPacket(Color color) implements Packet<ClaimColorPacket> {
    public static final ServerboundPacketType<ClaimColorPacket> TYPE = CodecPacketType.Server.create(
        Cadmus.id("claim_color"),
        Color.BYTE_CODEC.map(ClaimColorPacket::new, ClaimColorPacket::color),
        NetworkHandle.handle((packet, player) -> {
            if (player.getCommandSenderWorld().isClientSide()) return;
            if (ModUtils.canModifyColor(player) != null) return;
            CadmusSaveData.setTeamColor(player.getServer(), TeamApi.API.getId(player), packet.color());
        })
    );

    @Override
    public PacketType<ClaimColorPacket> type() {
        return TYPE;
    }
}
