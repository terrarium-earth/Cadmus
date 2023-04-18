package earth.terrarium.cadmus.common.network.messages.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

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
                var team = TeamSaveData.getPlayerTeam(player);
                if (team == null) return;
                if (!message.allDimensions) {
                    ClaimChunkSaveData.clear(level, team);
                } else if (level instanceof ServerLevel serverLevel) {
                    serverLevel.getServer().getAllLevels().forEach(l -> ClaimChunkSaveData.clear(l, team));
                }
            };
        }
    }
}
