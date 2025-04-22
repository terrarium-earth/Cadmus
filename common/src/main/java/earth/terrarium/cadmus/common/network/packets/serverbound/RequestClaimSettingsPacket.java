package earth.terrarium.cadmus.common.network.packets.serverbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.protections.ProtectionApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncClaimSettingsPacket;
import earth.terrarium.cadmus.common.protections.SettingsData;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;

import java.util.HashMap;
import java.util.Map;

public record RequestClaimSettingsPacket() implements Packet<RequestClaimSettingsPacket> {
    public static final ServerboundPacketType<RequestClaimSettingsPacket> TYPE = CodecPacketType.Server.create(
        Cadmus.id("request_claim_settings"),
        ByteCodec.unit(RequestClaimSettingsPacket::new),
        NetworkHandle.handle((packet, player) -> {
            var allSettings = new HashMap<TeamId, SettingsData>();
            var teams = TeamApi.API.getTeamsList(player);

            for (TeamId team : teams) {
                var settings = SettingsData.of(player, team);
                for (String setting : ProtectionApi.API.getSettings()) {
                    if (ModUtils.canUsePermission(player, team, setting) != null) continue;
                    settings.settings().put(setting, CadmusSaveData.getClaimSetting(player.getServer(), team, setting));
                }
                allSettings.put(team, settings);
            }

            NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimSettingsPacket(allSettings), player);
        })
    );

    @Override
    public PacketType<RequestClaimSettingsPacket> type() {
        return TYPE;
    }
}
