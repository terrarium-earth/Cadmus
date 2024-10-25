package earth.terrarium.cadmus.common.network.packets.clientbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.ClientboundPacketType;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import com.teamresourceful.resourcefullib.common.utils.TriState;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.CadmusClient;
import earth.terrarium.cadmus.client.ClaimMapScreen;
import net.minecraft.client.Minecraft;

import java.util.Map;

public record SyncClaimSettingsPacket(Map<String, TriState> settings, boolean canModifyColor) implements Packet<SyncClaimSettingsPacket> {

    public static final ClientboundPacketType<SyncClaimSettingsPacket> TYPE = CodecPacketType.Client.create(
        Cadmus.id("sync_claim_settings"),
        ObjectByteCodec.create(
            ByteCodec.mapOf(ByteCodec.STRING, TriState.BYTE_CODEC).fieldOf(SyncClaimSettingsPacket::settings),
            ByteCodec.BOOLEAN.fieldOf(SyncClaimSettingsPacket::canModifyColor),
            SyncClaimSettingsPacket::new
        ),
        NetworkHandle.handle(packet -> CadmusClient.updateClaimMapSettings(packet.settings(), packet.canModifyColor()))
    );

    @Override
    public ClientboundPacketType<SyncClaimSettingsPacket> type() {
        return TYPE;
    }
}
