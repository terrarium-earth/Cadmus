package earth.terrarium.cadmus.common.network.packets.serverbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import com.teamresourceful.resourcefullib.common.utils.Scheduling;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.protections.ProtectionApi;
import earth.terrarium.cadmus.api.teams.TeamApi;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncClaimSettingsPacket;
import earth.terrarium.cadmus.common.network.packets.clientbound.SyncClaimsPacket;
import earth.terrarium.cadmus.common.utils.CadmusSaveData;
import earth.terrarium.cadmus.common.utils.ModUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public record RequestClaimSettingsPacket() implements Packet<RequestClaimSettingsPacket> {
    public static final ServerboundPacketType<RequestClaimSettingsPacket> TYPE = CodecPacketType.Server.create(
        Cadmus.id("request_claim_settings"),
        ByteCodec.unit(RequestClaimSettingsPacket::new),
        NetworkHandle.handle((packet, player) -> {
            var settings = ProtectionApi.API.getSettings().stream().filter(
                setting -> ModUtils.canUsePermission(player, setting) == null
            ).collect(() -> new HashMap<String, TriState>(), (hashMap, s) -> hashMap.put(s, CadmusSaveData.getClaimSetting(player.getServer(), TeamApi.API.getId(player), s)), HashMap::putAll);
            NetworkHandler.CHANNEL.sendToPlayer(new SyncClaimSettingsPacket(settings), player);
        })
    );

    @Override
    public PacketType<RequestClaimSettingsPacket> type() {
        return TYPE;
    }
}
