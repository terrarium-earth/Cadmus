package earth.terrarium.cadmus.common.network.messages.client;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.claims.ClaimMapScreen;
import earth.terrarium.cadmus.common.claims.ClaimInfo;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record SendClaimedChunksPacket(Map<ChunkPos, ClaimInfo> claims,
                                      UUID teamId, Optional<String> displayName,
                                      Map<UUID, Component> teamDisplayNames, int claimedCount, int chunkLoadedCount,
                                      int maxClaims,
                                      int maxChunkLoaded) implements Packet<SendClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "send_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<SendClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<SendClaimedChunksPacket> {
        @Override
        public void encode(SendClaimedChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeMap(packet.claims, FriendlyByteBuf::writeChunkPos, (buf1, info) -> {
                buf1.writeUUID(info.teamId());
                buf1.writeEnum(info.type());
            });

            buf.writeUUID(packet.teamId);
            buf.writeOptional(packet.displayName, FriendlyByteBuf::writeUtf);
            buf.writeMap(packet.teamDisplayNames, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeComponent);
            buf.writeVarInt(packet.claimedCount);
            buf.writeVarInt(packet.chunkLoadedCount);
            buf.writeVarInt(packet.maxClaims);
            buf.writeVarInt(packet.maxChunkLoaded);
        }

        @Override
        public SendClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Map<ChunkPos, ClaimInfo> claims = buf.readMap(
                FriendlyByteBuf::readChunkPos,
                buf1 -> new ClaimInfo(buf1.readUUID(), buf1.readEnum(ClaimType.class))
            );
            UUID teamId = buf.readUUID();
            Optional<String> displayName = buf.readOptional(FriendlyByteBuf::readUtf);
            Map<UUID, Component> teamDisplayNames = buf.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readComponent);
            int claimedCount = buf.readVarInt();
            int chunkLoadedCount = buf.readVarInt();
            int maxClaims = buf.readVarInt();
            int maxChunkLoaded = buf.readVarInt();

            return new SendClaimedChunksPacket(claims, teamId, displayName, teamDisplayNames, claimedCount, chunkLoadedCount, maxClaims, maxChunkLoaded);
        }

        @Override
        public PacketContext handle(SendClaimedChunksPacket message) {
            return (player, level) -> Minecraft.getInstance().setScreen(new ClaimMapScreen(
                message.claims,
                message.teamId,
                message.displayName.map(Component::nullToEmpty).orElse(player.getDisplayName()),
                message.teamDisplayNames,
                message.claimedCount,
                message.chunkLoadedCount,
                message.maxClaims,
                message.maxChunkLoaded));
        }
    }
}
