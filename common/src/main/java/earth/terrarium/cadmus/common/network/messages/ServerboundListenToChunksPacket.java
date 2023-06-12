package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimListenHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public record ServerboundListenToChunksPacket(
    ResourceKey<Level> dimension, boolean subscribe
) implements Packet<ServerboundListenToChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "listen_to_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ServerboundListenToChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ServerboundListenToChunksPacket> {
        @Override
        public void encode(ServerboundListenToChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeResourceLocation(packet.dimension.location());
            buf.writeBoolean(packet.subscribe);
        }

        @Override
        public ServerboundListenToChunksPacket decode(FriendlyByteBuf buf) {
            return new ServerboundListenToChunksPacket(
                ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()),
                buf.readBoolean()
            );
        }

        @Override
        public PacketContext handle(ServerboundListenToChunksPacket message) {
            return (player, level) -> {
                MinecraftServer server = player.getServer();
                if (server == null) return;
                ClaimListenHandler handler = ClaimHandler.getListener(server.getLevel(message.dimension()));
                if (message.subscribe) {
                    handler.addListener(player);
                } else {
                    handler.removeListener(player);
                }
            };
        }
    }
}
