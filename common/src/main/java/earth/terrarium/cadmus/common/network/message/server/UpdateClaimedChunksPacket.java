package earth.terrarium.cadmus.common.network.message.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claiming.ClaimedChunk;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public record UpdateClaimedChunksPacket(Set<ClaimedChunk> claimedChunks) implements Packet<UpdateClaimedChunksPacket> {

    public static final ResourceLocation ID = new ResourceLocation(Cadmus.MOD_ID, "update_claimed_chunks");
    public static final Handler HANDLER = new Handler();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PacketHandler<UpdateClaimedChunksPacket> getHandler() {
        return HANDLER;
    }

    private static class Handler implements PacketHandler<UpdateClaimedChunksPacket> {
        @Override
        public void encode(UpdateClaimedChunksPacket packet, FriendlyByteBuf buf) {
            ClaimedChunk.encode(packet.claimedChunks(), buf);
        }

        @Override
        public UpdateClaimedChunksPacket decode(FriendlyByteBuf buf) {
            Set<ClaimedChunk> claimedChunks = ClaimedChunk.decode(buf);
            return new UpdateClaimedChunksPacket(claimedChunks);
        }

        @Override
        public PacketContext handle(UpdateClaimedChunksPacket message) {
            // TODO use team provider
            return (player, level) -> TeamSaveData.set((ServerPlayer) player, player.getTeam() == null ? "" : player.getTeam().getName(), message.claimedChunks());
        }
    }
}
