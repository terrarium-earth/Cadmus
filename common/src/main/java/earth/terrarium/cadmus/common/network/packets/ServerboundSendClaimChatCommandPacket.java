package earth.terrarium.cadmus.common.network.packets;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.network.Packet;
import com.teamresourceful.resourcefullib.common.network.base.PacketType;
import com.teamresourceful.resourcefullib.common.network.base.ServerboundPacketType;
import com.teamresourceful.resourcefullib.common.network.defaults.CodecPacketType;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.commands.claims.ClaimCommandType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Consumer;

public record ServerboundSendClaimChatCommandPacket(
    ClaimCommandType claimType,
    String arguments
) implements Packet<ServerboundSendClaimChatCommandPacket> {

    public static final ServerboundPacketType<ServerboundSendClaimChatCommandPacket> TYPE = new Type();

    @Override
    public PacketType<ServerboundSendClaimChatCommandPacket> type() {
        return TYPE;
    }

    private static class Type extends CodecPacketType<ServerboundSendClaimChatCommandPacket> implements ServerboundPacketType<ServerboundSendClaimChatCommandPacket> {

        public Type() {
            super(
                ServerboundSendClaimChatCommandPacket.class,
                Cadmus.id("send_claim_chat_command"),
                ObjectByteCodec.create(
                    ByteCodec.ofEnum(ClaimCommandType.class).fieldOf(ServerboundSendClaimChatCommandPacket::claimType),
                    ByteCodec.STRING_COMPONENT.fieldOf(ServerboundSendClaimChatCommandPacket::arguments),
                    ServerboundSendClaimChatCommandPacket::new
                )
            );
        }

        @Override
        public Consumer<Player> handle(ServerboundSendClaimChatCommandPacket packet) {
            return player -> Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withSuppressedOutput(),
                packet.claimType().command() + (packet.arguments.isEmpty() ? "" : " " + packet.arguments)
            );
        }
    }
}
