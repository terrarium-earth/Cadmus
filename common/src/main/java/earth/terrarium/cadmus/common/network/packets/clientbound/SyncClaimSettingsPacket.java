package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.api.teams.TeamId;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.client.ClaimMapScreen;
import earth.terrarium.cadmus.common.protections.ClaimSettings;
import earth.terrarium.cadmus.common.protections.SettingsData;
import net.minecraft.client.Minecraft;

import java.util.Map;

public record SyncClaimSettingsPacket(Map<TeamId, SettingsData> settings) implements Packet<SyncClaimSettingsPacket> {

    public static final ClientboundPacketType<SyncClaimSettingsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_claim_settings"),
        ByteCodec.mapOf(TeamId.BYTE_CODEC, SettingsData.CODEC).fieldOf(SyncClaimSettingsPacket::settings).map(SyncClaimSettingsPacket::new, SyncClaimSettingsPacket::settings),
        NetworkHandle.handle(packet -> CadmusClient.updateClaimMapSettings(packet.settings()))
    );

    @Override
    public ClientboundPacketType<SyncClaimSettingsPacket> type() {
        return TYPE;
    }
}
