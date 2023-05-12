package earth.terrarium.cadmus.common.network.messages;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claims.ClaimScreen;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.Optional;

public record ClientboundSendClaimedChunksPacket(Map<ChunkPos, Pair<String, ClaimType>> claims,
                                                 String id, ChatFormatting color, Optional<String> displayName,
                                                 Map<String, Component> teamDisplayNames, int claimedCount,
                                                 int chunkLoadedCount,
                                                 int maxClaims,
                                                 int maxChunkLoaded) implements Packet<ClientboundSendClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "send_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ClientboundSendClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ClientboundSendClaimedChunksPacket> {
        @Override
        public void encode(ClientboundSendClaimedChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeMap(packet.claims, FriendlyByteBuf::writeChunkPos, (buf1, info) -> {
                buf1.writeUtf(info.getFirst());
                buf1.writeEnum(info.getSecond());
            });

            buf.writeUtf(packet.id);
            buf.writeEnum(packet.color);
            buf.writeOptional(packet.displayName, FriendlyByteBuf::writeUtf);
            buf.writeMap(packet.teamDisplayNames, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeComponent);
            buf.writeVarInt(packet.claimedCount);
            buf.writeVarInt(packet.chunkLoadedCount);
            buf.writeVarInt(packet.maxClaims);
            buf.writeVarInt(packet.maxChunkLoaded);
        }

        @Override
        public ClientboundSendClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Map<ChunkPos, Pair<String, ClaimType>> claims = buf.readMap(
                FriendlyByteBuf::readChunkPos,
                buf1 -> Pair.of(buf1.readUtf(), buf1.readEnum(ClaimType.class))
            );
            String id = buf.readUtf();
            ChatFormatting color = buf.readEnum(ChatFormatting.class);
            Optional<String> displayName = buf.readOptional(FriendlyByteBuf::readUtf);
            Map<String, Component> teamDisplayNames = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readComponent);
            int claimedCount = buf.readVarInt();
            int chunkLoadedCount = buf.readVarInt();
            int maxClaims = buf.readVarInt();
            int maxChunkLoaded = buf.readVarInt();

            return new ClientboundSendClaimedChunksPacket(claims, id, color, displayName, teamDisplayNames, claimedCount, chunkLoadedCount, maxClaims, maxChunkLoaded);
        }

        @Override
        public PacketContext handle(ClientboundSendClaimedChunksPacket message) {
            return (player, level) -> Minecraft.getInstance().setScreen(new ClaimScreen(
                message.claims,
                message.id,
                message.color,
                message.displayName.map(Component::nullToEmpty).orElse(player.getDisplayName()),
                message.teamDisplayNames,
                message.claimedCount,
                message.chunkLoadedCount,
                message.maxClaims,
                message.maxChunkLoaded));
        }
    }
}
