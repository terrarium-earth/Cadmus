package earth.terrarium.cadmus.common.network.packets.serverbound;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.NetworkHandle;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommandType;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Consumer;

public record ChatClaimPacket(
    ClaimCommandType claimType,
    String arguments
) implements Packet<ChatClaimPacket> {

    public static final ServerboundPacketType<ChatClaimPacket> TYPE = CodecPacketType.Server.create(
        Cadmus.id("send_claim_chat_command"),
        ObjectByteCodec.create(
            ByteCodec.ofEnum(ClaimCommandType.class).fieldOf(ChatClaimPacket::claimType),
            ByteCodec.STRING_COMPONENT.fieldOf(ChatClaimPacket::arguments),
            ChatClaimPacket::new
        ),
        NetworkHandle.handle((packet, player) -> Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
            player.createCommandSourceStack().withSuppressedOutput(),
            packet.claimType().command() + (packet.arguments.isEmpty() ? "" : " " + packet.arguments)
        ))
    );

    @Override
    public PacketType<ChatClaimPacket> type() {
        return TYPE;
    }
}
