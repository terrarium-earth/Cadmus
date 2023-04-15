package earth.terrarium.cadmus.common.network.message.server;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import earth.terrarium.cadmus.Cadmus;
import earth.terrarium.cadmus.common.claiming.ClaimChunkSaveData;
import earth.terrarium.cadmus.common.claiming.ClaimInfo;
import earth.terrarium.cadmus.common.claiming.ClaimType;
import earth.terrarium.cadmus.common.claiming.ClaimUtils;
import earth.terrarium.cadmus.common.team.Team;
import earth.terrarium.cadmus.common.team.TeamSaveData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public record UpdateClaimedChunksPacket(Map<ChunkPos, ClaimType> claims) implements Packet<UpdateClaimedChunksPacket> {

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
            buf.writeMap(packet.claims, FriendlyByteBuf::writeChunkPos, FriendlyByteBuf::writeEnum);
        }

        @Override
        public UpdateClaimedChunksPacket decode(FriendlyByteBuf buf) {
            return new UpdateClaimedChunksPacket(buf.readMap(
                    HashMap::new,
                    FriendlyByteBuf::readChunkPos, buf1 ->
                            buf1.readEnum(ClaimType.class)));
        }

        @Override
        public PacketContext handle(UpdateClaimedChunksPacket message) {
            return (player, level) -> {
                Team team = TeamSaveData.getOrCreate((ServerPlayer) player);
                message.claims.forEach((chunkPos, claimType) -> ClaimChunkSaveData.set(player.level, chunkPos, new ClaimInfo(team, claimType)));
                ClaimUtils.sendSyncPacket((ServerPlayer) player);
            };
        }
    }
}
