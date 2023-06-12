package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.client.ClientClaims;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record ClientboundUpdateListeningChunksPacket(
    ResourceKey<Level> dimension,
    Component displayName, int color,
    Object2BooleanMap<ChunkPos> claims // true if claimed, false if unclaimed
) implements Packet<ClientboundUpdateListeningChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "update_listening_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ClientboundUpdateListeningChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ClientboundUpdateListeningChunksPacket> {
        @Override
        public void encode(ClientboundUpdateListeningChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeResourceLocation(packet.dimension.location());
            buf.writeComponent(packet.displayName);
            buf.writeInt(packet.color);
            buf.writeVarInt(packet.claims.size());
            packet.claims.forEach((chunkPos, isClaimed) -> {
                buf.writeChunkPos(chunkPos);
                buf.writeBoolean(isClaimed);
            });
        }

        @Override
        public ClientboundUpdateListeningChunksPacket decode(FriendlyByteBuf buf) {
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
            Component displayName = buf.readComponent();
            int color = buf.readInt();
            int size = buf.readVarInt();
            Object2BooleanMap<ChunkPos> claims = new Object2BooleanOpenHashMap<>(size);
            for (int i = 0; i < size; i++) {
                claims.put(buf.readChunkPos(), buf.readBoolean());
            }
            return new ClientboundUpdateListeningChunksPacket(dimension, displayName, color, claims);
        }

        @Override
        public PacketContext handle(ClientboundUpdateListeningChunksPacket message) {
            return (player, level) -> ClientClaims.get(message.dimension).update(message.displayName(), message.color(), message.claims());
        }
    }
}
