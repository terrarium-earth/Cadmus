package earth.terrarium.cadmus.common.network.messages;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claims.ClaimSaveData;
import earth.terrarium.cadmus.common.teams.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundClearChunksPacket(boolean allDimensions) implements Packet<ServerboundClearChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "clear_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<ServerboundClearChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<ServerboundClearChunksPacket> {
        @Override
        public void encode(ServerboundClearChunksPacket packet, FriendlyByteBuf buf) {
            buf.writeBoolean(packet.allDimensions);
        }

        @Override
        public ServerboundClearChunksPacket decode(FriendlyByteBuf buf) {
            return new ServerboundClearChunksPacket(buf.readBoolean());
        }

        @Override
        public PacketContext handle(ServerboundClearChunksPacket message) {
            return (player, level) -> {
                var team = TeamSaveData.getPlayerTeam((ServerPlayer) player);
                if (team == null) return;
                if (!message.allDimensions) {
                    ClaimSaveData.clear((ServerLevel) level, team.teamId());
                } else {
                    ((ServerLevel) level).getServer().getAllLevels().forEach(l -> ClaimSaveData.clear(l, team.teamId()));
                }
            };
        }
    }
}
