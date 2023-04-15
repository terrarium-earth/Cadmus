package earth.terrarium.cadmus.common.network.message.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ClearChunksPacket(boolean allDimensions) implements Packet<ClearChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "clear_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ClearChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ClearChunksPacket> {
        @Override
        public void encode(ClearChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeBoolean(packet.allDimensions);
        }

        @Override
        public ClearChunksPacket decode(FriendlyByteBuf buf) {
            return new ClearChunksPacket(buf.readBoolean());
        }

        @Override
        public PacketContext handle(ClearChunksPacket message) {
            return (player, level) -> {
                // TODO
            };
        }
    }
}
